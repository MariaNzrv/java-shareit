package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ItemServiceTest {

    private ItemService itemService;
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private BookingRepository bookingRepository;
    private CommentRepository commentRepository;
    private ItemRequestRepository itemRequestRepository;
    private Item item;
    private User userOwner;
    private User userAsker;
    private ItemRequest request;
    private ItemDto itemDto;
    private Booking oldBooking;
    private Booking newBooking;
    private Comment comment;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);
        bookingRepository = mock(BookingRepository.class);
        commentRepository = mock(CommentRepository.class);
        itemRequestRepository = mock(ItemRequestRepository.class);
        itemService = new ItemService(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
        LocalDateTime start = LocalDateTime.of(2024, Month.MAY, 29, 12, 0);
        userOwner = new User(1, "user@owner.ru", "owner");
        userAsker = new User(2, "user@asker.ru", "asker");
        request = new ItemRequest(1, "description1", userAsker, start);
        item = new Item(1, "name1", "description1", true, userOwner, request);
        comment = new Comment(1, "comment1", userAsker, item, start.plusMinutes(1));
        oldBooking = new Booking(2, start.plusMinutes(11), start.plusMinutes(20), item, userAsker, BookingState.APPROVED);
        newBooking = new Booking(3, start.plusMinutes(21), start.plusMinutes(30), item, userAsker, BookingState.APPROVED);

        itemDto = new ItemDto();
        itemDto.setName("name2");
        itemDto.setDescription("description2");
        itemDto.setAvailable(true);
        itemDto.setRequestId(request.getId());

        Mockito.doReturn(Optional.of(item)).when(itemRepository).findById(item.getId());
        Mockito.doReturn(Collections.singletonList(item)).when(itemRepository).findAllByOwnerId(userOwner.getId());
        Mockito.doReturn(Collections.singletonList(item)).when(itemRepository).findAllByRequestId(request.getId());
        Mockito.doReturn(Collections.singletonList(item)).when(itemRepository).findAllByNameOrDescriptionContainingIgnoreCaseAndAvailable(any(), any(), any());
        Mockito.doReturn(Collections.singletonList(comment)).when(commentRepository).findAllByItemId(item.getId());
        Mockito.doReturn(Optional.of(userOwner)).when(userRepository).findById(userOwner.getId());
        Mockito.doReturn(Optional.of(userAsker)).when(userRepository).findById(userAsker.getId());
        Mockito.doReturn(Optional.of(request)).when(itemRequestRepository).findById(request.getId());
        Mockito.doAnswer(invocationOnMock -> invocationOnMock.getArgument(0)).when(itemRepository).save(any());
        Mockito.doAnswer(invocationOnMock -> invocationOnMock.getArgument(0)).when(commentRepository).save(any());
        Mockito.doReturn(oldBooking).when(bookingRepository).findFirst1ByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(eq(item.getId()), any(), eq(BookingState.APPROVED));
        Mockito.doReturn(newBooking).when(bookingRepository).findFirst1ByItemIdAndStartIsAfterAndStatusOrderByStartAsc(eq(item.getId()), any(), eq(BookingState.APPROVED));
        Mockito.doReturn(newBooking).when(bookingRepository).findFirst1ByBookerIdAndItemIdAndEndIsBefore(eq(userAsker.getId()), eq(item.getId()), any());
    }

    @Test
    void testCreateItemOk() {
        Item createdItem = itemService.createItem(userOwner.getId(), itemDto);

        assertEquals(itemDto.getName(), createdItem.getName());
        assertEquals(itemDto.getDescription(), createdItem.getDescription());
        assertEquals(itemDto.getAvailable(), createdItem.getAvailable());
        assertEquals(request.getId(), createdItem.getRequest().getId());
        assertEquals(userOwner.getId(), createdItem.getOwner().getId());

        verify(userRepository, times(1)).findById(any());
        verify(itemRequestRepository, times(1)).findById(any());
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void testCreateItemUserNotFound() {
        assertThrows(EntityNotFoundException.class, () -> itemService.createItem(404, itemDto));

        verify(userRepository, times(1)).findById(any());
    }

    @Test
    void testCreateItemFieldsEmptyNotFound() {
        assertThrows(ValidationException.class, () -> itemService.createItem(404, new ItemDto()));
        itemDto.setName("");
        assertThrows(ValidationException.class, () -> itemService.createItem(404, itemDto));
    }

    @Test
    void testUpdateItemOk() {
        Item item = itemService.updateItem(userOwner.getId(), this.item.getId(), itemDto);

        assertEquals(itemDto.getName(), item.getName());
        assertEquals(itemDto.getDescription(), item.getDescription());
        assertEquals(itemDto.getAvailable(), item.getAvailable());
        assertEquals(request.getId(), item.getRequest().getId());
        assertEquals(userOwner.getId(), item.getOwner().getId());
        verify(itemRequestRepository, times(1)).findById(any());
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void testUpdateItemNotExists() {
        assertThrows(EntityNotFoundException.class, () -> itemService.updateItem(404, 404, new ItemDto()));
        verify(itemRepository, times(1)).findById(any());
        verify(itemRepository, times(0)).save(any());
    }

    @Test
    void testUpdateItemValidations() {
        assertThrows(AccessDeniedException.class, () -> itemService.updateItem(userAsker.getId(), item.getId(), itemDto));
        assertThrows(ValidationException.class, () -> itemService.updateItem(userOwner.getId(), null, itemDto));
        itemDto.setName("");
        assertThrows(ValidationException.class, () -> itemService.updateItem(userOwner.getId(), item.getId(), itemDto));
        verify(itemRepository, times(0)).save(any());
    }

    @Test
    void testFindByIdOk() {
        Item byId = itemService.findById(1);
        assertEquals(item, byId);
        verify(itemRepository, times(1)).findById(any());
    }

    @Test
    void testFindAllOk() {
        List<Item> list = itemService.findAllItemsOfUser(userOwner.getId());
        assertEquals(1, list.size());
        assertEquals(item, list.get(0));
        verify(itemRepository, times(1)).findAllByOwnerId(any());
    }

    @Test
    void testFindByRequestOk() {
        List<Item> list = itemService.findAllItemsByRequest(request.getId());
        assertEquals(1, list.size());
        assertEquals(item, list.get(0));
        verify(itemRepository, times(1)).findAllByRequestId(any());
    }

    @Test
    void testFindByIdNotFound() {
        assertThrows(EntityNotFoundException.class, () -> itemService.findById(404));
        verify(itemRepository, times(1)).findById(any());
    }

    @Test
    void testFindOwnItemWithBooking() {
        ItemWithBookingDto itemWithBooking = itemService.findItemWithBookingById(userOwner.getId(), item.getId());
        verify(itemRepository, times(1)).findById(any());
        verify(commentRepository, times(1)).findAllByItemId(any());
        verify(bookingRepository, times(1)).findFirst1ByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(any(), any(), any());
        verify(bookingRepository, times(1)).findFirst1ByItemIdAndStartIsAfterAndStatusOrderByStartAsc(any(), any(), any());

        assertEquals(item.getId(), itemWithBooking.getId());
        assertEquals(item.getName(), itemWithBooking.getName());
        assertEquals(item.getDescription(), itemWithBooking.getDescription());
        assertEquals(item.getAvailable(), itemWithBooking.getAvailable());
        assertEquals(oldBooking.getId(), itemWithBooking.getLastBooking().getId());
        assertEquals(newBooking.getId(), itemWithBooking.getNextBooking().getId());
        assertEquals(1, itemWithBooking.getComments().size());
        assertEquals(comment.getId(), itemWithBooking.getComments().get(0).getId());
    }

    @Test
    void testFindForeignItemWithBooking() {
        ItemWithBookingDto itemWithBooking = itemService.findItemWithBookingById(userAsker.getId(), item.getId());
        verify(itemRepository, times(1)).findById(any());
        verify(commentRepository, times(1)).findAllByItemId(any());
        verify(bookingRepository, times(0)).findFirst1ByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(any(), any(), any());
        verify(bookingRepository, times(0)).findFirst1ByItemIdAndStartIsAfterAndStatusOrderByStartAsc(any(), any(), any());

        assertEquals(item.getId(), itemWithBooking.getId());
        assertEquals(item.getName(), itemWithBooking.getName());
        assertEquals(item.getDescription(), itemWithBooking.getDescription());
        assertEquals(item.getAvailable(), itemWithBooking.getAvailable());
        assertNull(itemWithBooking.getLastBooking());
        assertNull(itemWithBooking.getNextBooking());
        assertEquals(1, itemWithBooking.getComments().size());
        assertEquals(comment.getId(), itemWithBooking.getComments().get(0).getId());
    }

    @Test
    void testFindAllItemsWithBooking() {
        List<ItemWithBookingDto> list = itemService.findAllItemsWithBooking(userOwner.getId(), null, null);
        verify(itemRepository, times(1)).findAllByOwnerId(any());
        verify(commentRepository, times(1)).findAllByItemId(any());
        verify(bookingRepository, times(1)).findFirst1ByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(any(), any(), any());
        verify(bookingRepository, times(1)).findFirst1ByItemIdAndStartIsAfterAndStatusOrderByStartAsc(any(), any(), any());

        assertEquals(1, list.size());
        ItemWithBookingDto itemWithBooking = list.get(0);
        assertEquals(item.getId(), itemWithBooking.getId());
        assertEquals(item.getName(), itemWithBooking.getName());
        assertEquals(item.getDescription(), itemWithBooking.getDescription());
        assertEquals(item.getAvailable(), itemWithBooking.getAvailable());
        assertEquals(oldBooking.getId(), itemWithBooking.getLastBooking().getId());
        assertEquals(newBooking.getId(), itemWithBooking.getNextBooking().getId());
        assertEquals(1, itemWithBooking.getComments().size());
        assertEquals(comment.getId(), itemWithBooking.getComments().get(0).getId());
    }

    @Test
    void testSearchItem() {
        List<Item> list = itemService.searchItem("name", null, null);
        verify(itemRepository, times(1)).findAllByNameOrDescriptionContainingIgnoreCaseAndAvailable(any(), any(), any());
        assertEquals(1, list.size());
        assertEquals(item, list.get(0));
    }

    @Test
    void testSearchItemSkipped() {
        List<Item> list = itemService.searchItem("", null, null);
        verify(itemRepository, times(0)).findAllByNameOrDescriptionContainingIgnoreCaseAndAvailable(any(), any(), any());
        assertEquals(0, list.size());
        list = itemService.searchItem(null, null, null);
        verify(itemRepository, times(0)).findAllByNameOrDescriptionContainingIgnoreCaseAndAvailable(any(), any(), any());
        assertEquals(0, list.size());
        list = itemService.searchItem(" ", null, null);
        verify(itemRepository, times(0)).findAllByNameOrDescriptionContainingIgnoreCaseAndAvailable(any(), any(), any());
        assertEquals(0, list.size());
    }

    @Test
    void testCreateCommentOk() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("text");
        Comment comment = itemService.createComment(userAsker.getId(), item.getId(), commentDto);

        verify(userRepository, times(1)).findById(userAsker.getId());
        verify(itemRepository, times(1)).findById(item.getId());
        verify(bookingRepository, times(1)).findFirst1ByBookerIdAndItemIdAndEndIsBefore(any(), any(), any());
        verify(commentRepository, times(1)).save(any());
        assertEquals(commentDto.getText(), comment.getText());
    }

    @Test
    void testCreateCommentValidations() {
        CommentDto commentDto = new CommentDto();
        assertThrows(ValidationException.class, () -> itemService.createComment(404, 404, commentDto));
        commentDto.setText("text");
        assertThrows(EntityNotFoundException.class, () -> itemService.createComment(404, 404, commentDto));
        assertThrows(EntityNotFoundException.class, () -> itemService.createComment(userAsker.getId(), 404, commentDto));
        assertThrows(ValidationException.class, () -> itemService.createComment(userOwner.getId(), item.getId(), commentDto));
    }
}
