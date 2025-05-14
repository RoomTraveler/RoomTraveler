# 방구석 여행자 (RoomTraveler)

이 프로젝트는 숙박 예약 서비스를 제공하는 웹 애플리케이션으로, 백엔드와 프론트엔드가 분리된 구조로 구성되어 있습니다. 프론트엔드는 Vue.js, 백엔드는 Spring Boot를 사용합니다.

## 백엔드와 프론트엔드 분리 구조 소개

이 프로젝트는 백엔드와 프론트엔드가 분리된 구조로 개발되었습니다. 이러한 구조의 장점은 다음과 같습니다:

1. **독립적인 개발**: 백엔드와 프론트엔드 팀이 독립적으로 개발할 수 있습니다.
2. **기술 스택 자유**: 각 부분에 가장 적합한 기술을 선택할 수 있습니다.
3. **확장성**: 필요에 따라 각 부분을 독립적으로 확장할 수 있습니다.
4. **유지보수 용이성**: 한 부분의 변경이 다른 부분에 영향을 미치지 않습니다.

## 프로젝트 구조

```
RoomTraveler/
├── backend/             # 백엔드 (Spring Boot)
│   ├── main/            # 메인 소스 코드
│   │   ├── java/        # Java 소스 파일
│   │   │   └── com/ssafy/trip/  # 패키지 구조
│   │   └── resources/   # 리소스 파일 (설정, 정적 파일 등)
│   ├── test/            # 테스트 코드
│   └── pom.xml          # Maven 프로젝트 설정 파일
│
├── frontend/            # 프론트엔드 (Vue.js)
│   ├── node_modules/    # npm 패키지 (git에서는 무시됨)
│   ├── src/             # 소스 코드
│   │   ├── assets/      # 이미지, 폰트 등 자산 파일
│   │   ├── components/  # Vue 컴포넌트
│   │   ├── router/      # Vue Router 설정
│   │   ├── store/       # 상태 관리 (Pinia/Vuex)
│   │   ├── views/       # 페이지 컴포넌트
│   │   ├── App.vue      # 루트 컴포넌트
│   │   └── main.js      # 진입점
│   ├── package.json     # npm 패키지 설정
│   ├── package-lock.json # npm 패키지 의존성 정보
│   └── vite.config.js   # Vite 설정 파일
│
└── README.md            # 프로젝트 설명 파일
```

## 기술 스택

### 프론트엔드
- **Vue.js 3**: 사용자 인터페이스 구축을 위한 프레임워크
- **Vite**: 빠른 개발 서버 및 빌드 도구
- **Pinia**: Vue 3용 상태 관리 라이브러리
- **Vue Router**: SPA 라우팅 라이브러리
- **Bootstrap 5**: UI 컴포넌트 및 스타일링
- **Axios**: HTTP 클라이언트

### 백엔드
- **Spring Boot 3**: 자바 기반 웹 애플리케이션 프레임워크
- **MyBatis**: SQL 매핑 프레임워크
- **MySQL**: 관계형 데이터베이스
- **RESTful API**: 프론트엔드와 백엔드 간 통신

### 개발 도구
- **npm**: 패키지 관리
- **Git**: 버전 관리
- **Maven**: 자바 프로젝트 빌드 및 의존성 관리

## 설치 방법

### 사전 요구사항
- Node.js 16 이상
- npm 8 이상
- Java 17 이상
- Maven 3.8 이상
- MySQL 8 이상

### 설치 단계

1. 저장소 클론
```bash
git clone https://github.com/your-username/roomtraveler.git
cd roomtraveler
```

2. 백엔드 설정
```bash
# backend 디렉토리로 이동
cd backend

# Maven 의존성 설치
mvn install
```

3. 프론트엔드 설정
```bash
# frontend 디렉토리로 이동
cd ../frontend

# npm 의존성 설치
npm install
```

4. 데이터베이스 설정
- MySQL에 `ssafytrip` 데이터베이스 생성
- `backend/main/resources/application.properties` 파일에서 데이터베이스 연결 정보 확인 및 수정

## 실행 방법

### 개발 모드

