package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class ItemDtoTest {
    @Test
    void serializeDeserializeItemOk() throws JsonProcessingException {
        ItemDto itemDto = new ItemDto(1, "book", "book for read", true, 2);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String result = objectMapper.writeValueAsString(itemDto);

        String expected = "{\"id\":1,\"name\":\"book\",\"description\":\"book for read\",\"available\":true,\"requestId\":2}";

        assertEquals(expected, result);

        ItemDto parsedItemDto = objectMapper.readValue(expected, ItemDto.class);

        assertEquals(itemDto, parsedItemDto);
    }
}
