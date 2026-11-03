package com.example.industrialai.service;

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
public class OperateService {

    private final QwenChatModel qwenWebChatModel;
    private final OperateReportRepository reportRepository;
    private final FaultRecordRepository faultRepository;
    private final RagService ragService;

    /**
     * 自动生成运维报告
     */
    public List<OperateReport> generateReport(String deviceId, String deviceType, String logContent) {
        String context = ragService.retrieveRelevantContent(logContent);
        String prompt = """
                你是一个资深的工业运维专家，请基于下面的设备运行日志和工业文档，生成一份标准化的运维分析报告。
                报告需要包含：
                1. 设备整体运行状态评估
                2. 发现的异常问题
                3. 对应的处理建议
                4. 后续的运维注意事项
                格式要专业，符合工业运维报告的规范。

                工业文档参考：
                {{context}}

                设备运行日志：
                {{log}}

                设备类型：{{deviceType}}
                """.replace("{{context}}", context).replace("{{log}}", logContent).replace("{{deviceType}}", deviceType);
        // 调用带有网路搜索功能的模型对象返回结果
        String reportContent = qwenWebChatModel.chat(prompt);
        OperateReport report = new OperateReport();
        report.setDeviceId(deviceId);
        report.setDeviceType(deviceType);
        report.setReportType("运维报告");
        report.setContent(reportContent);
        report.setCreateTime(LocalDateTime.now());
        reportRepository.save(report);
        return reportRepository.findByDeviceIdOrderByCreateTimeDesc(deviceId);
    }


    /**
     * 获取设备的所有报告
     */
    public List<OperateReport> getReportsByDeviceId(String deviceId) {
        return reportRepository.findByDeviceIdOrderByCreateTimeDesc(deviceId);
    }

}