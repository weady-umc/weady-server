package com.weady.weady.domain.weather.dto.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class MidLandItem {
    // 하늘(3~7일)
    private String wf3Am; private String wf3Pm;
    private String wf4Am; private String wf4Pm;
    private String wf5Am; private String wf5Pm;
    private String wf6Am; private String wf6Pm;
    private String wf7Am; private String wf7Pm;

    // POP(3~7일)
    private Integer rnSt3Am; private Integer rnSt3Pm;
    private Integer rnSt4Am; private Integer rnSt4Pm;
    private Integer rnSt5Am; private Integer rnSt5Pm;
    private Integer rnSt6Am; private Integer rnSt6Pm;
    private Integer rnSt7Am; private Integer rnSt7Pm;
}