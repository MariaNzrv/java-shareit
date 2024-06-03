package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class ComentDtoTest {
    @Test
    void serializeDeserializeCommentOk() throws JsonProcessingException {
        CommentDto commentDto = new CommentDto(1, "comment 1", "Oleg",
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40));

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String result = objectMapper.writeValueAsString(commentDto);

        String expected = "{\"id\":1,\"text\":\"comment 1\",\"authorName\":\"Oleg\",\"created\":[2024,5,25,12,40]}";

        assertEquals(expected, result);

        CommentDto parsedCommentDto = objectMapper.readValue(expected, CommentDto.class);

        assertEquals(commentDto, parsedCommentDto);
    }
}
