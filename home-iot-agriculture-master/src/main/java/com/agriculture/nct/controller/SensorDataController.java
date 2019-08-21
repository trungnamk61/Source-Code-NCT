package com.agriculture.nct.controller;

import com.agriculture.nct.services.HTTPService.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sensor")
@PreAuthorize("hasRole('USER')")
public class SensorDataController {

    private final SensorDataService sensorDataService;

    @Autowired
    public SensorDataController(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
    }

    @GetMapping()
    ResponseEntity getSensorDataByCropId(@RequestParam(value = "cropId", defaultValue = "0") int cropId){
        return sensorDataService.getSensorDataByCropId(cropId);
    }

    @GetMapping("/single")
    ResponseEntity getOneSensorDataByCropId(@RequestParam(value = "cropId", defaultValue = "0") int cropId){
        return sensorDataService.getOneSensorDataByCropId(cropId);
    }
}
