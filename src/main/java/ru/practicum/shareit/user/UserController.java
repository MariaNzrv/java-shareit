package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> findAll() {
        return UserMapper.toDto(userService.findAllUsers());
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable Integer userId) {
        User user = userService.findUserById(userId);
        return UserMapper.toDto(user);
    }

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        User savedUser = userService.createUser(userDto);
        return UserMapper.toDto(savedUser);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Integer userId, @RequestBody UserDto userDto) {
        User savedUser = userService.updateUser(userId, userDto);
        return UserMapper.toDto(savedUser);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Integer userId) {
        userService.deleteUser(userId);
    }

}
