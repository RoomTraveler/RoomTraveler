<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>내 찜 목록</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .favorite-card {
            transition: transform 0.3s;
            margin-bottom: 20px;
            height: 100%;
        }
        .favorite-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 20px rgba(0,0,0,0.1);
        }
        .card-img-top {
            height: 200px;
            object-fit: cover;
        }
        .empty-favorites {
            text-align: center;
            padding: 50px 0;
        }
        .favorite-actions {
            display: flex;
            justify-content: space-between;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />

    <div class="container mt-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>내 찜 목록</h2>
            <a href="${pageContext.request.contextPath}/accommodation/list" class="btn btn-outline-primary">숙소 더 찾아보기</a>
        </div>

        <!-- 알림 메시지 표시 -->
        <c:if test="${not empty message}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <!-- 에러 메시지 표시 -->
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <!-- 찜 목록 -->
        <c:choose>
            <c:when test="${empty favorites}">
                <div class="empty-favorites">
                    <h3>아직 찜한 숙소가 없습니다.</h3>
                    <p>마음에 드는 숙소를 찾아 하트 아이콘을 클릭하여 찜 목록에 추가해보세요!</p>
                    <a href="${pageContext.request.contextPath}/accommodation/list" class="btn btn-primary mt-3">숙소 찾아보기</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row">
                    <c:forEach var="favorite" items="${favorites}">
                        <div class="col-md-4 mb-4">
                            <div class="card favorite-card">
                                <c:choose>
                                    <c:when test="${not empty favorite.mainImageUrl}">
                                        <img src="${favorite.mainImageUrl}" class="card-img-top" alt="${favorite.accommodationTitle}">
                                    </c:when>
                                    <c:otherwise>
                                        <img src="https://via.placeholder.com/300x200?text=No+Image" class="card-img-top" alt="No Image">
                                    </c:otherwise>
                                </c:choose>
                                <div class="card-body d-flex flex-column">
                                    <h5 class="card-title">${favorite.accommodationTitle}</h5>
                                    <p class="card-text">${favorite.accommodationAddress}</p>
                                    <p class="card-text text-muted">
                                        <small>찜한 날짜: <fmt:formatDate value="${favorite.createdAt}" pattern="yyyy-MM-dd" /></small>
                                    </p>
                                    <div class="favorite-actions mt-auto">
                                        <a href="${pageContext.request.contextPath}/accommodation/detail/${favorite.accommodationId}" class="btn btn-primary">상세 보기</a>
                                        <a href="${pageContext.request.contextPath}/accommodation/remove-favorite/${favorite.favoriteId}" class="btn btn-outline-danger" 
                                           onclick="return confirm('정말로 찜 목록에서 삭제하시겠습니까?');">삭제</a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <jsp:include page="../fragments/footer.jsp" />

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>