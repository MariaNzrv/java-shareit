package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class ItemRequestRepositoryTest {
    @Autowired
    ItemRequestRepository itemRequestRepository;
    @Autowired
    UserRepository userRepository;

    private User user1;
    private User user2;
    private ItemRequest request1;
    private ItemRequest request2;
    private ItemRequest request3;
    private ItemRequest request4;

    @BeforeEach
    void beforeEach() {
        LocalDateTime date = LocalDateTime.of(2024, Month.MAY, 29, 12, 0);
        user1 = new User(1, "user@owner.ru", "owner");
        user2 = new User(2, "user@asker.ru", "asker");

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);

        request1 = new ItemRequest(1, "description1", user1, date);
        request2 = new ItemRequest(2, "description2", user1, date.plusMinutes(10));
        request3 = new ItemRequest(3, "description3", user2, date.plusMinutes(30));
        request4 = new ItemRequest(4, "description4", user2, date.plusMinutes(20));

        request1 = itemRequestRepository.save(request1);
        request2 = itemRequestRepository.save(request2);
        request3 = itemRequestRepository.save(request3);
        request4 = itemRequestRepository.save(request4);
    }

    @Test
    void testFindAllByRequestorIdOrderByCreatedDesc() {
        List<ItemRequest> result = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(user1.getId());
        assertEquals(2, result.size());

        assertEquals(request2, result.get(0));
        assertEquals(request1, result.get(1));
    }

    @Test
    void testFindAllByRequestorIdNotOrderByCreatedDesc() {
        List<ItemRequest> result = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(user1.getId());
        assertEquals(2, result.size());

        assertEquals(request3, result.get(0));
        assertEquals(request4, result.get(1));
    }
}
