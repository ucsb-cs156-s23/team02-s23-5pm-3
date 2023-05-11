package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Music;
import edu.ucsb.cs156.example.repositories.MusicRepository;

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

@WebMvcTest(controllers = MusicController.class)
@Import(TestConfig.class)
public class MusicControllerTests extends ControllerTestCase {

        @MockBean
        MusicRepository musicRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsbdates/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/musics/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/musics/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/musics?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/ucsbdates/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/musics/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/musics/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                Music music = Music.builder()
                                .title("Test Music")
                                .author("Test Name")
                                .rating(0)
                                .views(0)
                                .build();

                when(musicRepository.findById(eq(7L))).thenReturn(Optional.of(music));

                // act
                MvcResult response = mockMvc.perform(get("/api/musics?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(musicRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(music);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(musicRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/musics?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(musicRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Music with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsbdates() throws Exception {

                Music music1 = Music.builder()
                                .title("Test Music")
                                .author("Test Name")
                                .rating(0)
                                .views(0)
                                .build();

                Music music2 = Music.builder()
                                .title("Test Music 2")
                                .author("Test Name 2")
                                .rating(1)
                                .views(1)
                                .build();

                ArrayList<Music> expectedMusics = new ArrayList<>();
                expectedMusics.addAll(Arrays.asList(music1, music2));

                when(musicRepository.findAll()).thenReturn(expectedMusics);

                // act
                MvcResult response = mockMvc.perform(get("/api/musics/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(musicRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedMusics);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_ucsbdate() throws Exception {
                // arrange

                Music music1 = Music.builder()
                                .title("Test Music")
                                .author("Test Name")
                                .rating(0)
                                .views(0)
                                .build();

                when(musicRepository.save(eq(music1))).thenReturn(music1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/musics/post?title=Test Music&author=Test Name&rating=0&views=0")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(musicRepository, times(1)).save(music1);
                String expectedJson = mapper.writeValueAsString(music1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange
                Music music1 = Music.builder()
                                .title("Test Music")
                                .author("Test Name")
                                .rating(0)
                                .views(0)
                                .build();

                when(musicRepository.findById(eq(15L))).thenReturn(Optional.of(music1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/musics?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(musicRepository, times(1)).findById(15L);
                verify(musicRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Music with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_ucsbdate_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(musicRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/musics?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(musicRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Music with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_ucsbdate() throws Exception {
                // arrange

                Music musicOrig = Music.builder()
                                .title("Test Music")
                                .author("Test Name")
                                .rating(0)
                                .views(0)
                                .build();

                Music musicEdited = Music.builder()
                                .title("Changed Music")
                                .author("Changed Author")
                                .rating(1)
                                .views(1)
                                .build();

                String requestBody = mapper.writeValueAsString(musicEdited);

                when(musicRepository.findById(eq(67L))).thenReturn(Optional.of(musicOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/musics?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(musicRepository, times(1)).findById(67L);
                verify(musicRepository, times(1)).save(musicEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_ucsbdate_that_does_not_exist() throws Exception {
                // arrange
                Music musicEdited = Music.builder()
                                .title("Test Music")
                                .author("Test Name")
                                .rating(0)
                                .views(0)
                                .build();

                String requestBody = mapper.writeValueAsString(musicEdited);

                when(musicRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/musics?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(musicRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Music with id 67 not found", json.get("message"));

        }
}