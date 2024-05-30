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
    void testFindByIdFails() {
        assertThrows(ValidationException.class, () -> bookingService.findById(null));
        assertThrows(EntityNotFoundException.class, () -> bookingService.findById(404));
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
        ValidationException ex3 = assertThrows(ValidationException.class, () -> bookingService.createBooking(3, bookingDto));

        assertEquals("Неверно заполнены поля начала/окончания бронирования", ex.getMessage());
        assertEquals("Вещь недоступна для бронирования", ex2.getMessage());
        assertEquals("Нельзя забронировать вещь, которая принадлежит вам", ex3.getMessage());
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
        List<Booking> bookings = prepareBookingTest();

        when(bookingRepository.findAllByBookerIdOrderByEndDesc(any())).thenReturn(bookings);
        when(bookingRepository.findAllByBookerId(any(), any())).thenReturn(new PageImpl<>(bookings));

        List<Booking> result = bookingService.findAllBookingsOfUser(2, "ALL", null, null);
        List<Booking> result2 = bookingService.findAllBookingsOfUser(2, "ALL", 0, 100);

        assertEquals(result.size(), result2.size());
        assertTrue(result.containsAll(result2) && result2.containsAll(result));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByBookerIdOrderByEndDesc(any());
        verify(bookingRepository, times(1)).findAllByBookerId(any(), any());
    }

    @Test
    void testFindCurrentBookingsOfUserOk() {
        List<Booking> bookings = prepareBookingTest();

        when(bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(any(), any(), any())).thenReturn(bookings);
        when(bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfter(any(), any(), any(), any())).thenReturn(new PageImpl<>(bookings));

        List<Booking> result = bookingService.findAllBookingsOfUser(2, "CURRENT", null, null);
        List<Booking> result2 = bookingService.findAllBookingsOfUser(2, "CURRENT", 0, 100);

        assertEquals(result.size(), result2.size());
        assertTrue(result.containsAll(result2) && result2.containsAll(result));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(any(), any(), any());
        verify(bookingRepository, times(1)).findAllByBookerIdAndStartIsBeforeAndEndIsAfter(any(), any(), any(), any());
    }

    @Test
    void testFindPastBookingsOfUserOk() {
        List<Booking> bookings = prepareBookingTest();

        when(bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByEndDesc(any(), any())).thenReturn(bookings);
        when(bookingRepository.findAllByBookerIdAndEndIsBefore(any(), any(), any())).thenReturn(new PageImpl<>(bookings));

        List<Booking> result = bookingService.findAllBookingsOfUser(2, "PAST", null, null);
        List<Booking> result2 = bookingService.findAllBookingsOfUser(2, "PAST", 0, 100);

        assertEquals(result.size(), result2.size());
        assertTrue(result.containsAll(result2) && result2.containsAll(result));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByBookerIdAndEndIsBeforeOrderByEndDesc(any(), any());
        verify(bookingRepository, times(1)).findAllByBookerIdAndEndIsBefore(any(), any(), any());
    }

    @Test
    void testFindFutureBookingsOfUserOk() {
        List<Booking> bookings = prepareBookingTest();

        when(bookingRepository.findAllByBookerIdAndStartIsAfterOrderByEndDesc(any(), any())).thenReturn(bookings);
        when(bookingRepository.findAllByBookerIdAndStartIsAfter(any(), any(), any())).thenReturn(new PageImpl<>(bookings));

        List<Booking> result = bookingService.findAllBookingsOfUser(2, "FUTURE", null, null);
        List<Booking> result2 = bookingService.findAllBookingsOfUser(2, "FUTURE", 0, 100);

        assertEquals(result.size(), result2.size());
        assertTrue(result.containsAll(result2) && result2.containsAll(result));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByBookerIdAndStartIsAfterOrderByEndDesc(any(), any());
        verify(bookingRepository, times(1)).findAllByBookerIdAndStartIsAfter(any(), any(), any());
    }

    @Test
    void testFindWaitingBookingsOfUserOk() {
        List<Booking> bookings = prepareBookingTest();

        when(bookingRepository.findAllByBookerIdAndStatusOrderByEndDesc(any(), any())).thenReturn(bookings);
        when(bookingRepository.findAllByBookerIdAndStatus(any(), any(), any())).thenReturn(new PageImpl<>(bookings));

        List<Booking> result = bookingService.findAllBookingsOfUser(2, "WAITING", null, null);
        List<Booking> result2 = bookingService.findAllBookingsOfUser(2, "WAITING", 0, 100);

        assertEquals(result.size(), result2.size());
        assertTrue(result.containsAll(result2) && result2.containsAll(result));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByBookerIdAndStatusOrderByEndDesc(any(), any());
        verify(bookingRepository, times(1)).findAllByBookerIdAndStatus(any(), any(), any());
    }

    @Test
    void testFindRejectedBookingsOfUserOk() {
        List<Booking> bookings = prepareBookingTest();

        when(bookingRepository.findAllByBookerIdAndStatusOrderByEndDesc(any(), any())).thenReturn(bookings);
        when(bookingRepository.findAllByBookerIdAndStatus(any(), any(), any())).thenReturn(new PageImpl<>(bookings));

        List<Booking> result = bookingService.findAllBookingsOfUser(2, "REJECTED", null, null);
        List<Booking> result2 = bookingService.findAllBookingsOfUser(2, "REJECTED", 0, 100);

        assertEquals(result.size(), result2.size());
        assertTrue(result.containsAll(result2) && result2.containsAll(result));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByBookerIdAndStatusOrderByEndDesc(any(), any());
        verify(bookingRepository, times(1)).findAllByBookerIdAndStatus(any(), any(), any());
    }

    @Test
    void testFindAllBookingsOfUserFailedWithUnknownState() {
        List<Booking> bookings = prepareBookingTest();

        when(bookingRepository.findAllByBookerIdOrderByEndDesc(any())).thenReturn(bookings);

        IncorrectStateException ex = assertThrows(IncorrectStateException.class, () -> bookingService.findAllBookingsOfUser(2, "BEE", null, null));

        assertEquals("Unknown state: BEE", ex.getMessage());
    }

    @Test
    void testFindAllBookingsOfOwnerItemsOk() {
        List<Booking> bookings = prepareBookingTest();
        List<Item> items = new ArrayList<>();
        items.add(item);

        when(bookingRepository.findAllByItemIdInOrderByEndDesc(any())).thenReturn(bookings);
        when(bookingRepository.findAllByItemIdIn(any(), any())).thenReturn(new PageImpl<>(bookings));
        when(itemService.findAllItemsOfUser(any())).thenReturn(items);

        List<Booking> result = bookingService.findAllBookingsOfOwnerItems(3, "ALL", null, null);
        List<Booking> result2 = bookingService.findAllBookingsOfOwnerItems(3, "ALL", 0, 100);

        assertEquals(result.size(), result2.size());
        assertTrue(result.containsAll(result2) && result2.containsAll(result));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByItemIdInOrderByEndDesc(any());
        verify(bookingRepository, times(1)).findAllByItemIdIn(any(), any());
    }

    @Test
    void testFindCurrentBookingsOfOwnerItemsOk() {
        List<Booking> bookings = prepareBookingTest();
        List<Item> items = new ArrayList<>();
        items.add(item);

        when(bookingRepository.findAllByItemIdInAndStartIsBeforeAndEndIsAfterOrderByEndDesc(any(), any(), any())).thenReturn(bookings);
        when(bookingRepository.findAllByItemIdInAndStartIsBeforeAndEndIsAfter(any(), any(), any(), any())).thenReturn(new PageImpl<>(bookings));
        when(itemService.findAllItemsOfUser(any())).thenReturn(items);

        List<Booking> result = bookingService.findAllBookingsOfOwnerItems(3, "CURRENT", null, null);
        List<Booking> result2 = bookingService.findAllBookingsOfOwnerItems(3, "CURRENT", 0, 100);

        assertEquals(result.size(), result2.size());
        assertTrue(result.containsAll(result2) && result2.containsAll(result));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByItemIdInAndStartIsBeforeAndEndIsAfterOrderByEndDesc(any(), any(), any());
        verify(bookingRepository, times(1)).findAllByItemIdInAndStartIsBeforeAndEndIsAfter(any(), any(), any(), any());
    }

    @Test
    void testFindFutureBookingsOfOwnerItemsOk() {
        List<Booking> bookings = prepareBookingTest();
        List<Item> items = new ArrayList<>();
        items.add(item);

        when(bookingRepository.findAllByItemIdInAndStartIsAfterOrderByEndDesc(any(), any())).thenReturn(bookings);
        when(bookingRepository.findAllByItemIdInAndStartIsAfter(any(), any(), any())).thenReturn(new PageImpl<>(bookings));
        when(itemService.findAllItemsOfUser(any())).thenReturn(items);

        List<Booking> result = bookingService.findAllBookingsOfOwnerItems(3, "FUTURE", null, null);
        List<Booking> result2 = bookingService.findAllBookingsOfOwnerItems(3, "FUTURE", 0, 100);

        assertEquals(result.size(), result2.size());
        assertTrue(result.containsAll(result2) && result2.containsAll(result));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByItemIdInAndStartIsAfterOrderByEndDesc(any(), any());
        verify(bookingRepository, times(1)).findAllByItemIdInAndStartIsAfter(any(), any(), any());
    }

    @Test
    void testFindWaitingBookingsOfOwnerItemsOk() {
        List<Booking> bookings = prepareBookingTest();
        List<Item> items = new ArrayList<>();
        items.add(item);

        when(bookingRepository.findAllByItemIdInAndStatusOrderByEndDesc(any(), any())).thenReturn(bookings);
        when(bookingRepository.findAllByItemIdInAndStatus(any(), any(), any())).thenReturn(new PageImpl<>(bookings));
        when(itemService.findAllItemsOfUser(any())).thenReturn(items);

        List<Booking> result = bookingService.findAllBookingsOfOwnerItems(3, "WAITING", null, null);
        List<Booking> result2 = bookingService.findAllBookingsOfOwnerItems(3, "WAITING", 0, 100);

        assertEquals(result.size(), result2.size());
        assertTrue(result.containsAll(result2) && result2.containsAll(result));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByItemIdInAndStatusOrderByEndDesc(any(), any());
        verify(bookingRepository, times(1)).findAllByItemIdInAndStatus(any(), any(), any());
    }

    @Test
    void testFindRejectedBookingsOfOwnerItemsOk() {
        List<Booking> bookings = prepareBookingTest();
        List<Item> items = new ArrayList<>();
        items.add(item);

        when(bookingRepository.findAllByItemIdInAndStatusOrderByEndDesc(any(), any())).thenReturn(bookings);
        when(bookingRepository.findAllByItemIdInAndStatus(any(), any(), any())).thenReturn(new PageImpl<>(bookings));
        when(itemService.findAllItemsOfUser(any())).thenReturn(items);

        List<Booking> result = bookingService.findAllBookingsOfOwnerItems(3, "REJECTED", null, null);
        List<Booking> result2 = bookingService.findAllBookingsOfOwnerItems(3, "REJECTED", 0, 100);

        assertEquals(result.size(), result2.size());
        assertTrue(result.containsAll(result2) && result2.containsAll(result));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByItemIdInAndStatusOrderByEndDesc(any(), any());
        verify(bookingRepository, times(1)).findAllByItemIdInAndStatus(any(), any(), any());
    }

    @Test
    void testFindPastBookingsOfOwnerItemsOk() {
        List<Booking> bookings = prepareBookingTest();
        List<Item> items = new ArrayList<>();
        items.add(item);

        when(bookingRepository.findAllByItemIdInAndEndIsBeforeOrderByEndDesc(any(), any())).thenReturn(bookings);
        when(bookingRepository.findAllByItemIdInAndEndIsBefore(any(), any(), any())).thenReturn(new PageImpl<>(bookings));
        when(itemService.findAllItemsOfUser(any())).thenReturn(items);

        List<Booking> result = bookingService.findAllBookingsOfOwnerItems(3, "PAST", null, null);
        List<Booking> result2 = bookingService.findAllBookingsOfOwnerItems(3, "PAST", 0, 100);

        assertEquals(result.size(), result2.size());
        assertTrue(result.containsAll(result2) && result2.containsAll(result));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bookings, result);
        verify(bookingRepository, times(1)).findAllByItemIdInAndEndIsBeforeOrderByEndDesc(any(), any());
        verify(bookingRepository, times(1)).findAllByItemIdInAndEndIsBefore(any(), any(), any());
    }

    @Test
    void testFindAllBookingsOfOwnerItemsFailWithoutUserItems() {
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
