package com.weady.weady.domain.weather.dto.scheduler;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KmaResponse<T> {
    @Getter @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response<T> {
        @Getter @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Body<T> {
            @Getter @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Items<T> { private List<T> item; }
            private Items<T> items;
        }
        private Body<T> body;
    }
    private Response<T> response;

    public List<T> items() {
        if (response == null || response.getBody() == null) return List.of();
        var items = response.getBody().getItems();
        return (items == null || items.getItem() == null) ? List.of() : items.getItem();
    }
}