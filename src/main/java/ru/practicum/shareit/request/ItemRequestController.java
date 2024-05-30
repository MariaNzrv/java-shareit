package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                 @RequestBody ItemRequestDto itemRequestDto) {
        ItemRequest savedItemRequest = itemRequestService.createItemRequest(userId, itemRequestDto);
        return ItemRequestMapper.toDto(savedItemRequest);
    }

    @GetMapping
    public List<ItemRequestWithResponseDto> findAllItemRequestsOfUser(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        return itemRequestService.findAllItemRequestsOfUser(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithResponseDto findById(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                               @PathVariable Integer requestId) {
        return itemRequestService.findItemRequestWithResponseById(userId, requestId);
    }

    @GetMapping("/all")
    public List<ItemRequestWithResponseDto> findAllItemRequestsOfOtherUsers(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                                                            @RequestParam(required = false, name = "from") Integer from,
                                                                            @RequestParam(required = false, name = "size") Integer size) {
        return itemRequestService.findAllItemRequestsOfOtherUsers(userId, from, size);
    }
}
