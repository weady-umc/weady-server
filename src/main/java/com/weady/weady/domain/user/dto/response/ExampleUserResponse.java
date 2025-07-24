package com.weady.weady.domain.user.dto.response;


import lombok.Builder;

import java.time.LocalDateTime;

public class ExampleUserResponse {
    @Builder
    public record ExampleUserResponseDto(Long id,
                                         String email,
                                         String name,
                                         LocalDateTime createdAt) { }
}
