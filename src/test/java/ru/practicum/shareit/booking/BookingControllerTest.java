package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
public class BookingControllerTest {

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllBookingsOfUserWithDefaultPageParamsOk() throws Exception {
        when(bookingService.findAllBookingsOfUser(1, "ALL", 0, 1000000)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/bookings")
                .header("X-Sharer-User-Id", 1)
                .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(bookingService, times(1)).findAllBookingsOfUser(1, "ALL", 0, 1000000);
    }

    @Test
    void getAllBookingsOfOwnerItemsWithDefaultPageParamsOk() throws Exception {
        when(bookingService.findAllBookingsOfOwnerItems(1, "ALL", 0, 1000000)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/bookings/owner")
                .header("X-Sharer-User-Id", 1)
                .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(bookingService, times(1)).findAllBookingsOfOwnerItems(1, "ALL", 0, 1000000);
    }

    @Test
    void getBookingOk() throws Exception {
        User user = new User(2, "user@ya.ru", "Irina");
        Item item = new Item("book", "book for read", Boolean.TRUE);
        item.setId(3);
        item.setOwner(user);
        Booking booking = new Booking(1,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 41),
                item, user, BookingState.APPROVED);

        when(bookingService.findBookingById(2, 1)).thenReturn(booking);
        mockMvc.perform(get("/bookings/1")
                .header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.start", is("2024-05-25T12:40:00")))
                .andExpect(jsonPath("$.end", is("2024-05-25T12:41:00")))
                .andExpect(jsonPath("$.item.id", is(3)))
                .andExpect(jsonPath("$.item.name", is("book")))
                .andExpect(jsonPath("$.item.description", is("book for read")))
                .andExpect(jsonPath("$.item.available", is(true)))
                .andExpect(jsonPath("$.booker.id", is(2)))
                .andExpect(jsonPath("$.booker.name", is("Irina")))
                .andExpect(jsonPath("$.booker.email", is("user@ya.ru")))
                .andExpect(jsonPath("$.status", is("APPROVED")));
        verify(bookingService, times(1)).findBookingById(2, 1);
    }

    @Test
    void updateBookingApprovedOk() throws Exception {
        User user = new User(2, "user@ya.ru", "Irina");
        Item item = new Item("book", "book for read", Boolean.TRUE);
        item.setId(3);
        item.setOwner(user);
        Booking booking = new Booking(1,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 41),
                item, user, BookingState.APPROVED);

        when(bookingService.updateBooking(2, 1, Boolean.TRUE)).thenReturn(booking);
        mockMvc.perform(patch("/bookings/1")
                .header("X-Sharer-User-Id", 2)
                .param("approved", String.valueOf(Boolean.TRUE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.start", is("2024-05-25T12:40:00")))
                .andExpect(jsonPath("$.end", is("2024-05-25T12:41:00")))
                .andExpect(jsonPath("$.item.id", is(3)))
                .andExpect(jsonPath("$.item.name", is("book")))
                .andExpect(jsonPath("$.item.description", is("book for read")))
                .andExpect(jsonPath("$.item.available", is(true)))
                .andExpect(jsonPath("$.booker.id", is(2)))
                .andExpect(jsonPath("$.booker.name", is("Irina")))
                .andExpect(jsonPath("$.booker.email", is("user@ya.ru")))
                .andExpect(jsonPath("$.status", is("APPROVED")));
        verify(bookingService, times(1)).updateBooking(2, 1, Boolean.TRUE);
    }

    @Test
    void createBookingOk() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        User user = new User(2, "user@ya.ru", "Irina");
        Item item = new Item("book", "book for read", Boolean.TRUE);
        item.setId(3);
        item.setOwner(user);
        Booking booking = new Booking(1,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 41),
                item, user, BookingState.APPROVED);

        BookingDto bookingDto = new BookingDto(LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 41),
                3);

        when(bookingService.createBooking(2, bookingDto)).thenReturn(booking);
        mockMvc.perform(post("/bookings")
                .header("X-Sharer-User-Id", 2)
                .content(mapper.writeValueAsString(bookingDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.start", is("2024-05-25T12:40:00")))
                .andExpect(jsonPath("$.end", is("2024-05-25T12:41:00")))
                .andExpect(jsonPath("$.item.id", is(3)))
                .andExpect(jsonPath("$.item.name", is("book")))
                .andExpect(jsonPath("$.item.description", is("book for read")))
                .andExpect(jsonPath("$.item.available", is(true)))
                .andExpect(jsonPath("$.booker.id", is(2)))
                .andExpect(jsonPath("$.booker.name", is("Irina")))
                .andExpect(jsonPath("$.booker.email", is("user@ya.ru")))
                .andExpect(jsonPath("$.status", is("APPROVED")));
        verify(bookingService, times(1)).createBooking(2, bookingDto);
    }
}
