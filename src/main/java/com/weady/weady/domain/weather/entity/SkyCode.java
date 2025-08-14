package com.weady.weady.domain.weather.entity;

import lombok.Getter;

@Getter
public enum SkyCode {
    CLEAR("맑음"),          // SKY: 1
    PARTLY_CLOUDY("구름많음"), // SKY: 3
    CLOUDY("흐림"),         // SKY: 4
    RAIN("비"),             // PTY: 1, 2, 5
    SNOW("눈"),             // PTY: 3, 6, 7
    ;
    private final String description;
    SkyCode(String description) {
        this.description = description;
    }

    /**
     * 기상청 API 의 PTY(강수형태), SKY(하늘상태) 코드를 내부 SkyCode 로 변환
     * @param ptyCode 강수형태 코드 (0: 없음, 1: 비, 2: 비/눈, 3: 눈, 5: 빗방울, 6: 빗방울/눈날림, 7: 눈날림)
     * @param skyCode 하늘상태 코드 (1: 맑음, 3: 구름많음, 4: 흐림)
     * @return 매핑된 SkyCode
     */
    public static SkyCode fromKmaCodes(String ptyCode, String skyCode) {
        // 강수형태(PTY)가 우선순위가 높다.
        return switch (ptyCode) {
            case "1", "2", "5" -> RAIN;
            case "3", "6", "7" -> SNOW;
            default ->

                // 강수형태가 '없음(0)'일 경우, 하늘상태(SKY)로 판단한다.
                    switch (skyCode) {
                        case "1" -> CLEAR;
                        case "3" -> PARTLY_CLOUDY;
                        case "4" -> CLOUDY;
                        default ->
                                null;
                    };
        };

    }

    public static SkyCode fromKmaText(String text) {
        if (text == null) return CLEAR;
        String t = text.trim();
        // 비/눈 같이 섞이면 눈 우선
        if (t.contains("눈")) return SNOW;
        if (t.contains("비") || t.contains("소나기") || t.contains("빗방울")) return RAIN;
        if (t.contains("흐")) return CLOUDY;          // "흐림"
        if (t.contains("구름")) return PARTLY_CLOUDY;  // "구름많음"
        if (t.contains("맑")) return CLEAR;            // "맑음"
        return CLEAR;
    }


}
