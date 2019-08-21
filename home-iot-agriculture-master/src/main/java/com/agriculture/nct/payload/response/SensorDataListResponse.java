package com.agriculture.nct.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SensorDataListResponse {
    private int id;
    private String name;
    private int deviceId;
    private int type;
    List<SensorDataResponse> data;
}