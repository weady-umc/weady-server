package com.weady.weady.domain.fashion.mapper;

import com.weady.weady.domain.fashion.dto.Response.FashionDetailResponseDto;
import com.weady.weady.domain.fashion.dto.Response.FashionSummaryResponseDto;
import com.weady.weady.domain.fashion.entity.Fashion;
import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.TemperatureTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.domain.weather.entity.LocationWeatherSnapshot;

import java.util.List;

public class FashionMapper {
    public static FashionSummaryResponseDto toSummaryResponse(Long locationId,
                                                              Fashion fashion) {
        return FashionSummaryResponseDto.builder()
                .locationId(locationId)
                .recommendation(fashion.getName())
                .imageUrl(fashion.getImgUrl())
                .build();
    }

    public static FashionDetailResponseDto toDetailResponse(Location location,
                                                            FashionDetailResponseDto.Recommendation recommendation,
                                                            List<FashionDetailResponseDto.ChartPoint> chart,
                                                            FashionDetailResponseDto.Tags tags) {
        return FashionDetailResponseDto.builder()
                .locationId(location.getId())
                .locationBCode(location.getBCode())
                .address1(location.getAddress1())
                .address2(location.getAddress2())
                .address3(location.getAddress3())
                .address4(location.getAddress4())
                .recommendation(recommendation)
                .chart(chart)
                .tags(tags)
                .build();
    }

    public static FashionDetailResponseDto.Recommendation toRecommendation(LocationWeatherSnapshot snapshot, Fashion fashion) {
        return FashionDetailResponseDto.Recommendation.builder()
                .time(snapshot.getTime())
                .feelTmp(snapshot.getFeelTmp())
                .clothing(toClothing(fashion))
                .build();
    }

    public static FashionDetailResponseDto.Clothing toClothing(Fashion fashion) {
        return FashionDetailResponseDto.Clothing.builder()
                .name(fashion.getName())
                .imageUrl(fashion.getImgUrl())
                .build();
    }

    public static FashionDetailResponseDto.ChartPoint toChartPoint(LocationWeatherSnapshot snapshot, Fashion fashion) {
        return FashionDetailResponseDto.ChartPoint.builder()
                .time(snapshot.getTime())
                .feelTmp(snapshot.getFeelTmp())
                .clothing(toClothing(fashion))
                .build();
    }

    public static FashionDetailResponseDto.Tag toTag(SeasonTag tag) {
        return FashionDetailResponseDto.Tag.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }

    public static FashionDetailResponseDto.Tag toTag(WeatherTag tag) {
        return FashionDetailResponseDto.Tag.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }

    public static FashionDetailResponseDto.Tag toTag(TemperatureTag tag) {
        return FashionDetailResponseDto.Tag.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }

}
