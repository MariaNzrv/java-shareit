package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private final UserService userService;

    @GetMapping
    public List<UserDto> findAll() {
        List<User> users = userService.findAllUsers();
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            userDtos.add(UserMapper.toUserDto(user));
        }
        return userDtos;
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable Integer userId) {
        User user = userService.findUserById(userId);
        return UserMapper.toUserDto(user);
    }

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        User savedUser = userService.createUser(userDto);
        return UserMapper.toUserDto(savedUser);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Integer userId, @RequestBody UserDto userDto) {
        User savedUser = userService.updateUser(userId, userDto);
        return UserMapper.toUserDto(savedUser);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Integer userId) {
        userService.deleteUser(userId);
    }

}
