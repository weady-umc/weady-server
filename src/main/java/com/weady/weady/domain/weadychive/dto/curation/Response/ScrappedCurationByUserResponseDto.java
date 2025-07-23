package com.weady.weady.domain.weadychive.dto.curation.Response;

import lombok.Builder;

import java.util.List;

@Builder
public record ScrappedCurationByUserResponseDto(String userName,
                                                List<CurationDto> curations


){}
