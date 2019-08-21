package com.agriculture.nct.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Crop {
    int id;
    int user_id;
    int device_id;
    int plant_id;
    String name;
    Date start_time;
    Date end_time;
}
