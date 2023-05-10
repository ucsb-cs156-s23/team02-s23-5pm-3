package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.controllers.RestaurantController;
import edu.ucsb.cs156.example.entities.Restaurant;
import edu.ucsb.cs156.example.repositories.RestaurantRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RestaurantController.class)
@Import(TestConfig.class)
public class RestaurantControllerTests extends ControllerTestCase {

        @MockBean
        RestaurantRepository restaurantRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/restaurant/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/restaurant/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/restaurant/all"))
                                .andExpect(status().is(200)); // logged
        }

        // Authorization tests for /api/restaurant/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/restaurant/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/restaurant/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_restaurants() throws Exception {

                // arrange

                /* TODO: setup tests */

                Restaurant habit = Restaurant.builder()
                                .name("The Habit")
                                .address("888 Embarcadero del Norte")
                                .city("Isla Vista")
                                .state("CA")
                                .zip("93117")
                                .description("Burgers and Fries")
                                .build();

                Restaurant cristinos = Restaurant.builder()
                                .name("Cristino's Bakery")
                                .address("170 Aero Camino")
                                .city("Goleta")
                                .state("CA")
                                .zip("93117")
                                .description("This place is takeout only.  It may look mostly like a bakery with Mexican pastries, but it also has amazing burritos and tacos")
                                .build();

                ArrayList<Restaurant> expectedRestaurants = new ArrayList<>();
                expectedRestaurants.addAll(Arrays.asList(habit, cristinos));

                when(restaurantRepository.findAll()).thenReturn(expectedRestaurants);

                // act
                MvcResult response = mockMvc.perform(get("/api/restaurant/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(restaurantRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedRestaurants);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_restaurant() throws Exception {
                // arrange

                Restaurant freebirds = Restaurant.builder()
                                .name("Freebirds")
                                .address("879 Embarcadero del Norte")
                                .city("Isla Vista")
                                .state("CA")
                                .zip("93117")
                                .description("Burrito joint, and iconic Isla Vista location")
                                .build();

                when(restaurantRepository.save(eq(freebirds))).thenReturn(freebirds);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/restaurant/post")
                                .param("name", "Freebirds")
                                .param("address", "879 Embarcadero del Norte")
                                .param("city", "Isla Vista")
                                .param("state", "CA")
                                .param("zip", "93117")
                                .param("description", "Burrito joint, and iconic Isla Vista location")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(restaurantRepository, times(1)).save(freebirds);
                String expectedJson = mapper.writeValueAsString(freebirds);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }
}
