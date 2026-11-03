package com.example.industrialai.service;

import com.example.industrialai.entity.FaultRecord;
import com.example.industrialai.entity.OperateReport;
import com.example.industrialai.repository.FaultRecordRepository;
import com.example.industrialai.repository.OperateReportRepository;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FaultRecordService {
    private final RagService ragService;
    private final QwenChatModel qwenWebChatModel;
    private final FaultRecordRepository faultRecordRepository;
    private final OperateReportRepository operateReportRepository;
    /**
     * 智能故障诊断分析
     */
    public String diagnoseFault(String deviceId, String deviceType, String faultPhenomenon) {
        String context = ragService.retrieveRelevantContent(faultPhenomenon);
        String operateStr="",faultStr="";
        try {
            OperateReport operate = operateReportRepository.findByDeviceIdOrderByCreateTimeDesc(deviceId).get(0);
            operateStr=operate.getContent();
        }catch (Exception e){}
        try{
            FaultRecord faultRecord = getFaultsByDeviceId(deviceId).get(0);
            faultStr=faultRecord.getDiagnoseResult();
        }catch (IndexOutOfBoundsException e){}

        String prompt = """
                你是一个资深的工业故障诊断专家，请基于下面的工业设备文档、历史运维记录、历史故障分析，联网查询分析这个故障的原因，给出处理建议。
                分析要专业，步骤清晰，符合运维的操作规范。
                工业故障处理文档：
                {{context}}
                历史运维文档：
                {{operate}}
                历史故障文档：
                {{faultRecord}}
                设备类型：{{deviceType}}
                故障现象：{{phenomenon}}
                """.replace("{{context}}", context).replace("{{deviceType}}", deviceType).replace("{{phenomenon}}", faultPhenomenon).replace("{{operate}}",operateStr).replace("{{faultRecord}}",faultStr);
        String diagnoseResult = qwenWebChatModel.chat(prompt);
        FaultRecord record = new FaultRecord();
        record.setDeviceId(deviceId);
        record.setDeviceType(deviceType);
        record.setFaultPhenomenon(faultPhenomenon);
        record.setDiagnoseResult(diagnoseResult);
        record.setCreateTime(LocalDateTime.now());
        faultRecordRepository.save(record);
        return diagnoseResult;
    }

    /**
     * 获取设备的所有故障记录
     */
    public List<FaultRecord> getFaultsByDeviceId(String deviceId) {
        return faultRecordRepository.findByDeviceIdOrderByCreateTimeDesc(deviceId);
    }
}
