package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
@AutoConfigureMockMvc
public class ItemRequestControllerTest {
    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreateItemRequestOk() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        User user = new User(2, "user@ya.ru", "Irina");
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(5);
        itemRequest.setRequestor(user);
        itemRequest.setDescription("need book");
        itemRequest.setCreated(LocalDateTime.of(2024, Month.MAY, 25, 12, 41));

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("need book");

        when(itemRequestService.createItemRequest(2, itemRequestDto)).thenReturn(itemRequest);
        mockMvc.perform(post("/requests")
                .header("X-Sharer-User-Id", 2)
                .content(mapper.writeValueAsString(itemRequestDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.description", is("need book")))
                .andExpect(jsonPath("$.requestor", is(2)))
                .andExpect(jsonPath("$.created", is("2024-05-25T12:41:00")));
        verify(itemRequestService, times(1)).createItemRequest(2, itemRequestDto);
    }

    @Test
    void testFindAllItemRequestOfUserOk() throws Exception {
        when(itemRequestService.findAllItemRequestsOfUser(anyInt())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/requests")
                .header("X-Sharer-User-Id", 2)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemRequestService, times(1)).findAllItemRequestsOfUser(anyInt());
    }

    @Test
    void testFindAllItemRequestsOfOtherUsersWithoutPageOk() throws Exception {
        when(itemRequestService.findAllItemRequestsOfOtherUsers(2, null, null)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/requests/all")
                .header("X-Sharer-User-Id", 2)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemRequestService, times(1)).findAllItemRequestsOfOtherUsers(2, null, null);
    }

    @Test
    void testFindByIdOk() throws Exception {
        User user = new User(2, "user@ya.ru", "Irina");
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(5);
        itemRequest.setRequestor(user);
        itemRequest.setDescription("need book");
        itemRequest.setCreated(LocalDateTime.of(2024, Month.MAY, 25, 12, 41));

        ItemRequestWithResponseDto itemRequestDto = new ItemRequestWithResponseDto();
        itemRequestDto.setItems(new ArrayList<>());
        itemRequestDto.setId(5);
        itemRequestDto.setRequestor(2);
        itemRequestDto.setDescription("need book");
        itemRequestDto.setCreated(LocalDateTime.of(2024, Month.MAY, 25, 12, 41));

        when(itemRequestService.findItemRequestWithResponseById(2, 5)).thenReturn(itemRequestDto);
        mockMvc.perform(get("/requests/5")
                .header("X-Sharer-User-Id", 2)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.description", is("need book")))
                .andExpect(jsonPath("$.requestor", is(2)))
                .andExpect(jsonPath("$.created", is("2024-05-25T12:41:00")))
                .andExpect(jsonPath("$.items.length()", is(0)));
        verify(itemRequestService, times(1)).findItemRequestWithResponseById(2, 5);
    }

}
