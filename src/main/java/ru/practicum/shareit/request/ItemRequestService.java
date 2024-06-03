package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestService {
    private final ItemService itemService;
    private final UserService userService;
    private final ItemRequestRepository itemRequestRepository;

    public ItemRequest createItemRequest(Integer userId, ItemRequestDto itemRequestDto) {
        validateRequiredFields(itemRequestDto);

        User user = userService.findUserById(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequestor(user);
        return itemRequestRepository.save(itemRequest);
    }

    public List<ItemRequestWithResponseDto> findAllItemRequestsOfUser(Integer userId) {
        List<ItemRequestWithResponseDto> itemRequestWithResponseDtos = new ArrayList<>();
        userService.findUserById(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        for (ItemRequest itemRequest : itemRequests) {
            List<Item> items = itemService.findAllItemsByRequest(itemRequest.getId());
            itemRequestWithResponseDtos.add(ItemRequestMapper.toItemRequestWithResponseDto(itemRequest, items));
        }
        return itemRequestWithResponseDtos;
    }

    public ItemRequestWithResponseDto findItemRequestWithResponseById(Integer userId, Integer requestId) {
        userService.findUserById(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() -> {
            log.error("Запрос с Id = {} не существует", requestId);
            throw new EntityNotFoundException("Запрос с таким Id не существует");
        });
        List<Item> items = itemService.findAllItemsByRequest(requestId);
        return ItemRequestMapper.toItemRequestWithResponseDto(itemRequest, items);
    }

    public List<ItemRequestWithResponseDto> findAllItemRequestsOfOtherUsers(Integer userId, Integer from, Integer size) {
        userService.findUserById(userId);
        List<ItemRequest> itemRequests;
        List<ItemRequestWithResponseDto> itemRequestWithResponseDtos = new ArrayList<>();

        if (from < 0 || size <= 0) {
            log.error("Некорректные значения параметров from = {}, size={}", from, size);
            throw new ValidationException("Некорректные значения параметров from/size");
        }

        Sort sortByCreated = Sort.by(Sort.Direction.DESC, "created");
        Pageable page = PageRequest.of(from / size, size, sortByCreated);
        Page<ItemRequest> itemRequestPage = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId, page);
        itemRequests = itemRequestPage.getContent();

        for (ItemRequest itemRequest : itemRequests) {
            List<Item> items = itemService.findAllItemsByRequest(itemRequest.getId());
            itemRequestWithResponseDtos.add(ItemRequestMapper.toItemRequestWithResponseDto(itemRequest, items));
        }

        return itemRequestWithResponseDtos;
    }

    private void validateRequiredFields(ItemRequestDto itemRequestDto) {
        if (itemRequestDto.getDescription() == null ||
                itemRequestDto.getDescription().isBlank() ||
                itemRequestDto.getDescription().isEmpty()) {
            log.warn("Описание обязательно для заполнения");
            throw new ValidationException("Описание обязательно для заполнения");
        }
    }


}
