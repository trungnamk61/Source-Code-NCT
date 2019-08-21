package com.agriculture.nct.controller;

import com.agriculture.nct.model.Command;
import com.agriculture.nct.payload.request.ControlRequest;
import com.agriculture.nct.security.CurrentUser;
import com.agriculture.nct.security.UserPrincipal;
import com.agriculture.nct.services.HTTPService.DeviceService;
import com.agriculture.nct.services.MqttService.MqttControlService;
import com.agriculture.nct.util.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device")
@PreAuthorize("hasRole('USER')")
public class DeviceController {

    private MqttControlService mqttControlService;
    private final DeviceService deviceService;

    @Autowired
    public DeviceController(MqttControlService mqttControlService, DeviceService deviceService) {
        this.mqttControlService = mqttControlService;
        this.deviceService = deviceService;
    }

    @PostMapping("/control")
    ResponseEntity controlDevice(@RequestBody ControlRequest request) {
        return mqttControlService.controlDevice(
                new Command(request.getDeviceId(), request.getActuatorName(), request.getAction(), request.getParam(), "user")
        );

    }

    @GetMapping()
    ResponseEntity getDevices(@CurrentUser UserPrincipal currentUser,
                              @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                              @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
                              @RequestParam(value = "available", required = false) Boolean available) {
        return ResponseEntity.ok().body(deviceService.getAllDevices(currentUser, page, size, available));
    }

    @GetMapping("/{deviceId}")
    ResponseEntity getDeviceDetails(@PathVariable int deviceId) {
        return deviceService.getDeviceDetails(deviceId);
    }
}
