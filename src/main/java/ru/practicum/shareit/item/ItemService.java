package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    @Autowired
    private final ItemStorage itemStorage;
    @Autowired
    private final UserStorage userStorage;

    public Item createItem(Integer userId, ItemDto itemDto) {
        if (!userStorage.isUserExist(userId)) {
            log.warn("Пользователя с Id = {} не существует", userId);
            throw new RuntimeException("Пользователя с Id = " + userId + " не существует");
        }
        if (itemDto.getName() == null || itemDto.getDescription() == null || itemDto.getAvailable() == null) {
            log.warn("Не заполнены обязательные поля");
            throw new ValidationException("Не заполнены обязательные поля");
        }
        validateFieldsFormat(itemDto);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userStorage.findById(userId));
        return itemStorage.create(item);
    }

    public Item updateItem(Integer userId, Integer itemId, ItemDto itemDto) {
        if (itemId == null) {
            log.error("Id вещи не заполнен");
            throw new ValidationException("Для обновления данных надо указать Id вещи");
        }
        Item item = findById(itemId);
        if (item == null) {
            log.error("Вещи с Id = {} не существует", itemId);
            throw new RuntimeException("Вещи с таким Id не существует");
        }
        if (!item.getOwner().getId().equals(userId)) {
            log.error("Невозможно обновить информацию по вещи, принадлежащей другому пользователю");
            throw new RuntimeException("Невозможно обновить информацию по вещи, принадлежащей другому пользователю");
        }
        validateFieldsFormat(itemDto);
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return itemStorage.update(item);
    }

    public Item findById(Integer itemId) {
        if (itemId == null) {
            log.error("Id вещи не заполнен");
            throw new ValidationException("Для получения данных надо указать Id вещи");
        }
        return itemStorage.findById(itemId);
    }

    public List<Item> findAllItemsOfUser(Integer userId) {
        if (!userStorage.isUserExist(userId)) {
            log.warn("Пользователя с Id = {} не существует", userId);
            throw new RuntimeException("Пользователя с Id = " + userId + " не существует");
        }
        return itemStorage.findAllItemsOfUser(userId);
    }

    public List<Item> searchItem(String text) {
        if (text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        }
        String lowerText = text.toLowerCase(Locale.ROOT);
        return itemStorage.search(lowerText);
    }

    private void validateFieldsFormat(ItemDto itemDto) {
        if (itemDto.getName() != null && (itemDto.getName().isBlank() || itemDto.getName().isEmpty())) {
            log.warn("Название обязательно для заполнения");
            throw new ValidationException("Название обязательно для заполнения");
        }
        if (itemDto.getDescription() != null && (itemDto.getDescription().isEmpty() || itemDto.getDescription().isBlank())) {
            log.warn("Описание обязательно для заполнения");
            throw new ValidationException("Описание обязательно для заполнения");
        }
    }
}
