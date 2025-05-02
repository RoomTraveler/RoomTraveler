<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>숙박 지역 선택</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>
<h2>숙박 지역 선택</h2>

<label>시도:</label>
<select id="areaSelect"><option value="">전체</option></select>

<label>시군구:</label>
<select id="sigunguSelect"><option value="">전체</option></select>

<script>
    $(function(){
        const apiBase = '${pageContext.request.contextPath}/accommodation/api';

        //시도 목록 로드
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
    });
</script>
</body>
</html>
