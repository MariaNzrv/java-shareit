package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.model.BookingState;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class ItemWithBookingDtoTest {
    @Test
    void serializeDeserializeItemWithBookingOk() throws JsonProcessingException {
        ItemWithBookingDto itemWithBookingDto = new ItemWithBookingDto();
        itemWithBookingDto.setId(1);
        itemWithBookingDto.setName("book");
        itemWithBookingDto.setDescription("book for read");
        itemWithBookingDto.setAvailable(true);
        BookingDtoForItem lastBookingDtoForItem = new BookingDtoForItem(2,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 41),
                4, BookingState.APPROVED);
        BookingDtoForItem nextBookingDtoForItem = new BookingDtoForItem(3,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                4, BookingState.APPROVED);
        List<CommentDto> commentList = new ArrayList<>();
        itemWithBookingDto.setLastBooking(lastBookingDtoForItem);
        itemWithBookingDto.setNextBooking(nextBookingDtoForItem);
        itemWithBookingDto.setComments(commentList);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String result = objectMapper.writeValueAsString(itemWithBookingDto);

        String expected = "{\"id\":1,\"name\":\"book\",\"description\":\"book for read\",\"available\":true,\"lastBooking\":{\"id\":2,\"start\":[2024,5,25,12,40],\"end\":[2024,5,25,12,41],\"bookerId\":4,\"status\":\"APPROVED\"},\"nextBooking\":{\"id\":3,\"start\":[2025,5,25,12,40],\"end\":[2025,5,25,12,41],\"bookerId\":4,\"status\":\"APPROVED\"},\"comments\":[]}";

        assertEquals(expected, result);

        ItemWithBookingDto parsedItemWithBookingDto = objectMapper.readValue(expected, ItemWithBookingDto.class);

        assertEquals(itemWithBookingDto, parsedItemWithBookingDto);
    }
}
