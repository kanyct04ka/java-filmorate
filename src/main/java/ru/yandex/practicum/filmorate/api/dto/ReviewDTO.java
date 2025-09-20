package ru.yandex.practicum.filmorate.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class ReviewDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int reviewId;
    private int userId;
    private int filmId;
    private String content;
    private Boolean isPositive;
    private int useful;
}
