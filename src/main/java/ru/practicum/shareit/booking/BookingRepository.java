package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findAllByBookerIdOrderByEndDesc(Integer userId);

    List<Booking> findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(Integer userId,
                                                                               LocalDateTime dateTimeStart,
                                                                               LocalDateTime dateTimeEnd);

    List<Booking> findAllByBookerIdAndEndIsBeforeOrderByEndDesc(Integer userId, LocalDateTime dateTime);

    List<Booking> findAllByBookerIdAndStartIsAfterOrderByEndDesc(Integer userId, LocalDateTime dateTime);

    List<Booking> findAllByBookerIdAndStatusOrderByEndDesc(Integer userId, BookingState state);

    List<Booking> findAllByItemIdInOrderByEndDesc(Set<Integer> itemsIds);

    List<Booking> findAllByItemIdInAndStartIsBeforeAndEndIsAfterOrderByEndDesc(Set<Integer> itemsIds,
                                                                               LocalDateTime dateTimeStart,
                                                                               LocalDateTime dateTimeEnd);

    List<Booking> findAllByItemIdInAndEndIsBeforeOrderByEndDesc(Set<Integer> itemsIds, LocalDateTime dateTime);

    List<Booking> findAllByItemIdInAndStartIsAfterOrderByEndDesc(Set<Integer> itemsIds, LocalDateTime dateTime);

    List<Booking> findAllByItemIdInAndStatusOrderByEndDesc(Set<Integer> itemsIds, BookingState state);

    Booking findFirst1ByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(Integer itemId, LocalDateTime dateTime,
                                                                        BookingState state);

    Booking findFirst1ByItemIdAndStartIsAfterAndStatusOrderByStartAsc(Integer itemId, LocalDateTime dateTime,
                                                                      BookingState state);

    Booking findFirst1ByBookerIdAndItemIdAndEndIsBefore(Integer userId, Integer itemId, LocalDateTime dateTime);
}
