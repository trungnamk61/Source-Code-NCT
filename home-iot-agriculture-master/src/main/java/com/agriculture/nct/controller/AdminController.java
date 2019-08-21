package com.agriculture.nct.controller;

import com.agriculture.nct.database.DBWeb;
import com.agriculture.nct.exception.AppException;
import com.agriculture.nct.exception.BadRequestException;
import com.agriculture.nct.exception.ResourceNotFoundException;
import com.agriculture.nct.model.*;
import com.agriculture.nct.payload.request.CreateDeviceRequest;
import com.agriculture.nct.payload.request.SignUpRequest;
import com.agriculture.nct.payload.response.ApiResponse;
import com.agriculture.nct.payload.response.DeviceResponse;
import com.agriculture.nct.payload.response.PagedResponse;
import com.agriculture.nct.util.AppConstants;
import com.agriculture.nct.util.ModelMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Log4j2
public class AdminController {

    private final DBWeb dbWeb;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminController(DBWeb dbWeb, PasswordEncoder passwordEncoder) {
        this.dbWeb = dbWeb;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/fakedata")
    public ResponseEntity fakeData(@RequestParam(value = "deviceId") int deviceId, @RequestParam(value = "plantId") int plantId, @RequestParam(value = "days") int days) {
        Random rand = new Random();
        Plant plant = dbWeb.getPlantById(plantId).orElseThrow(() -> new ResourceNotFoundException("Plant", "id", plantId));
        for (int i = 0; i < 24 * days; i++) {
            Timestamp time = new Timestamp(System.currentTimeMillis() - 3600L * 1000L * i);
            dbWeb.addSensorData(deviceId, Sensor.TEMPERATURE, 15 + rand.nextFloat() * 15, time);
            dbWeb.addSensorData(deviceId, Sensor.HUMIDITY, 30 + rand.nextFloat() * 60, time);
            dbWeb.addSensorData(deviceId, Sensor.PH, plant.getMin_pH() + rand.nextFloat() * (plant.getMax_pH() - plant.getMin_pH() + 1) - 0.5, time);
            dbWeb.addSensorData(deviceId, Sensor.EC, plant.getMin_eC() + rand.nextFloat() * (plant.getMax_eC() - plant.getMin_eC() + 1) - 0.5, time);
            dbWeb.addSensorData(deviceId, Sensor.LIGHT, 5500 + rand.nextFloat() * 5000, time);
        }
        return ResponseEntity.ok().body(new ApiResponse(true, "fake success"));
    }

    @GetMapping("/user")
    public ResponseEntity getUsers(@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                   @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<User> users = dbWeb.findUsersByPage(pageable);

        if (users.getNumberOfElements() == 0) {
            return ResponseEntity.ok().body(new PagedResponse<>(Collections.emptyList(), users.getNumber(),
                    users.getSize(), users.getTotalElements(), users.getTotalPages(), users.isLast()));
        }

        return ResponseEntity.ok().body(new PagedResponse<>(users.getContent(), users.getNumber(),
                users.getSize(), users.getTotalElements(), users.getTotalPages(), users.isLast()));
    }


    @PostMapping("/user")
    public ResponseEntity<?> createUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (dbWeb.getUserByUsername(signUpRequest.getUsername()).isPresent())
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Username is already taken!"));

        if (dbWeb.getUserByEmail(signUpRequest.getEmail()).isPresent())
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Email Address already in use!"));

        // Creating user's account
        User user = new User(signUpRequest.getUsername(), signUpRequest.getFullName(), signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()), signUpRequest.getRole() != null ? signUpRequest.getRole() : Role.ROLE_USER.getId());

        int userId = dbWeb.addUser(user);

        if (userId == 0) throw new AppException("User created failed!");

        return ResponseEntity.ok().body(new ApiResponse(true, "User created successfully"));
    }

    @DeleteMapping("/user")
    ResponseEntity deleteUser(@RequestParam(value = "userId", defaultValue = "0") int userId) {
        dbWeb.getUserById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (dbWeb.deleteUser(userId))
            return ResponseEntity.ok().body(new ApiResponse(true, "Delete User successfully!"));
        else return ResponseEntity.unprocessableEntity().body(
                new ApiResponse(false, "Delete User failed! Can not delete User."));
    }

    @PostMapping("/device")
    ResponseEntity createDevice(@Valid @RequestBody CreateDeviceRequest createDeviceRequest) {
        int deviceId = dbWeb.createDevice();
        if (deviceId == 0)
            return ResponseEntity.unprocessableEntity().body(new ApiResponse(false, "Create new Device failed!"));
        if (!dbWeb.createDeviceActuator(deviceId, createDeviceRequest.getActuators()))
            return ResponseEntity.unprocessableEntity().body(new ApiResponse(false, "Create new Actuator failed!"));
        if (!dbWeb.createDeviceSensor(deviceId, createDeviceRequest.getSensors()))
            return ResponseEntity.unprocessableEntity().body(new ApiResponse(false, "Create new Sensor failed!"));
        return ResponseEntity.ok().body(dbWeb.getDeviceById(deviceId).orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId)));
    }

    @GetMapping("/device")
    public ResponseEntity getDevices(@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                     @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<Device> devices = dbWeb.findDeviceByPage(pageable);

        if (devices.getNumberOfElements() == 0) {
            return ResponseEntity.ok().body(new PagedResponse<>(Collections.emptyList(), devices.getNumber(),
                    devices.getSize(), devices.getTotalElements(), devices.getTotalPages(), devices.isLast()));
        }

        return ResponseEntity.ok().body(new PagedResponse<>(devices.getContent(), devices.getNumber(),
                devices.getSize(), devices.getTotalElements(), devices.getTotalPages(), devices.isLast()));
    }

    @DeleteMapping("/device")
    ResponseEntity deleteDevice(@RequestParam(value = "deviceId", defaultValue = "0") int deviceId) {
        dbWeb.getDeviceById(deviceId).orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));
        if (dbWeb.deleteDevice(deviceId))
            return ResponseEntity.ok().body(new ApiResponse(true, "Delete Device successfully!"));
        else return ResponseEntity.unprocessableEntity().body(
                new ApiResponse(false, "Delete Device failed! Can not delete Device."));
    }

    private void validatePageNumberAndSize(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if (size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }
}
