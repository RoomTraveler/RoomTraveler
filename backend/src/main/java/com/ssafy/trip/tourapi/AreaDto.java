package com.ssafy.trip.tourapi;

import com.fasterxml.jackson.annotation.JsonAlias;

public class AreaDto {
    @JsonAlias({"code", "areaCode"})
    private String code;

    @JsonAlias({"name", "areaName"})
    private String name;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
