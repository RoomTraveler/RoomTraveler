<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>숙소 목록</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .accommodation-card {
            transition: transform 0.3s;
            margin-bottom: 20px;
            height: 100%;
        }
        .accommodation-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
        }
        .card-img-top {
            height: 200px;
            object-fit: cover;
        }
        .filter-section {
            background-color: #f8f9fa;
            padding: 20px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />
    
    <div class="container mt-5">
        <h2 class="mb-4">숙소 목록</h2>
        
        <!-- 필터 섹션 -->
        <div class="filter-section mb-4">
            <form action="${pageContext.request.contextPath}/accommodation/filter" method="get" class="row g-3">
                <div class="col-md-3">
                    <label for="sidoCode" class="form-label">지역</label>
                    <select class="form-select" id="sidoCode" name="sidoCode">
                        <option value="">전체</option>
                        <!-- 시도 목록은 JavaScript로 동적 로드 -->
                    </select>
                </div>
                <div class="col-md-3">
                    <label for="gugunCode" class="form-label">시군구</label>
                    <select class="form-select" id="gugunCode" name="gugunCode">
                        <option value="">전체</option>
                        <!-- 구군 목록은 시도 선택 시 JavaScript로 동적 로드 -->
                    </select>
                </div>
                <div class="col-md-3">
                    <label for="keyword" class="form-label">검색어</label>
                    <input type="text" class="form-control" id="keyword" name="keyword" placeholder="숙소명, 주소 등" value="${keyword}">
                </div>
                <div class="col-md-3">
                    <label for="sortBy" class="form-label">정렬</label>
                    <select class="form-select" id="sortBy" name="sortBy">
                        <option value="createdAt">최신순</option>
                        <option value="price">가격순</option>
                        <option value="rating">평점순</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label for="minPrice" class="form-label">최소 가격</label>
                    <input type="number" class="form-control" id="minPrice" name="minPrice" placeholder="최소 가격">
                </div>
                <div class="col-md-3">
                    <label for="maxPrice" class="form-label">최대 가격</label>
                    <input type="number" class="form-control" id="maxPrice" name="maxPrice" placeholder="최대 가격">
                </div>
                <div class="col-md-3 d-flex align-items-end">
                    <button type="submit" class="btn btn-primary w-100">검색</button>
                </div>
            </form>
        </div>
        
        <!-- 숙소 목록 -->
        <div class="row">
            <c:if test="${empty accommodations}">
                <div class="col-12 text-center py-5">
                    <p class="lead">검색 결과가 없습니다.</p>
                </div>
            </c:if>
            
            <c:forEach var="accommodation" items="${accommodations}">
                <div class="col-md-4 mb-4">
                    <div class="card accommodation-card">
                        <c:choose>
                            <c:when test="${not empty accommodation.mainImageUrl}">
                                <img src="${accommodation.mainImageUrl}" class="card-img-top" alt="${accommodation.title}">
                            </c:when>
                            <c:otherwise>
                                <img src="${pageContext.request.contextPath}/resources/images/no-image.jpg" class="card-img-top" alt="이미지 없음">
                            </c:otherwise>
                        </c:choose>
                        <div class="card-body">
                            <h5 class="card-title">${accommodation.title}</h5>
                            <p class="card-text text-muted">${accommodation.sidoName} ${accommodation.gugunName}</p>
                            <p class="card-text">${accommodation.address}</p>
                            <div class="d-flex justify-content-between align-items-center">
                                <a href="${pageContext.request.contextPath}/accommodation/detail/${accommodation.accommodationId}" class="btn btn-outline-primary">상세 보기</a>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
    
    <jsp:include page="../fragments/footer.jsp" />
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 시도 목록 로드
        function loadSidos() {
            fetch('${pageContext.request.contextPath}/accommodation/api/sidos')
                .then(response => response.json())
                .then(data => {
                    const sidoSelect = document.getElementById('sidoCode');
                    data.forEach(sido => {
                        const option = document.createElement('option');
                        option.value = sido.code;
                        option.textContent = sido.name;
                        if (sido.code == '${sidoCode}') {
                            option.selected = true;
                        }
                        sidoSelect.appendChild(option);
                    });
                    
                    // 시도가 선택되어 있으면 구군 목록 로드
                    if ('${sidoCode}') {
                        loadGuguns('${sidoCode}');
                    }
                })
                .catch(error => console.error('Error loading sidos:', error));
        }
        
        // 구군 목록 로드
        function loadGuguns(sidoCode) {
            fetch(`${pageContext.request.contextPath}/accommodation/api/guguns?sido=${sidoCode}`)
                .then(response => response.json())
                .then(data => {
                    const gugunSelect = document.getElementById('gugunCode');
                    gugunSelect.innerHTML = '<option value="">전체</option>';
                    
                    data.forEach(gugun => {
                        const option = document.createElement('option');
                        option.value = gugun.code;
                        option.textContent = gugun.name;
                        if (gugun.code == '${gugunCode}') {
                            option.selected = true;
                        }
                        gugunSelect.appendChild(option);
                    });
                })
                .catch(error => console.error('Error loading guguns:', error));
        }
        
        // 시도 선택 시 구군 목록 로드
        document.getElementById('sidoCode').addEventListener('change', function() {
            const sidoCode = this.value;
            if (sidoCode) {
                loadGuguns(sidoCode);
            } else {
                document.getElementById('gugunCode').innerHTML = '<option value="">전체</option>';
            }
        });
        
        // 페이지 로드 시 시도 목록 로드
        document.addEventListener('DOMContentLoaded', function() {
            loadSidos();
            
            // 정렬 옵션 설정
            const sortBy = '${param.sortBy}';
            if (sortBy) {
                document.getElementById('sortBy').value = sortBy;
            }
            
            // 가격 범위 설정
            const minPrice = '${param.minPrice}';
            const maxPrice = '${param.maxPrice}';
            if (minPrice) {
                document.getElementById('minPrice').value = minPrice;
            }
            if (maxPrice) {
                document.getElementById('maxPrice').value = maxPrice;
            }
        });
    </script>
</body>
</html>