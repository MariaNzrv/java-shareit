package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
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

import static org.junit.jupiter.api.Assertions.*;
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
        when(userService.findUserById(2)).thenReturn(user);
        when(userService.findUserById(3)).thenReturn(user2);
        when(itemService.findById(any())).thenReturn(item);
    }

    @Test
    void findByIdFails() {
        assertThrows(ValidationException.class, () -> bookingService.findById(null));
        assertThrows(EntityNotFoundException.class, () -> bookingService.findById(404));
    }

    @Test
    void createBookingOk() {
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
    void createBookingWithPastStartDateError() {
        LocalDateTime now = LocalDateTime.now();

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(4);
        bookingDto.setStart(now.minusDays(2));
        bookingDto.setEnd(now.minusDays(1));

        ValidationException ex = assertThrows(ValidationException.class, () -> bookingService.createBooking(2, bookingDto));
        bookingDto.setStart(now.plusDays(1));
        bookingDto.setEnd(now.plusDays(2));
        item.setAvailable(false);
        ValidationException ex2 = assertThrows(ValidationException.class, () -> bookingService.createBooking(2, bookingDto));
        item.setAvailable(true);
        EntityNotFoundException ex3 = assertThrows(EntityNotFoundException.class, () -> bookingService.createBooking(3, bookingDto));

        assertEquals("Неверно заполнены поля начала/окончания бронирования", ex.getMessage());
        assertEquals("Вещь недоступна для бронирования", ex2.getMessage());
        assertEquals("Нельзя забронировать вещь, которая принадлежит вам", ex3.getMessage());
    }

    @Test
    void updateBookingToApprovedFailedBecauseOfBookingNotWAITING() {
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
    void updateBookingToApprovedOk() {
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
    void findBookingByIdOk() {
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
    void findBookingByIdFailedWithoutAccess() {
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
    void findAllBookingsOfUserOk() {
        List<Booking> bookings = prepareBookingTest();

        when(bookingRepository.findAllByBookerId(any(), any())).thenReturn(new PageImpl<>(bookings));

        List<Booking> result = bookingService.findAllBookingsOfUser(2, "ALL", 0, 100);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByBookerId(any(), any());
    }

    @Test
    void findCurrentBookingsOfUserOk() {
        List<Booking> bookings = prepareBookingTest();

        when(bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfter(any(), any(), any(), any())).thenReturn(new PageImpl<>(bookings));

        List<Booking> result = bookingService.findAllBookingsOfUser(2, "CURRENT", 0, 100);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByBookerIdAndStartIsBeforeAndEndIsAfter(any(), any(), any(), any());
    }

    @Test
    void findPastBookingsOfUserOk() {
        List<Booking> bookings = prepareBookingTest();

        when(bookingRepository.findAllByBookerIdAndEndIsBefore(any(), any(), any())).thenReturn(new PageImpl<>(bookings));

        List<Booking> result = bookingService.findAllBookingsOfUser(2, "PAST", 0, 100);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByBookerIdAndEndIsBefore(any(), any(), any());
    }

    @Test
    void findFutureBookingsOfUserOk() {
        List<Booking> bookings = prepareBookingTest();

        when(bookingRepository.findAllByBookerIdAndStartIsAfter(any(), any(), any())).thenReturn(new PageImpl<>(bookings));

        List<Booking> result = bookingService.findAllBookingsOfUser(2, "FUTURE", 0, 100);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByBookerIdAndStartIsAfter(any(), any(), any());
    }

    @Test
    void findWaitingBookingsOfUserOk() {
        List<Booking> bookings = prepareBookingTest();

        when(bookingRepository.findAllByBookerIdAndStatus(any(), any(), any())).thenReturn(new PageImpl<>(bookings));

        List<Booking> result = bookingService.findAllBookingsOfUser(2, "WAITING", 0, 100);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByBookerIdAndStatus(any(), any(), any());
    }

    @Test
    void findRejectedBookingsOfUserOk() {
        List<Booking> bookings = prepareBookingTest();

        when(bookingRepository.findAllByBookerIdAndStatus(any(), any(), any())).thenReturn(new PageImpl<>(bookings));

        List<Booking> result = bookingService.findAllBookingsOfUser(2, "REJECTED", 0, 100);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByBookerIdAndStatus(any(), any(), any());
    }

    @Test
    void findAllBookingsOfUserFailedWithUnknownState() {
        List<Booking> bookings = prepareBookingTest();

        when(bookingRepository.findAllByBookerIdOrderByEndDesc(any())).thenReturn(bookings);

        IncorrectStateException ex = assertThrows(IncorrectStateException.class, () -> bookingService.findAllBookingsOfUser(2, "BEE", null, null));

        assertEquals("Unknown state: BEE", ex.getMessage());
    }

    @Test
    void findAllBookingsOfOwnerItemsOk() {
        List<Booking> bookings = prepareBookingTest();
        List<Item> items = new ArrayList<>();
        items.add(item);

        when(bookingRepository.findAllByItemIdIn(any(), any())).thenReturn(new PageImpl<>(bookings));
        when(itemService.findAllItemsOfUser(any())).thenReturn(items);

        List<Booking> result = bookingService.findAllBookingsOfOwnerItems(3, "ALL", 0, 100);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByItemIdIn(any(), any());
    }

    @Test
    void findCurrentBookingsOfOwnerItemsOk() {
        List<Booking> bookings = prepareBookingTest();
        List<Item> items = new ArrayList<>();
        items.add(item);

        when(bookingRepository.findAllByItemIdInAndStartIsBeforeAndEndIsAfter(any(), any(), any(), any())).thenReturn(new PageImpl<>(bookings));
        when(itemService.findAllItemsOfUser(any())).thenReturn(items);

        List<Booking> result = bookingService.findAllBookingsOfOwnerItems(3, "CURRENT", 0, 100);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByItemIdInAndStartIsBeforeAndEndIsAfter(any(), any(), any(), any());
    }

    @Test
    void findFutureBookingsOfOwnerItemsOk() {
        List<Booking> bookings = prepareBookingTest();
        List<Item> items = new ArrayList<>();
        items.add(item);

        when(bookingRepository.findAllByItemIdInAndStartIsAfter(any(), any(), any())).thenReturn(new PageImpl<>(bookings));
        when(itemService.findAllItemsOfUser(any())).thenReturn(items);

        List<Booking> result = bookingService.findAllBookingsOfOwnerItems(3, "FUTURE", 0, 100);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByItemIdInAndStartIsAfter(any(), any(), any());
    }

    @Test
    void findWaitingBookingsOfOwnerItemsOk() {
        List<Booking> bookings = prepareBookingTest();
        List<Item> items = new ArrayList<>();
        items.add(item);

        when(bookingRepository.findAllByItemIdInAndStatus(any(), any(), any())).thenReturn(new PageImpl<>(bookings));
        when(itemService.findAllItemsOfUser(any())).thenReturn(items);

        List<Booking> result = bookingService.findAllBookingsOfOwnerItems(3, "WAITING", 0, 100);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByItemIdInAndStatus(any(), any(), any());
    }

    @Test
    void findRejectedBookingsOfOwnerItemsOk() {
        List<Booking> bookings = prepareBookingTest();
        List<Item> items = new ArrayList<>();
        items.add(item);

        when(bookingRepository.findAllByItemIdInAndStatus(any(), any(), any())).thenReturn(new PageImpl<>(bookings));
        when(itemService.findAllItemsOfUser(any())).thenReturn(items);

        List<Booking> result = bookingService.findAllBookingsOfOwnerItems(3, "REJECTED", 0, 100);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByItemIdInAndStatus(any(), any(), any());
    }

    @Test
    void findPastBookingsOfOwnerItemsOk() {
        List<Booking> bookings = prepareBookingTest();
        List<Item> items = new ArrayList<>();
        items.add(item);

        when(bookingRepository.findAllByItemIdInAndEndIsBefore(any(), any(), any())).thenReturn(new PageImpl<>(bookings));
        when(itemService.findAllItemsOfUser(any())).thenReturn(items);

        List<Booking> result = bookingService.findAllBookingsOfOwnerItems(3, "PAST", 0, 100);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByItemIdInAndEndIsBefore(any(), any(), any());
    }

    @Test
    void findAllBookingsOfOwnerItemsFailWithoutUserItems() {
        List<Booking> bookings = prepareBookingTest();

        when(bookingRepository.findAllByItemIdInOrderByEndDesc(any())).thenReturn(bookings);

        ValidationException ex = assertThrows(ValidationException.class, () -> bookingService.findAllBookingsOfOwnerItems(10, "ALL", null, null));

        assertEquals("У пользователя нет вещей", ex.getMessage());
    }

    private List<Booking> prepareBookingTest() {
        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingState.APPROVED);
        booking.setStart(LocalDateTime.of(2025, Month.MAY, 25, 12, 40));
        booking.setEnd(LocalDateTime.of(2025, Month.MAY, 25, 12, 45));
        booking.setId(1);

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        return bookings;
    }

}
