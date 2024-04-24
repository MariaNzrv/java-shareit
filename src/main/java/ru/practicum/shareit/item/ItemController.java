package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    @Autowired
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Integer userId,
                          @RequestBody ItemDto itemDto) {
        Item savedItem = itemService.createItem(userId, itemDto);
        return ItemMapper.toItemDto(savedItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Integer userId,
                          @PathVariable Integer itemId,
                          @RequestBody ItemDto itemDto) {
        Item savedItem = itemService.updateItem(userId, itemId, itemDto);
        return ItemMapper.toItemDto(savedItem);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable Integer itemId) {
        Item item = itemService.findById(itemId);
        return ItemMapper.toItemDto(item);
    }

    @GetMapping
    public List<ItemDto> findAllItemsOfUser(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        List<Item> items = itemService.findAllItemsOfUser(userId);
        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            itemDtos.add(ItemMapper.toItemDto(item));
        }
        return itemDtos;
    }

    @GetMapping("/search")
    public List<ItemDto> searh(@RequestParam("text") String text) {
        List<Item> items = itemService.searchItem(text);
        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            itemDtos.add(ItemMapper.toItemDto(item));
        }
        return itemDtos;
    }

}
