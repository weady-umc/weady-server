package com.weady.weady.domain.board.dto.request;

import com.weady.weady.domain.board.entity.board.ReportType;

public record ReportRequestDto(
        ReportType reportType,
        String content
){}
