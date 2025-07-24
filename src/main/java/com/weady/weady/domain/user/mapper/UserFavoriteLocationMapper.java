package com.weady.weady.domain.user.mapper;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.user.dto.response.AddUserFavoriteLocationResponse;
import com.weady.weady.domain.user.dto.response.GetUserFavoriteLocationResponse;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.entity.UserFavoriteLocation;
import com.weady.weady.domain.weather.entity.DailySummary;
import com.weady.weady.domain.weather.entity.LocationWeatherShortDetail;
import org.springframework.stereotype.Component;

@Component
public class UserFavoriteLocationMapper {

    public AddUserFavoriteLocationResponse toAddResponseDto(UserFavoriteLocation userFavoriteLocation){
        return new AddUserFavoriteLocationResponse(userFavoriteLocation.getLocation().getId());
    }

    public GetUserFavoriteLocationResponse toGetResponse(UserFavoriteLocation fav, LocationWeatherShortDetail weather, DailySummary summary) {
        Float currentTemp = (weather != null && weather.getTmp() != null) ? weather.getTmp() : null;
        Float maxTemp = (summary != null && summary.getActualTmx() != null) ? summary.getActualTmx() : null;
        Float minTemp = (summary != null && summary.getActualTmn() != null) ? summary.getActualTmn() : null;

        return new GetUserFavoriteLocationResponse(
                fav.getId(),
                fav.getLocation().getBCode(),
                fav.getLocation().getName(),
                currentTemp,
                maxTemp,
                minTemp
        );
    }
}
