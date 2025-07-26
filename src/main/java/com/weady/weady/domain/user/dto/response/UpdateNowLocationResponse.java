package com.weady.weady.domain.user.dto.response;

import lombok.Builder;

@Builder
public record UpdateNowLocationResponse(Long nowLocationId,
                                        String address1,
                                        String address2,
                                        String address3,
                                        String address4) {
}
