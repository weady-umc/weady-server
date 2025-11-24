package com.weady.weady.domain.weather.dto.scheduler;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class MidTempItem {
    private Integer taMin3; private Integer taMax3;
    private Integer taMin4; private Integer taMax4;
    private Integer taMin5; private Integer taMax5;
    private Integer taMin6; private Integer taMax6;
    private Integer taMin7; private Integer taMax7;
}
