# 방구석 여행자 (RoomTraveler) - 모노레포 프로젝트

이 프로젝트는 숙박 예약 서비스를 제공하는 웹 애플리케이션으로, 모노레포 구조로 구성되어 있습니다. 프론트엔드는 Vue.js, 백엔드는 Spring Boot를 사용합니다.

## 프로젝트 구조

```
my-monorepo/
├── apps/
│   ├── web/              # Vue 앱
│   │   ├── package.json
│   │   ├── vite.config.js
│   │   └── src/
│   └── api/              # Spring Boot 앱
│       ├── pom.xml
│       └── src/
│           └── main/java/…
├── packages/
│   ├── ui/               # 공통 UI 컴포넌트 패키지
│   │   ├── package.json
│   │   └── src/
│   └── utils/            # 공통 유틸리티 코드
│       ├── package.json
│       └── src/
├── package.json
└── turbo.json
```

## 기술 스택

### 프론트엔드
- Vue.js 3
- Vite
- Vuex (상태 관리)
- Vue Router
- Bootstrap 5

### 백엔드
- Spring Boot 3
- MyBatis
- MySQL
- RESTful API

### 개발 도구
- Turborepo (모노레포 관리)
- npm (패키지 관리)
- Git (버전 관리)

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

2. 의존성 설치
```bash
npm install
```

3. 데이터베이스 설정
- MySQL에 `ssafytrip` 데이터베이스 생성
- `apps/api/src/main/resources/application.properties` 파일에서 데이터베이스 연결 정보 확인 및 수정

## 실행 방법

### 개발 모드

1. 전체 프로젝트 실행
```bash
npm run dev
```

2. 프론트엔드만 실행
```bash
npm run dev:web
```

3. 백엔드만 실행
```bash
npm run dev:api
```

### 빌드

1. 전체 프로젝트 빌드
```bash
npm run build
```

2. 프론트엔드만 빌드
```bash
npm run build:web
```

3. 백엔드만 빌드
```bash
npm run build:api
```

## 개발 워크플로우

### 프론트엔드 개발

1. Vue 컴포넌트 개발
   - `apps/web/src/components/` 디렉토리에 컴포넌트 생성
   - `apps/web/src/views/` 디렉토리에 페이지 컴포넌트 생성

2. 라우팅 설정
   - `apps/web/src/router/index.js` 파일에서 라우트 설정

3. 상태 관리
   - `apps/web/src/store/` 디렉토리에서 Vuex 스토어 설정

### 백엔드 개발

1. API 엔드포인트 개발
   - `apps/api/src/main/java/com/ssafy/trip/` 디렉토리에 컨트롤러 생성

2. 데이터 모델 개발
   - `apps/api/src/main/java/com/ssafy/trip/` 디렉토리에 모델 클래스 생성

3. 데이터베이스 연동
   - `apps/api/src/main/resources/mappers/` 디렉토리에 MyBatis 매퍼 XML 파일 생성

### 공통 패키지 개발

1. UI 컴포넌트 개발
   - `packages/ui/src/` 디렉토리에 공통 UI 컴포넌트 생성

2. 유틸리티 함수 개발
   - `packages/utils/src/` 디렉토리에 공통 유틸리티 함수 생성

## 주요 기능

1. 사용자 인증
   - 회원가입, 로그인, 로그아웃
   - 사용자 프로필 관리

2. 숙소 검색 및 예약
   - 지역별, 키워드별 숙소 검색
   - 숙소 상세 정보 조회
   - 객실 예약

3. 리뷰 관리
   - 숙소 리뷰 작성, 수정, 삭제
   - 평점 시스템

4. 호스트 기능
   - 숙소 등록, 수정, 삭제
   - 객실 관리
   - 예약 관리

5. 관리자 기능
   - 사용자 관리
   - 숙소 관리
   - 지역 데이터 관리

## 한국관광공사 API 통합 구현

### 개요
이 프로젝트는 한국관광공사에서 제공하는 Tour API를 통합하여 관광 정보를 검색하고 표시하는 기능을 구현했습니다.
API 문서: https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15101578

### 구현된 기능

#### 1. 지역 코드 조회 API
- 시도 목록 조회: `/accommodation/api/sidos`
- 구군 목록 조회: `/accommodation/api/guguns?sido={sidoCode}`

#### 2. 관광지 정보 조회 API
- 지역 기반 관광정보 조회: `/accommodation/api/attractions`
  - 파라미터:
    - areaCode: 지역 코드 (선택적)
    - sigunguCode: 시군구 코드 (선택적)
    - contentTypeId: 관광타입 ID (선택적)
    - pageNo: 페이지 번호 (선택적)
    - numOfRows: 한 페이지 결과 수 (선택적)

#### 3. 키워드 검색 API
- 키워드 기반 관광정보 검색: `/accommodation/api/search`
  - 파라미터:
    - keyword: 검색 키워드 (필수)
    - areaCode: 지역 코드 (선택적)
    - sigunguCode: 시군구 코드 (선택적)
    - contentTypeId: 관광타입 ID (선택적)
    - pageNo: 페이지 번호 (선택적)
    - numOfRows: 한 페이지 결과 수 (선택적)

#### 4. 상세 정보 조회 API
- 관광지 상세 정보 조회: `/accommodation/api/attraction/detail`
  - 파라미터:
    - contentId: 콘텐츠 ID (필수)
    - contentTypeId: 관광타입 ID (선택적)

### 콘텐츠 타입 ID
- 12: 관광지
- 14: 문화시설
- 15: 축제공연행사
- 25: 여행코스
- 28: 레포츠
- 32: 숙박
- 38: 쇼핑
- 39: 음식점

### 테스트 페이지
API 기능을 테스트할 수 있는 페이지가 제공됩니다: `/tourapi/test`

이 페이지에서는 다음 기능을 테스트할 수 있습니다:
- 지역 코드 조회
- 키워드 검색
- 지역 기반 관광정보 조회
- 상세 정보 조회

### 구현 클래스
- `TourApiProperties`: API 설정 정보 클래스
- `TourApiRestController`: API 요청 처리 컨트롤러
- `ApiResponse`: API 응답 모델 클래스
- `AreaDto`: 지역 정보 모델 클래스
- `TourApiTestController`: 테스트 페이지 제공 컨트롤러

### 설정
`application.properties` 파일에 다음 설정이 필요합니다:

```properties
tourapi.base-url=https://apis.data.go.kr/B551011/KorService1
tourapi.mobile-os=ETC
tourapi.mobile-app=AppTest
tourapi.default-num-of-rows=100
tourapi.default-page-no=1
tourapi.service-key=YOUR_SERVICE_KEY
```

### 에러 처리
모든 API는 다음과 같은 에러 처리를 포함합니다:
- 서비스 키 누락 또는 유효하지 않은 경우
- API 응답이 없는 경우
- XML 에러 응답 처리
- JSON 파싱 오류 처리
- 결과 코드 검증
