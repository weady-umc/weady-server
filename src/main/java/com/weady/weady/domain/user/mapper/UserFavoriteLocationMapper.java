package com.weady.weady.domain.user.mapper;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.user.dto.response.AddUserFavoriteLocationResponse;
import com.weady.weady.domain.user.dto.response.GetUserFavoriteLocationResponse;
import com.weady.weady.domain.user.dto.response.GetUserNowLocationResponse;
import com.weady.weady.domain.user.entity.UserFavoriteLocation;
import com.weady.weady.domain.weather.entity.DailySummary;
import com.weady.weady.domain.weather.entity.LocationWeatherShortDetail;

public class UserFavoriteLocationMapper {

    public static AddUserFavoriteLocationResponse toAddResponseDto(UserFavoriteLocation userFavoriteLocation){
        return AddUserFavoriteLocationResponse.builder()
                .locationId(userFavoriteLocation.getLocation().getId())
                .build();
    }

    public static GetUserFavoriteLocationResponse toGetResponse(UserFavoriteLocation fav, LocationWeatherShortDetail weather, DailySummary summary) {
        Float currentTemp = (weather != null && weather.getTmp() != null) ? weather.getTmp() : null;
        Float maxTemp = (summary != null && summary.getActualTmx() != null) ? summary.getActualTmx() : null;
        Float minTemp = (summary != null && summary.getActualTmn() != null) ? summary.getActualTmn() : null;

        return GetUserFavoriteLocationResponse.builder()
                .favoriteId(fav.getId())
                .bCode(fav.getLocation().getBCode())
                .locationAddress1(fav.getLocation().getAddress1())
                .locationAddress2(fav.getLocation().getAddress2())
                .locationAddress3(fav.getLocation().getAddress3())
                .locationAddress4(fav.getLocation().getAddress4())
                .currentTemp(currentTemp)
                .actualTmx(maxTemp)
                .actualTmn(minTemp)
                .build();
    }

    public static GetUserNowLocationResponse toNowLocationResponse(Location location, LocationWeatherShortDetail weather, DailySummary summary) {
        Float currentTemp = (weather != null && weather.getTmp() != null) ? weather.getTmp() : null;
        Float maxTemp = (summary != null && summary.getActualTmx() != null) ? summary.getActualTmx() : null;
        Float minTemp = (summary != null && summary.getActualTmn() != null) ? summary.getActualTmn() : null;

        return GetUserNowLocationResponse.builder()
                .bCode(location.getBCode())
                .locationAddress1(location.getAddress1())
                .locationAddress2(location.getAddress2())
                .locationAddress3(location.getAddress3())
                .locationAddress4(location.getAddress4())
                .currentTemp(currentTemp)
                .actualTmx(maxTemp)
                .actualTmn(minTemp)
                .build();
    }
}
