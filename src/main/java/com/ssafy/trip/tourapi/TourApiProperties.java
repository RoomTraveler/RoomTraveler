package com.ssafy.trip.tourapi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tourapi")
public class TourApiProperties {
    private String serviceKey;
    private String baseUrl;
    private String mobileOs;
    private String mobileApp;
    private int defaultNumOfRows;
    private int defaultPageNo;

    // getters / setters
    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getMobileOs() { return mobileOs; }
    public void setMobileOs(String mobileOs) { this.mobileOs = mobileOs; }

    public String getMobileApp() { return mobileApp; }
    public void setMobileApp(String mobileApp) { this.mobileApp = mobileApp; }

    public int getDefaultNumOfRows() { return defaultNumOfRows; }
    public void setDefaultNumOfRows(int defaultNumOfRows) { this.defaultNumOfRows = defaultNumOfRows; }

    public int getDefaultPageNo() { return defaultPageNo; }
    public void setDefaultPageNo(int defaultPageNo) { this.defaultPageNo = defaultPageNo; }
}