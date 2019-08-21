package com.agriculture.nct.util;

import com.agriculture.nct.model.*;
import com.agriculture.nct.payload.response.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ModelMapper {
    public static DeviceResponse mapDeviceToDeviceResponse(Device device, Crop crop) {
        DeviceResponse deviceResponse = new DeviceResponse();

        deviceResponse.setId(device.getId());
        deviceResponse.setAlive(device.isAlive());
        deviceResponse.setCrop(crop);
        deviceResponse.setCreationDateTime(device.getCreatedAt());
        deviceResponse.setLastUpdateDateTime(device.getUpdatedAt());

        return deviceResponse;
    }

    public static CropResponse mapCropToCropResponse(Crop crop, Plant plant) {
        CropResponse cropResponse = new CropResponse();

        cropResponse.setId(crop.getId());
        cropResponse.setDeviceId(crop.getDevice_id());
        cropResponse.setPlantId(plant.getId());
        cropResponse.setPlantTypeName(plant.getName());
        cropResponse.setName(crop.getName());
        cropResponse.setStartTime(crop.getStart_time());
        cropResponse.setEndTime(crop.getEnd_time());
        cropResponse.setDone(((
                new Date().getTime() - crop.getStart_time().getTime())) / (24 * 60 * 60 * 1000)
                > plant.getEarly_day() + plant.getMid_day());

        return cropResponse;
    }

    public static CropDetailsResponse mapCropToCropDetailResponse(Crop crop, Plant plant, Device device, List<Sensor> sensors) {
        CropDetailsResponse cropDetailsResponse = new CropDetailsResponse();

        cropDetailsResponse.setId(crop.getId());
        cropDetailsResponse.setPlant(mapPlantToPlantResponse(plant));
        cropDetailsResponse.setDevice(mapDeviceToDeviceResponse(device, crop));
        cropDetailsResponse.setSensors(sensors);
        cropDetailsResponse.setName(crop.getName());
        cropDetailsResponse.setStartTime(crop.getStart_time());
        cropDetailsResponse.setEndTime(crop.getEnd_time());

        return cropDetailsResponse;
    }

    public static DeviceDetailsResponse mapDeviceToDeviceDetailResponse(Device device, List<Sensor> sensors, List<Actuator> actuators) {
        DeviceDetailsResponse deviceDetailsResponse = new DeviceDetailsResponse();

        deviceDetailsResponse.setId(device.getId());
        deviceDetailsResponse.setAlive(device.isAlive());
        deviceDetailsResponse.setSensors(sensors);
        deviceDetailsResponse.setActuators(actuators);
        deviceDetailsResponse.setCreatedAt(device.getCreatedAt());
        deviceDetailsResponse.setUpdatedAt(device.getUpdatedAt());

        return deviceDetailsResponse;
    }

    public static SensorDataListResponse mapSensorDataToSensorDataListResponse(Sensor sensor, List<SensorData> sensorDataList) {
        SensorDataListResponse cropDetailsResponse = new SensorDataListResponse();

        cropDetailsResponse.setId(sensor.getId());
        cropDetailsResponse.setDeviceId(sensor.getDeviceId());
        cropDetailsResponse.setName(sensor.getName());
        cropDetailsResponse.setType(sensor.getType());
        cropDetailsResponse.setData(sensorDataList.stream().map(
                ModelMapper::mapSensorDataToSensorDataResponse).collect(Collectors.toList()));

        return cropDetailsResponse;
    }

    public static SensorDataResponse mapSensorDataToSensorDataResponse(SensorData sensorData) {
        SensorDataResponse cropDetailsResponse = new SensorDataResponse();

        cropDetailsResponse.setId(sensorData.getId());
        cropDetailsResponse.setTimestamp(sensorData.getTimestamp());
        cropDetailsResponse.setValue(sensorData.getValue());

        return cropDetailsResponse;
    }


    public static PlantResponse mapPlantToPlantResponse(Plant plant) {
        PlantResponse plantResponse = new PlantResponse();

        plantResponse.setId(plant.getId());
        plantResponse.setName(plant.getName());
        plantResponse.setEarlyDay(plant.getEarly_day());
        plantResponse.setMidDay(plant.getMid_day());
        plantResponse.setLateDay(plant.getLate_day());
        plantResponse.setMinEC(plant.getMin_eC());
        plantResponse.setMaxEC(plant.getMax_eC());
        plantResponse.setMinPH(plant.getMin_pH());
        plantResponse.setMaxPH(plant.getMax_pH());

        return plantResponse;
    }
}