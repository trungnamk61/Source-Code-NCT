package com.agriculture.nct.payload.response;

import com.agriculture.nct.model.Actuator;
import com.agriculture.nct.model.Sensor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class DeviceDetailsResponse {
    private int id;
    private boolean alive;
    private int cropId;
    List<Sensor> sensors;
    List<Actuator> actuators;
    private Date createdAt;
    private Date updatedAt;
}