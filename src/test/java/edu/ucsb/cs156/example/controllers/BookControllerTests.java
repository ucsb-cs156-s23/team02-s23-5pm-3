package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Book;
import edu.ucsb.cs156.example.repositories.BookRepository;

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

@WebMvcTest(controllers = BookController.class)
@Import(TestConfig.class)
public class BookControllerTests extends ControllerTestCase {

        @MockBean
        BookRepository BookRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/Book/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/Book/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/Book/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/Book?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/Book/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/Book/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/Book/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Book Book2 = Book.builder()
                                .title("IT")
                                .author("Stephen King")
                                .genre("Horror")
                                .build();

                when(BookRepository.findById(eq(7L))).thenReturn(Optional.of(Book2));

                // act
                MvcResult response = mockMvc.perform(get("/api/Book?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(BookRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(Book2);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(BookRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/Book?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(BookRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Book with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_Books() throws Exception {

                // arrange

                Book Book1 = Book.builder()
                                .title("IT")
                                .author("Stephen King")
                                .genre("Horror")
                                .build();


                Book Book2 = Book.builder()
                                .title("IT")
                                .author("Stephen King")
                                .genre("Horror")
                                .build();

                ArrayList<Book> expectedDates = new ArrayList<>();
                expectedDates.addAll(Arrays.asList(Book1, Book2));

                when(BookRepository.findAll()).thenReturn(expectedDates);

                // act
                MvcResult response = mockMvc.perform(get("/api/Book/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(BookRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedDates);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_Book() throws Exception {
                // arrange


                Book Book1 = Book.builder()
                                .title("IT")
                                .author("Stephen King")
                                .genre("Horror")
                                .build();

                when(BookRepository.save(eq(Book1))).thenReturn(Book1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/Book/post?title=IT&author=Stephen King&genre=Horror")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(BookRepository, times(1)).save(Book1);
                String expectedJson = mapper.writeValueAsString(Book1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange


                Book Book1 = Book.builder()
                                .title("IT")
                                .author("Stephen King")
                                .genre("Horror")
                                .build();

                when(BookRepository.findById(eq(15L))).thenReturn(Optional.of(Book1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/Book?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(BookRepository, times(1)).findById(15L);
                verify(BookRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Book with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_Book_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(BookRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/Book?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(BookRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Book with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_Book() throws Exception {
                // arrange


                Book BookOrig = Book.builder()
                                .title("IT")
                                .author("Stephen King")
                                .genre("Horror")
                                .build();

                Book BookEdited = Book.builder()
                                .title("IT2")
                                .author("Stephen King2")
                                .genre("Horror2")
                                .build();

                String requestBody = mapper.writeValueAsString(BookEdited);

                when(BookRepository.findById(eq(67L))).thenReturn(Optional.of(BookOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/Book?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(BookRepository, times(1)).findById(67L);
                verify(BookRepository, times(1)).save(BookEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_Book_that_does_not_exist() throws Exception {
                // arrange


                Book ucsbEditedDate = Book.builder()
                                .title("IT")
                                .author("Stephen King")
                                .genre("Horror")
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbEditedDate);

                when(BookRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/Book?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(BookRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Book with id 67 not found", json.get("message"));

        }
}
