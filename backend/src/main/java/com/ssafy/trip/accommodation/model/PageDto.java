package com.ssafy.trip.accommodation.model;

import java.util.List;
import org.springframework.data.domain.Page;

public class PageDto<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private int numberOfElements; // 현재 페이지의 요소 수
    private boolean empty; // 페이지가 비어 있는지 여부

    // 기본 생성자 (JSON 역직렬화를 위해 필요할 수 있음)
    public PageDto() {}

    public PageDto(Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
        this.first = page.isFirst();
        this.numberOfElements = page.getNumberOfElements();
        this.empty = page.isEmpty();
    }

    // Getters
    public List<T> getContent() {
        return content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isLast() {
        return last;
    }

    public boolean isFirst() {
        return first;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public boolean isEmpty() {
        return empty;
    }

    // Setters (필요한 경우 추가)
    public void setContent(List<T> content) {
        this.content = content;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
} 