package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User savedUser;
    private Item savedItem;

    @BeforeEach
    void setUp() {
        User user = new User(2, "user@ya.ru", "Irina");
        savedUser = userRepository.save(user);

        Item item = new Item("book", "book for read", Boolean.TRUE);
        item.setOwner(savedUser);
        savedItem = itemRepository.save(item);
    }


    private Booking saveBooking(User savedUser, Item savedItem, LocalDateTime start, LocalDateTime end, BookingState state) {
        Booking booking = new Booking();
        booking.setBooker(savedUser);
        booking.setItem(savedItem);
        booking.setStatus(state);
        booking.setStart(start);
        booking.setEnd(end);
        return bookingRepository.save(booking);
    }

    @Test
    void findAllByBookerIdOrderByEndDescOk() {

        Booking savedBooking = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 41),
                BookingState.WAITING);

        Booking savedBooking2 = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                BookingState.APPROVED);

        List<Booking> result = bookingRepository.findAllByBookerIdOrderByEndDesc(savedUser.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(savedBooking2, result.get(0));
        Assertions.assertEquals(savedBooking, result.get(1));
    }

    @Test
    void findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDescOk() {

        Booking savedBooking = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 45),
                BookingState.WAITING);

        Booking savedBooking2 = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                BookingState.APPROVED);

        List<Booking> result = bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                savedUser.getId(),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 43),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 43));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(savedBooking, result.get(0));

    }

    @Test
    void findAllByBookerIdAndEndIsBeforeOrderByEndDescOk() {

        Booking savedBooking = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 45),
                BookingState.WAITING);

        Booking savedBooking2 = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                BookingState.APPROVED);

        List<Booking> result = bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByEndDesc(
                savedUser.getId(),
                LocalDateTime.of(2024, Month.MAY, 27, 12, 43));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(savedBooking, result.get(0));

    }

    @Test
    void findAllByBookerIdAndStartIsAfterOrderByEndDescOk() {

        Booking savedBooking = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 45),
                BookingState.WAITING);

        Booking savedBooking2 = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                BookingState.APPROVED);

        List<Booking> result = bookingRepository.findAllByBookerIdAndStartIsAfterOrderByEndDesc(
                savedUser.getId(),
                LocalDateTime.of(2024, Month.MAY, 27, 12, 43));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(savedBooking2, result.get(0));

    }

    @Test
    void findAllByBookerIdAndStatusOrderByEndDescOk() {

        Booking savedBooking = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 45),
                BookingState.WAITING);

        Booking savedBooking2 = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                BookingState.APPROVED);

        List<Booking> result = bookingRepository.findAllByBookerIdAndStatusOrderByEndDesc(
                savedUser.getId(),
                BookingState.APPROVED);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(savedBooking2, result.get(0));

    }

    @Test
    void findAllByItemIdInOrderByEndDescOk() {

        Booking savedBooking = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 45),
                BookingState.WAITING);

        Booking savedBooking2 = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                BookingState.APPROVED);
        Set<Integer> itemIds = new HashSet<>();
        itemIds.add(savedItem.getId());

        List<Booking> result = bookingRepository.findAllByItemIdInOrderByEndDesc(itemIds);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(savedBooking2, result.get(0));
        Assertions.assertEquals(savedBooking, result.get(1));

    }

    @Test
    void findAllByItemIdInAndStartIsBeforeAndEndIsAfterOrderByEndDescOk() {

        Booking savedBooking = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 45),
                BookingState.WAITING);

        Booking savedBooking2 = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                BookingState.APPROVED);
        Set<Integer> itemIds = new HashSet<>();
        itemIds.add(savedItem.getId());

        List<Booking> result = bookingRepository.findAllByItemIdInAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                itemIds,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 43),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 43));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(savedBooking, result.get(0));

    }

    @Test
    void findAllByItemIdInAndEndIsBeforeOrderByEndDescOk() {

        Booking savedBooking = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 45),
                BookingState.WAITING);

        Booking savedBooking2 = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                BookingState.APPROVED);
        Set<Integer> itemIds = new HashSet<>();
        itemIds.add(savedItem.getId());

        List<Booking> result = bookingRepository.findAllByItemIdInAndEndIsBeforeOrderByEndDesc(
                itemIds,
                LocalDateTime.of(2024, Month.MAY, 27, 12, 43));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(savedBooking, result.get(0));

    }

    @Test
    void findAllByItemIdInAndStartIsAfterOrderByEndDescOk() {

        Booking savedBooking = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 45),
                BookingState.WAITING);

        Booking savedBooking2 = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                BookingState.APPROVED);
        Set<Integer> itemIds = new HashSet<>();
        itemIds.add(savedItem.getId());

        List<Booking> result = bookingRepository.findAllByItemIdInAndStartIsAfterOrderByEndDesc(
                itemIds,
                LocalDateTime.of(2024, Month.MAY, 27, 12, 43));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(savedBooking2, result.get(0));

    }

    @Test
    void findAllByItemIdInAndStatusOrderByEndDescOk() {

        Booking savedBooking = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 45),
                BookingState.WAITING);

        Booking savedBooking2 = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                BookingState.APPROVED);
        Set<Integer> itemIds = new HashSet<>();
        itemIds.add(savedItem.getId());

        List<Booking> result = bookingRepository.findAllByItemIdInAndStatusOrderByEndDesc(
                itemIds,
                BookingState.WAITING);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(savedBooking, result.get(0));

    }

    @Test
    void findFirst1ByItemIdAndStartIsBeforeAndStatusOrderByStartDescOk() {

        Booking savedBooking = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 45),
                BookingState.WAITING);

        Booking savedBooking2 = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                BookingState.APPROVED);

        Booking result = bookingRepository.findFirst1ByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(
                savedItem.getId(),
                LocalDateTime.of(2024, Month.MAY, 27, 12, 43),
                BookingState.WAITING);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedBooking, result);

    }

    @Test
    void findFirst1ByItemIdAndStartIsAfterAndStatusOrderByStartAscOk() {

        Booking savedBooking = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 45),
                BookingState.WAITING);

        Booking savedBooking2 = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                BookingState.APPROVED);

        Booking result = bookingRepository.findFirst1ByItemIdAndStartIsAfterAndStatusOrderByStartAsc(
                savedItem.getId(),
                LocalDateTime.of(2024, Month.MAY, 27, 12, 43),
                BookingState.APPROVED);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedBooking2, result);

    }

    @Test
    void findFirst1ByBookerIdAndItemIdAndEndIsBeforeOk() {

        Booking savedBooking = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 45),
                BookingState.WAITING);

        Booking savedBooking2 = saveBooking(savedUser,
                savedItem,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                BookingState.APPROVED);

        Booking result = bookingRepository.findFirst1ByBookerIdAndItemIdAndEndIsBefore(
                savedUser.getId(),
                savedItem.getId(),
                LocalDateTime.of(2024, Month.MAY, 27, 12, 43));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedBooking, result);

    }

}
