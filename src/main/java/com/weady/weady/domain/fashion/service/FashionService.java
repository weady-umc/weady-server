package com.weady.weady.domain.fashion.service;

import com.weady.weady.domain.fashion.dto.Response.FashionDetailResponseDto;
import com.weady.weady.domain.fashion.dto.Response.FashionSummaryResponseDto;
import com.weady.weady.domain.fashion.entity.Fashion;
import com.weady.weady.domain.fashion.repository.FashionRepository;
import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.location.repository.LocationRepository;
import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.TemperatureTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.repository.UserRepository;
import com.weady.weady.domain.weather.entity.DailySummary;
import com.weady.weady.domain.weather.entity.LocationWeatherSnapshot;
import com.weady.weady.domain.weather.repository.DailySummaryRepository;
import com.weady.weady.domain.weather.repository.LocationWeatherSnapshotRepository;
import com.weady.weady.common.error.errorCode.FashionErrorCode;
import com.weady.weady.common.error.errorCode.UserErrorCode;
import com.weady.weady.common.error.errorCode.WeatherErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import com.weady.weady.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FashionService {
    private final FashionRepository fashionRepository;
    private final LocationWeatherSnapshotRepository locationWeatherSnapshotRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final DailySummaryRepository dailySummaryRepository;

    /**
     * 홈 화면에서 옷차림 요약 정보를 조회
     * @return FashionSummaryResponseDto
     * @thorws UserErrorCode.USER_NOT_FOUND 사용자가 존재하지 않을 경우 예외를 발생
     * @thorws WeatherErrorCode.WEATHER_SNAPSHOT_NOT_FOUND 날씨 snapshot이 존재하지 않을 경우 예외를 발생
     * @thorws FashionErrorCode.FASHION_NOT_FOUND 체감온도에 따른 옷차림 추천이 존재하지 않을 경우 예외를 발생
     */
    public FashionSummaryResponseDto getFashionSummary() {
        Long userId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Long locationId = null;
        if (user.getDefaultLocation() != null && user.getDefaultLocation().getLocation() != null) {
            locationId = user.getDefaultLocation().getLocation().getId();
        } else if (user.getNowLocation() != null) {
            locationId = user.getNowLocation().getId();
        }

        LocalDateTime today = LocalDateTime.now();
        int date = Integer.parseInt(today.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        int time = today.getHour() * 100;

        LocationWeatherSnapshot snapshot = locationWeatherSnapshotRepository
                .findByLocationIdAndDateAndTime(locationId, date, time)
                .orElseThrow(() -> new BusinessException(WeatherErrorCode.WEATHER_SNAPSHOT_NOT_FOUND));

        float feelTemp = snapshot.getFeelTmp();

        Fashion fashion = fashionRepository
                .findByTemperatureRange(feelTemp)
                .orElseThrow(() -> new BusinessException(FashionErrorCode.FASHION_NOT_FOUND));

        return new FashionSummaryResponseDto(
                locationId,
                fashion.getName(),
                fashion.getImgUrl()
        );
    }

    /**
     * 옷차림 화면에서 옷차림 상세 정보를 조회
     * @return FashionDetailResponseDto
     * @thorws UserErrorCode.USER_NOT_FOUND 사용자가 존재하지 않을 경우 예외를 발생
     * @thorws WeatherErrorCode.WEATHER_SNAPSHOT_NOT_FOUND 날씨 snapshot이 존재하지 않을 경우 예외를 발생
     * @thorws FashionErrorCode.FASHION_NOT_FOUND 체감온도에 따른 옷차림 추천이 존재하지 않을 경우 예외를 발생
     * @thorws LocationErrorCode.LOCATION_NOT_FOUND 위치가 존재하지 않을 경우 예외를 발생
     */
    public FashionDetailResponseDto getFashionDetail() {
        Long userId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Long locationId = null;
        if (user.getDefaultLocation() != null && user.getDefaultLocation().getLocation() != null) {
            locationId = user.getDefaultLocation().getLocation().getId();
        } else if (user.getNowLocation() != null) {
            locationId = user.getNowLocation().getId();
        }

        LocalDateTime today = LocalDateTime.now();
        int date = Integer.parseInt(today.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        int time = today.getHour() * 100;

        List<LocationWeatherSnapshot> snapshots =
                locationWeatherSnapshotRepository.findByLocationIdAndDateOrderByTimeAsc(locationId, date);

        if (snapshots.isEmpty()) {
            throw new BusinessException(WeatherErrorCode.WEATHER_SNAPSHOT_NOT_FOUND);
        }

        LocationWeatherSnapshot currentSnapshot = snapshots.stream()
                .filter(s -> s.getTime() == time)
                .findFirst()
                .orElse(snapshots.get(0));

        float feelTemp = currentSnapshot.getFeelTmp();

        List<Fashion> fashions = fashionRepository.findAll();

        Fashion currentFashion = findFashionFor(feelTemp, fashions);;

        FashionDetailResponseDto.Recommendation recommendation =
                new FashionDetailResponseDto.Recommendation(
                        currentSnapshot.getTime(),
                        currentSnapshot.getFeelTmp(),
                        toClothing(currentFashion)
                );

        List<FashionDetailResponseDto.ChartPoint> chart = snapshots.stream()
                .map(s -> {
                    Fashion fashion = findFashionFor(s.getFeelTmp(), fashions);
                    return new FashionDetailResponseDto.ChartPoint(
                            s.getTime(),
                            s.getFeelTmp(),
                            toClothing(fashion)
                    );
                })
                .toList();

        DailySummary dailySummary = dailySummaryRepository
                .findByLocationIdAndReportDateWithTags(locationId, today.toLocalDate())
                .orElseThrow(() -> new BusinessException(WeatherErrorCode.WEATHER_SNAPSHOT_NOT_FOUND));

        FashionDetailResponseDto.Tags tags = new FashionDetailResponseDto.Tags(
                toTag(dailySummary.getSeasonTag()),
                toTag(dailySummary.getWeatherTag()),
                toTag(dailySummary.getTemperatureTag())
        );

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new BusinessException(LocationErrorCode.LOCATION_NOT_FOUND));

        return new FashionDetailResponseDto(
                location.getId(),
                location.getBCode(),
                location.getAddress1(),
                location.getAddress2(),
                location.getAddress3(),
                location.getAddress4(),
                recommendation,
                chart,
                tags
        );
    }

    private Fashion findFashionFor(float feelTmp, List<Fashion> fashions) {
        return fashions.stream()
                .filter(f -> f.getStartTemp() <= feelTmp && feelTmp <= f.getEndTemp())
                .findFirst()
                .orElseThrow(() -> new BusinessException(FashionErrorCode.FASHION_NOT_FOUND));
    }

    private FashionDetailResponseDto.Clothing toClothing(Fashion fashion) {
        return new FashionDetailResponseDto.Clothing(fashion.getName(), fashion.getImgUrl());
    }

    private FashionDetailResponseDto.Tag toTag(SeasonTag tag) {
        return new FashionDetailResponseDto.Tag(tag.getId(), tag.getName());
    }

    private FashionDetailResponseDto.Tag toTag(WeatherTag tag) {
        return new FashionDetailResponseDto.Tag(tag.getId(), tag.getName());
    }

    private FashionDetailResponseDto.Tag toTag(TemperatureTag tag) {
        return new FashionDetailResponseDto.Tag(tag.getId(), tag.getName());
    }

}
