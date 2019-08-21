package com.agriculture.nct.schedule;

import com.agriculture.nct.database.DBCommon;
import com.agriculture.nct.database.DBServices;
import com.agriculture.nct.model.Command;
import com.agriculture.nct.services.MqttService.MqttControlService;
import com.agriculture.nct.util.PumpControl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class ScheduledControl {

    private MqttControlService mqttControlService;
    private PumpControl pumpControl;
    private DBServices dbServices;

    @Autowired
    public ScheduledControl(MqttControlService mqttControlService, DBServices dbServices) {
        this.mqttControlService = mqttControlService;
        this.pumpControl = new PumpControl();
        this.dbServices = dbServices;
    }

    // cron second minute hour day month day-of-week year
    @Scheduled(cron = "0/20 * * * * ?")
    //@Scheduled(cron = "0 0 0/4 * * *")
    public void autoPumpControl() {
        double curEC, curpH, phmax, phmin, ecmax;
        int curDay, devId;
        long test;
        Date startTime;
        List<DBCommon.CurrentCropsData> data = dbServices.getCurrentCropsData();
        log.info("Number of active Crops: " + data.size());
        for (DBCommon.CurrentCropsData record : data) {
            curEC = record.getCurrentEC();
            curpH = record.getCurrentPH();
            phmax = record.getMaxPH();
            phmin = record.getMinPH();
            ecmax = record.getMaxEC();
            log.info(phmin + " : " + ecmax);
            startTime = record.getStartTime();
            Date today = new Date();
            test =  TimeUnit.DAYS.convert(today.getTime() - startTime.getTime(), TimeUnit.MILLISECONDS);
            curDay = (int) test;
            devId = record.getDeviceId();
            ArrayList<Command> lsCmd = pumpControl.autoConrolPump(curEC, curpH, curDay, phmax, devId, phmin, ecmax);
            for(Command command : lsCmd) {
                mqttControlService.controlDevice(command);
            }
        }
    }

}
