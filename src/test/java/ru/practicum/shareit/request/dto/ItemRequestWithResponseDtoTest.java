package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class ItemRequestWithResponseDtoTest {
    @Test
    void serializeDeserializeItemRequestWithResponseDtoOk() throws JsonProcessingException {
        ItemRequestWithResponseDto itemRequestWithResponseDto = new ItemRequestWithResponseDto();
        List<ItemDto> items = new ArrayList<>();
        itemRequestWithResponseDto.setId(1);
        itemRequestWithResponseDto.setDescription("need pencil");
        itemRequestWithResponseDto.setRequestor(4);
        itemRequestWithResponseDto.setItems(items);
        itemRequestWithResponseDto.setCreated(LocalDateTime.of(2024, Month.MAY, 25, 12, 40));

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String result = objectMapper.writeValueAsString(itemRequestWithResponseDto);

        String expected = "{\"id\":1,\"description\":\"need pencil\",\"requestor\":4,\"created\":[2024,5,25,12,40],\"items\":[]}";

        assertEquals(expected, result);

        ItemRequestWithResponseDto parsedItemRequestWithResponseDto = objectMapper.readValue(expected, ItemRequestWithResponseDto.class);

        assertEquals(itemRequestWithResponseDto, parsedItemRequestWithResponseDto);
    }

}
