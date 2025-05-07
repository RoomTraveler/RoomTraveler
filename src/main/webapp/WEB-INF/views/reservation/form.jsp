<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>예약하기</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        .reservation-summary {
            background-color: #f8f9fa;
            border-radius: 5px;
            padding: 20px;
            margin-bottom: 20px;
        }
        .price-detail {
            border-top: 1px solid #dee2e6;
            padding-top: 15px;
            margin-top: 15px;
        }
        .total-price {
            font-size: 1.2rem;
            font-weight: bold;
        }
        .payment-method-card {
            cursor: pointer;
            transition: all 0.3s;
        }
        .payment-method-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        .payment-method-card.selected {
            border-color: #0d6efd;
            background-color: #f0f7ff;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />
    
    <div class="container mt-5 mb-5">
        <h2 class="mb-4">예약하기</h2>
        
        <!-- 예약 정보 요약 -->
        <div class="row mb-4">
            <div class="col-md-8">
                <div class="card">
                    <div class="card-body">
                        <h4 class="card-title">예약 정보</h4>
                        <div class="row">
                            <div class="col-md-4">
                                <c:choose>
                                    <c:when test="${not empty room.mainImageUrl}">
                                        <img src="${room.mainImageUrl}" class="img-fluid rounded" alt="${room.name}">
                                    </c:when>
                                    <c:otherwise>
                                        <img src="${pageContext.request.contextPath}/resources/images/no-image.jpg" class="img-fluid rounded" alt="이미지 없음">
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="col-md-8">
                                <h5>${room.name}</h5>
                                <p class="text-muted">${accommodation.title}</p>
                                <p><i class="bi bi-geo-alt"></i> ${accommodation.address}</p>
                                <p><i class="bi bi-calendar-check"></i> 체크인: <strong>${checkInDate}</strong> (${accommodation.checkInTime})</p>
                                <p><i class="bi bi-calendar-x"></i> 체크아웃: <strong>${checkOutDate}</strong> (${accommodation.checkOutTime})</p>
                                <p><i class="bi bi-people"></i> 인원: <strong>${guestCount}명</strong> (최대 ${room.capacity}명)</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="col-md-4">
                <div class="card">
                    <div class="card-body">
                        <h4 class="card-title">가격 정보</h4>
                        <p><i class="bi bi-currency-dollar"></i> 1박 요금: <fmt:formatNumber value="${room.price}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></p>
                        <p><i class="bi bi-calendar-week"></i> 숙박 일수: ${nights}박</p>
                        <div class="price-detail">
                            <p>객실 요금: <fmt:formatNumber value="${room.price * nights}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></p>
                            <p>세금 및 수수료: <fmt:formatNumber value="${room.price * nights * 0.1}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></p>
                            <p class="total-price">총 요금: <fmt:formatNumber value="${totalPrice}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- 예약자 정보 및 결제 정보 -->
        <form action="${pageContext.request.contextPath}/reservation/create" method="post">
            <input type="hidden" name="roomId" value="${room.roomId}">
            <input type="hidden" name="checkInDate" value="${checkInDate}">
            <input type="hidden" name="checkOutDate" value="${checkOutDate}">
            <input type="hidden" name="guestCount" value="${guestCount}">
            <input type="hidden" name="totalPrice" value="${totalPrice}">
            
            <div class="row">
                <div class="col-md-8">
                    <!-- 예약자 정보 -->
                    <div class="card mb-4">
                        <div class="card-body">
                            <h4 class="card-title">예약자 정보</h4>
                            <div class="mb-3">
                                <label for="guestName" class="form-label">이름</label>
                                <input type="text" class="form-control" id="guestName" name="guestName" value="${sessionScope.username}" required>
                            </div>
                            <div class="mb-3">
                                <label for="guestEmail" class="form-label">이메일</label>
                                <input type="email" class="form-control" id="guestEmail" name="guestEmail" value="${sessionScope.email}" required>
                            </div>
                            <div class="mb-3">
                                <label for="guestPhone" class="form-label">전화번호</label>
                                <input type="tel" class="form-control" id="guestPhone" name="guestPhone" placeholder="010-0000-0000" required>
                            </div>
                        </div>
                    </div>
                    
                    <!-- 결제 방법 -->
                    <div class="card mb-4">
                        <div class="card-body">
                            <h4 class="card-title">결제 방법</h4>
                            <div class="row">
                                <div class="col-md-4 mb-3">
                                    <div class="card payment-method-card" onclick="selectPaymentMethod('card')">
                                        <div class="card-body text-center">
                                            <i class="bi bi-credit-card fs-1"></i>
                                            <h5 class="mt-2">신용카드</h5>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4 mb-3">
                                    <div class="card payment-method-card" onclick="selectPaymentMethod('bank')">
                                        <div class="card-body text-center">
                                            <i class="bi bi-bank fs-1"></i>
                                            <h5 class="mt-2">계좌이체</h5>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4 mb-3">
                                    <div class="card payment-method-card" onclick="selectPaymentMethod('phone')">
                                        <div class="card-body text-center">
                                            <i class="bi bi-phone fs-1"></i>
                                            <h5 class="mt-2">휴대폰 결제</h5>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <input type="hidden" id="paymentMethod" name="paymentMethod" value="">
                            <div id="paymentDetails" class="mt-3" style="display: none;">
                                <!-- 결제 방법에 따른 추가 입력 필드 -->
                            </div>
                        </div>
                    </div>
                    
                    <!-- 요청 사항 -->
                    <div class="card mb-4">
                        <div class="card-body">
                            <h4 class="card-title">요청 사항</h4>
                            <div class="mb-3">
                                <label for="specialRequests" class="form-label">호스트에게 전달할 메시지</label>
                                <textarea class="form-control" id="specialRequests" name="specialRequests" rows="3" placeholder="특별한 요청 사항이 있으면 입력해주세요."></textarea>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="col-md-4">
                    <!-- 예약 요약 및 결제 버튼 -->
                    <div class="card sticky-top" style="top: 20px;">
                        <div class="card-body">
                            <h4 class="card-title">예약 요약</h4>
                            <p><i class="bi bi-building"></i> ${accommodation.title}</p>
                            <p><i class="bi bi-door-closed"></i> ${room.name}</p>
                            <p><i class="bi bi-calendar-check"></i> 체크인: ${checkInDate}</p>
                            <p><i class="bi bi-calendar-x"></i> 체크아웃: ${checkOutDate}</p>
                            <p><i class="bi bi-people"></i> 인원: ${guestCount}명</p>
                            <div class="price-detail">
                                <p class="total-price">총 요금: <fmt:formatNumber value="${totalPrice}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></p>
                            </div>
                            <div class="form-check mb-3">
                                <input class="form-check-input" type="checkbox" id="agreeTerms" required>
                                <label class="form-check-label" for="agreeTerms">
                                    <a href="#" data-bs-toggle="modal" data-bs-target="#termsModal">이용약관</a>에 동의합니다.
                                </label>
                            </div>
                            <div class="d-grid">
                                <button type="submit" class="btn btn-primary btn-lg" id="payButton" disabled>결제하기</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </div>
    
    <!-- 이용약관 모달 -->
    <div class="modal fade" id="termsModal" tabindex="-1" aria-labelledby="termsModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="termsModalLabel">이용약관</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <h5>예약 및 결제 약관</h5>
                    <p>1. 예약 확정 후 취소 시 환불 규정에 따라 수수료가 부과될 수 있습니다.</p>
                    <p>2. 체크인 시간은 ${accommodation.checkInTime}, 체크아웃 시간은 ${accommodation.checkOutTime}입니다.</p>
                    <p>3. 최대 인원을 초과하는 경우 추가 요금이 발생하거나 입실이 거부될 수 있습니다.</p>
                    <p>4. 객실 내 흡연은 금지되어 있으며, 위반 시 추가 청소비가 청구될 수 있습니다.</p>
                    <p>5. 예약자와 실제 투숙객의 정보가 일치해야 합니다.</p>
                    
                    <h5 class="mt-4">환불 정책</h5>
                    <p>- 체크인 7일 전 취소: 100% 환불</p>
                    <p>- 체크인 5일 전 취소: 70% 환불</p>
                    <p>- 체크인 3일 전 취소: 50% 환불</p>
                    <p>- 체크인 1일 전 취소: 환불 불가</p>
                    <p>- 노쇼(No-show): 환불 불가</p>
                    
                    <h5 class="mt-4">개인정보 수집 및 이용</h5>
                    <p>1. 수집항목: 이름, 이메일, 전화번호</p>
                    <p>2. 수집목적: 예약 확인 및 서비스 제공</p>
                    <p>3. 보유기간: 예약 완료 후 3년</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="../fragments/footer.jsp" />
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 결제 방법 선택
        function selectPaymentMethod(method) {
            // 모든 카드에서 selected 클래스 제거
            document.querySelectorAll('.payment-method-card').forEach(card => {
                card.classList.remove('selected');
            });
            
            // 선택된 카드에 selected 클래스 추가
            document.querySelector(`.payment-method-card[onclick="selectPaymentMethod('${method}')"]`).classList.add('selected');
            
            // 선택된 결제 방법 저장
            document.getElementById('paymentMethod').value = method;
            
            // 결제 방법에 따른 추가 입력 필드 표시
            const paymentDetails = document.getElementById('paymentDetails');
            paymentDetails.style.display = 'block';
            
            switch(method) {
                case 'card':
                    paymentDetails.innerHTML = `
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label for="cardNumber" class="form-label">카드 번호</label>
                                <input type="text" class="form-control" id="cardNumber" placeholder="0000-0000-0000-0000" required>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label for="cardName" class="form-label">카드 소유자 이름</label>
                                <input type="text" class="form-control" id="cardName" required>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label for="expiryDate" class="form-label">유효기간</label>
                                <input type="text" class="form-control" id="expiryDate" placeholder="MM/YY" required>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label for="cvv" class="form-label">CVV</label>
                                <input type="text" class="form-control" id="cvv" placeholder="123" required>
                            </div>
                        </div>
                    `;
                    break;
                case 'bank':
                    paymentDetails.innerHTML = `
                        <div class="mb-3">
                            <label for="bankName" class="form-label">은행명</label>
                            <select class="form-select" id="bankName" required>
                                <option value="">은행을 선택하세요</option>
                                <option value="KB">KB국민은행</option>
                                <option value="Shinhan">신한은행</option>
                                <option value="Woori">우리은행</option>
                                <option value="Hana">하나은행</option>
                                <option value="IBK">기업은행</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label for="accountNumber" class="form-label">계좌번호</label>
                            <input type="text" class="form-control" id="accountNumber" required>
                        </div>
                        <div class="mb-3">
                            <label for="accountHolder" class="form-label">예금주</label>
                            <input type="text" class="form-control" id="accountHolder" required>
                        </div>
                    `;
                    break;
                case 'phone':
                    paymentDetails.innerHTML = `
                        <div class="mb-3">
                            <label for="phoneNumber" class="form-label">휴대폰 번호</label>
                            <input type="tel" class="form-control" id="phoneNumber" placeholder="010-0000-0000" required>
                        </div>
                        <div class="mb-3">
                            <label for="carrier" class="form-label">통신사</label>
                            <select class="form-select" id="carrier" required>
                                <option value="">통신사를 선택하세요</option>
                                <option value="SKT">SKT</option>
                                <option value="KT">KT</option>
                                <option value="LGU+">LGU+</option>
                                <option value="알뜰폰">알뜰폰</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label for="birthDate" class="form-label">생년월일</label>
                            <input type="text" class="form-control" id="birthDate" placeholder="YYMMDD" required>
                        </div>
                    `;
                    break;
            }
            
            // 결제 버튼 활성화 여부 확인
            checkPayButtonStatus();
        }
        
        // 이용약관 동의 체크박스 이벤트
        document.getElementById('agreeTerms').addEventListener('change', function() {
            checkPayButtonStatus();
        });
        
        // 결제 버튼 활성화 여부 확인
        function checkPayButtonStatus() {
            const paymentMethod = document.getElementById('paymentMethod').value;
            const agreeTerms = document.getElementById('agreeTerms').checked;
            
            document.getElementById('payButton').disabled = !(paymentMethod && agreeTerms);
        }
    </script>
</body>
</html>