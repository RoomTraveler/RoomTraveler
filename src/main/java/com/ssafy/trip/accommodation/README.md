# API 기반 숙소 정보 시스템

이 문서는 한국관광공사 Tour API를 통해 숙소 정보를 가져오는 시스템에 대한 설명입니다.

## 개요

이 시스템은 한국관광공사에서 제공하는 Tour API를 통해 숙소 정보를 가져와 표시하는 기능을 구현했습니다. 기존의 데이터베이스 기반 숙소 정보 시스템과 함께 사용할 수 있으며, API 호출이 실패할 경우 데이터베이스에서 정보를 가져오는 폴백 메커니즘을 제공합니다.

## 주요 기능

1. **API 기반 숙소 정보 조회**
   - 모든 숙소 목록 조회
   - 지역별 숙소 목록 조회
   - 키워드 검색
   - 숙소 상세 정보 조회
   - 객실 정보 조회

2. **API 데이터 초기 가져오기**
   - 관리자 페이지에서 API 데이터 가져오기 기능
   - 지역별 필터링 옵션
   - 가져온 데이터 자동 DB 저장
   - 숙소, 객실, 이미지 데이터 통합 가져오기

3. **API 설정 관리**
   - API 설정 조회 및 수정
   - API 테스트 기능

4. **캐싱 기능**
   - 자주 사용되는 API 응답 캐싱
   - 캐시 만료 시간 설정

5. **폴백 메커니즘**
   - API 호출 실패 시 데이터베이스에서 정보 조회

## 구현 클래스

### 서비스 클래스
- `ApiAccommodationService`: API를 통해 숙소 정보를 가져오는 서비스
- `TourApiProperties`: Tour API 설정 정보를 담는 클래스
- `AccommodationService`: 숙소 정보 관리 서비스
- `AccommodationServiceImpl`: 숙소 정보 관리 서비스 구현체

### 컨트롤러 클래스
- `ApiAccommodationController`: API 기반 숙소 정보를 제공하는 컨트롤러
- `ApiConfigController`: API 설정을 관리하는 컨트롤러
- `AdminController`: 관리자 기능을 제공하는 컨트롤러 (API 데이터 가져오기 포함)
- `AccommodationController`: 숙소 정보 관리 컨트롤러

### DAO 클래스
- `AccommodationDao`: 숙소 데이터 액세스 인터페이스
- `RoomDao`: 객실 데이터 액세스 인터페이스
- `ImageDao`: 이미지 데이터 액세스 인터페이스

### 설정 클래스
- `RestTemplateConfig`: RestTemplate 및 ObjectMapper 빈을 제공하는 설정 클래스

## API 엔드포인트

### 숙소 정보 API
- `GET /api/accommodation/list`: 모든 숙소 목록 조회
- `GET /api/accommodation/region`: 지역별 숙소 목록 조회
- `GET /api/accommodation/search`: 키워드로 숙소 검색
- `GET /api/accommodation/detail/{accommodationId}`: 숙소 상세 정보 조회
- `GET /api/accommodation/room-detail/{roomId}`: 객실 상세 정보 조회
- `GET /api/accommodation/filter`: 필터링된 숙소 목록 조회

### API 설정 관리
- `GET /admin/api-config`: API 설정 페이지
- `POST /admin/api-config/update`: API 설정 업데이트
- `POST /admin/api-config/test`: API 테스트

## 설정



## 사용 방법

### 관리자 기능

1. 관리자 페이지(`/admin`)에 접속하여 대시보드를 확인합니다.
2. API 설정 관리(`/admin/api-config`)에서 API 설정을 확인하고 필요한 경우 수정합니다.
3. API 테스트 기능을 사용하여 API가 정상적으로 동작하는지 확인합니다.
4. 숙소 관리(`/admin/accommodations`) 페이지에서 다음 기능을 사용할 수 있습니다:
   - "TourAPI에서 숙소 가져오기" 버튼을 클릭하여 API에서 숙소 데이터를 가져옵니다.
   - 지역(시도, 구군) 및 가져올 숙소 수를 선택할 수 있습니다.
   - 가져온 데이터는 자동으로 데이터베이스에 저장됩니다.
   - "샘플 객실 및 지역 데이터 생성" 버튼을 클릭하여 테스트용 샘플 데이터를 생성할 수 있습니다.

### 호스트 기능

1. 호스트 페이지(`/host/list`)에서 등록된 숙소 목록을 확인할 수 있습니다.
2. 숙소 등록 및 관리 기능을 통해 API에서 가져온 데이터를 보완하거나 수정할 수 있습니다.

### 일반 사용자 기능

1. 웹 애플리케이션에서 `/api/accommodation/` 경로로 시작하는 URL을 통해 API 기반 숙소 정보를 조회할 수 있습니다.
2. 숙소 목록(`/accommodation/list`) 페이지에서 API에서 가져온 숙소 정보를 확인할 수 있습니다.
3. 숙소 상세(`/accommodation/detail/{accommodationId}`) 페이지에서 API에서 가져온 숙소의 상세 정보를 확인할 수 있습니다.

## 테스트 방법

### API 데이터 가져오기 테스트

1. 관리자 계정으로 로그인합니다.
2. `/admin/api-config` 페이지에서 API 설정이 올바르게 구성되어 있는지 확인합니다.
3. API 테스트 기능을 사용하여 API 응답을 확인합니다.
4. `/admin/accommodations` 페이지로 이동합니다.
5. "TourAPI에서 숙소 가져오기" 버튼을 클릭하고 필요한 옵션을 선택합니다.
6. 가져오기 작업이 완료되면 페이지가 새로고침되고 가져온 숙소 목록이 표시됩니다.
7. 숙소 상세 페이지로 이동하여 객실 정보와 이미지가 올바르게 표시되는지 확인합니다.

### 역할별 기능 테스트

1. **관리자 테스트**:
   - 숙소 상태 변경 (활성화/비활성화)
   - 숙소 삭제
   - 사용자 관리
   - API 설정 관리

2. **호스트 테스트**:
   - 숙소 등록 및 수정
   - 객실 관리
   - 예약 관리

3. **일반 사용자 테스트**:
   - 숙소 검색 및 필터링
   - 객실 예약
   - 장바구니 기능
   - 결제 프로세스

## 주의 사항

- API 호출 횟수에 제한이 있을 수 있으므로, 캐싱 기능을 적극 활용하는 것이 좋습니다.
- API 키는 보안을 위해 안전하게 관리해야 합니다.
- API 응답 형식이 변경될 경우 시스템이 정상적으로 동작하지 않을 수 있으므로, 정기적인 모니터링이 필요합니다.
- 대량의 데이터를 가져올 경우 시스템 성능에 영향을 줄 수 있으므로, 적절한 수의 데이터만 가져오는 것이 좋습니다.
