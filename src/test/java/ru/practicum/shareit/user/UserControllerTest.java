package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
public class UserControllerTest {
    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testFindAllOk() throws Exception {
        when(userService.findAllUsers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/users")
                .header("X-Sharer-User-Id", 1)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(userService, times(1)).findAllUsers();
    }

    @Test
    void testFindByIdOk() throws Exception {
        User user = new User(2, "user@ya.ru", "Irina");

        when(userService.findUserById(2)).thenReturn(user);
        mockMvc.perform(get("/users/2")
                .header("X-Sharer-User-Id", 2)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.email", is("user@ya.ru")))
                .andExpect(jsonPath("$.name", is("Irina")));
        verify(userService, times(1)).findUserById(2);
    }

    @Test
    void testCreateUserOk() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        User user = new User(2, "user@ya.ru", "Irina");

        UserDto userDto = new UserDto();
        userDto.setEmail("user@ya.ru");
        userDto.setName("Irina");

        when(userService.createUser(userDto)).thenReturn(user);
        mockMvc.perform(post("/users")
                .content(mapper.writeValueAsString(userDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.email", is("user@ya.ru")))
                .andExpect(jsonPath("$.name", is("Irina")));
        verify(userService, times(1)).createUser(userDto);
    }

    @Test
    void testUpdateUserOk() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        User user = new User(2, "user@ya.ru", "Irina");

        UserDto userDto = new UserDto();
        userDto.setEmail("user@ya.ru");
        userDto.setName("Irina");

        when(userService.updateUser(2, userDto)).thenReturn(user);
        mockMvc.perform(patch("/users/2")
                .content(mapper.writeValueAsString(userDto))
                .header("X-Sharer-User-Id", 2)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.email", is("user@ya.ru")))
                .andExpect(jsonPath("$.name", is("Irina")));
        verify(userService, times(1)).updateUser(2, userDto);
    }

    @Test
    void testDeleteUserOk() throws Exception {
        mockMvc.perform(delete("/users/2")
                .header("X-Sharer-User-Id", 2)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(userService, times(1)).deleteUser(2);
    }

    @Test
    void testDeleteUnexistedUserThrowsException() throws Exception {
        Mockito.doThrow(EntityNotFoundException.class).when(userService).deleteUser(2);
        mockMvc.perform(delete("/users/2")
                .header("X-Sharer-User-Id", 2)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(userService, times(1)).deleteUser(2);
    }

}
