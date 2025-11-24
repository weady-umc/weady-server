package com.weady.weady.domain.curation.dto.Response;


import lombok.Builder;

import java.util.List;

@Builder
public record CurationByCurationIdResponseDto(Long curationId,
                                              String curationTitle,
                                              List<ImgDto> imgs

){}
