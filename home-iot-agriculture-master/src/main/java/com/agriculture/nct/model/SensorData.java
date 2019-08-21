package com.agriculture.nct.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@AllArgsConstructor
@Getter
public class SensorData {
    private int id;
    private int sensorId;
    private double value;
    private Date timestamp;
}
