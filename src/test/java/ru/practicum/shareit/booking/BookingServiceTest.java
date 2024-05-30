package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.IncorrectStateException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BookingServiceTest {

    private BookingService bookingService;
    private BookingRepository bookingRepository;
    private UserService userService;
    private ItemService itemService;
    private User user;
    private Item item;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        userService = mock(UserService.class);
        itemService = mock(ItemService.class);
        bookingService = new BookingService(itemService, userService, bookingRepository);

        user = new User(2, "user@ya.ru", "Irina");
        User user2 = new User(3, "user2@ya.ru", "Irina2");
        item = new Item("book", "book for read", Boolean.TRUE);
        item.setOwner(user2);
        item.setId(4);
        when(userService.findUserById(any())).thenReturn(user);
        when(itemService.findById(any())).thenReturn(item);
    }

    @Test
    void testCreateBookingOk() {
        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingState.WAITING);
        booking.setStart(LocalDateTime.of(2025, Month.MAY, 25, 12, 40));
        booking.setEnd(LocalDateTime.of(2025, Month.MAY, 25, 12, 45));

        when(bookingRepository.save(any())).thenReturn(booking);
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(4);
        bookingDto.setStart(LocalDateTime.of(2025, Month.MAY, 25, 12, 40));
        bookingDto.setEnd(LocalDateTime.of(2025, Month.MAY, 25, 12, 45));

        Booking result = bookingService.createBooking(2, bookingDto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(booking, result);
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void testCreateBookingWithPastStartDateError() {
        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingState.WAITING);
        booking.setStart(LocalDateTime.of(2024, Month.MAY, 25, 12, 40));
        booking.setEnd(LocalDateTime.of(2024, Month.MAY, 25, 12, 45));

        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(4);
        bookingDto.setStart(LocalDateTime.of(2024, Month.MAY, 25, 12, 40));
        bookingDto.setEnd(LocalDateTime.of(2024, Month.MAY, 25, 12, 45));

        ValidationException ex = assertThrows(ValidationException.class, () -> bookingService.createBooking(2, bookingDto));

        assertEquals("Неверно заполнены поля начала/окончания бронирования", ex.getMessage());
    }

    @Test
    void testUpdateBookingToApprovedFailedBecauseOfBookingNotWAITING() {
        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingState.APPROVED);
        booking.setStart(LocalDateTime.of(2025, Month.MAY, 25, 12, 40));
        booking.setEnd(LocalDateTime.of(2025, Month.MAY, 25, 12, 45));
        booking.setId(1);

        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingRepository.findById(any())).thenReturn(java.util.Optional.of(booking));

        ValidationException ex = assertThrows(ValidationException.class, () -> bookingService.updateBooking(3, 1, true));

        assertEquals("Запрос на бронирования был обработан ранее", ex.getMessage());
    }

    @Test
    void testUpdateBookingToApprovedOk() {
        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingState.APPROVED);
        booking.setStart(LocalDateTime.of(2025, Month.MAY, 25, 12, 40));
        booking.setEnd(LocalDateTime.of(2025, Month.MAY, 25, 12, 45));
        booking.setId(1);

        Booking booking2 = new Booking();
        booking2.setBooker(user);
        booking2.setItem(item);
        booking2.setStatus(BookingState.WAITING);
        booking2.setStart(LocalDateTime.of(2025, Month.MAY, 25, 12, 40));
        booking2.setEnd(LocalDateTime.of(2025, Month.MAY, 25, 12, 45));
        booking2.setId(1);

        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingRepository.findById(any())).thenReturn(java.util.Optional.of(booking2));

        Booking result = bookingService.updateBooking(3, 1, true);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(booking, result);
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void testFindBookingByIdOk() {
        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingState.APPROVED);
        booking.setStart(LocalDateTime.of(2025, Month.MAY, 25, 12, 40));
        booking.setEnd(LocalDateTime.of(2025, Month.MAY, 25, 12, 45));
        booking.setId(1);

        when(bookingRepository.findById(any())).thenReturn(java.util.Optional.of(booking));

        Booking result = bookingService.findById(1);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(booking, result);
        verify(bookingRepository, times(1)).findById(any());
    }

    @Test
    void testFindBookingByIdFailedWithoutAccess() {
        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingState.APPROVED);
        booking.setStart(LocalDateTime.of(2025, Month.MAY, 25, 12, 40));
        booking.setEnd(LocalDateTime.of(2025, Month.MAY, 25, 12, 45));
        booking.setId(1);

        when(bookingRepository.findById(any())).thenReturn(java.util.Optional.of(booking));

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> bookingService.findBookingById(10, 1));

        assertEquals("Нет прав для просмотра информации", ex.getMessage());
    }

    @Test
    void testFindAllBookingsOfUserOk() {
        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingState.APPROVED);
        booking.setStart(LocalDateTime.of(2025, Month.MAY, 25, 12, 40));
        booking.setEnd(LocalDateTime.of(2025, Month.MAY, 25, 12, 45));
        booking.setId(1);

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);

        when(bookingRepository.findAllByBookerIdOrderByEndDesc(any())).thenReturn(bookings);

        List<Booking> result = bookingService.findAllBookingsOfUser(2, "ALL", null, null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByBookerIdOrderByEndDesc(any());
    }

    @Test
    void testFindAllBookingsOfUserFailedWithUnknownState() {
        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingState.APPROVED);
        booking.setStart(LocalDateTime.of(2025, Month.MAY, 25, 12, 40));
        booking.setEnd(LocalDateTime.of(2025, Month.MAY, 25, 12, 45));
        booking.setId(1);

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);

        when(bookingRepository.findAllByBookerIdOrderByEndDesc(any())).thenReturn(bookings);

        IncorrectStateException ex = assertThrows(IncorrectStateException.class, () -> bookingService.findAllBookingsOfUser(2, "BEE", null, null));

        assertEquals("Unknown state: BEE", ex.getMessage());
    }

    @Test
    void testFindAllBookingsOfOwnerItemsOk() {
        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingState.APPROVED);
        booking.setStart(LocalDateTime.of(2025, Month.MAY, 25, 12, 40));
        booking.setEnd(LocalDateTime.of(2025, Month.MAY, 25, 12, 45));
        booking.setId(1);

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        List<Item> items = new ArrayList<>();
        items.add(item);

        when(bookingRepository.findAllByItemIdInOrderByEndDesc(any())).thenReturn(bookings);
        when(itemService.findAllItemsOfUser(any())).thenReturn(items);

        List<Booking> result = bookingService.findAllBookingsOfOwnerItems(3, "ALL", null, null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByItemIdInOrderByEndDesc(any());
    }

    @Test
    void testFindAllBookingsOfOwnerItemsFailWithoutUserItems() {
        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingState.APPROVED);
        booking.setStart(LocalDateTime.of(2025, Month.MAY, 25, 12, 40));
        booking.setEnd(LocalDateTime.of(2025, Month.MAY, 25, 12, 45));
        booking.setId(1);

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);

        when(bookingRepository.findAllByItemIdInOrderByEndDesc(any())).thenReturn(bookings);

        ValidationException ex = assertThrows(ValidationException.class, () -> bookingService.findAllBookingsOfOwnerItems(10, "ALL", null, null));

        assertEquals("У пользователя нет вещей", ex.getMessage());
    }

}
