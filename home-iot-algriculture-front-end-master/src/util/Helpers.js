import {
    EC_SENSOR, FAN,
    HUMIDITY_SENSOR, LED,
    LIGHT_SENSOR,
    PH_SENSOR,
    PUMP_A,
    PUMP_B, PUMP_PH_DOWN, PUMP_PH_UP,
    PUMP_UP, PUMP_WATER,
    TEMPERATURE_SENSOR
} from "../constants";
import moment from "moment";

export function formatDate(date) {
    return moment(date).format("MMMM YYYY");
}

export function formatDateTime(date) {
    return moment(date).format("MMMM Do YYYY, h:mm:ss a");
}

export function totalDate(startDate, endDate) {
    return moment(endDate).diff(moment(startDate), 'days');
}

export function dayNo(date) {
    return moment(date).fromNow();
}

export function getImgUrl(plantId) {
    switch (plantId) {
        case 1:
            return '/static/lettuce-800x600.png';
        case 2:
            return '/static/bok-chok-800x600.png';
        case 3:
            return '/static/onion-800x600.png';
        case 4:
            return '/static/spinach-800x600.png';
        case 5:
            return '/static/cucumber-800x600.png';
        default:
            return '/static/lettuce-800x600.png';
    }
}

export function getUnits(sensorType) {
    switch (sensorType) {
        case PH_SENSOR:
            return "";
        case EC_SENSOR:
            return "ms/cm";
        case TEMPERATURE_SENSOR:
            return "°C";
        case HUMIDITY_SENSOR:
            return "%";
        case LIGHT_SENSOR:
            return "lux";
        default:
            return 0;
    }
}

export function getCurrentValue(type, currentValues) {
    switch (type) {
        case EC_SENSOR:
            return currentValues["EC"];
        case PH_SENSOR:
            return currentValues["pH"];
        case TEMPERATURE_SENSOR:
            return currentValues["temperature"];
        case HUMIDITY_SENSOR:
            return currentValues["humidity"];
        case LIGHT_SENSOR:
            return currentValues["light_intensity"];
        default:
            return;
    }
}

export function getCurrentStatus(type, currentStatus) {
    switch (type) {
        case PUMP_A:
            return currentStatus["pump_a_state"];
        case PUMP_B:
            return currentStatus["pump_b_state"];
        case PUMP_UP:
            return currentStatus["pump_up_state"];
        case PUMP_WATER:
            return currentStatus["pump_water_state"];
        case PUMP_PH_UP:
            return currentStatus["pump_ph_up_state"];
        case PUMP_PH_DOWN:
            return currentStatus["pump_ph_down_state"];
        case LED:
            return currentStatus["led_state"];
        case FAN:
            return currentStatus["fan_state"];
        default:
            return "OFF";
    }
}

export function getActuatorSwitch(actuatorType) {
    switch (actuatorType) {
        case PUMP_A:
            return "PUMP_A";
        case PUMP_B:
            return "PUMP_B";
        case PUMP_UP:
            return "PUMP_UP";
        case PUMP_WATER:
            return "PUMP_WATER";
        case PUMP_PH_UP:
            return "PUMP_PH_UP";
        case PUMP_PH_DOWN:
            return "PUMP_PH_DOWN";
        case LED:
            return "LED";
        case FAN:
            return "FAN";
        default:
            return "";
    }
}