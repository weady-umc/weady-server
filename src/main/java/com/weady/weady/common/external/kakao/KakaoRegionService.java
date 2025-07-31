package com.weady.weady.common.external.kakao;

import com.weady.weady.common.error.errorCode.KakaoErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoRegionService {
    private final KakaoClient kakaoClient;

    /**
     * 위도(longitude), 경도(latitude)를 받아서 법정동 코드(b_code)를 반환
     */
    public String getBCodeByCoordinates(double longitude, double latitude) {
        KakaoApiResponse response = kakaoClient.getRegionInfo(longitude, latitude);

        if (response == null || response.documents() == null || response.documents().isEmpty()) {
            throw new BusinessException(KakaoErrorCode.KAKAO_RESPONSE_ERROR);
        }

        return response.documents().get(0).code(); 
    }
}
