package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingSearchState;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class BookingIntegrationTest {

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

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    UserRepository userRepository;

    private Integer user2Id;
    private Integer itemId;
    private Integer oldApprovedBookingId;
    private Integer currentRejectedBookingId;
    private Integer futureWaitingBookingId;

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

        user2Id = resultUser2.getId();
        itemId = resultItem.getId();

        oldApprovedBookingId = createBooking(LocalDateTime.now().minusDays(10), BookingState.APPROVED);
        currentRejectedBookingId = createBooking(LocalDateTime.now().minusMinutes(10), BookingState.REJECTED);
        futureWaitingBookingId = createBooking(LocalDateTime.now().plusMinutes(10), BookingState.WAITING);

    }

    @Test
    void findAllTypesOfBookings() {
        List<BookingDtoResponse> allBookingsOfUser = bookingController.findAllBookingsOfUser(user2Id, BookingSearchState.ALL.name(), 0, 100);
        assertEquals(3, allBookingsOfUser.size());
    }

    @Test
    void findPastTypesOfBookings() {
        List<BookingDtoResponse> allBookingsOfUser = bookingController.findAllBookingsOfUser(user2Id, BookingSearchState.PAST.name(), 0, 100);
        assertEquals(1, allBookingsOfUser.size());
        assertEquals(oldApprovedBookingId, allBookingsOfUser.get(0).getId());
    }

    @Test
    void findFutureTypesOfBookings() {
        List<BookingDtoResponse> allBookingsOfUser = bookingController.findAllBookingsOfUser(user2Id, BookingSearchState.FUTURE.name(), 0, 100);
        assertEquals(1, allBookingsOfUser.size());
        assertEquals(futureWaitingBookingId, allBookingsOfUser.get(0).getId());
    }

    @Test
    void findRejectedTypesOfBookings() {
        List<BookingDtoResponse> allBookingsOfUser = bookingController.findAllBookingsOfUser(user2Id, BookingSearchState.REJECTED.name(), 0, 100);
        assertEquals(1, allBookingsOfUser.size());
        assertEquals(currentRejectedBookingId, allBookingsOfUser.get(0).getId());
    }

    @Test
    void findCurrentTypesOfBookings() {
        List<BookingDtoResponse> allBookingsOfUser = bookingController.findAllBookingsOfUser(user2Id, BookingSearchState.CURRENT.name(), 0, 100);
        assertEquals(1, allBookingsOfUser.size());
        assertEquals(currentRejectedBookingId, allBookingsOfUser.get(0).getId());
    }

    @Test
    void findWaitingTypesOfBookings() {
        List<BookingDtoResponse> allBookingsOfUser = bookingController.findAllBookingsOfUser(user2Id, BookingSearchState.WAITING.name(), 0, 100);
        assertEquals(1, allBookingsOfUser.size());
        assertEquals(futureWaitingBookingId, allBookingsOfUser.get(0).getId());
    }

    private Integer createBooking(LocalDateTime startTime, BookingState bookingState) {
        Booking booking = new Booking();
        Item item = itemRepository.findById(itemId).get();
        User user = userRepository.findById(user2Id).get();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(bookingState);
        booking.setStart(startTime);
        booking.setEnd(startTime.plusDays(1));
        return bookingRepository.save(booking).getId();
    }

}
