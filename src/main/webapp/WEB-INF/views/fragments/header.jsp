<!-- /fragments/header.jsp -->
<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%-- 페이지들이 가져갈 공통적인 내용들 위치 --%>
<link
  href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
  rel="stylesheet"
/>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<style>
  header > #logo {
    width: 90px;
    margin-bottom: 8px;
    margin-left: 10px;
  }

  header > h1 {
    line-height: 50px;
    display: inline-block;
    height: 50px;
  }
</style>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="root" value="${pageContext.servletContext.contextPath }" />
<div class="container">
  <!-- 전체 .container 시작 -->
  <header class="d-flex justify-content-center my-5 align-items-center">
    <h1 class="text-center">Welcome To</h1>
    <img src="${root }/img/ssafy_logo.png" id="logo" />
  </header>
  <div class="d-flex justify-content-end">
    <a href="${root }/user" class="mx-3">메인으로</a> |
    <a href = "${root }/trip?action=get-plan" class="mx-3">나의 여행지 리스트</a> |
    <a href="${root }/host" class="mx-3 text-success">호스트 사이트</a>
    <!-- 회원가입을 위한 페이지를 요청하는 링크 -->
    <c:if test="${empty username }">
    	| <a href="/user/regist-user-form" class="mx-3">회원가입</a> |
    	<a href="/user/login-form" class="mx-3">로그인</a>
    </c:if>
    <c:if test="${not empty username }">
    	| 
    	<!-- 알림 아이콘 -->
    	<a href="${root}/notification" class="mx-3 position-relative">
    	  <i class="bi bi-bell-fill"></i>
    	  <!-- 알림 카운트는 AJAX로 로드됨 -->
    	  <div id="notification-count-container"></div>
    	</a> |
    	<a href="/user/logout" class="mx-3">로그아웃</a>
    </c:if>

  </div>
  <hr />
  <!-- TODO: 07-2. <script>에서 파라미터 또는 attribute에 alertMsg가이 있다면 alert으로 출력하세요. -->
  <script>
  	const msg = `${alertMsg}` || `${param.alertMsg}`;
  	if (msg) {
  		alert(msg);
  	}
  </script>

  <!--END-->

  <!--TODO: 11-2. session 영역에 저장된 alertMsg를 삭제하세요.-->
  <script>
  	<c:remove var="alertMsg"/>
  </script>

  <!-- 알림 카운트 로드 -->
  <c:if test="${not empty userId}">
    <script>
      // 페이지 로드 시 알림 카운트 로드
      document.addEventListener('DOMContentLoaded', function() {
        loadNotificationCount();
      });

      // 알림 카운트 로드 함수
      function loadNotificationCount() {
        fetch('${root}/notification/count/unread')
          .then(response => response.text())
          .then(html => {
            document.getElementById('notification-count-container').innerHTML = html;
          })
          .catch(error => console.error('알림 카운트 로드 중 오류:', error));
      }

      // 30초마다 알림 카운트 갱신
      setInterval(loadNotificationCount, 30000);
    </script>
  </c:if>

  <!-- END -->
</div>
