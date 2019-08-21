package com.agriculture.nct.database;

import java.sql.*;
import java.util.*;
import java.util.Date;

import javax.sql.DataSource;

import com.agriculture.nct.model.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import static com.agriculture.nct.util.AppConstants.MYSQL_PASSWORD;
import static com.agriculture.nct.util.AppConstants.MYSQL_USERNAME;

public class DBCommon {

    protected static DataSource dataSource = null;
    protected static JdbcTemplate jdbc = null;


    public DBCommon() {
        if (jdbc != null) return;

        DriverManagerDataSource mysqlDS = new DriverManagerDataSource();
        mysqlDS.setDriverClassName("com.mysql.cj.jdbc.Driver");
        mysqlDS.setUrl("jdbc:mysql://localhost:3306/ivofarm");
        mysqlDS.setUsername(MYSQL_USERNAME);
        mysqlDS.setPassword(MYSQL_PASSWORD);
        dataSource = mysqlDS;
        jdbc = new JdbcTemplate(dataSource);
    }

    protected class UserMapper implements RowMapper<User> {
        public User mapRow(ResultSet rs, int rn) throws SQLException {
            return new User(rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getInt("role"),
                    rs.getTimestamp("create_time"),
                    rs.getTimestamp("last_login"));
        }
    }

    protected class DeviceMapper implements RowMapper<Device> {
        public Device mapRow(ResultSet rs, int rn) throws SQLException {
            return new Device(rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getBoolean("alive"),
                    rs.getInt("current_crop"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at")
            );
        }
    }

    protected class CropMapper implements RowMapper<Crop> {
        public Crop mapRow(ResultSet rs, int rn) throws SQLException {
            return new Crop(rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("device_id"),
                    rs.getInt("plant_id"),
                    rs.getString("name"),
                    rs.getTimestamp("start_time"),
                    rs.getTimestamp("end_time")
            );
        }
    }

    protected class PlantMapper implements RowMapper<Plant> {
        public Plant mapRow(ResultSet rs, int rn) throws SQLException {
            return new Plant(rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("early_day"),
                    rs.getInt("mid_day"),
                    rs.getInt("late_day"),
                    rs.getFloat("min_eC"),
                    rs.getFloat("max_eC"),
                    rs.getFloat("min_pH"),
                    rs.getFloat("max_pH"),
                    rs.getBoolean("verified")
            );
        }
    }

    protected class SensorMapper implements RowMapper<Sensor> {
        public Sensor mapRow(ResultSet rs, int rn) throws SQLException {
            return new Sensor(rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("device"),
                    rs.getInt("sensor_type"),
                    rs.getFloat("value"));
        }
    }

    protected class ActuatorMapper implements RowMapper<Actuator> {
        public Actuator mapRow(ResultSet rs, int rn) throws SQLException {
            return new Actuator(rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("device"),
                    rs.getInt("actuator_type"),
                    rs.getString("status"));
        }
    }

    protected class SensorDataMapper implements RowMapper<SensorData> {
        public SensorData mapRow(ResultSet rs, int rn) throws SQLException {
            return new SensorData(rs.getInt("id"),
                    rs.getInt("sensor"),
                    rs.getDouble("value"),
                    rs.getTimestamp("time"));
        }
    }

    protected class CommandMapper implements RowMapper<Command> {
        public Command mapRow(ResultSet rs, int rn) throws SQLException {
            return new Command(rs.getInt("id"),
                    rs.getInt("device"),
                    rs.getString("actuator_name"),
                    rs.getString("action"),
                    rs.getInt("param"),
                    rs.getString("source"),
                    rs.getTimestamp("time"),
                    rs.getBoolean("is_done"));
        }
    }

    public List<User> getAllUsers() {
        return jdbc.query("select * from user order by create_time", new UserMapper());
    }

    public Optional<User> getUserById(int id) {
        return Optional.ofNullable(jdbc.queryForObject("select * from user where id = ?", new Object[]{id}, new UserMapper()));
    }

    public Optional<User> getUserByUsername(String username) {
        return Optional.ofNullable(DataAccessUtils.singleResult(
                jdbc.query("select * from user where username LIKE ?", new Object[]{username}, new UserMapper())));
    }

    public Optional<User> getUserByEmail(String email) {
        return Optional.ofNullable(DataAccessUtils.singleResult(
                jdbc.query("select * from user where email LIKE ?", new Object[]{email}, new UserMapper())));
    }

    public int addUser(User user) throws DataAccessException {
        int id = 0;
        String INSERT_USER_SQL = "INSERT INTO user(username, full_name, email, password, role) "
                + "VALUES(?,?,?,?,?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int affectedRows = jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_USER_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setInt(5, user.getRoleId());
            return ps;
        }, keyHolder);

