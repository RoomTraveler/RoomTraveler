<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>호스트 등록</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .form-container {
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f8f9fa;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        .required-field::after {
            content: " *";
            color: red;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />

    <div class="container mt-5 mb-5">
        <h2 class="text-center mb-4">호스트 등록</h2>

        <div class="form-container">
            <form action="${pageContext.request.contextPath}/host/regist" method="post">
                <div class="mb-3">
                    <label for="businessName" class="form-label required-field">사업자명</label>
                    <input type="text" class="form-control" id="businessName" name="businessName" required>
                </div>

                <div class="mb-3">
                    <label for="businessRegNo" class="form-label required-field">사업자 등록번호</label>
                    <input type="text" class="form-control" id="businessRegNo" name="businessRegNo" 
                           placeholder="000-00-00000" required>
                    <div class="form-text">하이픈(-)을 포함하여 입력해주세요.</div>
                </div>


                <div class="mb-3">
                    <label for="bankName" class="form-label required-field">은행명</label>
                    <select class="form-select" id="bankName" name="bankName" required>
                        <option value="">은행을 선택하세요</option>
                        <option value="KB국민은행">KB국민은행</option>
                        <option value="신한은행">신한은행</option>
                        <option value="우리은행">우리은행</option>
                        <option value="하나은행">하나은행</option>
                        <option value="농협은행">농협은행</option>
                        <option value="기업은행">기업은행</option>
                        <option value="SC제일은행">SC제일은행</option>
                        <option value="카카오뱅크">카카오뱅크</option>
                        <option value="토스뱅크">토스뱅크</option>
                    </select>
                </div>

                <div class="mb-3">
                    <label for="accountNumber" class="form-label required-field">계좌번호</label>
                    <input type="text" class="form-control" id="accountNumber" name="accountNumber" 
                           placeholder="하이픈(-) 없이 입력해주세요" required>
                </div>

                <div class="mb-3">
                    <label for="accountHolder" class="form-label required-field">예금주</label>
                    <input type="text" class="form-control" id="accountHolder" name="accountHolder" required>
                </div>

                <div class="mb-3">
                    <label for="profileText" class="form-label">호스트 소개</label>
                    <textarea class="form-control" id="profileText" name="profileText" rows="4"
                              placeholder="호스트로서 자신을 소개해주세요. (선택사항)"></textarea>
                </div>

                <div class="alert alert-info">
                    <p><strong>호스트 등록 안내:</strong></p>
                    <ul>
                        <li>호스트 등록 후 관리자 승인이 필요합니다.</li>
                        <li>사업자 정보는 실제 등록된 정보와 일치해야 합니다.</li>
                        <li>허위 정보 기재 시 호스트 자격이 박탈될 수 있습니다.</li>
                    </ul>
                </div>

                <div class="d-grid gap-2">
                    <button type="submit" class="btn btn-primary">호스트 등록 신청</button>
                    <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">취소</a>
                </div>
            </form>
        </div>
    </div>

    <jsp:include page="../fragments/footer.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 사업자 등록번호 형식 검증
        document.getElementById('businessRegNo').addEventListener('blur', function() {
            const regex = /^\d{3}-\d{2}-\d{5}$/;
            if (!regex.test(this.value) && this.value !== '') {
                alert('사업자 등록번호 형식이 올바르지 않습니다. (예: 123-45-67890)');
                this.focus();
            }
        });


        // Form submission handler to combine bank account fields
        document.querySelector('form').addEventListener('submit', function(e) {
            e.preventDefault();

            const bankName = document.getElementById('bankName').value;
            const accountNumber = document.getElementById('accountNumber').value;
            const accountHolder = document.getElementById('accountHolder').value;

            // Create a hidden field for bankAccount
            const bankAccountField = document.createElement('input');
            bankAccountField.type = 'hidden';
            bankAccountField.name = 'bankAccount';
            bankAccountField.value = bankName + ' ' + accountNumber + ' (' + accountHolder + ')';

            this.appendChild(bankAccountField);
            this.submit();
        });
    </script>
</body>
</html>
