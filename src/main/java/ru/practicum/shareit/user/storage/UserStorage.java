package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    User create(User user);

    User update(User user);

    List<User> findAll();

    User findById(Integer userId);

    void delete(Integer userId);
}
