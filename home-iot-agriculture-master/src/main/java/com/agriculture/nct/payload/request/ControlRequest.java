package com.agriculture.nct.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Setter
@Getter
public class ControlRequest {
    @NotNull
    private int deviceId;

    @NotBlank
    private String actuatorName;

    @NotNull
    private String action;

    @NotNull
    private int param;
}