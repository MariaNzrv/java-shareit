package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingSearchState;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.IncorrectStateException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
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
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.error("Подтверждение или отклонение запроса на бронирование " +
                    "может быть выполнено только владельцем вещи");
            throw new EntityNotFoundException("Подтверждение или отклонение запроса на бронирование " +
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
        return bookingRepository.findById(bookingId).orElseThrow(() -> {
            log.error("Бронирования с Id = {} не существует", bookingId);
            throw new EntityNotFoundException("Бронирования с таким Id не существует");
        });
    }

    public Booking findBookingById(Integer userId, Integer bookingId) {
        Booking booking = findById(bookingId);
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            log.error("Нет прав для просмотра информации");
            throw new EntityNotFoundException("Нет прав для просмотра информации");
        }
        return booking;
    }

    public List<Booking> findAllBookingsOfUser(Integer userId, String state, Integer from, Integer size) {
        userService.findUserById(userId);
        BookingSearchState bookingSearchState = validateBookingSearchState(state);
        Pageable page = getPageable(from, size);

        switch (bookingSearchState) {
            case CURRENT:
                return bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfter(userId,
                        LocalDateTime.now(), LocalDateTime.now(), page).getContent();
            case PAST:
                return bookingRepository.findAllByBookerIdAndEndIsBefore(userId, LocalDateTime.now(), page).getContent();
            case FUTURE:
                return bookingRepository.findAllByBookerIdAndStartIsAfter(userId, LocalDateTime.now(), page).getContent();
            case WAITING:
                return bookingRepository.findAllByBookerIdAndStatus(userId, BookingState.WAITING, page).getContent();
            case REJECTED:
                return bookingRepository.findAllByBookerIdAndStatus(userId, BookingState.REJECTED, page).getContent();
            case ALL:
            default:
                return bookingRepository.findAllByBookerId(userId, page).getContent();
        }
    }

    private Pageable getPageable(Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            log.error("Некорректные значения параметров from = {}, size={}", from, size);
            throw new ValidationException("Некорректные значения параметров from/size");
        }

        Sort sortByEnd = Sort.by(Sort.Direction.DESC, "end");
        Pageable page = PageRequest.of(from / size, size, sortByEnd);
        return page;
    }

    public List<Booking> findAllBookingsOfOwnerItems(Integer userId, String state, Integer from, Integer size) {
        List<Item> items = itemService.findAllItemsOfUser(userId);
        if (items.isEmpty()) {
            log.error("У пользователя нет вещей");
            throw new ValidationException("У пользователя нет вещей");
        }
        Set<Integer> itemsIds = new HashSet<>();
        for (Item item : items) {
            itemsIds.add(item.getId());
        }
        BookingSearchState bookingSearchState = validateBookingSearchState(state);

        Pageable page = getPageable(from, size);

        switch (bookingSearchState) {
            case CURRENT:
                return bookingRepository.findAllByItemIdInAndStartIsBeforeAndEndIsAfter(itemsIds,
                        LocalDateTime.now(), LocalDateTime.now(), page).getContent();
            case PAST:
                return bookingRepository.findAllByItemIdInAndEndIsBefore(itemsIds, LocalDateTime.now(), page).getContent();
            case FUTURE:
                return bookingRepository.findAllByItemIdInAndStartIsAfter(itemsIds, LocalDateTime.now(), page).getContent();
            case WAITING:
                return bookingRepository.findAllByItemIdInAndStatus(itemsIds, BookingState.WAITING, page).getContent();
            case REJECTED:
                return bookingRepository.findAllByItemIdInAndStatus(itemsIds, BookingState.REJECTED, page).getContent();
            case ALL:
            default:
                return bookingRepository.findAllByItemIdIn(itemsIds, page).getContent();
        }
    }

    private BookingSearchState validateBookingSearchState(String state) {
        BookingSearchState bookingSearchState;
        try {
            bookingSearchState = BookingSearchState.valueOf(state);
        } catch (IllegalArgumentException e) {
            log.error("Unknown state: " + state);
            throw new IncorrectStateException("Unknown state: " + state);
        }
        return bookingSearchState;
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
            throw new EntityNotFoundException("Нельзя забронировать вещь, которая принадлежит вам");
        }
    }

    private void validateRequiredFields(BookingDto bookingDto) {
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null || bookingDto.getItemId() == null) {
            log.warn("Не заполнены обязательные поля");
            throw new ValidationException("Не заполнены обязательные поля");
        }
    }
}
