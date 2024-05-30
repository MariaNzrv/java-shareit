package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class BookingMapperTest {

    @Test
    void convertToDtoOk() {
        User user = new User(2, "user@ya.ru", "Irina");
        Item item = new Item("book", "book for read", Boolean.TRUE);
        item.setId(3);
        item.setOwner(user);
        Booking booking = new Booking(1,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 41),
                item, user, BookingState.APPROVED);

        BookingDtoResponse result = BookingMapper.toDto(booking);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getId());
        Assertions.assertEquals(LocalDateTime.of(2024, Month.MAY, 25, 12, 40), result.getStart());
        Assertions.assertEquals(LocalDateTime.of(2024, Month.MAY, 25, 12, 41), result.getEnd());
        Assertions.assertEquals(3, result.getItem().getId());
        Assertions.assertEquals("book", result.getItem().getName());
        Assertions.assertEquals("book for read", result.getItem().getDescription());
        Assertions.assertEquals(true, result.getItem().getAvailable());
        Assertions.assertEquals(2, result.getBooker().getId());
        Assertions.assertEquals("Irina", result.getBooker().getName());
        Assertions.assertEquals("user@ya.ru", result.getBooker().getEmail());
        Assertions.assertEquals(BookingState.APPROVED, result.getStatus());
    }

    @Test
    void convertToListDtoOk() {
        User user = new User(2, "user@ya.ru", "Irina");
        Item item = new Item("book", "book for read", Boolean.TRUE);
        item.setId(3);
        item.setOwner(user);
        Booking booking = new Booking(1,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 41),
                item, user, BookingState.APPROVED);

        User user2 = new User(4, "user2@ya.ru", "Irina2");
        Item item2 = new Item("book2", "book2 for read", Boolean.TRUE);
        item2.setId(5);
        item2.setOwner(user2);
        Booking booking2 = new Booking(6,
                LocalDateTime.of(2025, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2025, Month.MAY, 25, 12, 41),
                item2, user2, BookingState.APPROVED);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        bookings.add(booking2);

        List<BookingDtoResponse> result = BookingMapper.toDto(bookings);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(1, result.get(0).getId());

        Assertions.assertEquals(LocalDateTime.of(2024, Month.MAY, 25, 12, 40), result.get(0).getStart());
        Assertions.assertEquals(LocalDateTime.of(2024, Month.MAY, 25, 12, 41), result.get(0).getEnd());
        Assertions.assertEquals(3, result.get(0).getItem().getId());
        Assertions.assertEquals("book", result.get(0).getItem().getName());
        Assertions.assertEquals("book for read", result.get(0).getItem().getDescription());
        Assertions.assertEquals(true, result.get(0).getItem().getAvailable());
        Assertions.assertEquals(2, result.get(0).getBooker().getId());
        Assertions.assertEquals("Irina", result.get(0).getBooker().getName());
        Assertions.assertEquals("user@ya.ru", result.get(0).getBooker().getEmail());
        Assertions.assertEquals(BookingState.APPROVED, result.get(0).getStatus());

        Assertions.assertEquals(6, result.get(1).getId());
        Assertions.assertEquals(LocalDateTime.of(2025, Month.MAY, 25, 12, 40), result.get(1).getStart());
        Assertions.assertEquals(LocalDateTime.of(2025, Month.MAY, 25, 12, 41), result.get(1).getEnd());
        Assertions.assertEquals(5, result.get(1).getItem().getId());
        Assertions.assertEquals("book2", result.get(1).getItem().getName());
        Assertions.assertEquals("book2 for read", result.get(1).getItem().getDescription());
        Assertions.assertEquals(true, result.get(1).getItem().getAvailable());
        Assertions.assertEquals(4, result.get(1).getBooker().getId());
        Assertions.assertEquals("Irina2", result.get(1).getBooker().getName());
        Assertions.assertEquals("user2@ya.ru", result.get(1).getBooker().getEmail());
        Assertions.assertEquals(BookingState.APPROVED, result.get(1).getStatus());

    }

    @Test
    void convertToBookingOk() {
        User user = new User(2, "user@ya.ru", "Irina");
        Item item = new Item("book", "book for read", Boolean.TRUE);
        item.setId(3);
        item.setOwner(user);

        BookingDto bookingDto = new BookingDto(LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 41), 3);

        Booking result = BookingMapper.toBooking(bookingDto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(LocalDateTime.of(2024, Month.MAY, 25, 12, 40), result.getStart());
        Assertions.assertEquals(LocalDateTime.of(2024, Month.MAY, 25, 12, 41), result.getEnd());
    }

    @Test
    void convertToBookingDtoForItemOk() {
        User user = new User(2, "user@ya.ru", "Irina");
        Item item = new Item("book", "book for read", Boolean.TRUE);
        item.setId(3);
        item.setOwner(user);
        Booking booking = new Booking(1,
                LocalDateTime.of(2024, Month.MAY, 25, 12, 40),
                LocalDateTime.of(2024, Month.MAY, 25, 12, 41),
                item, user, BookingState.APPROVED);

        BookingDtoForItem result = BookingMapper.toItemDto(booking);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getId());
        Assertions.assertEquals(LocalDateTime.of(2024, Month.MAY, 25, 12, 40), result.getStart());
        Assertions.assertEquals(LocalDateTime.of(2024, Month.MAY, 25, 12, 41), result.getEnd());
        Assertions.assertEquals(2, result.getBookerId());
        Assertions.assertEquals(BookingState.APPROVED, result.getStatus());
    }
}
