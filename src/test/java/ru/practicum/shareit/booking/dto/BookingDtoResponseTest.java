package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class BookingDtoResponseTest {

    @Test
    void serializeDeserializeBookingDtoResponseTestOk() throws JsonProcessingException {
        ItemDto itemDto = new ItemDto(1, "book", "book for read", Boolean.TRUE, 3);
        UserDto userDto = new UserDto(4, "user@ya.ru", "Irina");
        BookingDtoResponse bookingDtoResponse = new BookingDtoResponse(2,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 41),
                itemDto, userDto,
                BookingState.APPROVED);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String result = objectMapper.writeValueAsString(bookingDtoResponse);

        String expected = "{\"id\":2,\"start\":[2024,5,25,12,40],\"end\":[2024,5,25,12,41],\"item\":{\"id\":1,\"name\":\"book\",\"description\":\"book for read\",\"available\":true,\"requestId\":3},\"booker\":{\"id\":4,\"email\":\"user@ya.ru\",\"name\":\"Irina\"},\"status\":\"APPROVED\"}";

        assertEquals(expected, result);

        BookingDtoResponse parsedBookingDtoResponse = objectMapper.readValue(expected, BookingDtoResponse.class);

        assertEquals(bookingDtoResponse, parsedBookingDtoResponse);
    }
}
