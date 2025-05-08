<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>장바구니 - 여행의 즐거움</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .cart-item {
            border: 1px solid #ddd;
            border-radius: 8px;
            margin-bottom: 20px;
            padding: 15px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .cart-item-image {
            width: 100%;
            height: 200px;
            object-fit: cover;
            border-radius: 4px;
        }
        .cart-summary {
            background-color: #f8f9fa;
            border-radius: 8px;
            padding: 20px;
            position: sticky;
            top: 20px;
        }
        .empty-cart {
            text-align: center;
            padding: 50px 0;
        }
        .empty-cart i {
            font-size: 48px;
            color: #ccc;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />
    
    <div class="container my-5">
        <h2 class="mb-4">장바구니</h2>
        
        <c:if test="${not empty message}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <c:choose>
            <c:when test="${empty cart.items}">
                <div class="empty-cart">
                    <i class="bi bi-cart"></i>
                    <h3>장바구니가 비어있습니다</h3>
                    <p>객실을 장바구니에 추가해보세요!</p>
                    <a href="/accommodation/list" class="btn btn-primary">객실 둘러보기</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row">
                    <div class="col-md-8">
                        <c:forEach items="${cart.items}" var="item">
                            <div class="cart-item">
                                <div class="row">
                                    <div class="col-md-4">
                                        <c:choose>
                                            <c:when test="${not empty item.imageUrl}">
                                                <img src="${item.imageUrl}" alt="${item.roomName}" class="cart-item-image">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="/img/no-image.jpg" alt="이미지 없음" class="cart-item-image">
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="col-md-8">
                                        <h4>${item.accommodationTitle}</h4>
                                        <h5>${item.roomName}</h5>
                                        <p>
                                            <strong>체크인:</strong> ${item.checkInDate}<br>
                                            <strong>체크아웃:</strong> ${item.checkOutDate}<br>
                                            <strong>숙박일수:</strong> ${item.nights}박<br>
                                            <strong>인원:</strong> ${item.guestCount}명<br>
                                            <strong>가격:</strong> <fmt:formatNumber value="${item.price}" type="currency" currencySymbol="₩" maxFractionDigits="0"/>
                                        </p>
                                        <div class="d-flex justify-content-end">
                                            <a href="/cart/remove/${item.cartItemId}" class="btn btn-outline-danger">삭제</a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                    <div class="col-md-4">
                        <div class="cart-summary">
                            <h4 class="mb-3">주문 요약</h4>
                            <p><strong>총 객실 수:</strong> ${cart.totalItems}개</p>
                            <p><strong>총 가격:</strong> <fmt:formatNumber value="${cart.totalPrice}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></p>
                            
                            <form action="/cart/checkout" method="post">
                                <button type="submit" class="btn btn-primary w-100 mb-2">예약하기</button>
                            </form>
                            
                            <a href="/cart/clear" class="btn btn-outline-secondary w-100">장바구니 비우기</a>
                        </div>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
    
    <jsp:include page="../fragments/footer.jsp" />
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>