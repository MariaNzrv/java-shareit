package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemRequestMapperTest {

    private User user;
    private Item item;
    private ItemDto itemDto;
    private ItemRequest request;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void beforeEach() {
        user = new User(1, "user@owner.ru", "owner");
        LocalDateTime date = LocalDateTime.of(2024, Month.MAY, 29, 12, 0);
        request = new ItemRequest(1, "description", user, date);
        itemRequestDto = new ItemRequestDto(1, "description", user.getId(), date);
        item = new Item(1, "name", "description", true, user, request);
        itemDto = new ItemDto(1, "name", "description", true, 1);
    }

    @Test
    void testToDto() {
        ItemRequestDto actualDto = ItemRequestMapper.toDto(request);
        assertEquals(itemRequestDto, actualDto);
    }

    @Test
    void testToDtoList() {
        List<ItemRequestDto> listDto = ItemRequestMapper.toDto(List.of(request, request));
        assertEquals(2, listDto.size());
        assertEquals(itemRequestDto, listDto.get(0));
        assertEquals(itemRequestDto, listDto.get(1));
    }

    @Test
    void testToItemRequest() {
        ItemRequest actual = ItemRequestMapper.toItemRequest(itemRequestDto);
        assertEquals(request.getDescription(), actual.getDescription());
    }

    @Test
    void testToItemRequestWithResponseDto() {
        ItemRequestWithResponseDto actual = ItemRequestMapper.toItemRequestWithResponseDto(request, Arrays.asList(item, item));

        assertEquals(request.getId(), actual.getId());
        assertEquals(request.getDescription(), actual.getDescription());
        assertEquals(request.getRequestor().getId(), actual.getRequestor());
        assertEquals(request.getCreated(), actual.getCreated());
        assertEquals(2, actual.getItems().size());
        assertEquals(itemDto, actual.getItems().get(0));
        assertEquals(itemDto, actual.getItems().get(1));
    }

}
