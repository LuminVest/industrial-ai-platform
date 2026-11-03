package com.example.industrialai.controller;

import com.example.industrialai.entity.FaultRecord;
import com.example.industrialai.entity.OperateReport;
import com.example.industrialai.service.OperateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operate")
@RequiredArgsConstructor
public class OperateController {

    private final OperateService operateService;

    /**
     * 生成运维报告接口
     */
    @PostMapping("/generateReport")
    public List<OperateReport> generateReport(@RequestBody Map<String, String> params) {
        String deviceId = params.get("deviceId");
        String deviceType = params.get("deviceType");
        String logContent = params.get("logContent");
        return operateService.generateReport(deviceId, deviceType, logContent);
    }



    /**
     * 获取设备的报告
     */
    @GetMapping("/reports/{deviceId}")
    public List<OperateReport> getReports(@PathVariable String deviceId) {
        return operateService.getReportsByDeviceId(deviceId);
    }


}