<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>알림 목록</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <style>
        .notification-item {
            border-left: 4px solid #007bff;
            margin-bottom: 10px;
            padding: 10px;
            background-color: #f8f9fa;
            transition: all 0.3s ease;
        }
        
        .notification-item:hover {
            background-color: #e9ecef;
            transform: translateX(5px);
        }
        
        .notification-item.unread {
            border-left-color: #dc3545;
            background-color: #fff8f8;
        }
        
        .notification-title {
            font-weight: bold;
            margin-bottom: 5px;
        }
        
        .notification-content {
            color: #6c757d;
        }
        
        .notification-time {
            font-size: 0.8rem;
            color: #6c757d;
            text-align: right;
        }
        
        .notification-type {
            display: inline-block;
            padding: 2px 8px;
            border-radius: 10px;
            font-size: 0.8rem;
            margin-right: 10px;
        }
        
        .notification-type.BOOKING {
            background-color: #d1e7dd;
            color: #0f5132;
        }
        
        .notification-type.CANCELLATION {
            background-color: #f8d7da;
            color: #842029;
        }
        
        .notification-type.REVIEW {
            background-color: #cff4fc;
            color: #055160;
        }
        
        .notification-type.SYSTEM {
            background-color: #e2e3e5;
            color: #41464b;
        }
    </style>
</head>
<body>
    <div class="container mt-5">
        <!-- 헤더 포함 -->
        <jsp:include page="../fragments/header.jsp" />
        
        <div class="row mb-4">
            <div class="col">
                <h2>알림 목록</h2>
                <p class="text-muted">
                    <!-- 읽지 않은 알림 개수 표시 -->
                    읽지 않은 알림: <span class="badge bg-danger">${unreadCount}</span>
                </p>
            </div>
            <div class="col-auto">
                <div class="btn-group" role="group">
                    <a href="${root}/notification" class="btn btn-outline-primary">전체 알림</a>
                    <a href="${root}/notification?filter=unread" class="btn btn-outline-danger">읽지 않은 알림</a>
                </div>
                <button id="markAllAsRead" class="btn btn-success ms-2">모두 읽음 표시</button>
            </div>
        </div>
        
        <!-- 알림이 없는 경우 -->
        <c:if test="${empty notifications}">
            <div class="alert alert-info">
                알림이 없습니다.
            </div>
        </c:if>
        
        <!-- 알림 목록 -->
        <div class="notification-list">
            <c:forEach var="notification" items="${notifications}">
                <div class="notification-item ${notification.isRead ? '' : 'unread'}" 
                     data-notification-id="${notification.notificationId}">
                    <div class="row">
                        <div class="col">
                            <span class="notification-type ${notification.notificationType}">
                                ${notification.notificationType}
                            </span>
                            <span class="notification-title">${notification.title}</span>
                        </div>
                        <div class="col-auto">
                            <c:if test="${notification.isRead}">
                                <span class="badge bg-secondary">읽음</span>
                            </c:if>
                            <c:if test="${!notification.isRead}">
                                <span class="badge bg-danger">읽지 않음</span>
                            </c:if>
                        </div>
                    </div>
                    <div class="notification-content">${notification.content}</div>
                    <div class="notification-time">${notification.createdAt}</div>
                    <div class="mt-2">
                        <button class="btn btn-sm btn-outline-primary mark-as-read" 
                                data-notification-id="${notification.notificationId}"
                                ${notification.isRead ? 'disabled' : ''}>
                            읽음 표시
                        </button>
                        <button class="btn btn-sm btn-outline-danger delete-notification" 
                                data-notification-id="${notification.notificationId}">
                            삭제
                        </button>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
    
    <!-- 자바스크립트 -->
    <script>
        // 알림 읽음 표시
        document.querySelectorAll('.mark-as-read').forEach(button => {
            button.addEventListener('click', function() {
                const notificationId = this.getAttribute('data-notification-id');
                markAsRead(notificationId, this);
            });
        });
        
        // 알림 삭제
        document.querySelectorAll('.delete-notification').forEach(button => {
            button.addEventListener('click', function() {
                const notificationId = this.getAttribute('data-notification-id');
                deleteNotification(notificationId, this);
            });
        });
        
        // 모두 읽음 표시
        document.getElementById('markAllAsRead').addEventListener('click', function() {
            markAllAsRead();
        });
        
        // 알림 읽음 표시 함수
        function markAsRead(notificationId, button) {
            fetch(`/api/notifications/${notificationId}/read`, {
                method: 'PUT'
            })
            .then(response => {
                if (response.ok) {
                    // 버튼 비활성화
                    button.disabled = true;
                    // 알림 항목 스타일 변경
                    const notificationItem = button.closest('.notification-item');
                    notificationItem.classList.remove('unread');
                    // 읽음 상태 배지 변경
                    const badge = notificationItem.querySelector('.badge');
                    badge.classList.remove('bg-danger');
                    badge.classList.add('bg-secondary');
                    badge.textContent = '읽음';
                    // 읽지 않은 알림 카운트 업데이트
                    updateUnreadCount(-1);
                }
            })
            .catch(error => console.error('Error:', error));
        }
        
        // 알림 삭제 함수
        function deleteNotification(notificationId, button) {
            if (!confirm('이 알림을 삭제하시겠습니까?')) return;
            
            fetch(`/api/notifications/${notificationId}`, {
                method: 'DELETE'
            })
            .then(response => {
                if (response.ok) {
                    // 알림 항목 제거
                    const notificationItem = button.closest('.notification-item');
                    // 읽지 않은 알림이었다면 카운트 업데이트
                    if (notificationItem.classList.contains('unread')) {
                        updateUnreadCount(-1);
                    }
                    notificationItem.remove();
                    
                    // 알림이 모두 삭제되었는지 확인
                    if (document.querySelectorAll('.notification-item').length === 0) {
                        const alertDiv = document.createElement('div');
                        alertDiv.className = 'alert alert-info';
                        alertDiv.textContent = '알림이 없습니다.';
                        document.querySelector('.notification-list').appendChild(alertDiv);
                    }
                }
            })
            .catch(error => console.error('Error:', error));
        }
        
        // 모두 읽음 표시 함수
        function markAllAsRead() {
            const userId = '${userId}'; // 서버에서 전달된 사용자 ID
            
            fetch(`/api/notifications/user/${userId}/read-all`, {
                method: 'PUT'
            })
            .then(response => {
                if (response.ok) {
                    // 모든 알림 항목 스타일 변경
                    document.querySelectorAll('.notification-item.unread').forEach(item => {
                        item.classList.remove('unread');
                        // 읽음 상태 배지 변경
                        const badge = item.querySelector('.badge');
                        badge.classList.remove('bg-danger');
                        badge.classList.add('bg-secondary');
                        badge.textContent = '읽음';
                        // 읽음 버튼 비활성화
                        const button = item.querySelector('.mark-as-read');
                        button.disabled = true;
                    });
                    
                    // 읽지 않은 알림 카운트 초기화
                    document.querySelector('.badge.bg-danger').textContent = '0';
                }
            })
            .catch(error => console.error('Error:', error));
        }
        
        // 읽지 않은 알림 카운트 업데이트 함수
        function updateUnreadCount(change) {
            const unreadCountBadge = document.querySelector('.badge.bg-danger');
            let count = parseInt(unreadCountBadge.textContent) + change;
            count = Math.max(0, count); // 음수가 되지 않도록
            unreadCountBadge.textContent = count;
        }
    </script>
</body>
</html>