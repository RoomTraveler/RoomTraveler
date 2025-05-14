<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>한국관광공사 API 테스트</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        h1 { color: #333; }
        .section { margin-bottom: 30px; border: 1px solid #ddd; padding: 15px; border-radius: 5px; }
        .form-group { margin-bottom: 10px; }
        label { display: inline-block; width: 120px; }
        select, input { padding: 5px; width: 200px; }
        button { padding: 8px 15px; background-color: #4CAF50; color: white; border: none; cursor: pointer; }
        button:hover { background-color: #45a049; }
        #results { margin-top: 20px; border: 1px solid #ddd; padding: 15px; min-height: 200px; }
        .result-item { margin-bottom: 15px; padding-bottom: 15px; border-bottom: 1px solid #eee; }
        .result-item img { max-width: 200px; max-height: 150px; }
        .result-item h3 { margin-top: 0; }
        pre { background-color: #f5f5f5; padding: 10px; overflow: auto; }
    </style>
</head>
<body>
<h1>한국관광공사 API 테스트</h1>

<div class="section">
    <h2>지역 코드 조회</h2>
    <div class="form-group">
        <label>시도:</label>
        <select id="areaSelect"><option value="">전체</option></select>
    </div>
    <div class="form-group">
        <label>시군구:</label>
        <select id="sigunguSelect"><option value="">전체</option></select>
    </div>
</div>

<div class="section">
    <h2>관광지 검색</h2>
    <div class="form-group">
        <label>키워드:</label>
        <input type="text" id="keyword" placeholder="검색어 입력">
    </div>
    <div class="form-group">
        <label>컨텐츠 타입:</label>
        <select id="contentType">
            <option value="">전체</option>
            <option value="12">관광지</option>
            <option value="14">문화시설</option>
            <option value="15">축제공연행사</option>
            <option value="25">여행코스</option>
            <option value="28">레포츠</option>
            <option value="32">숙박</option>
            <option value="38">쇼핑</option>
            <option value="39">음식점</option>
        </select>
    </div>
    <button id="searchBtn">검색</button>
</div>

<div class="section">
    <h2>지역 기반 관광정보 조회</h2>
    <div class="form-group">
        <label>컨텐츠 타입:</label>
        <select id="areaContentType">
            <option value="">전체</option>
            <option value="12">관광지</option>
            <option value="14">문화시설</option>
            <option value="15">축제공연행사</option>
            <option value="25">여행코스</option>
            <option value="28">레포츠</option>
            <option value="32">숙박</option>
            <option value="38">쇼핑</option>
            <option value="39">음식점</option>
        </select>
    </div>
    <button id="areaSearchBtn">지역 검색</button>
</div>

<div class="section">
    <h2>상세 정보 조회</h2>
    <div class="form-group">
        <label>콘텐츠 ID:</label>
        <input type="text" id="contentId" placeholder="콘텐츠 ID 입력">
    </div>
    <div class="form-group">
        <label>컨텐츠 타입:</label>
        <select id="detailContentType">
            <option value="">선택 안함</option>
            <option value="12">관광지</option>
            <option value="14">문화시설</option>
            <option value="15">축제공연행사</option>
            <option value="25">여행코스</option>
            <option value="28">레포츠</option>
            <option value="32">숙박</option>
            <option value="38">쇼핑</option>
            <option value="39">음식점</option>
        </select>
    </div>
    <button id="detailBtn">상세 정보 조회</button>
</div>

<div id="results">
    <p>결과가 여기에 표시됩니다.</p>
</div>

<script>
    $(function(){
        const apiBase = '${pageContext.request.contextPath}/accommodation/api';

        // 시도 목록 로드
        $.ajax({ url: apiBase + '/sidos', dataType: 'json' })
            .done(data => data.forEach(sido =>
                $('#areaSelect').append(
                    $('<option>').val(sido.code).text(sido.name)
                )
            ))
            .fail(xhr => console.error('시도 로드 실패:', xhr.responseJSON || xhr.responseText));

        // 시도 선택 시 구군 로드
        $('#areaSelect').change(function(){
            const code = $(this).val();
            $('#sigunguSelect').empty().append('<option value="">전체</option>');
            if (!code) return;

            $.ajax({ url: apiBase + '/guguns', data: { sido: code }, dataType: 'json' })
                .done(data => data.forEach(gugun =>
                    $('#sigunguSelect').append(
                        $('<option>').val(gugun.code).text(gugun.name)
                    )
                ))
                .fail(xhr => console.error('구군 로드 실패:', xhr.responseJSON || xhr.responseText));
        });

        // 키워드 검색
        $('#searchBtn').click(function() {
            const keyword = $('#keyword').val();
            if (!keyword) {
                alert('키워드를 입력하세요');
                return;
            }

            const params = {
                keyword: keyword,
                contentTypeId: $('#contentType').val(),
                areaCode: $('#areaSelect').val(),
                sigunguCode: $('#sigunguSelect').val()
            };

            $('#results').html('<p>검색 중...</p>');

            $.ajax({
                url: apiBase + '/search',
                data: params,
                dataType: 'json'
            })
            .done(data => {
                if (!data.items || data.items.length === 0) {
                    $('#results').html('<p>검색 결과가 없습니다.</p>');
                    return;
                }

                let html = '<h3>"' + data.keyword + '" 검색 결과 (총 ' + data.totalCount + '개 중 ' + data.items.length + '개)</h3>';

                data.items.forEach(function(item) {
                    html += 
                    '<div class="result-item">' +
                        '<h3>' + item.title + '</h3>' +
                        (item.firstimage ? '<img src="' + item.firstimage + '" alt="' + item.title + '">' : '') +
                        '<p>' + item.addr1 + ' ' + (item.addr2 || '') + '</p>' +
                        (item.tel ? '<p>연락처: ' + item.tel + '</p>' : '') +
                        '<button class="detail-btn" data-id="' + item.contentid + '" data-type="' + item.contenttypeid + '">상세 정보</button>' +
                    '</div>';
                });

                $('#results').html(html);
            })
            .fail(xhr => {
                console.error('검색 실패:', xhr.responseJSON || xhr.responseText);
                $('#results').html('<p>검색 중 오류가 발생했습니다.</p>');
            });
        });

        // 지역 기반 검색
        $('#areaSearchBtn').click(function() {
            const areaCode = $('#areaSelect').val();
            const sigunguCode = $('#sigunguSelect').val();

            if (!areaCode) {
                alert('지역을 선택하세요');
                return;
            }

            const params = {
                areaCode: areaCode,
                sigunguCode: sigunguCode,
                contentTypeId: $('#areaContentType').val()
            };

            $('#results').html('<p>검색 중...</p>');

            $.ajax({
                url: apiBase + '/attractions',
                data: params,
                dataType: 'json'
            })
            .done(data => {
                if (!data.items || data.items.length === 0) {
                    $('#results').html('<p>검색 결과가 없습니다.</p>');
                    return;
                }

                let html = '<h3>지역 기반 검색 결과 (총 ' + data.totalCount + '개 중 ' + data.items.length + '개)</h3>';

                data.items.forEach(function(item) {
                    html += 
                    '<div class="result-item">' +
                        '<h3>' + item.title + '</h3>' +
                        (item.firstimage ? '<img src="' + item.firstimage + '" alt="' + item.title + '">' : '') +
                        '<p>' + item.addr1 + ' ' + (item.addr2 || '') + '</p>' +
                        (item.tel ? '<p>연락처: ' + item.tel + '</p>' : '') +
                        '<button class="detail-btn" data-id="' + item.contentid + '" data-type="' + item.contenttypeid + '">상세 정보</button>' +
                    '</div>';
                });

                $('#results').html(html);
            })
            .fail(xhr => {
                console.error('검색 실패:', xhr.responseJSON || xhr.responseText);
                $('#results').html('<p>검색 중 오류가 발생했습니다.</p>');
            });
        });

        // 상세 정보 조회
        $('#detailBtn').click(function() {
            const contentId = $('#contentId').val();
            if (!contentId) {
                alert('콘텐츠 ID를 입력하세요');
                return;
            }

            fetchDetail(contentId, $('#detailContentType').val());
        });

        // 검색 결과에서 상세 정보 버튼 클릭
        $(document).on('click', '.detail-btn', function() {
            const contentId = $(this).data('id');
            const contentTypeId = $(this).data('type');
            fetchDetail(contentId, contentTypeId);
        });

        function fetchDetail(contentId, contentTypeId) {
            $('#results').html('<p>상세 정보 로드 중...</p>');

            const params = { contentId: contentId };
            if (contentTypeId) params.contentTypeId = contentTypeId;

            $.ajax({
                url: apiBase + '/attraction/detail',
                data: params,
                dataType: 'json'
            })
            .done(data => {
                if (!data.item || Object.keys(data.item).length === 0) {
                    $('#results').html('<p>상세 정보가 없습니다.</p>');
                    return;
                }

                const item = data.item;

                let html = 
                '<h3>' + item.title + '</h3>' +
                (item.firstimage ? '<img src="' + item.firstimage + '" alt="' + item.title + '">' : '') +
                '<p>' + item.addr1 + ' ' + (item.addr2 || '') + '</p>' +
                (item.tel ? '<p>연락처: ' + item.tel + '</p>' : '') +
                (item.homepage ? '<p>홈페이지: ' + item.homepage + '</p>' : '') +
                (item.overview ? '<div><h4>개요</h4><p>' + item.overview + '</p></div>' : '');

                if (item.introInfo) {
                    html += '<h4>추가 정보</h4><pre>' + 
                        JSON.stringify(item.introInfo, null, 2) + 
                        '</pre>';
                }

                $('#results').html(html);
            })
            .fail(xhr => {
                console.error('상세 정보 로드 실패:', xhr.responseJSON || xhr.responseText);
                $('#results').html('<p>상세 정보 로드 중 오류가 발생했습니다.</p>');
            });
        }
    });
</script>
</body>
</html>