        if (affectedRows > 0)
            id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        return id;
    }

    public List<Device> getDevicesOfFrame(int frameId) {
        return jdbc.query("select * from device where frame = ?",
                new Object[]{frameId},
                new DeviceMapper());
    }

    public List<Device> getAllDevices() {
        return jdbc.query("select * from device", new DeviceMapper());
    }

    public Optional<Device> getDeviceById(int id) {
        return Optional.ofNullable(DataAccessUtils.singleResult(
                jdbc.query("select * from device where id = ?", new Object[]{id}, new DeviceMapper())));
    }

    public List<Sensor> getSensorsOfDevice(int devId) {
        return jdbc.query("SELECT sensor.*, s1.value FROM sensor_data s1 LEFT JOIN sensor_data s2 ON s1.sensor = s2.sensor AND s1.time < s2.time, sensor\n" +
                "WHERE s2.sensor IS NULL AND s1.sensor = sensor.id AND device = ?\n" +
                "ORDER BY s1.sensor;", new Object[]{devId}, new SensorMapper());
    }

    public Sensor getSensorById(int id) {
        return jdbc.queryForObject("select * from sensor where id = ?",
                new Object[]{id},
                new SensorMapper());
    }

    public List<Actuator> getActuatorsOfDevice(int devId) {
        return jdbc.query("select * from actuator where device = ?",
                new Object[]{devId},
                new ActuatorMapper());
    }

    public Actuator getActuatorById(int id) {
        return jdbc.queryForObject("select * from actuator where id = ?",
                new Object[]{id},
                new ActuatorMapper());
    }

    public Optional<List<SensorData>> getSensorData(int sensorId, int cropId) {
        return Optional.ofNullable(jdbc.query("select * from sensor_data where sensor = ? AND crop = ? order by `time` LIMIT 1000",
                new Object[]{sensorId, cropId}, new SensorDataMapper()));
    }

    public Optional<SensorData> getOneSensorData(int sensorId, int cropId) {
        return Optional.ofNullable(jdbc.queryForObject("select * from sensor_data where sensor = ? AND crop = ? order by `time` LIMIT 1",
                new Object[]{sensorId, cropId}, new SensorDataMapper()));
    }

    public void addSensorData(int devId, int sensorType, double value) throws DataAccessException {
        jdbc.update("insert into sensor_data(crop, sensor, value) select device.current_crop, sensor.id, ? from sensor, device where sensor.device = device.id and device.id = ? and sensor_type = ?",
                value, devId, sensorType);
    }

    public void addSensorData(int devId, int sensorType, double value, Timestamp time) throws DataAccessException {
        jdbc.update("insert into sensor_data(crop, sensor, value, time) select device.current_crop, sensor.id, ?, ? from sensor, device where sensor.device = device.id and device.id = ? and sensor_type = ?",
                value, time, devId, sensorType);
    }

    public void addSensorData(int devId, int packet_no, double temp, double humid, double ph, double ec, double lightVal) throws DataAccessException {
        addSensorData(devId, Sensor.TEMPERATURE, temp);
        addSensorData(devId, Sensor.HUMIDITY, humid);
        addSensorData(devId, Sensor.PH, ph);
        addSensorData(devId, Sensor.EC, ec);
        addSensorData(devId, Sensor.LIGHT, lightVal);
    }


    public void addCommand(Command command) {
        jdbc.update("insert into command(device, actuator_name, action, param, source, is_done) values(?,?,?,?,?,?)",
                command.getDeviceId(), command.getActuatorName(), command.getAction(), command.getParam(), command.getSource(), command.isDone());
    }

    public List<Command> getAllCommands(int devId, boolean onlyNotDone) {
        if (onlyNotDone)
            return jdbc.query("select * from command where device = ? and not is_done order by time",
                    new Object[]{devId}, new CommandMapper());
        else return jdbc.query("select * from command where device = ? order by time",
                new Object[]{devId}, new CommandMapper());
    }

    public Command getNextCommand(int devId) {
        return jdbc.queryForObject("select * from command where device = ? and not is_done order by time limit 1",
                new Object[]{devId}, new CommandMapper());
    }

    public Command getLastCommand(int devId) {
        return jdbc.queryForObject("select * from command where device = ? and not is_done order by time desc limit 1",
                new Object[]{devId}, new CommandMapper());
    }

    public List<CurrentCropsData> getCurrentCropsData() {
        return jdbc.query("SELECT device_id, max_pH, min_pH, max_eC, start_time, sd1.value as curEC, sd2.value as curPH, crop.id as cropId " +
                "FROM plant, crop, " +
                "(select value from sensor_data WHERE sensor IN (SELECT id FROM sensor WHERE sensor_type = 4 && device IN(SELECT id FROM device WHERE alive = true)) ORDER BY time DESC limit 1) as sd1, " +
                "(select value from sensor_data WHERE sensor IN (SELECT id FROM sensor WHERE sensor_type = 1 && device IN(SELECT id FROM device WHERE alive = true)) ORDER BY time DESC limit 1) as sd2 " +
                "WHERE plant.id = 2", new CurrentCropsDataMapper());
    }

    protected class CurrentCropsDataMapper implements RowMapper<CurrentCropsData> {
        public CurrentCropsData mapRow(ResultSet rs, int rn) throws SQLException {
            return new CurrentCropsData(
                    rs.getInt("cropId"),
                    rs.getInt("device_id"),
                    rs.getFloat("max_pH"),
                    rs.getFloat("min_pH"),
                    rs.getFloat("max_eC"),
                    rs.getFloat("curEC"),
                    rs.getFloat("curPH"),
                    rs.getTimestamp("start_time")
            );
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public class CurrentCropsData {
        int cropId;
        int deviceId;
        float maxPH;
        float minPH;
        float maxEC;
        float currentEC;
        float currentPH;
        Date startTime;
    }
}
