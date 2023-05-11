package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Park;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ParkRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;

@Api(description = "Parks")
@RequestMapping("/api/parks")
@RestController
@Slf4j
public class ParksController extends ApiController {

    @Autowired
    ParkRepository parkRepository;

    @ApiOperation(value = "List all parks")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Park> allParks() {
        Iterable<Park> parks = parkRepository.findAll();
        return parks;
    }

    @ApiOperation(value = "Get a single park")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Park getById(
            @ApiParam("id") @RequestParam Long id) {
        Park park = parkRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Park.class, id));

        return park;
    }

    @ApiOperation(value = "Create a new park")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Park postPark(
            @ApiParam("name") @RequestParam String name,
            @ApiParam("address") @RequestParam String address,
            @ApiParam("rating") @RequestParam String rating)
            throws JsonProcessingException {

        Park park = new Park();
        park.setName(name);
        park.setAddress(address);
        park.setRating(rating);

        Park savedPark = parkRepository.save(park);

        return savedPark;
    }

    @ApiOperation(value = "Delete a Park")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deletePark(
            @ApiParam("id") @RequestParam Long id) {
        Park park = parkRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Park.class, id));

        parkRepository.delete(park);
        return genericMessage("Park with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single park")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Park updatePark(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Park incoming) {

        Park park = parkRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Park.class, id));

        park.setName(incoming.getName());
        park.setAddress(incoming.getAddress());
        park.setRating(incoming.getRating());

        parkRepository.save(park);

        return park;
    }
}
