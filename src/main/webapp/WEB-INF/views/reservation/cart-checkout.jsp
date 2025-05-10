<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>예약 확인 - 여행의 즐거움</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .checkout-item {
            border: 1px solid #ddd;
            border-radius: 8px;
            margin-bottom: 20px;
            padding: 15px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .checkout-summary {
            background-color: #f8f9fa;
            border-radius: 8px;
            padding: 20px;
            position: sticky;
            top: 20px;
        }
        .checkout-item-image {
            width: 100%;
            height: 150px;
            object-fit: cover;
            border-radius: 4px;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />
    
    <div class="container my-5">
        <h2 class="mb-4">예약 확인</h2>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <div class="row">
            <div class="col-md-8">
                <div class="card mb-4">
                    <div class="card-header">
                        <h4>예약 정보</h4>
                    </div>
                    <div class="card-body">
                        <c:forEach items="${cartItems}" var="item" varStatus="status">
                            <div class="checkout-item">
                                <div class="row">
                                    <div class="col-md-4">
                                        <c:choose>
                                            <c:when test="${not empty item.imageUrl}">
                                                <img src="${item.imageUrl}" alt="${item.roomName}" class="checkout-item-image">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="/img/no-image.jpg" alt="이미지 없음" class="checkout-item-image">
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="col-md-8">
                                        <h5>${item.accommodationTitle}</h5>
                                        <h6>${item.roomName}</h6>
                                        <p>
                                            <strong>체크인:</strong> ${item.checkInDate}<br>
                                            <strong>체크아웃:</strong> ${item.checkOutDate}<br>
                                            <strong>숙박일수:</strong> ${item.nights}박<br>
                                            <strong>인원:</strong> ${item.guestCount}명<br>
                                            <strong>가격:</strong> <fmt:formatNumber value="${item.price}" type="currency" currencySymbol="₩" maxFractionDigits="0"/>
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </div>
                
                <form action="/reservation/create-from-cart" method="post">
                    <div class="card mb-4">
                        <div class="card-header">
                            <h4>특별 요청 사항</h4>
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <textarea name="specialRequests" class="form-control" rows="4" placeholder="호스트에게 전달할 특별 요청 사항이 있으면 입력해주세요."></textarea>
                            </div>
                        </div>
                    </div>
                    
                    <div class="card mb-4">
                        <div class="card-header">
                            <h4>이용 약관</h4>
                        </div>
                        <div class="card-body">
                            <div class="form-check mb-3">
                                <input class="form-check-input" type="checkbox" id="termsCheck" required>
                                <label class="form-check-label" for="termsCheck">
                                    만 14세 이상이며 이용약관에 동의합니다.
                                </label>
                            </div>
                        </div>
                    </div>
                    
                    <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                        <a href="/cart" class="btn btn-outline-secondary me-md-2">장바구니로 돌아가기</a>
                        <button type="submit" class="btn btn-primary">결제하기</button>
                    </div>
                </form>
            </div>
            
            <div class="col-md-4">
                <div class="checkout-summary">
                    <h4 class="mb-3">결제 요약</h4>
                    <p><strong>총 객실 수:</strong> ${cartItems.size()}개</p>
                    <p><strong>총 가격:</strong> <fmt:formatNumber value="${totalPrice}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></p>
                    <hr>
                    <p class="mb-0"><small>* 결제는 예약 완료 후 진행됩니다.</small></p>
                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="../fragments/footer.jsp" />
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>