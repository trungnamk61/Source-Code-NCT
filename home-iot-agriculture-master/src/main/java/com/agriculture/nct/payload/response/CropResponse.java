package com.agriculture.nct.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CropResponse {
    private int id;
    int deviceId;
    int plantId;
    String plantTypeName;
    String name;
    Date startTime;
    Date endTime;
    boolean isDone;
}