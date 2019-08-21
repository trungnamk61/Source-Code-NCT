package com.agriculture.nct.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SensorDataResponse {
    private int id;
    private double value;
    private Date timestamp;
}