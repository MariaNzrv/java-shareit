package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ItemRequestServiceTest {

    private ItemRequestService itemRequestService;
    private UserService userService;
    private ItemService itemService;
    private ItemRequestRepository itemRequestRepository;
    private User user;
    private ItemRequest itemRequest;
    private ItemRequest itemRequest2;
    private Item item;

    @BeforeEach
    void beforeEach() {
        itemService = mock(ItemService.class);
        userService = mock(UserService.class);
        itemRequestRepository = mock(ItemRequestRepository.class);

        itemRequestService = new ItemRequestService(itemService, userService, itemRequestRepository);

        user = new User(100, "user@owner.ru", "owner");
        itemRequest = new ItemRequest(200, "description1", user, LocalDateTime.of(2024, Month.MAY, 29, 12, 0));
        itemRequest2 = new ItemRequest(201, "description2", user, LocalDateTime.of(2024, Month.MAY, 29, 13, 30));
        item = new Item(300, "name1", "description1", true, user, itemRequest);
        Mockito.doReturn(user).when(userService).findUserById(user.getId());
        Mockito.doReturn(Collections.singletonList(item)).when(itemService).findAllItemsByRequest(itemRequest.getId());
        Mockito.doReturn(Collections.singletonList(item)).when(itemService).findAllItemsByRequest(itemRequest2.getId());
        Mockito.doReturn(Collections.singletonList(itemRequest)).when(itemRequestRepository).findAllByRequestorIdOrderByCreatedDesc(user.getId());
        Mockito.doReturn(Collections.singletonList(itemRequest2)).when(itemRequestRepository).findAllByRequestorIdNotOrderByCreatedDesc(user.getId());
        Mockito.doReturn(Optional.of(itemRequest)).when(itemRequestRepository).findById(itemRequest.getId());
        Mockito.doAnswer(invocationOnMock -> invocationOnMock.getArgument(0)).when(itemRequestRepository).save(any());
    }

    @Test
    void testCreateItemRequestOk() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("description");
        requestDto.setCreated(LocalDateTime.now());

        ItemRequest itemRequest = itemRequestService.createItemRequest(user.getId(), requestDto);

        assertEquals(user.getId(), itemRequest.getRequestor().getId());
        assertEquals(requestDto.getDescription(), itemRequest.getDescription());
        assertEquals(requestDto.getCreated(), itemRequest.getCreated());
        verify(userService, times(1)).findUserById(any());
        verify(itemRequestRepository, times(1)).save(any());
    }

    @Test
    void testCreateItemRequestValidations() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("");
        requestDto.setCreated(LocalDateTime.now());

        assertThrows(ValidationException.class, () -> itemRequestService.createItemRequest(1, requestDto));
    }

    @Test
    void testFindAllItemRequestsOfUser() {
        List<ItemRequestWithResponseDto> allItemRequestsOfUser = itemRequestService.findAllItemRequestsOfUser(user.getId());

        verify(userService, times(1)).findUserById(user.getId());
        verify(itemRequestRepository, times(1)).findAllByRequestorIdOrderByCreatedDesc(user.getId());
        verify(itemService, times(1)).findAllItemsByRequest(itemRequest.getId());

        assertEquals(1, allItemRequestsOfUser.size());
        ItemRequestWithResponseDto itemRequestWithResponseDto = allItemRequestsOfUser.get(0);
        assertEquals(user.getId(), itemRequestWithResponseDto.getRequestor());
        assertEquals(itemRequest.getCreated(), itemRequestWithResponseDto.getCreated());
        assertEquals(itemRequest.getId(), itemRequestWithResponseDto.getId());
        assertEquals(itemRequest.getDescription(), itemRequestWithResponseDto.getDescription());
        assertEquals(1, itemRequestWithResponseDto.getItems().size());
        assertEquals(ItemMapper.toDto(item), itemRequestWithResponseDto.getItems().get(0));
    }

    @Test
    void testFindItemRequestWithResponseById() {
        ItemRequestWithResponseDto itemRequestWithResponseDto = itemRequestService.findItemRequestWithResponseById(user.getId(), itemRequest.getId());

        verify(userService, times(1)).findUserById(user.getId());
        verify(itemRequestRepository, times(1)).findById(itemRequest.getId());
        verify(itemService, times(1)).findAllItemsByRequest(itemRequest.getId());

        assertEquals(user.getId(), itemRequestWithResponseDto.getRequestor());
        assertEquals(itemRequest.getCreated(), itemRequestWithResponseDto.getCreated());
        assertEquals(itemRequest.getId(), itemRequestWithResponseDto.getId());
        assertEquals(itemRequest.getDescription(), itemRequestWithResponseDto.getDescription());
        assertEquals(1, itemRequestWithResponseDto.getItems().size());
        assertEquals(ItemMapper.toDto(item), itemRequestWithResponseDto.getItems().get(0));
    }

    @Test
    void testFindAllItemRequestsOfOtherUsers() {
        List<ItemRequestWithResponseDto> allItemRequestsOfUser = itemRequestService.findAllItemRequestsOfOtherUsers(user.getId(), null, null);

        verify(userService, times(1)).findUserById(user.getId());
        verify(itemRequestRepository, times(1)).findAllByRequestorIdNotOrderByCreatedDesc(user.getId());
        verify(itemService, times(1)).findAllItemsByRequest(itemRequest2.getId());

        assertEquals(1, allItemRequestsOfUser.size());
        ItemRequestWithResponseDto itemRequestWithResponseDto = allItemRequestsOfUser.get(0);
        assertEquals(user.getId(), itemRequestWithResponseDto.getRequestor());
        assertEquals(itemRequest2.getCreated(), itemRequestWithResponseDto.getCreated());
        assertEquals(itemRequest2.getId(), itemRequestWithResponseDto.getId());
        assertEquals(itemRequest2.getDescription(), itemRequestWithResponseDto.getDescription());
        assertEquals(1, itemRequestWithResponseDto.getItems().size());
        assertEquals(ItemMapper.toDto(item), itemRequestWithResponseDto.getItems().get(0));
    }
}
