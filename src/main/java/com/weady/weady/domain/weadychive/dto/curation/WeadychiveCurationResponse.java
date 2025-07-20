package com.weady.weady.domain.weadychive.dto.curation;

import com.weady.weady.domain.curation.dto.CurationCategoryResponse;
import lombok.Builder;

import java.util.List;

public class WeadychiveCurationResponse {

    @Builder
    public record scrappedCurationByUserResponseDto(String userName,
                                                    List<WeadychiveCurationResponse.CurationDto> curations


    ){}

    @Builder
    public record CurationDto(
            Long curationId,
            String curationTitle,
            String backgroundImgUrl
    ) {}

}
