package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class BookingDtoTest {

    @Test
    void serializeDeserializeBookingOk() throws JsonProcessingException {
        BookingDto bookingDto = new BookingDto(
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 41),
                1);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String result = objectMapper.writeValueAsString(bookingDto);

        String expected = "{\"start\":[2024,5,25,12,40],\"end\":[2024,5,25,12,41],\"itemId\":1}";

        assertEquals(expected, result);

        BookingDto parsedBookingDto = objectMapper.readValue(expected, BookingDto.class);

        assertEquals(bookingDto, parsedBookingDto);
    }
}
