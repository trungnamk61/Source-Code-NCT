package com.agriculture.nct.services.HTTPService;

import com.agriculture.nct.database.DBWeb;
import com.agriculture.nct.model.Plant;
import com.agriculture.nct.payload.response.PlantResponse;
import com.agriculture.nct.util.ModelMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class PlantService {

    private final DBWeb dbWeb;

    @Autowired
    public PlantService(DBWeb dbWeb) {
        this.dbWeb = dbWeb;
    }

    public List<PlantResponse> getAllPlants() {
        List<Plant> plants = dbWeb.findAllPlants();

        return plants.stream().map(ModelMapper::mapPlantToPlantResponse).collect(Collectors.toList());
    }
}