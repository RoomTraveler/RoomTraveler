package com.ssafy.trip.tourapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
    private Response response;
    public Response getResponse() { return response; }
    public void setResponse(Response response) { this.response = response; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Body body;
        public Body getBody() { return body; }
        public void setBody(Body body) { this.body = body; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private Items items;
        public Items getItems() { return items; }
        public void setItems(Items items) { this.items = items; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        private List<AreaDto> item;
        public List<AreaDto> getItem() { return item; }
        public void setItem(List<AreaDto> item) { this.item = item; }
    }
}