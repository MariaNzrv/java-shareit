package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Integer> {
    List<ItemRequest> findAllByRequestorIdOrderByCreatedDesc(Integer userId);

    Page<ItemRequest> findAllByRequestorIdNotOrderByCreatedDesc(Integer userId, Pageable page);

    List<ItemRequest> findAllByRequestorIdNotOrderByCreatedDesc(Integer userId);
}
