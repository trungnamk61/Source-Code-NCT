package com.agriculture.nct.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Sensor {
    public final static int PH = 1;
    public final static int TEMPERATURE = 2;
    public final static int HUMIDITY = 3;
    public final static int EC = 4;
    public final static int LIGHT = 5;
    public final static int WATER_LEVEL = 6;

    static public String nameByType(int type) {
        switch (type) {
            case PH:
                return "pH";
            case TEMPERATURE:
                return "Temperature";
            case HUMIDITY:
                return "Humidity";
            case EC:
                return "eC";
            case LIGHT:
                return "Light";
            case WATER_LEVEL:
                return "Water lever";
        }

        return "";
    }

    private int id;
    private String name;
    private int deviceId;
    private int type;
    private float currentValue;
}
