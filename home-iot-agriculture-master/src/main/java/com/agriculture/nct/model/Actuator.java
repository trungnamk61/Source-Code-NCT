package com.agriculture.nct.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Actuator {
    public final static int PUMP_A = 1;
    public final static int PUMP_B = 2;
    public final static int PUMP_UP = 3;
    public final static int PUMP_WATER = 4;
    public final static int PUMP_PH_UP = 5;
    public final static int PUMP_PH_DOWN = 6;
    public final static int LED = 7;
    public final static int FAN = 8;
    public final static String ON = "ON";
    public final static String OFF = "OFF";

    static public String nameByType(int type) {
        switch (type) {
            case PUMP_A:
                return "A-solution pump";
            case PUMP_B:
                return "B-solution pump";
            case PUMP_UP:
                return "Water up pump";
            case PUMP_WATER:
                return "Water pump";
            case PUMP_PH_UP:
                return "pH-up pump";
            case PUMP_PH_DOWN:
                return "pH-down pump";
            case LED:
                return "Led";
            case FAN:
                return "Fan";
        }

        return "";
    }

    private int id;
    private String name;
    private int deviceId;
    private int type;
    private String status;
}
