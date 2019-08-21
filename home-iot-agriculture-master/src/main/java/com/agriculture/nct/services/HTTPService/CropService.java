package com.agriculture.nct.services.HTTPService;

import com.agriculture.nct.database.DBWeb;
import com.agriculture.nct.exception.BadRequestException;
import com.agriculture.nct.exception.ResourceNotFoundException;
import com.agriculture.nct.model.*;
import com.agriculture.nct.payload.request.CreateCropRequest;
import com.agriculture.nct.payload.response.ApiResponse;
import com.agriculture.nct.payload.response.CropResponse;
import com.agriculture.nct.payload.response.PagedResponse;
import com.agriculture.nct.security.JwtTokenProvider;
import com.agriculture.nct.security.UserPrincipal;
import com.agriculture.nct.services.MqttService.MqttControlService;
import com.agriculture.nct.util.AppConstants;
import com.agriculture.nct.util.ModelMapper;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.agriculture.nct.util.AppConstants.DEFAULT_COLLECT_PERIOD;
import static com.agriculture.nct.util.AppConstants.START_CROP_ACTION;
import static com.agriculture.nct.util.AppConstants.STOP_CROP_ACTION;

@Service
@Log4j2
public class CropService {
    private final DBWeb dbWeb;
    private final MqttControlService mqttControlService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public CropService(DBWeb dbWeb, MqttControlService mqttControlService, JwtTokenProvider jwtTokenProvider) {
        this.dbWeb = dbWeb;
        this.mqttControlService = mqttControlService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public ResponseEntity getCropData(int cropId) throws ResourceNotFoundException {
        Crop crop = dbWeb.getCropById(cropId).orElseThrow(() -> new ResourceNotFoundException("Crop", "id", cropId));
        int plantId = crop.getPlant_id();
        int deviceId = crop.getDevice_id();
        Plant plant = dbWeb.getPlantById(plantId).orElseThrow(() -> new ResourceNotFoundException("Plant", "id", plantId));
        Device device = dbWeb.getDeviceById(deviceId).orElseThrow(
                () -> new ResourceNotFoundException("Device", "id", deviceId));
        List<Sensor> sensors = dbWeb.getSensorsOfDevice(deviceId);

        return ResponseEntity.ok().body(ModelMapper.mapCropToCropDetailResponse(crop, plant, device, sensors));
    }

    public PagedResponse<CropResponse> getAllCrops(UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        // Retrieve Crops
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "end_time");
        Page<Crop> crops = dbWeb.findCropByPage(currentUser.getId(), pageable);

        if (crops.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), crops.getNumber(),
                    crops.getSize(), crops.getTotalElements(), crops.getTotalPages(), crops.isLast());
        }
        Map<Integer, Plant> plantMap;
        try {
            // Map Crops to CropResponses containing Plant
            plantMap = getCropPlantMap(crops.getContent());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BadRequestException("False to fetch crop!", e);
        }
        List<CropResponse> cropResponses = crops.map(crop ->
                ModelMapper.mapCropToCropResponse(crop, plantMap.get(crop.getPlant_id()))).getContent();

        return new PagedResponse<>(cropResponses, crops.getNumber(),
                crops.getSize(), crops.getTotalElements(), crops.getTotalPages(), crops.isLast());
    }

    public ResponseEntity createCrop(int userId, CreateCropRequest createCropRequest) {
        int deviceId = createCropRequest.getDeviceId();
        Device device = dbWeb.getDeviceById(deviceId).orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));
        if (device.getUserId() != userId) throw new BadRequestException("The device is not registered by this user.");
        int plantId = createCropRequest.getPlantId();
        dbWeb.getPlantById(plantId).orElseThrow(() -> new ResourceNotFoundException("Plant", "id", plantId));

        int cropId = dbWeb.createCrop(createCropRequest.getName(), userId, deviceId, plantId);

        if (cropId == 0)
            return ResponseEntity.unprocessableEntity().body(new ApiResponse(false, "Create new Crop failed!"));

        Map<String, Object> startCommand = new HashMap<>();
        startCommand.put("action", START_CROP_ACTION);
        startCommand.put("time_period", DEFAULT_COLLECT_PERIOD);
        startCommand.put("device_token", jwtTokenProvider.generateDeviceToken(deviceId, cropId, userId));

        boolean isDone = mqttControlService.publish(createCropRequest.getDeviceId(), new JSONObject(startCommand).toString());
        if (!isDone)
            return ResponseEntity.unprocessableEntity().body(new ApiResponse(false, "Device start Crop failed!"));
        dbWeb.addCommand(new Command(deviceId, "device", START_CROP_ACTION, DEFAULT_COLLECT_PERIOD, Command.FROM_USER));

        if (!dbWeb.addCropDevice(deviceId, cropId))
            return ResponseEntity.unprocessableEntity().body(new ApiResponse(false, "Device start Crop failed!"));

        return ResponseEntity.ok().body(new ApiResponse(true, "Create new Crop successfully"));
    }

    public ResponseEntity deleteCrop(int cropId) {
        Crop crop = dbWeb.getCropById(cropId).orElseThrow(
                () -> new ResourceNotFoundException("Crop", "id", cropId));
        if (crop.getEnd_time() == null) {
            return ResponseEntity.unprocessableEntity().body(
                    new ApiResponse(false, "Delete Crop failed! Crop is planting."));
        }
        if (dbWeb.deleteCrop(cropId))
            return ResponseEntity.ok().body(
                    new ApiResponse(true, "Delete Crop successfully!"));
        else return ResponseEntity.unprocessableEntity().body(
                new ApiResponse(false, "Delete Crop failed! Can not delete crop."));
    }

    public ResponseEntity stopCrop(int cropId) {
        Crop crop = dbWeb.getCropById(cropId).orElseThrow(() -> new ResourceNotFoundException("Crop", "id", cropId));

        int deviceId = crop.getDevice_id();
        Map<String, String> stopCommand = new HashMap<>();
        stopCommand.put("action", STOP_CROP_ACTION);

        if (crop.getEnd_time() != null)
            throw new BadRequestException("Crop " + crop.getName() + " has already stopped.");
        if (!dbWeb.stopCrop(cropId))
            return ResponseEntity.ok().body(new ApiResponse(false, "Failed to send stop command"));
        boolean isPublishDone = mqttControlService.publish(deviceId, new JSONObject(stopCommand).toString());
        if (!isPublishDone) ResponseEntity.ok().body(new ApiResponse(false, "Failed to send stop command"));
        dbWeb.addCropDevice(deviceId, 0);
        return ResponseEntity.ok().body(new ApiResponse(true, "Stop Crop successfully"));
    }

    private Map<Integer, Plant> getCropPlantMap(List<Crop> crops) throws SQLException {
        // Get Plant details of the given list of crops
        List<Integer> plantIds = crops.stream()
                .map(Crop::getPlant_id)
                .distinct()
                .collect(Collectors.toList());

        List<Plant> plants = dbWeb.findPlantByIdIn(plantIds);

        return plants.stream()
                .collect(Collectors.toMap(Plant::getId, Function.identity()));
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