1. 백엔드 실행
```bash
# backend 디렉토리에서
mvn spring-boot:run
```

2. 프론트엔드 실행
```bash
# frontend 디렉토리에서
npm run dev
```


> **주의**: `npm run dev` 명령은 반드시 frontend 디렉토리에서 실행해야 합니다. backend 디렉토리에서 실행하면 작동하지 않습니다.

### 빌드

1. 백엔드 빌드
```bash
# backend 디렉토리에서
mvn clean package

# 빌드된 JAR 파일은 target 디렉토리에 생성됩니다
# 예: target/trip-0.0.1-SNAPSHOT.jar
```

2. 프론트엔드 빌드
```bash
# frontend 디렉토리에서
npm run build

# 빌드된 파일은 dist 디렉토리에 생성됩니다
```

### 배포 방법

#### 백엔드 배포
```bash
# JAR 파일 실행
java -jar backend/target/trip-0.0.1-SNAPSHOT.jar
```

#### 프론트엔드 배포
프론트엔드 빌드 결과물(dist 폴더)을 웹 서버(Nginx, Apache 등)에 배포하거나, 
백엔드의 정적 리소스 디렉토리에 복사하여 함께 배포할 수 있습니다.

```bash
# 예: Nginx에 배포
cp -r frontend/dist/* /var/www/html/
```

## 개발 워크플로우

### 백엔드와 프론트엔드 분리 개발 방법

백엔드와 프론트엔드가 분리된 구조에서는 다음과 같은 워크플로우로 개발합니다:

1. **백엔드 API 개발**
   - RESTful API 엔드포인트 설계 및 구현
   - API 문서화 (Swagger 등)
   - 단위 테스트 및 통합 테스트 작성

2. **프론트엔드 개발**
   - 백엔드 API를 호출하는 서비스 구현
   - 컴포넌트 및 페이지 개발
   - 상태 관리 및 라우팅 설정

3. **통합 테스트**
   - 백엔드와 프론트엔드 연동 테스트
   - 엔드투엔드 테스트

### 프론트엔드 개발 가이드

1. **컴포넌트 개발**
   - `frontend/src/components/` 디렉토리에 재사용 가능한 컴포넌트 생성
   - `frontend/src/views/` 디렉토리에 페이지 컴포넌트 생성

2. **API 통신**
   - `frontend/src/api/` 디렉토리에 API 호출 함수 구현
   - Axios를 사용하여 백엔드 API 호출

3. **상태 관리**
   - `frontend/src/store/` 디렉토리에 Pinia 스토어 구현
   - 전역 상태 관리 및 API 호출 결과 캐싱

4. **라우팅**
   - `frontend/src/router/index.js` 파일에서 라우트 설정
   - 인증 및 권한 기반 라우트 가드 구현

### 백엔드 개발 가이드

1. **컨트롤러 개발**
   - `backend/main/java/com/ssafy/trip/controller/` 디렉토리에 REST 컨트롤러 생성
   - API 엔드포인트 정의 및 요청 처리

2. **서비스 계층 개발**
   - `backend/main/java/com/ssafy/trip/service/` 디렉토리에 비즈니스 로직 구현
   - 트랜잭션 관리 및 예외 처리

3. **데이터 액세스 계층 개발**
   - `backend/main/java/com/ssafy/trip/mapper/` 디렉토리에 MyBatis 매퍼 인터페이스 생성
   - `backend/main/resources/mappers/` 디렉토리에 XML 매퍼 파일 생성

4. **모델 개발**
   - `backend/main/java/com/ssafy/trip/model/` 디렉토리에 엔티티 및 DTO 클래스 생성

## 주요 기능

1. **사용자 인증**
   - 회원가입, 로그인, 로그아웃
   - JWT 기반 인증
   - 사용자 프로필 관리

2. **숙소 검색 및 예약**
   - 지역별, 키워드별 숙소 검색
   - 필터링 및 정렬 기능
   - 숙소 상세 정보 조회
   - 객실 예약 및 결제

3. **리뷰 시스템**
   - 숙소 리뷰 작성, 수정, 삭제
   - 별점 평가
   - 리뷰 필터링 및 정렬

