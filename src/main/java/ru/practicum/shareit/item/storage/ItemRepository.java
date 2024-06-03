package ru.practicum.shareit.item.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    List<Item> findAllByOwnerId(Integer userId);

    Page<Item> findAllByOwnerId(Integer userId, Pageable pageable);

    List<Item> findAllByNameOrDescriptionContainingIgnoreCaseAndAvailable(String nameText, String descriptionText,
                                                                          Boolean isAvailable);

    Page<Item> findAllByNameOrDescriptionContainingIgnoreCaseAndAvailable(String nameText, String descriptionText,
                                                                          Boolean isAvailable, Pageable pageable);

    List<Item> findAllByRequestId(Integer requestId);
}