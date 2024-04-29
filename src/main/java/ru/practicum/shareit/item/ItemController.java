package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
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
    public ItemDto findById(@PathVariable Integer itemId) {
        Item item = itemService.findById(itemId);
        return ItemMapper.toDto(item);
    }

    @GetMapping
    public List<ItemDto> findAllItemsOfUser(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        return ItemMapper.toDto(itemService.findAllItemsOfUser(userId));
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam("text") String text) {
        return ItemMapper.toDto(itemService.searchItem(text));
    }

}
