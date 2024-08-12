package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.model.BookingState;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class BookingDtoForItemTest {

    @Test
    void serializeDeserializeBookingDtoForItemOk() throws JsonProcessingException {
        BookingDtoForItem bookingDtoForItem = new BookingDtoForItem(2,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 41),
                1, BookingState.APPROVED);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String result = objectMapper.writeValueAsString(bookingDtoForItem);

        String expected = "{\"id\":2,\"start\":[2024,5,25,12,40],\"end\":[2024,5,25,12,41],\"bookerId\":1,\"status\":\"APPROVED\"}";

        assertEquals(expected, result);

        BookingDtoForItem parsedBookingDtoForItem = objectMapper.readValue(expected, BookingDtoForItem.class);

        assertEquals(bookingDtoForItem, parsedBookingDtoForItem);
    }
}
