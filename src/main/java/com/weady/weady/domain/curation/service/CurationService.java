package com.weady.weady.domain.curation.service;


import com.weady.weady.common.error.errorCode.LocationErrorCode;
import com.weady.weady.common.error.errorCode.TagsErrorCode;
import com.weady.weady.common.external.s3.S3Uploader;
import com.weady.weady.domain.curation.dto.Request.CurationRequestDto;
import com.weady.weady.domain.curation.dto.Response.CurationByCurationIdResponseDto;
import com.weady.weady.domain.curation.dto.Response.CurationByLocationResponseDto;
import com.weady.weady.domain.curation.dto.Response.CurationCategoryResponseDto;
import com.weady.weady.domain.curation.entity.Curation;
import com.weady.weady.domain.curation.entity.CurationCategory;
import com.weady.weady.domain.curation.entity.CurationImg;
import com.weady.weady.domain.curation.mapper.CurationCategoryMapper;
import com.weady.weady.domain.curation.mapper.CurationMapper;
import com.weady.weady.domain.curation.repository.curation.CurationImgRepository;
import com.weady.weady.domain.curation.repository.curation.CurationRepository;
import com.weady.weady.domain.curation.repository.curationCategory.CurationCategoryRepository;
import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.location.repository.LocationRepository;
import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.domain.tags.repository.season.SeasonRepository;
import com.weady.weady.domain.tags.repository.weather.WeatherRepository;
import com.weady.weady.domain.weather.entity.DailySummary;
import com.weady.weady.domain.weather.repository.DailySummaryRepository;
import com.weady.weady.common.error.errorCode.CurationErrorCode;
import com.weady.weady.common.error.errorCode.DailySummaryErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class CurationService {

    private final CurationCategoryRepository curationCategoryRepository; //location 별 curation 찾기 용도
    private final CurationRepository curationRepository; //curation 상세 조회 용도
    private final DailySummaryRepository dailySummaryRepository; //curation_category_id를 통한 큐레이션 찾기 용도
    private final LocationRepository locationRepository;
    private final S3Uploader s3Uploader;
    private final SeasonRepository seasonRepository;
    private final WeatherRepository weatherRepository;
    private final CurationImgRepository curationImgRepository;

    /**
     * 지역별 날씨에 맞는 큐레이션 제공
     * @return curationByLocationResponseDto
     * @throws ...
     */
    public CurationByLocationResponseDto getCurationByLocation(Long locationId){

        //locationId로 bcode 받아오기
        String bcode = locationRepository.findBCodeById(locationId)
                .orElseThrow(()-> new BusinessException(LocationErrorCode.BCODE_NOT_FOUND));
        String last5 = bcode.substring(5); // 뒤 5자리
        String first5 = bcode.substring(0, 5); //앞 5자리
        String first2 = bcode.substring(0,2); //앞 두자리 -> 시


        List<Long> locationIds = new ArrayList<>();

        /**
         * 시,구,동 필터링
         */

        if(last5.equals("00000") && first5.endsWith("00")){ //시까지 온 경우

            Location city = locationRepository.findById(locationId)
                    .orElseThrow(()->new BusinessException(LocationErrorCode.LOCATION_NOT_FOUND));
            String cityName = city.getAddress1(); //이 locationId가 어느 시? 인지를 확인

            locationIds = locationRepository.findIdsByBcodePrefix(first2); //해당 시의 구들 locationId 모두 가져오기


            List<CurationCategory> curationCategories = curationCategoryRepository.findByLocationIdIn(locationIds);


            //확장성을 생각한다면 if문들을 써야 할듯... 현재는 서울특별시 한정이므로...
            Location standardLocation = locationRepository.findLocationBybCode("1114000000")//서울의 경우 중구가 기준
                    .orElseThrow(()-> new BusinessException(LocationErrorCode.LOCATION_NOT_FOUND));

            Long standardLocationId = standardLocation.getId(); //중구의 locationId

            DailySummary dailySummary = dailySummaryRepository.findByLocationId(standardLocationId)
                    .orElseThrow(()-> new BusinessException(DailySummaryErrorCode.DAILY_SUMMARY_NOT_FOUND));

            //가져온 dailySummary에서 season, weather tag 가져오기
            SeasonTag seasonTag = dailySummary.getSeasonTag();
            WeatherTag weatherTag = dailySummary.getWeatherTag();

            String seasonTagText = seasonTag.getName();
            String weatherTagText = weatherTag.getName();


            //일단 다 가지고 와서 필터링 진행
            //그 후 4개 선정
            List<Curation> allCurations = curationCategories.stream()
                    .flatMap(category -> category.getCurations().stream())
                    .filter(curation ->
                            curation.getSeasonTag().equals(seasonTag) &&
                                    curation.getWeatherTag().equals(weatherTag)
                    )
                    .collect(Collectors.toList());

            // 무작위로 섞어서 4개만 추출
            Collections.shuffle(allCurations);
            List<Curation> selected = allCurations.stream().limit(4).toList();

            // 응답 생성
            return CurationCategoryMapper.toCurationByLocationResponseDto(locationId, cityName, seasonTagText, weatherTagText, selected);

        }else if(last5.equals("00000")){ //구까지 온 경우

            CurationCategory curationCategory = curationCategoryRepository.findByLocationId(locationId)
                    .orElseThrow(()-> new BusinessException(CurationErrorCode.CURATION_CATEGORY_NOT_FOUND));

            DailySummary dailySummary = dailySummaryRepository.findByLocationId(locationId)
                    .orElseThrow(()-> new BusinessException(DailySummaryErrorCode.DAILY_SUMMARY_NOT_FOUND));

            //가져온 dailySummary에서 season, weather tag 가져오기
            SeasonTag seasonTag = dailySummary.getSeasonTag();
            WeatherTag weatherTag = dailySummary.getWeatherTag();

            String seasonTagText = seasonTag.getName();
            String weatherTagText = weatherTag.getName();

            String locationName = curationCategory.getViewName();

            //태그로 큐레이션 필터링
            List<Curation> curations = curationCategory.getCurations().stream()
                    .filter(curation ->
                            curation.getSeasonTag().equals(seasonTag) &&
                                    curation.getWeatherTag().equals(weatherTag)
                    ).collect(Collectors.toList());

            return CurationCategoryMapper.toCurationByLocationResponseDto(locationId, locationName, seasonTagText, weatherTagText, curations);



        }else{ //동까지 온 경우
            String transformedBCode = first5 + "00000";

            Long locationId1 = locationRepository.findIdByBCode(transformedBCode);


            //locationId1으로 CurationCategory에서 해당하는 카테고리들 가져와야
            CurationCategory curationCategory = curationCategoryRepository.findByLocationId(locationId1)
                    .orElseThrow(()-> new BusinessException(CurationErrorCode.CURATION_CATEGORY_NOT_FOUND));

            DailySummary dailySummary = dailySummaryRepository.findByLocationId(locationId)
                    .orElseThrow(()-> new BusinessException(DailySummaryErrorCode.DAILY_SUMMARY_NOT_FOUND));

            //가져온 dailySummary에서 season, weather tag 가져오기
            SeasonTag seasonTag = dailySummary.getSeasonTag();
            WeatherTag weatherTag = dailySummary.getWeatherTag();

            String seasonTagText = seasonTag.getName();
            String weatherTagText = weatherTag.getName();

            String locationName = curationCategory.getViewName();

            //태그로 큐레이션 필터링
            List<Curation> curations = curationCategory.getCurations().stream()
                    .filter(curation ->
                            curation.getSeasonTag().equals(seasonTag) &&
                                    curation.getWeatherTag().equals(weatherTag)
                    ).collect(Collectors.toList());

            return CurationCategoryMapper.toCurationByLocationResponseDto(locationId1, locationName, seasonTagText, weatherTagText, curations);


        }

    }


    /**
     * 큐레이션 상세정보 조회
     * @return curationByCurationIdResponseDto
     * @thorws CurationErrorCode.CURATION_NOT_FOUND 큐레이션이 존재하지 않을경우 예외 발생
     */
    public CurationByCurationIdResponseDto getSpecificCuration(Long curationId){
        Curation curation = curationRepository.findById(curationId)
                .orElseThrow(() -> new BusinessException(CurationErrorCode.CURATION_NOT_FOUND));

        String curationTitle = curation.getTitle();
        List<CurationImg> imgs = curation.getImgs();

        return CurationMapper.toCurationResponseDto(curationId, curationTitle, imgs);

    }

    /**
     * curation_category 넘겨주기
     * @return List<CurationCategoryResponse.curationCategoryResponseDto>
     * @throws ...
     */
    public List<CurationCategoryResponseDto> getCurationCategory(){
        List<CurationCategory> curationCategories = curationCategoryRepository.findAll();

        return CurationCategoryMapper.toDtoList(curationCategories);
    }


    /**
     * curation_category_id 로 위치별 큐레이션 조회하기
     * @return
     * @thorws DailySummaryErrorCode.DAILY_SUMMARY_NOT_FOUND
     */
    public CurationByLocationResponseDto getCurationByCurationCategoryId(Long curationCategoryId){

        //curationCategoryId로 해당하는 curationCategory 가져오기
        CurationCategory curationCategory = curationCategoryRepository.findById(curationCategoryId)
                .orElseThrow(() -> new BusinessException(CurationErrorCode.CURATION_CATEGORY_NOT_FOUND));

        //curationCategoryId로 해당하는 location_id 찾기
        Long locationId2 = curationCategory.getLocation().getId();


        //location_id로 dailySummary 가져오기
        DailySummary dailySummary = dailySummaryRepository.findByLocationId(locationId2)
                .orElseThrow(() -> new BusinessException(DailySummaryErrorCode.DAILY_SUMMARY_NOT_FOUND));


        //가져온 dailySummary에서 season, weather tag 가져오기
        SeasonTag seasonTag = dailySummary.getSeasonTag();
        WeatherTag weatherTag = dailySummary.getWeatherTag();

        String seasonTagText = seasonTag.getName();
        String weatherTagText = weatherTag.getName();


        String locationName = curationCategory.getViewName();

        //태그로 큐레이션 필터링
        List<Curation> curations = curationCategory.getCurations().stream()
                .filter(curation ->
                        curation.getSeasonTag().equals(seasonTag) &&
                        curation.getWeatherTag().equals(weatherTag)
                ).collect(Collectors.toList());

        return CurationCategoryMapper.toCurationByLocationResponseDto(locationId2, locationName, seasonTagText , weatherTagText, curations);
    }


    /**
     * curation 업로드하기
     * @return
     * @thorws
     */
    public void saveCuration(MultipartFile backgroundImage,
                             List<MultipartFile> contentImages,
                             CurationRequestDto dto){

        CurationCategory curationCategory = curationCategoryRepository.findById(dto.curationCategoryId())
                .orElseThrow(()->new BusinessException(CurationErrorCode.CURATION_CATEGORY_NOT_FOUND));

        SeasonTag seasonTag = seasonRepository.findById(dto.seasonTagId())
                .orElseThrow(()-> new BusinessException(TagsErrorCode.SEASON_TAG_NOT_FOUND));

        WeatherTag weatherTag = weatherRepository.findById(dto.weatherTagId())
                .orElseThrow(()-> new BusinessException(TagsErrorCode.WEATHER_TAG_NOT_FOUND));

        //썸네일 s3 변환
        String backgroundImgUrl = s3Uploader.upload(backgroundImage, "curation");

        // Curation 저장
        Curation curation = Curation.builder()
                .title(dto.title())
                .backgroundImgUrl(backgroundImgUrl)
                .curationCategory(curationCategory)
                .seasonTag(seasonTag)
                .weatherTag(weatherTag)
                .build();

        curationRepository.save(curation);

        // curation 세부 Images 저장
        List<CurationImg> imgList = new ArrayList<>();
        for (int i = 0; i < contentImages.size(); i++) {
            MultipartFile imgFile = contentImages.get(i);
            String url = s3Uploader.upload(imgFile, "curationImg");
            String address = dto.imgAddresses().get(i);

            CurationImg img = CurationImg.builder()
                    .imgUrl(url)
                    .imgOrder(i + 1)
                    .imgAddress(address)
                    .curation(curation)
                    .build();

            imgList.add(img);
        }

        curationImgRepository.saveAll(imgList);
    }



}
