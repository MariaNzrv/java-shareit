package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class ItemRequestDtoTest {
    @Test
    void serializeDeserializeItemRequestDtoOk() throws JsonProcessingException {
        ItemRequestDto itemRequestDto = new ItemRequestDto(1, "need pencil", 4,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40));
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String result = objectMapper.writeValueAsString(itemRequestDto);

        String expected = "{\"id\":1,\"description\":\"need pencil\",\"requestor\":4,\"created\":[2024,5,25,12,40]}";

        assertEquals(expected, result);

        ItemRequestDto parsedItemRequestDto = objectMapper.readValue(expected, ItemRequestDto.class);

        assertEquals(itemRequestDto, parsedItemRequestDto);
    }
}
