package com.weady.weady.domain.curation.dto.Request;


import lombok.Builder;
import java.util.List;

@Builder
public record CurationRequestDto(String title,
                                Long curationCategoryId,
                                Long seasonTagId,
                                Long weatherTagId,
                                List<String> imgAddresses) {
}
