package com.agriculture.nct.services.HTTPService;

import com.agriculture.nct.database.DBWeb;
import com.agriculture.nct.model.Crop;
import com.agriculture.nct.model.Sensor;
import com.agriculture.nct.model.SensorData;
import com.agriculture.nct.exception.ResourceNotFoundException;
import com.agriculture.nct.payload.response.SensorDataListResponse;
import com.agriculture.nct.util.ModelMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class SensorDataService {

    private final DBWeb dbWeb;

    @Autowired
    public SensorDataService(DBWeb dbWeb) {
        this.dbWeb = dbWeb;
    }

    public ResponseEntity getSensorDataByCropId(int cropId) {
        Crop crop = dbWeb.getCropById(cropId).orElseThrow(() -> new ResourceNotFoundException("Crop", "id", cropId));

        int deviceId = crop.getDevice_id();
        List<Sensor> sensors = dbWeb.getSensorsOfDevice(deviceId);
        Map<Integer, List<SensorData>> sensorListMap = new HashMap<>();

        for (Sensor sensor : sensors) {
            List<SensorData> sensorDataList = dbWeb.getSensorData(sensor.getId(), cropId).orElseThrow(
                    () -> new ResourceNotFoundException("SensorData", "id", sensor.getId()));
            sensorListMap.put(sensor.getId(), sensorDataList);
        }

        List<SensorDataListResponse> dataListResponse = sensors.stream().map(sensor ->
                ModelMapper.mapSensorDataToSensorDataListResponse(
                        sensor, sensorListMap.get(sensor.getId()))).collect(Collectors.toList());

        return ResponseEntity.ok().body(dataListResponse);
    }

    public ResponseEntity getOneSensorDataByCropId(int cropId) {
        Crop crop = dbWeb.getCropById(cropId).orElseThrow(() -> new ResourceNotFoundException("Crop", "id", cropId));

        int deviceId = crop.getDevice_id();
        List<Sensor> sensors = dbWeb.getSensorsOfDevice(deviceId);
        Map<Integer, SensorData> sensorListMap = new HashMap<>();

        for (Sensor sensor : sensors) {
            SensorData sensorDataList = dbWeb.getOneSensorData(sensor.getId(), cropId).orElseThrow(
                    () -> new ResourceNotFoundException("SensorData", "id", sensor.getId()));
            sensorListMap.put(sensor.getId(), sensorDataList);
        }

        List<SensorDataListResponse> dataListResponse = sensors.stream().map(sensor ->
                ModelMapper.mapSensorDataToSensorDataListResponse(
                        sensor, Collections.singletonList(sensorListMap.get(sensor.getId())))).collect(Collectors.toList());

        return ResponseEntity.ok().body(dataListResponse);
    }
}