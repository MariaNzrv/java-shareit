package ru.practicum.shareit.booking.model;

import lombok.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private Integer id;
    private LocalDateTime start;
    private LocalDateTime end;
    @NonNull
    private Item item;
    @NonNull
    private User booker;
    private BookingState status;
}
