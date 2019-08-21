package com.agriculture.nct.services.HTTPService;

import com.agriculture.nct.database.DBWeb;
import com.agriculture.nct.model.*;
import com.agriculture.nct.exception.BadRequestException;
import com.agriculture.nct.exception.ResourceNotFoundException;
import com.agriculture.nct.payload.response.DeviceResponse;
import com.agriculture.nct.payload.response.PagedResponse;
import com.agriculture.nct.security.UserPrincipal;
import com.agriculture.nct.util.AppConstants;
import com.agriculture.nct.util.ModelMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@Log4j2
public class DeviceService {

    private final DBWeb dbWeb;

    @Autowired
    public DeviceService(DBWeb dbWeb) {
        this.dbWeb = dbWeb;
    }


    public ResponseEntity getDeviceDetails(int deviceId) throws ResourceNotFoundException {
        Device device = dbWeb.getDeviceById(deviceId).orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));
        List<Sensor> sensors = dbWeb.getSensorsOfDevice(deviceId);
        List<Actuator> actuators = dbWeb.getActuatorsOfDevice(deviceId);
        return ResponseEntity.ok().body(ModelMapper.mapDeviceToDeviceDetailResponse(device, sensors, actuators));
    }

    public PagedResponse<DeviceResponse> getAllDevices(UserPrincipal currentUser, int page, int size, @Nullable Boolean available) {
        validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<Device> devices = dbWeb.findDeviceByPage(currentUser.getId(), pageable, available);

        if (devices.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), devices.getNumber(),
                    devices.getSize(), devices.getTotalElements(), devices.getTotalPages(), devices.isLast());
        }

        Map<Integer, Crop> cropMap;
        try {
            cropMap = getDeviceCropMap(devices.getContent());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BadRequestException("False to fetch devices!", e);
        }

        List<DeviceResponse> deviceResponses = devices.map(
                (Device device) -> ModelMapper.mapDeviceToDeviceResponse(device, cropMap.get(device.getCurrentCrop()))).getContent();

        return new PagedResponse<>(deviceResponses, devices.getNumber(),
                devices.getSize(), devices.getTotalElements(), devices.getTotalPages(), devices.isLast());
    }

    private Map<Integer, Crop> getDeviceCropMap(List<Device> devices) throws SQLException {
        // Get Plant details of the given list of crops
        List<Integer> cropIds = devices.stream()
                .map(Device::getCurrentCrop)
                .distinct()
                .collect(Collectors.toList());

        List<Crop> crops = dbWeb.findCropByIdIn(cropIds);

        return crops.stream().collect(Collectors.toMap(Crop::getId, Function.identity()));
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