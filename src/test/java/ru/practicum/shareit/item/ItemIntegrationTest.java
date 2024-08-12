package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class ItemIntegrationTest {

    @Autowired
    UserController userController;

    @Autowired
    ItemController itemController;

    @Autowired
    BookingController bookingController;

    @Autowired
    List<JpaRepository> repositoryList;

    @Autowired
    BookingRepository bookingRepository;

    private Integer user1Id;
    private Integer user2Id;
    private Integer itemId;

    @BeforeEach
    void beforeEach() {
        repositoryList.forEach(CrudRepository::deleteAll);
        UserDto userDto = new UserDto();
        userDto.setName("owner");
        userDto.setEmail("owner@owner.ru");
        UserDto resultUser1 = userController.create(userDto);
        userDto.setName("asker");
        userDto.setEmail("asker@asker.ru");
        UserDto resultUser2 = userController.create(userDto);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("name");
        itemDto.setDescription("description");
        itemDto.setAvailable(true);
        ItemDto resultItem = itemController.create(resultUser1.getId(), itemDto);

        user1Id = resultUser1.getId();
        user2Id = resultUser2.getId();
        itemId = resultItem.getId();

        Booking booking = new Booking();
        Item item = new Item();
        item.setId(itemId);
        User user = new User();
        user.setId(user2Id);
        booking.setStart(LocalDateTime.now().minusDays(3));
        booking.setEnd(LocalDateTime.now().minusDays(2));
        booking.setBooker(user);
        booking.setItem(item);
        bookingRepository.save(booking);
    }

    @Test
    void updateItemThrowExceptionWhenUserIsNotOwner() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("name2");
        itemDto.setDescription("description2");
        assertThrows(AccessDeniedException.class, () -> itemController.update(user2Id, itemId, itemDto));
    }

    @Test
    void bookingThrowsExceptionIfItemNotAvailable() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("name");
        itemDto.setDescription("description");
        itemDto.setAvailable(false);
        itemController.update(user1Id, itemId, itemDto);

        BookingDto bookingDto = new BookingDto(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(10), itemId);
        assertThrows(ValidationException.class, () -> bookingController.create(user2Id, bookingDto));
    }

    @Test
    void createCommentThrowExceptionWhenUserNotBooked() {

        UserDto userDto = new UserDto();
        userDto.setName("user3");
        userDto.setEmail("user3@user3.ru");
        UserDto user3 = userController.create(userDto);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("good item");
        CommentDto comment = itemController.createComment(user2Id, itemId, commentDto);

        assertNotNull(comment.getId(), "user that booked item can create comments");
        assertThrows(ValidationException.class, () -> itemController.createComment(user3.getId(), itemId, commentDto));
    }
}
