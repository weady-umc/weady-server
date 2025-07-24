package com.weady.weady.domain.user.dto.response;

public record GetUserFavoriteLocationResponse(

        Long favoriteId,
        String bCode,
        String locationName,
        Float tmp,
        Float actualTmx,
        Float actualTmn

) {}
