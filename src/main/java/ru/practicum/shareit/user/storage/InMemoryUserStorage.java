package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Integer, User> users = new HashMap<>();
    private Integer idCounter = 1;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        Integer id = getUniqueId();

        user.setId(id);
        users.put(id, user);
        log.info("Создан пользователь с Id: '{}'", id);
        return users.get(id);
    }

    @Override
    public User update(User user) {
        Integer id = user.getId();

        users.put(id, user);
        log.info("Обновлен пользователь с Id: '{}'", id);
        return users.get(id);
    }

    @Override
    public User findById(Integer userId) {
        return users.get(userId);
    }

    @Override
    public void delete(Integer userId) {
        users.remove(userId);
        log.info("Удален пользователь с Id: '{}'", userId);
    }

    private Integer getUniqueId() {
        // вычисление уникального Id
        Integer result = idCounter;
        idCounter++;
        return result;
    }
}
