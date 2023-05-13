package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Animal;
import edu.ucsb.cs156.example.repositories.AnimalRepository;

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

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = AnimalController.class)
@Import(TestConfig.class)
public class AnimalControllerTests extends ControllerTestCase {

        @MockBean
        AnimalRepository animalRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/animals/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/animals/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/animals/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/animals?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/animals/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/animals/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/animals/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Animal animal = Animal.builder()
                                .name("wolf")
                                .genus("canis")
                                .species("canis lupis")
                                .build();

                when(animalRepository.findById(eq(7L))).thenReturn(Optional.of(animal));

                // act
                MvcResult response = mockMvc.perform(get("/api/animals?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(animalRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(animal);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(animalRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/animals?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(animalRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Animal with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_animals() throws Exception {

                // arrange

                Animal animal1 = Animal.builder()
                            .name("wolf")
                            .genus("canis")
                            .species("canis lupis")
                            .build();

                Animal animal2 = Animal.builder()
                            .name("test name")
                            .genus("test genus")
                            .species("test species")
                            .build();

                ArrayList<Animal> expectedAnimals = new ArrayList<>();
                expectedAnimals.addAll(Arrays.asList(animal1, animal2));

                when(animalRepository.findAll()).thenReturn(expectedAnimals);

                // act
                MvcResult response = mockMvc.perform(get("/api/animals/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(animalRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedAnimals);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }


        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_animal() throws Exception {
                // arrange

                Animal animal1 = Animal.builder()
                        .name("wolf")
                        .genus("canis")
                        .species("canis lupis")
                        .build();

                when(animalRepository.save(eq(animal1))).thenReturn(animal1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/animals/post?name=wolf&genus=canis&species=canis lupis")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(animalRepository, times(1)).save(animal1);
                String expectedJson = mapper.writeValueAsString(animal1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_animal() throws Exception {
                // arrange

                Animal animal1 = Animal.builder()
                        .name("wolf")
                        .genus("canis")
                        .species("canis lupis")
                        .build();

                when(animalRepository.findById(eq(15L))).thenReturn(Optional.of(animal1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/animals?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(animalRepository, times(1)).findById(15L);
                verify(animalRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Animal with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_animal_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(animalRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/animals?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(animalRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Animal with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_animal() throws Exception {
                // arrange

                Animal animalOrig = Animal.builder()
                        .name("wolf")
                        .genus("canis")
                        .species("canis lupis")
                        .build();

                Animal animalEdited = Animal.builder()
                        .name("new wolf")
                        .genus("new canis")
                        .species("new canis lupis")
                        .build();

                String requestBody = mapper.writeValueAsString(animalEdited);

                when(animalepository.findById(eq(67L))).thenReturn(Optional.of(animalOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/animals?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(animalRepository, times(1)).findById(67L);
                verify(animalRepository, times(1)).save(animalEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_animal_that_does_not_exist() throws Exception {
                // arrange

                Animal animalEdited = Animal.builder()
                        .name("new wolf")
                        .genus("new canis")
                        .species("new canis lupis")
                        .build();

                String requestBody = mapper.writeValueAsString(animalEdited);

                when(animalRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/animals?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(animalRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Animal with id 67 not found", json.get("message"));

        }
}
