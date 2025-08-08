package com.weady.weady.domain.board.dto.request;

import lombok.Builder;

import java.util.List;

@Builder
public record BoardCreateRequestDto ( // 이미지 파일 제외 postData 에 해당하는 데이터만.
                                      Boolean isPublic,
                                      String content,

                                      Long seasonTagId,
                                      Long weatherTagId,
                                      Long temperatureTagId,

                                      List<BoardPlaceRequestDto> boardPlaceRequestDtoList,
                                      List<Long> styleIds,
                                      List<BoardBrandRequestDto> boardBrandRequestDtoList
) {}

