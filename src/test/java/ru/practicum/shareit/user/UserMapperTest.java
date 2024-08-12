package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserMapperTest {

    private User user;
    private UserDto userDto;

    @BeforeEach
    void beforeEach() {
        user = new User(1, "user@owner.ru", "owner");
        userDto = new UserDto(1, "user@owner.ru", "owner");
    }

    @Test
    void toDtoOk() {
        UserDto userDtoActual = UserMapper.toDto(user);
        assertEquals(userDto, userDtoActual);
    }

    @Test
    void toDtoListOk() {
        List<UserDto> userDtoListActual = UserMapper.toDto(Arrays.asList(user, user));
        assertEquals(2, userDtoListActual.size());
        assertEquals(userDto, userDtoListActual.get(0));
        assertEquals(userDto, userDtoListActual.get(1));
    }

    @Test
    void toItemOk() {
        User userActual = UserMapper.toUser(userDto);
        user.setId(null);
        assertEquals(user, userActual);
    }

}
