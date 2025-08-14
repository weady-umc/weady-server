package com.weady.weady.scheduler;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.location.repository.LocationRepository;
import com.weady.weady.domain.weather.service.DailySummarySchedulerService;
import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.TemperatureTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.domain.tags.repository.season.SeasonRepository;
import com.weady.weady.domain.tags.repository.temperature.TemperatureRepository;
import com.weady.weady.domain.tags.repository.weather.WeatherRepository;
import com.weady.weady.domain.weather.entity.DailySummary;
import com.weady.weady.domain.weather.entity.LocationWeatherSnapshot;
import com.weady.weady.domain.weather.repository.DailySummaryRepository;
import com.weady.weady.domain.weather.repository.LocationWeatherSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class DailySummarySchedulerServiceTest {

    @Autowired
    DailySummarySchedulerService dailySummarySchedulerService;
    @Autowired LocationRepository locationRepo;
    @Autowired LocationWeatherSnapshotRepository snapRepo;
    @Autowired DailySummaryRepository summaryRepo;
    @Autowired WeatherRepository weatherRepo;
    @Autowired TemperatureRepository tempRepo;
    @Autowired SeasonRepository seasonRepo;

    @Test
    void 내일_비오는날_Summary_생성() {
        seedTags();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        Location loc = locationRepo.save(Location.builder()
                .address1("테스트시").address2("테스트구")
                .nx(60).ny(127).build());

        List<LocationWeatherSnapshot> snaps = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            snaps.add(LocationWeatherSnapshot.builder()
                    .location(loc)
                    .date(Integer.parseInt(tomorrow.toString().replace("-", "")))
                    .time(h * 100)
                    .tmp(25f + (h >= 14 ? - (h-14) * 0.5f : h * 0.2f))
                    .feelTmp(25f)
                    .sky(1)     // 맑음
                    .wsd(2f)
                    .pty(0)
                    .pop(10f)
                    .pcp(0f)
                    .observationDate(Integer.parseInt(LocalDate.now().toString().replace("-", "")))
                    .observationTime(1400)
                    .build());
        }

        for (int h = 10; h <= 13; h++) {
            LocationWeatherSnapshot s = snaps.get(h);
            s.setPty(1);     // 비
            s.setPop(70f);   // ≥60%
            s.setPcp(1f);
            s.setSky(4);     // 강수 시간은 sky 집계에서 제외되지만 값은 넣어둬도 무방
        }
        snapRepo.saveAll(snaps);

        dailySummarySchedulerService.buildDailySummary(tomorrow);

        var list = summaryRepo.findByLocationIdAndReportDate(loc.getId(), tomorrow).stream().toList();
        assertThat(list).hasSize(1);
        DailySummary ds = list.get(0);
        assertThat(ds.getWeatherTag().getName()).isEqualTo("비 오는 날");
        assertThat(ds.getReportDate()).isEqualTo(tomorrow);


        assertThat(ds.getActualTmx()).isNotNull();
        assertThat(ds.getActualTmn()).isNotNull();
        assertThat(ds.getTemperatureTag()).isNotNull();
        assertThat(ds.getSeasonTag()).isNotNull();
    }

    private void seedTags() {
        if (weatherRepo.findByName("맑은 날").isEmpty())
            weatherRepo.save(WeatherTag.builder().name("맑은 날").build());
        if (weatherRepo.findByName("구름 많은 날").isEmpty())
            weatherRepo.save(WeatherTag.builder().name("구름 많은 날").build());
        if (weatherRepo.findByName("흐린 날").isEmpty())
            weatherRepo.save(WeatherTag.builder().name("흐린 날").build());
        if (weatherRepo.findByName("비 오는 날").isEmpty())
            weatherRepo.save(WeatherTag.builder().name("비 오는 날").build());
        if (weatherRepo.findByName("바람 많은 날").isEmpty())
            weatherRepo.save(WeatherTag.builder().name("바람 많은 날").build());
        if (weatherRepo.findByName("눈 오는 날").isEmpty())
            weatherRepo.save(WeatherTag.builder().name("눈 오는 날").build());

        if (seasonRepo.findByName("봄").isEmpty())
            seasonRepo.save(SeasonTag.builder().name("봄").build());
        if (seasonRepo.findByName("여름").isEmpty())
            seasonRepo.save(SeasonTag.builder().name("여름").build());
        if (seasonRepo.findByName("가을").isEmpty())
            seasonRepo.save(SeasonTag.builder().name("가을").build());
        if (seasonRepo.findByName("겨울").isEmpty())
            seasonRepo.save(SeasonTag.builder().name("겨울").build());

        // 온도 태그(예시) – 실제 서비스 운영값과 동일하게 세팅
        if (tempRepo.findAll().isEmpty()) {
            tempRepo.save(TemperatureTag.builder().name("추움").minTemperature(-100f).maxTemperature(10f).build());
            tempRepo.save(TemperatureTag.builder().name("선선").minTemperature(10f).maxTemperature(20f).build());
            tempRepo.save(TemperatureTag.builder().name("보통").minTemperature(20f).maxTemperature(27f).build());
            tempRepo.save(TemperatureTag.builder().name("더움").minTemperature(27f).maxTemperature(100f).build());
        }
    }
}