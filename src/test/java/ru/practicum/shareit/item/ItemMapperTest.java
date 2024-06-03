package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemMapperTest {

    private User user;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void beforeEach() {
        user = new User(1, "user@owner.ru", "owner");
        ItemRequest request = new ItemRequest(1, "description", null, null);
        item = new Item(1, "name", "description", true, user, request);
        itemDto = new ItemDto(1, "name", "description", true, 1);
    }

    @Test
    void toDtoOk() {
        ItemDto itemDtoActual = ItemMapper.toDto(item);
        assertEquals(itemDto, itemDtoActual);
    }

    @Test
    void toDtoListOk() {
        List<ItemDto> itemDtoListActual = ItemMapper.toDto(Arrays.asList(item, item));
        assertEquals(2, itemDtoListActual.size());
        assertEquals(itemDto, itemDtoListActual.get(0));
        assertEquals(itemDto, itemDtoListActual.get(1));
    }

    @Test
    void toItemOk() {
        Item itemActual = ItemMapper.toItem(itemDto);
        item.setId(null);
        item.setOwner(null);
        item.setRequest(null);
        assertEquals(item, itemActual);
    }

    @Test
    void toItemWithBookingOk() {
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = from.plusMinutes(10);
        Booking booking = new Booking(1, from, to, item, user, BookingState.APPROVED);
        BookingDtoForItem bookingDto = new BookingDtoForItem(item.getId(), from, to, user.getId(), BookingState.APPROVED);

        Comment comment = new Comment(1, "comment", user, item, from);
        CommentDto commentDto = new CommentDto(1, "comment", "owner", from);

        ItemWithBookingDto itemWithBookingDto = ItemMapper.toItemWithBookingDto(item, booking, booking, Collections.singletonList(comment));
        assertEquals(item.getName(), itemWithBookingDto.getName());
        assertEquals(item.getDescription(), itemWithBookingDto.getDescription());
        assertEquals(item.getAvailable(), itemWithBookingDto.getAvailable());
        assertEquals(1, itemWithBookingDto.getComments().size());
        assertEquals(commentDto, itemWithBookingDto.getComments().get(0));
        assertEquals(bookingDto, itemWithBookingDto.getLastBooking());
        assertEquals(bookingDto, itemWithBookingDto.getNextBooking());
    }

}
