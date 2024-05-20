package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

import java.util.ArrayList;
import java.util.List;

public class BookingMapper {
    public static BookingDtoResponse toDto(Booking booking) {
        return new BookingDtoResponse(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                ItemMapper.toDto(booking.getItem()),
                UserMapper.toDto(booking.getBooker()),
                booking.getStatus()
        );
    }

    public static List<BookingDtoResponse> toDto(List<Booking> bookings) {
        List<BookingDtoResponse> bookingDtos = new ArrayList<>();
        for (Booking booking : bookings) {
            bookingDtos.add(toDto(booking));
        }
        return bookingDtos;
    }

    public static Booking toBooking(BookingDto bookingDto) {
        return new Booking(bookingDto.getStart(), bookingDto.getEnd());
    }

    public static BookingDtoForItem toItemDto(Booking booking) {
        return new BookingDtoForItem(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getBooker().getId(),
                booking.getStatus()
        );
    }
}
