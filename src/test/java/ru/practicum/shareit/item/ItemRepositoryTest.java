package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private Item item;
    private User user;
    private ItemRequest request;

    @BeforeEach
    void beforeEach() {
        user = new User(1, "user@ya.ru", "Irina");
        user = userRepository.save(user);

        request = new ItemRequest(1, "description", user, LocalDateTime.now());
        request = itemRequestRepository.save(request);

        item = new Item("name", "description", true);
        item.setOwner(user);
        item.setRequest(request);
        item = itemRepository.save(item);
    }

    @Test
    void testFindAllByOwnerIdOk() {
        List<Item> list = itemRepository.findAllByOwnerId(user.getId());
        assertEquals(1, list.size());
        assertEquals(item, list.get(0));
    }

    @Test
    void testFindAllByNameOrDescriptionContainingIgnoreCaseAndAvailableOk() {
        List<Item> list = itemRepository.findAllByNameOrDescriptionContainingIgnoreCaseAndAvailable("ME", "", true);
        assertEquals(1, list.size());
        assertEquals(item, list.get(0));

        list = itemRepository.findAllByNameOrDescriptionContainingIgnoreCaseAndAvailable("", "eSCRipti", true);
        assertEquals(1, list.size());
        assertEquals(item, list.get(0));
    }

    @Test
    void testFindAllByNameOrDescriptionContainingIgnoreCaseAndAvailableNotFound() {
        List<Item> list = itemRepository.findAllByNameOrDescriptionContainingIgnoreCaseAndAvailable("asd", "description", false);
        assertEquals(0, list.size());

        list = itemRepository.findAllByNameOrDescriptionContainingIgnoreCaseAndAvailable("", "dsfgsdfg", true);
        assertEquals(0, list.size());
    }

    @Test
    void testFindAllByRequestId() {
        List<Item> list = itemRepository.findAllByRequestId(request.getId());
        assertEquals(1, list.size());
        assertEquals(item, list.get(0));

        list = itemRepository.findAllByRequestId(404);
        assertEquals(0, list.size());
    }
}
