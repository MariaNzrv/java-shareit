package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Integer userId,
                          @RequestBody ItemDto itemDto) {
        Item savedItem = itemService.createItem(userId, itemDto);
        return ItemMapper.toDto(savedItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Integer userId,
                          @PathVariable Integer itemId,
                          @RequestBody ItemDto itemDto) {
        Item savedItem = itemService.updateItem(userId, itemId, itemDto);
        return ItemMapper.toDto(savedItem);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingDto findById(@RequestHeader("X-Sharer-User-Id") Integer userId, @PathVariable Integer itemId) {
        return itemService.findItemWithBookingById(userId, itemId);
    }

    @GetMapping
    public List<ItemWithBookingDto> findAllItemsOfUser(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                                       @RequestParam(defaultValue = "0", name = "from") Integer from,
                                                       @RequestParam(defaultValue = "1000000", name = "size") Integer size) {
        return itemService.findAllItemsWithBooking(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam("text") String text,
                                @RequestParam(defaultValue = "0", name = "from") Integer from,
                                @RequestParam(defaultValue = "1000000", name = "size") Integer size) {
        return ItemMapper.toDto(itemService.searchItem(text, from, size));
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                    @PathVariable Integer itemId,
                                    @RequestBody CommentDto commentDto) {
        Comment savedComment = itemService.createComment(userId, itemId, commentDto);
        return CommentMapper.toDto(savedComment);
    }

}
