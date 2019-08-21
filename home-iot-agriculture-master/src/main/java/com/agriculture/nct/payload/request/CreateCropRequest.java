package com.agriculture.nct.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

@Getter
@Setter
public class CreateCropRequest {
    @NotBlank
    @Size(min = 4, max = 140)
    private String name;

    @NotNull
    private int deviceId;

    @NotNull
    private int plantId;
}