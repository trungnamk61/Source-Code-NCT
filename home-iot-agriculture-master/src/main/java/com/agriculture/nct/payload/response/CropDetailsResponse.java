package com.agriculture.nct.payload.response;

import com.agriculture.nct.model.Sensor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class CropDetailsResponse {
    private int id;
    DeviceResponse device;
    PlantResponse plant;
    List<Sensor> sensors;
    String name;
    Date startTime;
    Date endTime;
}