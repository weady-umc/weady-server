package com.weady.weady.domain.user.dto.response;

import lombok.Builder;

@Builder
public record UpdateUserProfileResponse(Long userId,
                                        String name,
                                        String profileImageUrl) {
}
