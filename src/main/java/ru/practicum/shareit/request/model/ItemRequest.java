package ru.practicum.shareit.request.model;

import lombok.*;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    private Integer id;
    @NonNull
    private String description;
    @NonNull
    private User requestor;
    @NonNull
    private LocalDateTime created = LocalDateTime.now();
}
