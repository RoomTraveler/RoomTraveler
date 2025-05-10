<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>결제하기</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        .payment-method-card {
            cursor: pointer;
            transition: all 0.3s;
            border: 2px solid #dee2e6;
            border-radius: 10px;
            padding: 20px;
            text-align: center;
            margin-bottom: 20px;
        }
        .payment-method-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        .payment-method-card.selected {
            border-color: #0d6efd;
            background-color: #f0f7ff;
        }
        .payment-icon {
            font-size: 2.5rem;
            margin-bottom: 10px;
            color: #0d6efd;
        }
        .payment-summary {
            background-color: #f8f9fa;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 20px;
        }
        .total-price {
            font-size: 1.5rem;
            font-weight: bold;
            color: #dc3545;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />
    
    <div class="container mt-5 mb-5">
        <h2 class="mb-4">결제하기</h2>
        
        <!-- 예약 정보 요약 -->
        <div class="payment-summary">
            <div class="row">
                <div class="col-md-6">
                    <h4>예약 정보</h4>
                    <p><strong>예약 번호:</strong> ${reservation.reservationId}</p>
                    <p><strong>숙소:</strong> ${reservation.accommodationTitle}</p>
                    <p><strong>객실:</strong> ${reservation.roomName}</p>
                    <p><strong>체크인:</strong> <fmt:formatDate value="${reservation.checkInDate}" pattern="yyyy-MM-dd" /></p>
                    <p><strong>체크아웃:</strong> <fmt:formatDate value="${reservation.checkOutDate}" pattern="yyyy-MM-dd" /></p>
                    <p><strong>인원:</strong> ${reservation.guestCount}명</p>
                </div>
                <div class="col-md-6">
                    <h4>결제 금액</h4>
                    <p><strong>객실 요금:</strong> <fmt:formatNumber value="${reservation.totalPrice}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></p>
                    <p><strong>세금 및 수수료:</strong> <fmt:formatNumber value="${reservation.totalPrice * 0.1}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></p>
                    <p class="total-price"><strong>총 결제 금액:</strong> <fmt:formatNumber value="${reservation.totalPrice * 1.1}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></p>
                </div>
            </div>
        </div>
        
        <!-- 결제 폼 -->
        <form id="paymentForm" action="${pageContext.request.contextPath}/payment/process" method="post">
            <input type="hidden" name="reservationId" value="${reservation.reservationId}">
            <input type="hidden" name="amount" value="${reservation.totalPrice * 1.1}">
            <input type="hidden" id="paymentMethod" name="paymentMethod" value="">
            
            <div class="card mb-4">
                <div class="card-body">
                    <h4 class="card-title">결제 방법 선택</h4>
                    <div class="row">
                        <div class="col-md-4">
                            <div class="payment-method-card" data-method="CARD" onclick="selectPaymentMethod('CARD')">
                                <div class="payment-icon"><i class="bi bi-credit-card"></i></div>
                                <h5>신용카드</h5>
                                <p class="text-muted">모든 신용/체크카드 사용 가능</p>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="payment-method-card" data-method="BANK_TRANSFER" onclick="selectPaymentMethod('BANK_TRANSFER')">
                                <div class="payment-icon"><i class="bi bi-bank"></i></div>
                                <h5>계좌이체</h5>
                                <p class="text-muted">실시간 계좌이체로 결제</p>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="payment-method-card" data-method="PHONE" onclick="selectPaymentMethod('PHONE')">
                                <div class="payment-icon"><i class="bi bi-phone"></i></div>
                                <h5>휴대폰 결제</h5>
                                <p class="text-muted">통신요금과 함께 청구</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- 결제 방법에 따른 추가 입력 필드 -->
            <div id="paymentDetails" class="card mb-4" style="display: none;">
                <div class="card-body">
                    <h4 class="card-title">결제 정보 입력</h4>
                    <div id="cardPaymentForm" style="display: none;">
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label for="cardNumber" class="form-label">카드 번호</label>
                                <input type="text" class="form-control" id="cardNumber" name="cardInfo" placeholder="0000-0000-0000-0000" required>
                            </div>
                            <div class="col-md-6">
                                <label for="cardExpiry" class="form-label">유효기간</label>
                                <input type="text" class="form-control" id="cardExpiry" placeholder="MM/YY" required>
                            </div>
                        </div>
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label for="cardHolder" class="form-label">카드 소유자</label>
                                <input type="text" class="form-control" id="cardHolder" placeholder="카드에 표시된 이름" required>
                            </div>
                            <div class="col-md-6">
                                <label for="cardCvv" class="form-label">보안코드 (CVV)</label>
                                <input type="text" class="form-control" id="cardCvv" placeholder="카드 뒷면 3자리" required>
                            </div>
                        </div>
                    </div>
                    
                    <div id="bankTransferForm" style="display: none;">
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label for="bankName" class="form-label">은행 선택</label>
                                <select class="form-select" id="bankName" required>
                                    <option value="">은행을 선택하세요</option>
                                    <option value="KB국민은행">KB국민은행</option>
                                    <option value="신한은행">신한은행</option>
                                    <option value="우리은행">우리은행</option>
                                    <option value="하나은행">하나은행</option>
                                    <option value="NH농협은행">NH농협은행</option>
                                    <option value="IBK기업은행">IBK기업은행</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label for="accountNumber" class="form-label">계좌번호</label>
                                <input type="text" class="form-control" id="accountNumber" name="bankInfo" placeholder="'-' 없이 입력" required>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label for="accountHolder" class="form-label">예금주</label>
                            <input type="text" class="form-control" id="accountHolder" placeholder="예금주 이름" required>
                        </div>
                    </div>
                    
                    <div id="phonePaymentForm" style="display: none;">
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label for="phoneNumber" class="form-label">휴대폰 번호</label>
                                <input type="text" class="form-control" id="phoneNumber" name="phoneInfo" placeholder="010-0000-0000" required>
                            </div>
                            <div class="col-md-6">
                                <label for="carrier" class="form-label">통신사</label>
                                <select class="form-select" id="carrier" required>
                                    <option value="">통신사를 선택하세요</option>
                                    <option value="SKT">SKT</option>
                                    <option value="KT">KT</option>
                                    <option value="LG U+">LG U+</option>
                                    <option value="알뜰폰">알뜰폰</option>
                                </select>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label for="birthDate" class="form-label">생년월일</label>
                            <input type="text" class="form-control" id="birthDate" placeholder="YYMMDD" required>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- 약관 동의 -->
            <div class="card mb-4">
                <div class="card-body">
                    <h4 class="card-title">약관 동의</h4>
                    <div class="form-check mb-2">
                        <input class="form-check-input" type="checkbox" id="agreeAll" onchange="toggleAllAgreements()">
                        <label class="form-check-label fw-bold" for="agreeAll">
                            전체 동의
                        </label>
                    </div>
                    <hr>
                    <div class="form-check mb-2">
                        <input class="form-check-input agreement-checkbox" type="checkbox" id="agreeTerms" required>
                        <label class="form-check-label" for="agreeTerms">
                            <span class="text-danger">[필수]</span> 결제 서비스 이용약관 동의
                        </label>
                        <button type="button" class="btn btn-sm btn-link" data-bs-toggle="modal" data-bs-target="#termsModal">
                            보기
                        </button>
                    </div>
                    <div class="form-check mb-2">
                        <input class="form-check-input agreement-checkbox" type="checkbox" id="agreePrivacy" required>
                        <label class="form-check-label" for="agreePrivacy">
                            <span class="text-danger">[필수]</span> 개인정보 수집 및 이용 동의
                        </label>
                        <button type="button" class="btn btn-sm btn-link" data-bs-toggle="modal" data-bs-target="#privacyModal">
                            보기
                        </button>
                    </div>
                    <div class="form-check mb-2">
                        <input class="form-check-input agreement-checkbox" type="checkbox" id="agreeRefund">
                        <label class="form-check-label" for="agreeRefund">
                            <span class="text-danger">[필수]</span> 환불 규정 동의
                        </label>
                        <button type="button" class="btn btn-sm btn-link" data-bs-toggle="modal" data-bs-target="#refundModal">
                            보기
                        </button>
                    </div>
                </div>
            </div>
            
            <!-- 결제 버튼 -->
            <div class="d-grid gap-2">
                <button type="submit" class="btn btn-primary btn-lg" id="payButton" disabled>
                    <fmt:formatNumber value="${reservation.totalPrice * 1.1}" type="currency" currencySymbol="₩" maxFractionDigits="0"/> 결제하기
                </button>
                <a href="${pageContext.request.contextPath}/reservation/detail/${reservation.reservationId}" class="btn btn-outline-secondary">
                    취소
                </a>
            </div>
        </form>
    </div>
    
    <!-- 약관 모달 -->
    <div class="modal fade" id="termsModal" tabindex="-1" aria-labelledby="termsModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="termsModalLabel">결제 서비스 이용약관</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <h5>제1조 (목적)</h5>
                    <p>이 약관은 회사가 제공하는 결제 서비스의 이용과 관련하여 회사와 이용자 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.</p>
                    
                    <h5>제2조 (정의)</h5>
                    <p>1. "서비스"라 함은 회사가 제공하는 결제 서비스를 의미합니다.</p>
                    <p>2. "이용자"라 함은 이 약관에 따라 회사가 제공하는 서비스를 이용하는 자를 말합니다.</p>
                    
                    <h5>제3조 (약관의 효력 및 변경)</h5>
                    <p>1. 이 약관은 서비스를 이용하고자 하는 모든 이용자에게 적용됩니다.</p>
                    <p>2. 회사는 필요한 경우 약관을 변경할 수 있으며, 변경된 약관은 서비스 내에 공지함으로써 효력이 발생합니다.</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                </div>
            </div>
        </div>
    </div>
    
    <div class="modal fade" id="privacyModal" tabindex="-1" aria-labelledby="privacyModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="privacyModalLabel">개인정보 수집 및 이용 동의</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <h5>1. 수집하는 개인정보 항목</h5>
                    <p>- 결제 정보: 카드번호, 유효기간, 카드 소유자 이름, 계좌번호, 예금주, 휴대폰 번호, 통신사, 생년월일</p>
                    
                    <h5>2. 수집 및 이용 목적</h5>
                    <p>- 결제 서비스 제공 및 결제 처리</p>
                    <p>- 결제 관련 민원 처리 및 분쟁 해결</p>
                    
                    <h5>3. 보유 및 이용 기간</h5>
                    <p>- 관련 법령에 따라 5년간 보관 후 파기</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                </div>
            </div>
        </div>
    </div>
    
    <div class="modal fade" id="refundModal" tabindex="-1" aria-labelledby="refundModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="refundModalLabel">환불 규정</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <h5>환불 정책</h5>
                    <p>- 체크인 7일 전 취소: 100% 환불</p>
                    <p>- 체크인 5일 전 취소: 70% 환불</p>
                    <p>- 체크인 3일 전 취소: 50% 환불</p>
                    <p>- 체크인 1일 전 취소: 환불 불가</p>
                    <p>- 노쇼(No-show): 환불 불가</p>
                    
                    <h5>환불 처리 기간</h5>
                    <p>- 카드 결제: 취소 후 3~5일 이내 환불</p>
                    <p>- 계좌이체: 취소 후 3~7일 이내 환불</p>
                    <p>- 휴대폰 결제: 취소 후 최대 2개월 이내 환불 (통신사 정책에 따름)</p>
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
            // 선택된 결제 방법 저장
            document.getElementById('paymentMethod').value = method;
            
            // 모든 카드에서 selected 클래스 제거
            document.querySelectorAll('.payment-method-card').forEach(card => {
                card.classList.remove('selected');
            });
            
            // 선택된 카드에 selected 클래스 추가
            document.querySelector(`.payment-method-card[data-method="${method}"]`).classList.add('selected');
            
            // 결제 상세 정보 표시
            const paymentDetails = document.getElementById('paymentDetails');
            paymentDetails.style.display = 'block';
            
            // 모든 결제 폼 숨기기
            document.getElementById('cardPaymentForm').style.display = 'none';
            document.getElementById('bankTransferForm').style.display = 'none';
            document.getElementById('phonePaymentForm').style.display = 'none';
            
            // 선택된 결제 방법에 따른 폼 표시
            switch(method) {
                case 'CARD':
                    document.getElementById('cardPaymentForm').style.display = 'block';
                    break;
                case 'BANK_TRANSFER':
                    document.getElementById('bankTransferForm').style.display = 'block';
                    break;
                case 'PHONE':
                    document.getElementById('phonePaymentForm').style.display = 'block';
                    break;
            }
            
            // 결제 버튼 활성화 여부 확인
            checkPayButtonStatus();
        }
        
        // 전체 동의 체크박스 토글
        function toggleAllAgreements() {
            const agreeAll = document.getElementById('agreeAll').checked;
            document.querySelectorAll('.agreement-checkbox').forEach(checkbox => {
                checkbox.checked = agreeAll;
            });
            
            // 결제 버튼 활성화 여부 확인
            checkPayButtonStatus();
        }
        
        // 개별 약관 동의 체크박스 이벤트 리스너 등록
        document.querySelectorAll('.agreement-checkbox').forEach(checkbox => {
            checkbox.addEventListener('change', function() {
                // 모든 체크박스가 선택되었는지 확인
                const allChecked = Array.from(document.querySelectorAll('.agreement-checkbox')).every(cb => cb.checked);
                document.getElementById('agreeAll').checked = allChecked;
                
                // 결제 버튼 활성화 여부 확인
                checkPayButtonStatus();
            });
        });
        
        // 결제 버튼 활성화 여부 확인
        function checkPayButtonStatus() {
            const paymentMethod = document.getElementById('paymentMethod').value;
            const agreeTerms = document.getElementById('agreeTerms').checked;
            const agreePrivacy = document.getElementById('agreePrivacy').checked;
            const agreeRefund = document.getElementById('agreeRefund').checked;
            
            // 결제 방법 선택 및 필수 약관 동의 시 결제 버튼 활성화
            document.getElementById('payButton').disabled = !(paymentMethod && agreeTerms && agreePrivacy && agreeRefund);
        }
        
        // 폼 제출 전 유효성 검사
        document.getElementById('paymentForm').addEventListener('submit', function(event) {
            const paymentMethod = document.getElementById('paymentMethod').value;
            
            if (!paymentMethod) {
                event.preventDefault();
                alert('결제 방법을 선택해주세요.');
                return;
            }
            
            // 결제 방법에 따른 유효성 검사
            switch(paymentMethod) {
                case 'CARD':
                    const cardNumber = document.getElementById('cardNumber').value;
                    const cardExpiry = document.getElementById('cardExpiry').value;
                    const cardHolder = document.getElementById('cardHolder').value;
                    const cardCvv = document.getElementById('cardCvv').value;
                    
                    if (!cardNumber || !cardExpiry || !cardHolder || !cardCvv) {
                        event.preventDefault();
                        alert('카드 정보를 모두 입력해주세요.');
                        return;
                    }
                    break;
                    
                case 'BANK_TRANSFER':
                    const bankName = document.getElementById('bankName').value;
                    const accountNumber = document.getElementById('accountNumber').value;
                    const accountHolder = document.getElementById('accountHolder').value;
                    
                    if (!bankName || !accountNumber || !accountHolder) {
                        event.preventDefault();
                        alert('계좌 정보를 모두 입력해주세요.');
                        return;
                    }
                    break;
                    
                case 'PHONE':
                    const phoneNumber = document.getElementById('phoneNumber').value;
                    const carrier = document.getElementById('carrier').value;
                    const birthDate = document.getElementById('birthDate').value;
                    
                    if (!phoneNumber || !carrier || !birthDate) {
                        event.preventDefault();
                        alert('휴대폰 결제 정보를 모두 입력해주세요.');
                        return;
                    }
                    break;
            }
        });
    </script>
</body>
</html>