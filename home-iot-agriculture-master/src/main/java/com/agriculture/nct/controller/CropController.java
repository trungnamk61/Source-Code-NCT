package com.agriculture.nct.controller;

import com.agriculture.nct.payload.request.CreateCropRequest;
import com.agriculture.nct.security.CurrentUser;
import com.agriculture.nct.security.UserPrincipal;
import com.agriculture.nct.services.HTTPService.CropService;
import com.agriculture.nct.util.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/crop")
@PreAuthorize("hasRole('USER')")
public class CropController {

    private final CropService cropService;

    @Autowired
    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @GetMapping()
    ResponseEntity getCrops(@CurrentUser UserPrincipal currentUser,
                            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok().body(cropService.getAllCrops(currentUser, page, size));
    }

    @PostMapping()
    ResponseEntity createCrop(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody CreateCropRequest createCropRequest) {
        return cropService.createCrop(currentUser.getId(), createCropRequest);
    }

    @DeleteMapping()
    ResponseEntity deleteCrop(@RequestParam(value = "cropId", defaultValue = "0") int cropId) {
        return cropService.deleteCrop(cropId);
    }

    @PostMapping("/stop")
    ResponseEntity stopCrop(@RequestParam(value = "cropId", defaultValue = "0") int cropId) {
        return cropService.stopCrop(cropId);
    }

    @GetMapping("/{cropId}")
    ResponseEntity getCropData(@PathVariable int cropId) {
        return cropService.getCropData(cropId);
    }
}
