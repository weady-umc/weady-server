package com.weady.weady.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record GetUserDefaultLocationResponse(Long defaultLocationId,
                                             String bCode,
                                             String address1,
                                             String address2,
                                             String address3,
                                             String address4) { }
