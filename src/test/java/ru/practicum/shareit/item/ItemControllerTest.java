package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
public class ItemControllerTest {
    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createItemOk() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        User user = new User(2, "user@ya.ru", "Irina");
        User user2 = new User(6, "user2@ya.ru", "Oleg");
        Item item = new Item("book", "book for read", Boolean.TRUE);
        item.setId(3);
        item.setOwner(user);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(5);
        itemRequest.setRequestor(user2);
        itemRequest.setDescription("need book");
        itemRequest.setCreated(LocalDateTime.of(2024, Month.MAY, 25, 12, 41));
        item.setRequest(itemRequest);

        ItemDto itemDto = new ItemDto(3, "book", "book for read", Boolean.TRUE, 5);

        when(itemService.createItem(2, itemDto)).thenReturn(item);
        mockMvc.perform(post("/items")
                .header("X-Sharer-User-Id", 2)
                .content(mapper.writeValueAsString(itemDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("book")))
                .andExpect(jsonPath("$.description", is("book for read")))
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.requestId", is(5)));

        verify(itemService, times(1)).createItem(2, itemDto);
    }

    @Test
    void updateItemOk() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        User user = new User(2, "user@ya.ru", "Irina");
        User user2 = new User(6, "user2@ya.ru", "Oleg");
        Item item = new Item("book", "book for read", Boolean.TRUE);
        item.setId(3);
        item.setOwner(user);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(5);
        itemRequest.setRequestor(user2);
        itemRequest.setDescription("need book");
        itemRequest.setCreated(LocalDateTime.of(2024, Month.MAY, 25, 12, 41));
        item.setRequest(itemRequest);

        ItemDto itemDto = new ItemDto(3, "book", "book for read", Boolean.TRUE, 5);

        when(itemService.updateItem(2, 3, itemDto)).thenReturn(item);
        mockMvc.perform(patch("/items/3")
                .header("X-Sharer-User-Id", 2)
                .content(mapper.writeValueAsString(itemDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("book")))
                .andExpect(jsonPath("$.description", is("book for read")))
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.requestId", is(5)));

        verify(itemService, times(1)).updateItem(2, 3, itemDto);
    }

    @Test
    void findItemWithBookingByIdOk() throws Exception {
        User user = new User(2, "user@ya.ru", "Irina");
        User user2 = new User(6, "user2@ya.ru", "Oleg");
        Item item = new Item("book", "book for read", Boolean.TRUE);
        item.setId(3);
        item.setOwner(user);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(5);
        itemRequest.setRequestor(user2);
        itemRequest.setDescription("need book");
        itemRequest.setCreated(LocalDateTime.of(2023, Month.MAY, 25, 12, 41));
        item.setRequest(itemRequest);

        BookingDtoForItem lastBookingDtoForItem = new BookingDtoForItem(7,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 41),
                6, BookingState.APPROVED);
        BookingDtoForItem nextBookingDtoForItem = new BookingDtoForItem(8,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                6, BookingState.APPROVED);

        ItemWithBookingDto itemDto = new ItemWithBookingDto();
        itemDto.setId(3);
        itemDto.setName("book");
        itemDto.setDescription("book for read");
        itemDto.setAvailable(true);
        itemDto.setLastBooking(lastBookingDtoForItem);
        itemDto.setNextBooking(nextBookingDtoForItem);


        CommentDto commentDto = new CommentDto();
        commentDto.setId(9);
        commentDto.setText("comment");
        commentDto.setAuthorName("Unknown");
        commentDto.setCreated(LocalDateTime.of(2024, Month.MAY, 25, 12, 42));

        List<CommentDto> commentDtoList = new ArrayList<>();
        commentDtoList.add(commentDto);
        itemDto.setComments(commentDtoList);

        when(itemService.findItemWithBookingById(2, 3)).thenReturn(itemDto);
        mockMvc.perform(get("/items/3")
                .header("X-Sharer-User-Id", 2)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("book")))
                .andExpect(jsonPath("$.description", is("book for read")))
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.lastBooking.id", is(7)))
                .andExpect(jsonPath("$.lastBooking.start", is("2024-05-25T12:40:00")))
                .andExpect(jsonPath("$.lastBooking.end", is("2024-05-25T12:41:00")))
                .andExpect(jsonPath("$.lastBooking.bookerId", is(6)))
                .andExpect(jsonPath("$.lastBooking.status", is("APPROVED")))
                .andExpect(jsonPath("$.nextBooking.id", is(8)))
                .andExpect(jsonPath("$.nextBooking.start", is("2025-05-25T12:40:00")))
                .andExpect(jsonPath("$.nextBooking.end", is("2025-05-25T12:41:00")))
                .andExpect(jsonPath("$.nextBooking.bookerId", is(6)))
                .andExpect(jsonPath("$.nextBooking.status", is("APPROVED")))
                .andExpect(jsonPath("$.comments[0].id", is(9)))
                .andExpect(jsonPath("$.comments[0].text", is("comment")))
                .andExpect(jsonPath("$.comments[0].authorName", is("Unknown")))
                .andExpect(jsonPath("$.comments[0].created", is("2024-05-25T12:42:00")))
                .andExpect(jsonPath("$.comments.length()", is(1)));
        verify(itemService, times(1)).findItemWithBookingById(2, 3);
    }

    @Test
    void findAllItemsOfUserWithoutPageOk() throws Exception {
        when(itemService.findAllItemsWithBooking(2, 0, 1000000)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/items")
                .header("X-Sharer-User-Id", 2)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemService, times(1)).findAllItemsWithBooking(2, 0, 1000000);
    }

    @Test
    void findItemsBySearchWithoutPageOk() throws Exception {
        when(itemService.searchItem("book", 0, 1000000)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/items/search")
                .header("X-Sharer-User-Id", 2)
                .param("text", "book")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemService, times(1)).searchItem("book", 0, 1000000);
    }

    @Test
    void createCommentOk() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        User user = new User(2, "user@ya.ru", "Irina");
        User user2 = new User(6, "user2@ya.ru", "Oleg");
        Item item = new Item("book", "book for read", Boolean.TRUE);
        item.setId(3);
        item.setOwner(user);

        Comment comment = new Comment(9, "comment", user2, item, LocalDateTime.of(2024, Month.MAY, 25, 12, 42));


        CommentDto commentDto = new CommentDto();
        commentDto.setText("comment");

        when(itemService.createComment(6, 3, commentDto)).thenReturn(comment);
        mockMvc.perform(post("/items/3/comment")
                .header("X-Sharer-User-Id", 6)
                .content(mapper.writeValueAsString(commentDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(9)))
                .andExpect(jsonPath("$.authorName", is("Oleg")))
                .andExpect(jsonPath("$.text", is("comment")))
                .andExpect(jsonPath("$.created", is("2024-05-25T12:42:00")));
        verify(itemService, times(1)).createComment(6, 3, commentDto);
    }

}
