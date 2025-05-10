<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>API 설정 관리</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />
    
    <div class="container mt-5">
        <h2 class="mb-4">API 설정 관리</h2>
        
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
        
        <div class="card">
            <div class="card-header">
                <h5 class="mb-0">Tour API 설정</h5>
            </div>
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/admin/api-config/update" method="post">
                    <div class="mb-3">
                        <label for="baseUrl" class="form-label">기본 URL</label>
                        <input type="text" class="form-control" id="baseUrl" name="baseUrl" value="${apiConfig.baseUrl}" required>
                        <div class="form-text">API 호출을 위한 기본 URL입니다.</div>
                    </div>
                    
                    <div class="mb-3">
                        <label for="serviceKey" class="form-label">서비스 키</label>
                        <input type="text" class="form-control" id="serviceKey" name="serviceKey" value="${apiConfig.serviceKey}" required>
                        <div class="form-text">API 인증을 위한 서비스 키입니다.</div>
                    </div>
                    
                    <div class="mb-3">
                        <label for="mobileOs" class="form-label">모바일 OS</label>
                        <input type="text" class="form-control" id="mobileOs" name="mobileOs" value="${apiConfig.mobileOs}" required>
                        <div class="form-text">API 호출 시 사용할 모바일 OS 값입니다.</div>
                    </div>
                    
                    <div class="mb-3">
                        <label for="mobileApp" class="form-label">모바일 앱</label>
                        <input type="text" class="form-control" id="mobileApp" name="mobileApp" value="${apiConfig.mobileApp}" required>
                        <div class="form-text">API 호출 시 사용할 모바일 앱 이름입니다.</div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="defaultPageNo" class="form-label">기본 페이지 번호</label>
                            <input type="number" class="form-control" id="defaultPageNo" name="defaultPageNo" value="${apiConfig.defaultPageNo}" min="1" required>
                            <div class="form-text">API 호출 시 사용할 기본 페이지 번호입니다.</div>
                        </div>
                        
                        <div class="col-md-6 mb-3">
                            <label for="defaultNumOfRows" class="form-label">기본 결과 수</label>
                            <input type="number" class="form-control" id="defaultNumOfRows" name="defaultNumOfRows" value="${apiConfig.defaultNumOfRows}" min="1" required>
                            <div class="form-text">API 호출 시 사용할 기본 결과 수입니다.</div>
                        </div>
                    </div>
                    
                    <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                        <button type="submit" class="btn btn-primary">저장</button>
                        <a href="${pageContext.request.contextPath}/admin" class="btn btn-secondary">취소</a>
                    </div>
                </form>
            </div>
        </div>
        
        <div class="card mt-4">
            <div class="card-header">
                <h5 class="mb-0">API 테스트</h5>
            </div>
            <div class="card-body">
                <div class="mb-3">
                    <label for="testEndpoint" class="form-label">테스트할 엔드포인트</label>
                    <select class="form-select" id="testEndpoint">
                        <option value="searchStay1">숙소 검색</option>
                        <option value="detailCommon1">숙소 상세 정보</option>
                        <option value="detailInfo1">객실 정보</option>
                        <option value="detailImage1">이미지 정보</option>
                    </select>
                </div>
                
                <div class="mb-3">
                    <label for="testParams" class="form-label">추가 파라미터 (JSON 형식)</label>
                    <textarea class="form-control" id="testParams" rows="3">{"keyword": "호텔"}</textarea>
                    <div class="form-text">테스트할 추가 파라미터를 JSON 형식으로 입력하세요.</div>
                </div>
                
                <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                    <button type="button" class="btn btn-primary" id="testApiBtn">API 테스트</button>
                </div>
                
                <div class="mt-3" id="testResult" style="display: none;">
                    <h6>테스트 결과:</h6>
                    <pre class="bg-light p-3 rounded" id="testResultContent"></pre>
                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="../fragments/footer.jsp" />
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.getElementById('testApiBtn').addEventListener('click', function() {
            const endpoint = document.getElementById('testEndpoint').value;
            const paramsStr = document.getElementById('testParams').value;
            let params = {};
            
            try {
                params = JSON.parse(paramsStr);
            } catch (e) {
                alert('파라미터 형식이 올바르지 않습니다. JSON 형식으로 입력해주세요.');
                return;
            }
            
            // API 테스트 요청
            fetch('${pageContext.request.contextPath}/admin/api-config/test', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    endpoint: endpoint,
                    params: params
                })
            })
            .then(response => response.json())
            .then(data => {
                const resultElement = document.getElementById('testResultContent');
                resultElement.textContent = JSON.stringify(data, null, 2);
                document.getElementById('testResult').style.display = 'block';
            })
            .catch(error => {
                alert('API 테스트 중 오류가 발생했습니다: ' + error.message);
            });
        });
    </script>
</body>
</html>