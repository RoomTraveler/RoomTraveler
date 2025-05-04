# trip_Spring_Gwangju_04_범수_황성헌

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
