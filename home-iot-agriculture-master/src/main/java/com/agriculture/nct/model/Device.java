package com.agriculture.nct.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
public class Device {
    private int id;
    private int userId;
    private boolean alive;
    private int currentCrop;
    private Date createdAt;
    private Date updatedAt;
}
