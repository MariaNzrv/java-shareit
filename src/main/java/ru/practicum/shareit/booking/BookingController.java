package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDtoResponse create(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                     @RequestBody BookingDto bookingDto) {
        Booking savedBooking = bookingService.createBooking(userId, bookingDto);
        return BookingMapper.toDto(savedBooking);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoResponse update(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                     @PathVariable Integer bookingId,
                                     @RequestParam("approved") Boolean isApproved) {
        Booking savedBooking = bookingService.updateBooking(userId, bookingId, isApproved);
        return BookingMapper.toDto(savedBooking);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoResponse findById(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                       @PathVariable Integer bookingId) {
        Booking booking = bookingService.findBookingById(userId, bookingId);
        return BookingMapper.toDto(booking);
    }

    @GetMapping
    public List<BookingDtoResponse> findAllBookingsOfUser(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                                          @RequestParam(name = "state",
                                                                  required = false,
                                                                  defaultValue = "ALL") String state,
                                                          @RequestParam(required = false, name = "from") Integer from,
                                                          @RequestParam(required = false, name = "size") Integer size) {
        return BookingMapper.toDto(bookingService.findAllBookingsOfUser(userId, state, from, size));
    }

    @GetMapping("/owner")
    public List<BookingDtoResponse> findAllBookingsOfOwnerItems(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                                                @RequestParam(name = "state",
                                                                        required = false,
                                                                        defaultValue = "ALL") String state,
                                                                @RequestParam(required = false, name = "from") Integer from,
                                                                @RequestParam(required = false, name = "size") Integer size) {
        return BookingMapper.toDto(bookingService.findAllBookingsOfOwnerItems(userId, state, from, size));
    }


}
