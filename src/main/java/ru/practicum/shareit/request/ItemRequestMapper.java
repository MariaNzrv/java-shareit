package ru.practicum.shareit.request;

import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.ArrayList;
import java.util.List;

public class ItemRequestMapper {
    public static ItemRequestDto toDto(ItemRequest itemRequest) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getRequestor().getId(),
                itemRequest.getCreated()
        );
    }

    public static List<ItemRequestDto> toDto(List<ItemRequest> itemRequests) {
        List<ItemRequestDto> itemRequestDtos = new ArrayList<>();
        for (ItemRequest itemRequest : itemRequests) {
            itemRequestDtos.add(toDto(itemRequest));
        }
        return itemRequestDtos;
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
        return new ItemRequest(
                itemRequestDto.getDescription());
    }

    public static ItemRequestWithResponseDto toItemRequestWithResponseDto(ItemRequest itemRequest,
                                                                          List<Item> items) {
        ItemRequestWithResponseDto itemRequestWithResponseDto = new ItemRequestWithResponseDto();
        itemRequestWithResponseDto.setId(itemRequest.getId());
        itemRequestWithResponseDto.setDescription(itemRequest.getDescription());
        itemRequestWithResponseDto.setCreated(itemRequest.getCreated());
        itemRequestWithResponseDto.setRequestor(itemRequest.getRequestor().getId());
        if (items != null) {
            itemRequestWithResponseDto.setItems(ItemMapper.toDto(items));
        }
        return itemRequestWithResponseDto;
    }
}
