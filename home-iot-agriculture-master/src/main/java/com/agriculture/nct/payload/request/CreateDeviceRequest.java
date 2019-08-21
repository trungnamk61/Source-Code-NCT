package com.agriculture.nct.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;
import java.util.List;

@Getter
@Setter
public class CreateDeviceRequest {
    @NotNull
    @Size(max = 8)
    private List<@Min(1) @Max(8) Integer> actuators;

    @NotNull
    @Size(min = 1, max = 6)
    private List<@Min(1) @Max(6) Integer> sensors;
}