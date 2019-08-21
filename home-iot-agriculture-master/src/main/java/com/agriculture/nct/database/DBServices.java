package com.agriculture.nct.database;

import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class DBServices extends DBCommon {
    public boolean registerDevice(int userId, int deviceId) {
        return jdbc.update("update device set user_id = ?, updated_at = NOW() where id = ?", userId, deviceId) == 1;
    }

    public void updateKeepAlive(List<Integer> activeDevicesId) throws SQLException {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < activeDevicesId.size(); i++) {
            builder.append("?,");
        }

        String stmt = "UPDATE device SET alive = CASE WHEN id IN ("
                + builder.deleteCharAt(builder.length() - 1).toString() + ") THEN 1 ELSE 0 END, updated_at = ? WHERE user_id IS NOT NULL";

        PreparedStatement pstmt = Objects.requireNonNull(jdbc.getDataSource()).getConnection().prepareStatement(stmt);

        int index = 1;
        for (int id : activeDevicesId) {
            pstmt.setObject(index++, id);
        }
        pstmt.setTimestamp(index, new Timestamp(System.currentTimeMillis()));

        pstmt.execute();
    }

    public void updateActuatorStatus(Map<Integer, String> actuatorStatus, int deviceId) {
        actuatorStatus.forEach((k, v) -> {
            jdbc.update("UPDATE actuator SET status = ? WHERE device = ? AND actuator_type = ?", v, deviceId, k);
        });
    }
}
