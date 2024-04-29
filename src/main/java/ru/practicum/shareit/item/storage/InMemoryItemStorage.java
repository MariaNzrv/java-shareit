package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class InMemoryItemStorage implements ItemStorage {
    private final HashMap<Integer, Item> items = new HashMap<>();
    private Integer idCounter = 1;

    @Override
    public Item create(Item item) {
        Integer id = getUniqueId();

        item.setId(id);
        items.put(id, item);
        log.info("Добавлена вещь с Id: '{}'", id);
        return items.get(id);
    }

    @Override
    public Item findById(Integer itemId) {
        return items.get(itemId);
    }

    @Override
    public Item update(Item item) {
        Integer id = item.getId();

        items.put(id, item);
        log.info("Обновлена вещь с Id: '{}'", id);
        return items.get(id);
    }

    @Override
    public List<Item> findAllItemsOfUser(Integer userId) {
        List<Item> userItems = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getOwner().getId().equals(userId)) {
                userItems.add(item);
            }
        }

        return userItems;
    }

    @Override
    public List<Item> search(String lowerText) {
        List<Item> result = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getAvailable() == Boolean.TRUE &&
                    (item.getName().toLowerCase(Locale.ROOT).contains(lowerText) ||
                            item.getDescription().toLowerCase(Locale.ROOT).contains(lowerText))) {
                result.add(item);
            }
        }

        return result;
    }

    private Integer getUniqueId() {
        // вычисление уникального Id
        Integer result = idCounter;
        idCounter++;
        return result;
    }
}
