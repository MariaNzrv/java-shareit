package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    public Item createItem(Integer userId, ItemDto itemDto) {
        validateRequiredFields(itemDto);
        validateFieldsFormat(itemDto);

        User user = getUserById(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = getRequestById(itemDto.getRequestId());
            item.setRequest(itemRequest);
        }
        return itemRepository.save(item);
    }

    public Item updateItem(Integer userId, Integer itemId, ItemDto itemDto) {
        if (itemId == null) {
            log.error("Id вещи не заполнен");
            throw new ValidationException("Для обновления данных надо указать Id вещи");
        }
        Item item = findById(itemId);
        if (!item.getOwner().getId().equals(userId)) {
            log.error("Невозможно обновить информацию по вещи, принадлежащей другому пользователю");
            throw new AccessDeniedException("Невозможно обновить информацию по вещи, принадлежащей другому пользователю");
        }
        validateFieldsFormat(itemDto);
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = getRequestById(itemDto.getRequestId());
            item.setRequest(itemRequest);
        }
        return itemRepository.save(item);
    }

    public Item findById(Integer itemId) {
        if (itemId == null) {
            log.error("Id вещи не заполнен");
            throw new ValidationException("Для получения данных надо указать Id вещи");
        }

        return itemRepository.findById(itemId).orElseThrow(() -> {
            log.error("Вещи с Id = {} не существует", itemId);
            throw new EntityNotFoundException("Вещи с таким Id не существует");
        });
    }

    public ItemWithBookingDto findItemWithBookingById(Integer userId, Integer itemId) {
        getUserById(userId);
        Item item = findById(itemId);
        List<Comment> comments = commentRepository.findAllByItemId(itemId);

        if (item.getOwner().getId().equals(userId)) {
            Booking lastBooking = bookingRepository.findFirst1ByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(itemId,
                    LocalDateTime.now(), BookingState.APPROVED);

            Booking nextBooking = bookingRepository.findFirst1ByItemIdAndStartIsAfterAndStatusOrderByStartAsc(itemId,
                    LocalDateTime.now(), BookingState.APPROVED);
            return ItemMapper.toItemWithBookingDto(item, lastBooking, nextBooking, comments);
        } else {
            return ItemMapper.toItemWithBookingDto(item, null, null, comments);
        }

    }

    public List<Item> findAllItemsOfUser(Integer userId) {
        getUserById(userId);
        return itemRepository.findAllByOwnerId(userId);
    }

    public List<Item> findAllItemsByRequest(Integer requestId) {
        return itemRepository.findAllByRequestId(requestId);
    }

    public List<ItemWithBookingDto> findAllItemsWithBooking(Integer userId, Integer from, Integer size) {
        List<Item> items;
        getUserById(userId);
        if (from < 0 || size <= 0) {
            log.error("Некорректные значения параметров from = {}, size={}", from, size);
            throw new ValidationException("Некорректные значения параметров from/size");
        }
        Pageable page = PageRequest.of(from / size, size);
        items = itemRepository.findAllByOwnerId(userId, page).getContent();
        List<ItemWithBookingDto> itemsWithBookings = new ArrayList<>();

        for (Item item : items) {
            Booking lastBooking = bookingRepository.findFirst1ByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(item.getId(),
                    LocalDateTime.now(), BookingState.APPROVED);

            Booking nextBooking = bookingRepository.findFirst1ByItemIdAndStartIsAfterAndStatusOrderByStartAsc(item.getId(),
                    LocalDateTime.now(), BookingState.APPROVED);

            List<Comment> comments = commentRepository.findAllByItemId(item.getId());

            itemsWithBookings.add(ItemMapper.toItemWithBookingDto(item, lastBooking, nextBooking, comments));
        }

        return itemsWithBookings;
    }

    public List<Item> searchItem(String text, Integer from, Integer size) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        if (from < 0 || size <= 0) {
            log.error("Некорректные значения параметров from = {}, size={}", from, size);
            throw new ValidationException("Некорректные значения параметров from/size");
        }
        Pageable page = PageRequest.of(from / size, size);
        return itemRepository.findAllByNameOrDescriptionContainingIgnoreCaseAndAvailable(text, text, Boolean.TRUE, page).getContent();
    }

    public Comment createComment(Integer userId, Integer itemId, CommentDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().isEmpty() || commentDto.getText().isBlank()) {
            log.warn("Отсутствует текст комментария");
            throw new ValidationException("Отсутствует текст комментария");
        }
        User user = getUserById(userId);
        Item item = findById(itemId);
        Booking booking = bookingRepository.findFirst1ByBookerIdAndItemIdAndEndIsBefore(userId, itemId,
                LocalDateTime.now());
        if (booking == null) {
            log.warn("Вещь не была арендована");
            throw new ValidationException("Вещь не была арендована");
        }

        Comment comment = CommentMapper.toComment(commentDto);
        comment.setCreated(LocalDateTime.now());
        comment.setAuthor(user);
        comment.setItem(item);

        return commentRepository.save(comment);
    }

    private void validateFieldsFormat(ItemDto itemDto) {
        if (itemDto.getName() != null && (itemDto.getName().isBlank() || itemDto.getName().isEmpty())) {
            log.warn("Название обязательно для заполнения");
            throw new ValidationException("Название обязательно для заполнения");
        }
        if (itemDto.getDescription() != null && (itemDto.getDescription().isEmpty() || itemDto.getDescription().isBlank())) {
            log.warn("Описание обязательно для заполнения");
            throw new ValidationException("Описание обязательно для заполнения");
        }
    }

    private void validateRequiredFields(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getDescription() == null || itemDto.getAvailable() == null) {
            log.warn("Не заполнены обязательные поля");
            throw new ValidationException("Не заполнены обязательные поля");
        }
    }

    private User getUserById(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Пользователя с Id = {} не существует", userId);
            throw new EntityNotFoundException("Пользователя с Id = " + userId + " не существует");
        });
    }

    private ItemRequest getRequestById(Integer requestId) {
        return itemRequestRepository.findById(requestId).orElseThrow(() -> {
            log.warn("Запрос с Id = {} не существует", requestId);
            throw new EntityNotFoundException("Запрос с Id = " + requestId + " не существует");
        });
    }
}
