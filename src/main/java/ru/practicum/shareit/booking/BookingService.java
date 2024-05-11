package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingSearchState;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exception.IncorrectStateException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final ItemService itemService;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    public Booking createBooking(Integer userId, BookingDto bookingDto) {
        validateRequiredFields(bookingDto);
        User user = userService.findUserById(userId);
        Item item = itemService.findById(bookingDto.getItemId());
        validateFields(bookingDto, item, user);

        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingState.WAITING);
        return bookingRepository.save(booking);
    }

    public Booking updateBooking(Integer userId, Integer bookingId, Boolean isApproved) {
        Booking booking = findById(bookingId);
        if (booking == null) {
            log.error("Бронирования с Id = {} не существует", bookingId);
            throw new RuntimeException("Бронирования с таким Id не существует");
        }
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.error("Подтверждение или отклонение запроса на бронирование " +
                    "может быть выполнено только владельцем вещи");
            throw new RuntimeException("Подтверждение или отклонение запроса на бронирование " +
                    "может быть выполнено только владельцем вещи");
        }
        if (!booking.getStatus().equals(BookingState.WAITING)) {
            log.error("Запрос на бронирования был обработан ранее");
            throw new ValidationException("Запрос на бронирования был обработан ранее");
        }

        if (isApproved) {
            booking.setStatus(BookingState.APPROVED);
        } else {
            booking.setStatus(BookingState.REJECTED);
        }

        return bookingRepository.save(booking);
    }

    public Booking findById(Integer bookingId) {
        if (bookingId == null) {
            log.error("Id бронирования не заполнен");
            throw new ValidationException("Id бронирования не заполнен");
        }
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            return null;
        }
        return booking.get();
    }

    public Booking findBookingById(Integer userId, Integer bookingId) {
        Booking booking = findById(bookingId);
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            log.error("Нет прав для просмотра информации");
            throw new RuntimeException("Нет прав для просмотра информации");
        }
        return booking;
    }

    public List<Booking> findAllBookingsOfUser(Integer userId, String state) {
        userService.findUserById(userId);
        BookingSearchState bookingSearchState;
        try {
            bookingSearchState = BookingSearchState.valueOf(state);
        } catch (IllegalArgumentException e) {
            log.error("Unknown state: " + state);
            throw new IncorrectStateException("Unknown state: " + state);
        }
        switch (bookingSearchState) {
            case ALL:
                return bookingRepository.findAllByBookerIdOrderByEndDesc(userId);
            case CURRENT:
                return bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(userId,
                        LocalDateTime.now(), LocalDateTime.now());
            case PAST:
                return bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByEndDesc(userId, LocalDateTime.now());
            case FUTURE:
                return bookingRepository.findAllByBookerIdAndStartIsAfterOrderByEndDesc(userId, LocalDateTime.now());
            case WAITING:
                return bookingRepository.findAllByBookerIdAndStatusOrderByEndDesc(userId, BookingState.WAITING);
            case REJECTED:
                return bookingRepository.findAllByBookerIdAndStatusOrderByEndDesc(userId, BookingState.REJECTED);
            default:
                log.error("Unknown state: " + state);
                throw new IncorrectStateException("Unknown state: " + state);
        }
    }

    public List<Booking> findAllBookingsOfOwnerItems(Integer userId, String state) {
        List<Item> items = itemService.findAllItemsOfUser(userId);
        if (items.isEmpty()) {
            log.error("У пользователя нет вещей");
            throw new ValidationException("У пользователя нет вещей");
        }
        Set<Integer> itemsIds = new HashSet<>();
        for (Item item : items) {
            itemsIds.add(item.getId());
        }
        BookingSearchState bookingSearchState;
        try {
            bookingSearchState = BookingSearchState.valueOf(state);
        } catch (IllegalArgumentException e) {
            log.error("Unknown state: " + state);
            throw new IncorrectStateException("Unknown state: " + state);
        }
        switch (bookingSearchState) {
            case ALL:
                return bookingRepository.findAllByItemIdInOrderByEndDesc(itemsIds);
            case CURRENT:
                return bookingRepository.findAllByItemIdInAndStartIsBeforeAndEndIsAfterOrderByEndDesc(itemsIds,
                        LocalDateTime.now(), LocalDateTime.now());
            case PAST:
                return bookingRepository.findAllByItemIdInAndEndIsBeforeOrderByEndDesc(itemsIds, LocalDateTime.now());
            case FUTURE:
                return bookingRepository.findAllByItemIdInAndStartIsAfterOrderByEndDesc(itemsIds, LocalDateTime.now());
            case WAITING:
                return bookingRepository.findAllByItemIdInAndStatusOrderByEndDesc(itemsIds, BookingState.WAITING);
            case REJECTED:
                return bookingRepository.findAllByItemIdInAndStatusOrderByEndDesc(itemsIds, BookingState.REJECTED);
            default:
                log.error("Unknown state: " + state);
                throw new IncorrectStateException("Unknown state: " + state);
        }
    }

    private void validateFields(BookingDto bookingDto, Item item, User user) {
        if (bookingDto.getStart().equals(bookingDto.getEnd()) ||
                bookingDto.getStart().isAfter(bookingDto.getEnd()) ||
                bookingDto.getEnd().isBefore(LocalDateTime.now()) ||
                bookingDto.getStart().isBefore(LocalDateTime.now())) {
            log.warn("Неверно заполнены поля начала/окончания бронирования");
            throw new ValidationException("Неверно заполнены поля начала/окончания бронирования");
        }

        if (!item.getAvailable()) {
            log.warn("Вещь недоступна для бронирования");
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(user.getId())) {
            log.warn("Нельзя забронировать вещь, которая принадлежит вам");
            throw new RuntimeException("Нельзя забронировать вещь, которая принадлежит вам");
        }
    }

    private void validateRequiredFields(BookingDto bookingDto) {
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null || bookingDto.getItemId() == null) {
            log.warn("Не заполнены обязательные поля");
            throw new ValidationException("Не заполнены обязательные поля");
        }
    }
}
