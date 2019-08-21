package com.agriculture.nct.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Plant {
    int id;
    String name;
    int early_day;
    int mid_day;
    int late_day;
    float min_eC;
    float max_eC;
    float min_pH;
    float max_pH;
    boolean verified;
}
