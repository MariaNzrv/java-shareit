package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemMapper {
    public static ItemDto toDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable()
        );
    }

    public static List<ItemDto> toDto(List<Item> items) {
        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            itemDtos.add(toDto(item));
        }
        return itemDtos;
    }

    public static Item toItem(ItemDto itemDto) {
        return new Item(
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable());
    }

    public static ItemWithBookingDto toItemWithBookingDto(Item item, Booking lastBooking, Booking nextBooking,
                                                          List<Comment> comments) {
        ItemWithBookingDto itemWithBookingDto = new ItemWithBookingDto();
        itemWithBookingDto.setId(item.getId());
        itemWithBookingDto.setName(item.getName());
        itemWithBookingDto.setDescription(item.getDescription());
        itemWithBookingDto.setAvailable(item.getAvailable());
        if (lastBooking != null) {
            itemWithBookingDto.setLastBooking(BookingMapper.toItemDto(lastBooking));
        }
        if (nextBooking != null) {
            itemWithBookingDto.setNextBooking(BookingMapper.toItemDto(nextBooking));
        }
        if (comments != null) {
            itemWithBookingDto.setComments(CommentMapper.toDto(comments));
        }
        return itemWithBookingDto;
    }

}
