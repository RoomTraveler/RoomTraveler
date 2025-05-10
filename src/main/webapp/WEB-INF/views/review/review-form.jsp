<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>리뷰 작성</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <style>
        .rating {
            display: flex;
            flex-direction: row-reverse;
            justify-content: flex-end;
        }

        .rating > input {
            display: none;
        }

        .rating > label {
            position: relative;
            width: 1.1em;
            font-size: 2.5em;
            color: #FFD700;
            cursor: pointer;
        }

        .rating > label::before {
            content: "\2605";
            position: absolute;
            opacity: 0;
        }

        .rating > label:hover:before,
        .rating > label:hover ~ label:before {
            opacity: 1 !important;
        }

        .rating > input:checked ~ label:before {
            opacity: 1;
        }

        .rating > input:checked ~ label:hover:before {
            opacity: 1;
        }
    </style>
</head>
<body>
    <div class="container mt-5">
        <!-- 헤더 포함 -->
        <jsp:include page="../fragments/header.jsp" />

        <div class="row mb-4">
            <div class="col">
                <h2>리뷰 작성</h2>
                <p class="text-muted">숙소에 대한 솔직한 리뷰를 남겨주세요.</p>
            </div>
        </div>

        <div class="card">
            <div class="card-body">
                <form action="${root}/review/write" method="post">
                    <input type="hidden" name="accommodationId" value="${accommodationId}">

                    <!-- 평점 선택 -->
                    <div class="mb-3">
                        <label class="form-label">평점</label>
                        <div class="rating">
                            <input type="radio" id="star5" name="rating" value="5" required />
                            <label for="star5" title="5점"></label>
                            <input type="radio" id="star4" name="rating" value="4" />
                            <label for="star4" title="4점"></label>
                            <input type="radio" id="star3" name="rating" value="3" />
                            <label for="star3" title="3점"></label>
                            <input type="radio" id="star2" name="rating" value="2" />
                            <label for="star2" title="2점"></label>
                            <input type="radio" id="star1" name="rating" value="1" />
                            <label for="star1" title="1점"></label>
                        </div>
                        <div class="form-text">별점을 선택해주세요 (필수)</div>
                    </div>

                    <!-- 제목 입력 -->
                    <div class="mb-3">
                        <label for="title" class="form-label">제목</label>
                        <input type="text" class="form-control" id="title" name="title" required
                               placeholder="리뷰 제목을 입력해주세요">
                    </div>

                    <!-- 내용 입력 -->
                    <div class="mb-3">
                        <label for="content" class="form-label">내용</label>
                        <textarea class="form-control" id="content" name="content" rows="5" required
                                  placeholder="숙소에 대한 경험을 자세히 적어주세요. 다른 여행자들에게 도움이 됩니다."></textarea>
                    </div>

                    <!-- 숙박 날짜 선택 -->
                    <div class="mb-3">
                        <label for="stayDate" class="form-label">숙박 날짜</label>
                        <input type="date" class="form-control" id="stayDate" name="stayDate" required>
                    </div>

                    <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                        <a href="${root}/accommodation/detail?accommodationId=${accommodationId}" class="btn btn-secondary">취소</a>
                        <button type="submit" class="btn btn-primary">리뷰 등록</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script>
        // 오늘 날짜를 기본값으로 설정
        document.addEventListener('DOMContentLoaded', function() {
            const today = new Date();
            const yyyy = today.getFullYear();
            const mm = String(today.getMonth() + 1).padStart(2, '0');
            const dd = String(today.getDate()).padStart(2, '0');
            const formattedDate = yyyy + '-' + mm + '-' + dd;
            document.getElementById('stayDate').value = formattedDate;

            // 최대 날짜를 오늘로 제한
            document.getElementById('stayDate').max = formattedDate;
        });
    </script>
</body>
</html>
