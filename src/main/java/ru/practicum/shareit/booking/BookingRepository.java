package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findAllByBookerIdOrderByEndDesc(Integer userId);

    Page<Booking> findAllByBookerId(Integer userId, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(Integer userId,
                                                                               LocalDateTime dateTimeStart,
                                                                               LocalDateTime dateTimeEnd);

    Page<Booking> findAllByBookerIdAndStartIsBeforeAndEndIsAfter(Integer userId,
                                                                 LocalDateTime dateTimeStart,
                                                                 LocalDateTime dateTimeEnd,
                                                                 Pageable pageable);

    List<Booking> findAllByBookerIdAndEndIsBeforeOrderByEndDesc(Integer userId, LocalDateTime dateTime);

    Page<Booking> findAllByBookerIdAndEndIsBefore(Integer userId, LocalDateTime dateTime, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartIsAfterOrderByEndDesc(Integer userId, LocalDateTime dateTime);

    Page<Booking> findAllByBookerIdAndStartIsAfter(Integer userId, LocalDateTime dateTime, Pageable pageable);

    List<Booking> findAllByBookerIdAndStatusOrderByEndDesc(Integer userId, BookingState state);

    Page<Booking> findAllByBookerIdAndStatus(Integer userId, BookingState state, Pageable pageable);

    List<Booking> findAllByItemIdInOrderByEndDesc(Set<Integer> itemsIds);

    Page<Booking> findAllByItemIdIn(Set<Integer> itemsIds, Pageable pageable);

    List<Booking> findAllByItemIdInAndStartIsBeforeAndEndIsAfterOrderByEndDesc(Set<Integer> itemsIds,
                                                                               LocalDateTime dateTimeStart,
                                                                               LocalDateTime dateTimeEnd);

    Page<Booking> findAllByItemIdInAndStartIsBeforeAndEndIsAfter(Set<Integer> itemsIds,
                                                                 LocalDateTime dateTimeStart,
                                                                 LocalDateTime dateTimeEnd, Pageable pageable);

    List<Booking> findAllByItemIdInAndEndIsBeforeOrderByEndDesc(Set<Integer> itemsIds, LocalDateTime dateTime);

    Page<Booking> findAllByItemIdInAndEndIsBefore(Set<Integer> itemsIds, LocalDateTime dateTime, Pageable pageable);

    List<Booking> findAllByItemIdInAndStartIsAfterOrderByEndDesc(Set<Integer> itemsIds, LocalDateTime dateTime);

    Page<Booking> findAllByItemIdInAndStartIsAfter(Set<Integer> itemsIds, LocalDateTime dateTime, Pageable pageable);

    List<Booking> findAllByItemIdInAndStatusOrderByEndDesc(Set<Integer> itemsIds, BookingState state);

    Page<Booking> findAllByItemIdInAndStatus(Set<Integer> itemsIds, BookingState state, Pageable pageable);

    Booking findFirst1ByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(Integer itemId, LocalDateTime dateTime,
                                                                        BookingState state);

    Booking findFirst1ByItemIdAndStartIsAfterAndStatusOrderByStartAsc(Integer itemId, LocalDateTime dateTime,
                                                                      BookingState state);

    Booking findFirst1ByBookerIdAndItemIdAndEndIsBefore(Integer userId, Integer itemId, LocalDateTime dateTime);
}