4. **호스트 기능**
   - 숙소 등록, 수정, 삭제
   - 객실 관리
   - 예약 관리 및 승인

5. **관리자 기능**
   - 사용자 관리
   - 숙소 및 리뷰 관리
   - 통계 및 보고서

## 한국관광공사 API 통합 구현

### 개요
이 프로젝트는 한국관광공사에서 제공하는 Tour API를 통합하여 관광 정보를 검색하고 표시하는 기능을 구현했습니다.
API 문서: https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15101578

### 백엔드 API 엔드포인트

#### 1. 지역 코드 조회 API
- 시도 목록 조회: `GET /api/tour/sidos`
- 구군 목록 조회: `GET /api/tour/guguns?sido={sidoCode}`

#### 2. 관광지 정보 조회 API
- 지역 기반 관광정보 조회: `GET /api/tour/attractions`
  - 파라미터:
    - areaCode: 지역 코드 (선택적)
    - sigunguCode: 시군구 코드 (선택적)
    - contentTypeId: 관광타입 ID (선택적)
    - pageNo: 페이지 번호 (선택적)
    - numOfRows: 한 페이지 결과 수 (선택적)

#### 3. 키워드 검색 API
- 키워드 기반 관광정보 검색: `GET /api/tour/search`
  - 파라미터:
    - keyword: 검색 키워드 (필수)
    - areaCode: 지역 코드 (선택적)
    - sigunguCode: 시군구 코드 (선택적)
    - contentTypeId: 관광타입 ID (선택적)
    - pageNo: 페이지 번호 (선택적)
    - numOfRows: 한 페이지 결과 수 (선택적)

#### 4. 상세 정보 조회 API
- 관광지 상세 정보 조회: `GET /api/tour/attraction/{contentId}`
  - 파라미터:
    - contentId: 콘텐츠 ID (경로 변수)
    - contentTypeId: 관광타입 ID (선택적 쿼리 파라미터)

### 프론트엔드 구현
프론트엔드에서는 다음과 같이 백엔드 API를 호출하여 관광 정보를 표시합니다:

```javascript
// 예시: 관광지 검색 API 호출
import axios from 'axios';

// API 서비스 함수
export const searchAttractions = async (keyword, areaCode, sigunguCode) => {
  try {
    const response = await axios.get('/api/tour/search', {
      params: {
        keyword,
        areaCode,
        sigunguCode
      }
    });
    return response.data;
  } catch (error) {
    console.error('관광지 검색 중 오류 발생:', error);
    throw error;
  }
};
```

### 콘텐츠 타입 ID 참조
- 12: 관광지
- 14: 문화시설
- 15: 축제공연행사
- 25: 여행코스
- 28: 레포츠
- 32: 숙박
- 38: 쇼핑
- 39: 음식점

### 백엔드 구현 클래스
- `TourApiProperties`: API 설정 정보 클래스 (application.properties 값 매핑)
- `TourApiService`: 한국관광공사 API 호출 및 데이터 처리
- `TourApiController`: API 엔드포인트 제공
- `TourApiResponse`: API 응답 모델 클래스
- `AreaDto`: 지역 정보 모델 클래스

### 백엔드 설정
`backend/main/resources/application.properties` 파일에 다음 설정이 필요합니다:

```properties
# 한국관광공사 API 설정
tourapi.base-url=https://apis.data.go.kr/B551011/KorService1
tourapi.mobile-os=ETC
tourapi.mobile-app=RoomTraveler
tourapi.default-num-of-rows=20
tourapi.default-page-no=1
tourapi.service-key=YOUR_SERVICE_KEY
```

### 에러 처리
백엔드에서는 다음과 같은 에러 처리를 구현했습니다:
- 서비스 키 누락 또는 유효하지 않은 경우 예외 처리
- API 응답 타임아웃 처리
- XML/JSON 파싱 오류 처리
- 결과 코드 검증 및 적절한 HTTP 상태 코드 반환

프론트엔드에서는 다음과 같이 에러를 처리합니다:
- API 호출 실패 시 사용자 친화적인 오류 메시지 표시
- 네트워크 오류 시 재시도 기능 제공
- 로딩 상태 표시
