package com.weady.weady.domain.curation.dto.Response;


import lombok.Builder;

import java.util.List;

@Builder
public record CurationByLocationResponseDto(Long locationId,
                                            String locationName,
                                            List<CurationDto> curations


){}
