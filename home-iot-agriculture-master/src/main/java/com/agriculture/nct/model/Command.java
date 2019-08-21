package com.agriculture.nct.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@RequiredArgsConstructor
public class Command {
    public static final String FROM_USER = "user";
    public static final String FROM_SERVER = "auto";

    private int id;

    @NonNull
    @JsonProperty("dev_id")
    private int deviceId;

    @NonNull
    @JsonProperty("actuator_name")
    private String actuatorName;

    @NonNull
    @JsonProperty("action")
    private String action;

    @NonNull
    private float param;

    @NonNull
    private String source;

    private Date time;

    private boolean isDone;
}
