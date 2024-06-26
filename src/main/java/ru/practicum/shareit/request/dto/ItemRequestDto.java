package ru.practicum.shareit.request.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private Integer id;
    @NonNull
    private String description;
    @NonNull
    private Integer requestor;
    @NonNull
    private LocalDateTime created;
}
