package ru.practicum.shareit.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class UserDtoTest {
    @Test
    void serializeDeserializeUserDtoOk() throws JsonProcessingException {
        UserDto userDto = new UserDto(1, "user@ya.ru", "Irina");
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String result = objectMapper.writeValueAsString(userDto);

        String expected = "{\"id\":1,\"email\":\"user@ya.ru\",\"name\":\"Irina\"}";

        assertEquals(expected, result);

        UserDto parsedUserDto = objectMapper.readValue(expected, UserDto.class);

        assertEquals(userDto, parsedUserDto);
    }
}
