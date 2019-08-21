package com.agriculture.nct.database;

import com.agriculture.nct.model.*;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class DBWeb extends DBCommon {

    public Optional<User> findByUsernameOrEmail(String username, String email) {
        Optional<User> user = Optional.empty();
        if (username != null) {
            user = getUserByUsername(username);
            if (user.isPresent()) return user;
        }
        if (email != null) {
            user = getUserByEmail(email);
            if (user.isPresent()) return user;
        }
        return user;
    }

    public Page<Device> findDeviceByPage(Pageable pageable) {
        String rowCountSql = "SELECT count(1) AS row_count FROM device";
        int total = jdbc.queryForObject(rowCountSql, (rs, rowNum) -> rs.getInt(1));

        String querySql = "SELECT * FROM device LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getOffset();
        List<Device> devices = jdbc.query(querySql, new DeviceMapper());

        return new PageImpl<>(devices, pageable, total);
    }

    public Page<Device> findDeviceByPage(int userId, Pageable pageable, @Nullable Boolean available) {
        String avaiSql = available != null ? " AND alive = " + !available + " AND current_crop IS NULL" : "";

        String rowCountSql = "SELECT count(1) AS row_count FROM device WHERE user_id = " + userId + avaiSql;
        int total = jdbc.queryForObject(rowCountSql, (rs, rowNum) -> rs.getInt(1));

        String querySql = "SELECT * FROM device WHERE user_id = " + userId + avaiSql + " LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getOffset();
        List<Device> devices = jdbc.query(querySql, new DeviceMapper());

        return new PageImpl<>(devices, pageable, total);
    }

    public Page<Crop> findCropByPage(int userId, Pageable pageable) {
        String rowCountSql = "SELECT count(1) AS row_count FROM crop WHERE user_id = " + userId;
        int total = jdbc.queryForObject(rowCountSql, (rs, rowNum) -> rs.getInt(1));

        String querySql = "SELECT * FROM crop WHERE user_id = " + userId + " ORDER BY end_time ASC LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getOffset();
        List<Crop> crops = jdbc.query(querySql, new CropMapper());

        return new PageImpl<>(crops, pageable, total);
    }

    public Optional<Plant> getPlantById(int plantId) {
        return Optional.ofNullable(DataAccessUtils.singleResult(
                jdbc.query("SELECT * FROM plant WHERE id = ?", new Object[]{plantId}, new PlantMapper())));
    }

    public List<Plant> findAllPlants() {
        return jdbc.query("SELECT * FROM plant WHERE verified = true", new PlantMapper());
    }

    public List<Plant> findPlantByIdIn(List<Integer> plantIds) throws SQLException {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < plantIds.size(); i++) {
            builder.append("?,");
        }

        String stmt = "SELECT * FROM plant WHERE id IN ("
                + builder.deleteCharAt(builder.length() - 1).toString() + ")";

        PreparedStatement pstmt = Objects.requireNonNull(jdbc.getDataSource()).getConnection().prepareStatement(stmt);

        int index = 1;
        for (int id : plantIds) {
            pstmt.setObject(index++, id);
        }

        List<Plant> plants = new ArrayList<>();
        ResultSet rs = pstmt.executeQuery();

        PlantMapper cropMapper = new PlantMapper();
        // Fetch each row from the result set
        while (rs.next()) {
            plants.add(cropMapper.mapRow(rs, 0));
        }

        return plants;
    }

    public List<Crop> findCropByIdIn(List<Integer> cropIds) throws SQLException {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < cropIds.size(); i++) {
            builder.append("?,");
        }

        String stmt = "SELECT * FROM crop WHERE id IN (" + builder.deleteCharAt(builder.length() - 1).toString() + ")";

        PreparedStatement pstmt = Objects.requireNonNull(jdbc.getDataSource()).getConnection().prepareStatement(stmt);

        int index = 1;
        for (int id : cropIds) {
            pstmt.setObject(index++, id);
        }

        List<Crop> crops = new ArrayList<>();
        ResultSet rs = pstmt.executeQuery();

        CropMapper cropMapper = new CropMapper();
        // Fetch each row from the result set
        while (rs.next()) {
            crops.add(cropMapper.mapRow(rs, 0));
        }

        return crops;
    }

    public Optional<Crop> getCropById(int cropId) {
        return Optional.ofNullable(DataAccessUtils.singleResult(
                jdbc.query("SELECT * FROM crop WHERE id = ?", new Object[]{cropId}, new CropMapper())));
    }

    public int createCrop(String name, int userId, int deviceId, int plantId) {
        int id = 0;
        String INSERT_CROP_SQL = "INSERT INTO crop(name, user_id, device_id, plant_id) VALUES(?,?,?,?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int affectedRows = jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_CROP_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setInt(2, userId);
            ps.setInt(3, deviceId);
            ps.setInt(4, plantId);
            return ps;
        }, keyHolder);

        if (affectedRows > 0)
            id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        return id;
    }

    public boolean addCropDevice(int deviceId, int cropId) {
        return jdbc.update("UPDATE device SET current_crop = ? WHERE id = ?", cropId < 1 ? null : cropId, deviceId) == 1;
    }

    public boolean deleteCrop(int cropId) {
        return jdbc.update("DELETE FROM crop WHERE id = ?", cropId) == 1;
    }

    public boolean stopCrop(int cropId) {
        return jdbc.update("UPDATE crop SET end_time = NOW() WHERE id = ?", cropId) == 1;
    }

    public boolean updateLastLogin(int userId) {
        return jdbc.update("UPDATE user SET last_login = NOW() WHERE id = ?", userId) == 1;
    }

    public Page<User> findUsersByPage(Pageable pageable) {
        String rowCountSql = "SELECT count(1) AS row_count FROM user";
        int total = jdbc.queryForObject(rowCountSql, (rs, rowNum) -> rs.getInt(1));

        String querySql = "SELECT * FROM user LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getOffset();
        List<User> users = jdbc.query(querySql, new UserMapper());

        return new PageImpl<>(users, pageable, total);
    }

    public boolean deleteUser(int userId) {
        return jdbc.update("DELETE FROM user WHERE id = ?", userId) == 1;
    }

    public int createDevice() {
        int id = 0;
        String INSERT_DEVICE_SQL = "INSERT INTO device() VALUE()";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int affectedRows = jdbc.update(connection -> connection.prepareStatement(INSERT_DEVICE_SQL, Statement.RETURN_GENERATED_KEYS), keyHolder);

        if (affectedRows > 0)
            id = Objects.requireNonNull(keyHolder.getKey()).intValue();

        return id;
    }

    public boolean createDeviceSensor(int deviceId, List<Integer> sensorTypeList) {
        StringBuilder builder = new StringBuilder();
        if (sensorTypeList.size() == 0) return true;
        for (int i = 0; i < sensorTypeList.size(); i++) {
            builder.append("(?,?,?),");
        }

        String INSERT_DEVICE_SQL = "INSERT INTO sensor(name,device,sensor_type) VALUES" + builder.deleteCharAt(builder.length() - 1).toString();

        int affectedRows = jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_DEVICE_SQL);
            int index = 1;
            for (int typeId : sensorTypeList) {
                ps.setString(index++, Sensor.nameByType(typeId));
                ps.setInt(index++, deviceId);
                ps.setInt(index++, typeId);
            }
            return ps;
        });

        return affectedRows == sensorTypeList.size();
    }

    public boolean createDeviceActuator(int deviceId, List<Integer> actuatorTypeList) {
        StringBuilder builder = new StringBuilder();
        if (actuatorTypeList.size() == 0) return true;
        for (int i = 0; i < actuatorTypeList.size(); i++) {
            builder.append("(?,?,?),");
        }

        String INSERT_DEVICE_SQL = "INSERT INTO actuator(name,device,actuator_type) VALUES" + builder.deleteCharAt(builder.length() - 1).toString();

        int affectedRows = jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_DEVICE_SQL);
            int index = 1;
            for (int typeId : actuatorTypeList) {
                ps.setString(index++, Actuator.nameByType(typeId));
                ps.setInt(index++, deviceId);
                ps.setInt(index++, typeId);
            }
            return ps;
        });

        return affectedRows == actuatorTypeList.size();
    }

    public boolean deleteDevice(int deviceId) {
        return jdbc.update("DELETE FROM device WHERE id = ?", deviceId) == 1;
    }
}
