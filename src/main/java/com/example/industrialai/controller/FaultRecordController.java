package com.example.industrialai.controller;

import com.example.industrialai.entity.FaultRecord;
import com.example.industrialai.entity.OperateReport;
import com.example.industrialai.service.FaultRecordService;
import com.example.industrialai.service.OperateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operate")
@RequiredArgsConstructor
public class FaultRecordController {

    private final FaultRecordService faultRecordService;


    /**
     * 故障诊断接口
     */
    @PostMapping("/diagnose")
    public String diagnoseFault(@RequestBody Map<String, String> params) {
        String deviceId = params.get("deviceId");
        String deviceType = params.get("deviceType");
        String faultPhenomenon = params.get("faultPhenomenon");
        return faultRecordService.diagnoseFault(deviceId, deviceType, faultPhenomenon);
    }

    /**
     * 获取设备的故障记录
     */
    @GetMapping("/faults/{deviceId}")
    public List<FaultRecord> getFaults(@PathVariable String deviceId) {
        return faultRecordService.getFaultsByDeviceId(deviceId);
    }
}