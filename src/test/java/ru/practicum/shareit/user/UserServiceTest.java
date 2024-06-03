package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.practicum.shareit.exception.ConflictValidationException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void beforeEach() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
        user1 = new User(1, "user@owner.ru", "owner");
        user2 = new User(2, "user@asker.ru", "asker");

        Mockito.doReturn(Arrays.asList(user1, user2)).when(userRepository).findAll();
        Mockito.doReturn(Optional.of(user1)).when(userRepository).findById(user1.getId());
        Mockito.doReturn(Optional.of(user2)).when(userRepository).findById(user2.getId());
        Mockito.doReturn(Optional.empty()).when(userRepository).findById(404);
        Mockito.doAnswer(invocationOnMock -> invocationOnMock.getArgument(0)).when(userRepository).save(any());
    }

    @Test
    void findAllUsers() {
        List<User> allUsers = userService.findAllUsers();

        verify(userRepository, times(1)).findAll();

        assertEquals(2, allUsers.size());
        assertEquals(user1, allUsers.get(0));
        assertEquals(user2, allUsers.get(1));
    }

    @Test
    void findByIdOk() {
        User actual = userService.findUserById(user1.getId());

        verify(userRepository, times(1)).findById(user1.getId());

        assertEquals(user1, actual);
    }

    @Test
    void findByIdNotFound() {
        assertThrows(EntityNotFoundException.class, () -> userService.findUserById(404));
        verify(userRepository, times(1)).findById(404);
    }

    @Test
    void createUserOk() {
        UserDto userDto = new UserDto();
        userDto.setEmail("newEmail@email.com");
        userDto.setName("newName");
        User user = userService.createUser(userDto);

        verify(userRepository, times(1)).save(any());

        assertEquals(userDto.getEmail(), user.getEmail());
        assertEquals(userDto.getName(), user.getName());
    }

    @Test
    void createUserValidations() {
        UserDto userDto = new UserDto();
        assertThrows(ValidationException.class, () -> userService.createUser(userDto));
        verify(userRepository, times(0)).save(any());

        userDto.setName(" ");
        userDto.setEmail(" ");
        assertThrows(ValidationException.class, () -> userService.createUser(userDto));
        verify(userRepository, times(0)).save(any());

        userDto.setName("name");
        userDto.setEmail("email");
        assertThrows(ValidationException.class, () -> userService.createUser(userDto));
        verify(userRepository, times(0)).save(any());
    }

    @Test
    void updateUser() {
        UserDto userDto = new UserDto();
        userDto.setName("newName");
        userDto.setEmail("newEmail@email.com");
        User user = userService.updateUser(user1.getId(), userDto);
        verify(userRepository, times(1)).save(any());

        assertEquals(userDto.getName(), user.getName());
        assertEquals(userDto.getEmail(), user.getEmail());
    }

    @Test
    void updateUserValidations() {
        UserDto userDto = new UserDto();
        userDto.setName(" ");
        userDto.setEmail(" ");
        assertThrows(ValidationException.class, () -> userService.updateUser(user1.getId(), userDto));
        verify(userRepository, times(0)).save(any());

        userDto.setName("name");
        userDto.setEmail("email");
        assertThrows(ValidationException.class, () -> userService.updateUser(user1.getId(), userDto));
        verify(userRepository, times(0)).save(any());

        userDto.setName("name");
        userDto.setEmail("user@asker.ru");
        assertThrows(ConflictValidationException.class, () -> userService.updateUser(user1.getId(), userDto));
        verify(userRepository, times(0)).save(any());
    }

    @Test
    void deleteUserOk() {
        userService.deleteUser(user1.getId());

        verify(userRepository, times(1)).findById(user1.getId());
        verify(userRepository, times(1)).delete(user1);
    }

    @Test
    void deleteUserNotFound() {
        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(404));

        verify(userRepository, times(1)).findById(404);
        verify(userRepository, times(0)).delete(any());
    }
}
