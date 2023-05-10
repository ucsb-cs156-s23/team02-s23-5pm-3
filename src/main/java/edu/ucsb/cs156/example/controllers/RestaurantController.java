package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Restaurant;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RestaurantRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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


@Api(description = "Restaurant")
@RequestMapping("/api/restaurant")
@RestController
@Slf4j
public class RestaurantController extends ApiController {

    @Autowired
    RestaurantRepository restaurantRepository;

    @ApiOperation(value = "List all restaurants")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Restaurant> allRestaurants() {
        Iterable<Restaurant> restaurants = restaurantRepository.findAll();
        return restaurants;
    }

    @ApiOperation(value = "Create a new restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Restaurant postRestaurant(
        @ApiParam("name") @RequestParam String name,
        @ApiParam("address") @RequestParam String address,
        @ApiParam("city") @RequestParam String city,
        @ApiParam("state") @RequestParam String state,
        @ApiParam("zip") @RequestParam String zip,
        @ApiParam("description") @RequestParam String description
        )
        {

        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setCity(city);
        restaurant.setState(state);
        restaurant.setZip(zip);
        restaurant.setDescription(description);

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        return savedRestaurant;
    }

}
