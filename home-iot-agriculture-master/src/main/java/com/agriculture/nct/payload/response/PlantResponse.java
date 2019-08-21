package com.agriculture.nct.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlantResponse {
    int id;
    String name;
    int earlyDay;
    int midDay;
    int lateDay;
    float minEC;
    float maxEC;
    float minPH;
    float maxPH;
}