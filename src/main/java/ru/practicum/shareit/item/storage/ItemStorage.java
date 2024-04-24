package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item create(Item item);
    Item findById(Integer itemId);
    Item update(Item item);
    List<Item> findAllItemsOfUser(Integer userId);

    List<Item> search(String lowerText);
}
