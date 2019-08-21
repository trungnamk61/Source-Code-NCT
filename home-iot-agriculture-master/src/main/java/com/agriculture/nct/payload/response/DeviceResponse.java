package com.agriculture.nct.payload.response;

import com.agriculture.nct.model.Crop;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DeviceResponse {
    private int id;
    private Crop crop;
    private Date creationDateTime;
    private Date lastUpdateDateTime;
    private boolean alive;
}