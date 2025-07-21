package com.weady.weady.domain.user.mapper;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.user.dto.AddUserFavoriteLocationResponse;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.entity.UserFavoriteLocation;
import org.springframework.stereotype.Component;

@Component
public class UserFavoriteLocationMapper {

    public UserFavoriteLocation toEntity(User user, Location location){
        return UserFavoriteLocation.builder()
                .user(user)
                .location(location)
                .build();
    }

    public AddUserFavoriteLocationResponse toAddResponseDto(UserFavoriteLocation userFavoriteLocation){
        return new AddUserFavoriteLocationResponse(userFavoriteLocation.getLocation().getId());
    }
}
