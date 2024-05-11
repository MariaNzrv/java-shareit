package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictValidationException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userStorage;

    public List<User> findAllUsers() {
        return userStorage.findAll();
    }

    public User findUserById(Integer userId) {
        Optional<User> user = userStorage.findById(userId);
        if (user.isEmpty()) {
            log.error("Пользователя с Id = {} не существует", userId);
            throw new RuntimeException("Пользователя с Id = " + userId + " не существует");
        }
        return user.get();
    }

    public User createUser(UserDto userDto) {
        String email = userDto.getEmail();
        if (email == null) {
            log.warn("Электронная почта обязательна для заполнения");
            throw new ValidationException("Электронная почта обязательна для заполнения");
        }
        validateUserFieldsFormat(email, userDto.getName());

        User user = UserMapper.toUser(userDto);

        return userStorage.save(user);
    }

    public User updateUser(Integer userId, UserDto newUser) {
        validateUserId(userId);
        validateUserFieldsFormat(newUser.getEmail(), newUser.getName());
        for (User user : userStorage.findAll()) {
            if (user.getEmail().equals(newUser.getEmail()) && !user.getId().equals(userId)) {
                log.warn("Пользователь с такой Электронной почтой уже есть в системе");
                throw new ConflictValidationException("Пользователь с такой Электронной почтой уже есть в системе");
            }
        }

        User user = findUserById(userId);

        if (newUser.getEmail() != null) {
            user.setEmail(newUser.getEmail());
        }
        if (newUser.getName() != null) {
            user.setName(newUser.getName());
        }
        return userStorage.save(user);
    }

    public void deleteUser(Integer userId) {
        validateUserId(userId);
        User user = findUserById(userId);
        userStorage.delete(user);
    }

    private void validateUserFieldsFormat(String email2, String name) {
        if (email2 != null && (email2.isBlank() || email2.indexOf('@') == -1)) {
            log.warn("Электронная почта не может быть пустой и должна содержать символ @");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (name != null && name.isBlank()) {
            log.warn("Логин не может содержать пробелы");
            throw new ValidationException("Логин не может содержать пробелы");
        }
    }

    private void validateUserId(Integer userId) {
        if (userId == null) {
            log.error("Id не заполнен");
            throw new ValidationException("Для обновления данных пользователя надо указать его Id");
        }
    }

}
