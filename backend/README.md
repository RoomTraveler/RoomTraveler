# 방구석 여행자 (RoomTraveler) - 백엔드

이 디렉토리는 방구석 여행자 프로젝트의 Spring Boot 기반 백엔드 애플리케이션을 포함하고 있습니다. 프론트엔드와 분리된 구조로, RESTful API를 제공합니다.

## 백엔드 구조

```
backend/
├── main/                 # 메인 소스 코드
│   ├── java/             # Java 소스 파일
│   │   └── com/ssafy/trip/
│   │       ├── controller/  # REST 컨트롤러
│   │       ├── service/     # 비즈니스 로직
│   │       ├── mapper/      # MyBatis 매퍼 인터페이스
│   │       ├── model/       # 데이터 모델 (DTO, 엔티티)
│   │       ├── exception/   # 예외 처리
│   │       ├── config/      # 설정 클래스
│   │       └── util/        # 유틸리티 클래스
│   └── resources/        # 리소스 파일
│       ├── mappers/      # MyBatis XML 매퍼 파일
│       ├── static/       # 정적 리소스
│       ├── templates/    # 템플릿 파일 (필요한 경우)
│       └── application.properties  # 애플리케이션 설정
├── test/                 # 테스트 코드
│   └── java/
│       └── com/ssafy/trip/
└── pom.xml               # Maven 프로젝트 설정
```

## 기술 스택

- **Spring Boot 3**: 자바 기반 웹 애플리케이션 프레임워크
- **MyBatis**: SQL 매핑 프레임워크
- **MySQL**: 관계형 데이터베이스
- **Spring Security**: 인증 및 권한 부여
- **JWT**: JSON Web Token 기반 인증
- **Lombok**: 반복 코드 감소를 위한 라이브러리
- **JUnit 5**: 단위 테스트 프레임워크

## 주요 API 엔드포인트

### 사용자 API

- `POST /api/user/register`: 회원가입
- `POST /api/user/login`: 로그인
- `POST /api/user/logout`: 로그아웃
- `GET /api/user/me`: 현재 로그인한 사용자 정보 조회
- `PUT /api/user/me`: 사용자 정보 수정
- `PUT /api/user/password`: 비밀번호 변경

### 숙소 API

- `GET /api/accommodations`: 숙소 목록 조회
- `GET /api/accommodations/{id}`: 숙소 상세 정보 조회
- `POST /api/accommodations`: 숙소 등록 (호스트 권한 필요)
- `PUT /api/accommodations/{id}`: 숙소 정보 수정 (호스트 권한 필요)
- `DELETE /api/accommodations/{id}`: 숙소 삭제 (호스트 권한 필요)

### 리뷰 API

- `GET /api/accommodations/{id}/reviews`: 숙소 리뷰 목록 조회
- `POST /api/accommodations/{id}/reviews`: 리뷰 작성
- `PUT /api/reviews/{id}`: 리뷰 수정
- `DELETE /api/reviews/{id}`: 리뷰 삭제
- `GET /api/user/me/reviews`: 내 리뷰 목록 조회

### 예약 API

- `GET /api/user/me/reservations`: 내 예약 목록 조회
- `POST /api/accommodations/{id}/reserve`: 숙소 예약
- `PUT /api/reservations/{id}/cancel`: 예약 취소
- `GET /api/host/reservations`: 호스트의 숙소 예약 목록 조회 (호스트 권한 필요)
- `PUT /api/host/reservations/{id}/confirm`: 예약 확정 (호스트 권한 필요)

### 관광 정보 API (한국관광공사 API 연동)

- `GET /api/tour/sidos`: 시도 목록 조회
- `GET /api/tour/guguns`: 구군 목록 조회
- `GET /api/tour/attractions`: 관광지 정보 조회
- `GET /api/tour/search`: 키워드 기반 관광지 검색
- `GET /api/tour/attraction/{contentId}`: 관광지 상세 정보 조회

## 데이터베이스 설계

### 주요 테이블

- `users`: 사용자 정보
- `accommodations`: 숙소 정보
- `rooms`: 객실 정보
- `reservations`: 예약 정보
- `reviews`: 리뷰 정보
- `areas`: 지역 정보 (시도, 구군)

## 개발 가이드

### API 개발 프로세스

1. **모델 클래스 정의**
   - `model` 패키지에 DTO 및 엔티티 클래스 생성

2. **매퍼 인터페이스 정의**
   - `mapper` 패키지에 MyBatis 매퍼 인터페이스 생성
   - `resources/mappers` 디렉토리에 XML 매퍼 파일 생성

3. **서비스 계층 구현**
   - `service` 패키지에 비즈니스 로직 구현
   - 트랜잭션 관리 및 예외 처리

4. **컨트롤러 구현**
   - `controller` 패키지에 REST 컨트롤러 생성
   - API 엔드포인트 정의 및 요청 처리

5. **테스트 작성**
   - `test` 디렉토리에 단위 테스트 및 통합 테스트 작성

### 보안 가이드

- 모든 비밀번호는 BCrypt로 암호화하여 저장
- JWT 토큰은 적절한 만료 시간 설정
- 권한 기반 접근 제어 구현
- API 요청에 대한 유효성 검사 수행

## 실행 방법

1. 의존성 설치:
```bash
mvn install
```

2. 애플리케이션 실행:
```bash
mvn spring-boot:run
```

3. 프로덕션 빌드:
```bash
mvn clean package
```

## 환경 설정

`application.properties` 파일에서 다음 설정을 구성할 수 있습니다:

```properties
# 데이터베이스 설정
spring.datasource.url=jdbc:mysql://localhost:3306/ssafytrip
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# MyBatis 설정
mybatis.mapper-locations=classpath:mappers/**/*.xml
mybatis.type-aliases-package=com.ssafy.trip.model

# JWT 설정
jwt.secret=your_jwt_secret_key
jwt.expiration=86400000

# 한국관광공사 API 설정
tourapi.base-url=https://apis.data.go.kr/B551011/KorService1
tourapi.service-key=your_service_key
```