<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>계획s</title>
    <style>
        .plan {
            border: 1px solid #ddd;
            border-radius: 10px;
            padding: 16px;
            margin-bottom: 30px;
            background-color: #f9f9f9;
        }

        .attraction {
            display: flex;
            align-items: center;
            margin-bottom: 12px;
            padding: 10px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.05);
        }

        .attraction img {
            width: 100px;
            height: 75px;
            object-fit: cover;
            border-radius: 6px;
            margin-right: 15px;
            background: #eee;
        }

        .attraction-info {
            display: flex;
            flex-direction: column;
        }

        .attraction-info .title {
            font-weight: bold;
            font-size: 16px;
        }

        .attraction-info .meta {
            color: #666;
            font-size: 14px;
        }
    </style>

</head>
<body>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<div id="plans-container">Loading...</div>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
</body>
</html>

<script>
    const contentTypeMap = {
        "12": "관광지",
        "14": "문화시설",
        "15": "축제공연행사",
        "25": "여행코스",
        "28": "레포츠",
        "32": "숙박",
        "38": "쇼핑",
        "39": "음식점"
    };

    const noImageUrl = 'https://www.freeiconspng.com/uploads/no-image-icon-6.png';
    const cachedNoImage = new Image();
    cachedNoImage.src = noImageUrl;

    fetch('/map/plans/1') // id 사용자따라
        .then(response => {
            if (!response.ok) throw new Error('네트워크 오류 발생');
            return response.json();
        })
        .then(plans => {
            const container = document.getElementById('plans-container');
            container.innerHTML = '';

            plans.forEach((plan, index) => {
                const planDiv = document.createElement('div');
                planDiv.className = 'plan';

                const title = document.createElement('h3');
                title.textContent = `플랜 \${index + 1}`;
                planDiv.appendChild(title);

                plan.planAttractions
                    .sort((a, b) => a.order - b.order)
                    .forEach(attraction => {
                        const attractionDiv = document.createElement('div');
                        attractionDiv.className = 'attraction';

                        const img = document.createElement('img');
                        img.src = attraction.imageUrl || cachedNoImage.src;
                        img.alt = attraction.title;

                        const infoDiv = document.createElement('div');
                        infoDiv.className = 'attraction-info';

                        const titleEl = document.createElement('div');
                        titleEl.className = 'title';
                        titleEl.textContent = `\${attraction.order + 1}. \${attraction.title}`;

                        const meta = document.createElement('div');
                        meta.className = 'meta';
                        meta.textContent = `분류: \${contentTypeMap[attraction.contentType] || '기타'}`;

                        infoDiv.appendChild(titleEl);
                        infoDiv.appendChild(meta);

                        attractionDiv.appendChild(img);
                        attractionDiv.appendChild(infoDiv);

                        planDiv.appendChild(attractionDiv);
                    });

                container.appendChild(planDiv);
            });
        })
        .catch(error => {
            console.error('에러 발생:', error);
            document.getElementById('plans-container').textContent = '데이터를 불러오지 못했습니다.';
        });
</script>
