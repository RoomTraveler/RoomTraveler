<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<!-- 읽지 않은 알림 개수 표시 -->
<c:if test="${unreadCount > 0}">
    <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
        ${unreadCount}
        <span class="visually-hidden">읽지 않은 알림</span>
    </span>
</c:if>