# Vue.js와 Spring Boot 백엔드 통신 가이드

이 문서는 Vue.js 프론트엔드와 Spring Boot 백엔드 간의 통신 설정 방법을 설명합니다.

## 개요

프론트엔드(Vue.js)와 백엔드(Spring Boot)는 서로 다른 포트에서 실행되기 때문에 CORS(Cross-Origin Resource Sharing) 설정이 필요합니다.

- Vue.js 개발 서버: `http://localhost:5173` (기본 포트)
- Spring Boot 백엔드: `http://localhost:8080`

## 문제 해결 방법

### 1. 백엔드 CORS 설정

Spring Boot 백엔드에서는 CORS를 설정하여 Vue.js 프론트엔드의 요청을 허용해야 합니다. 이를 위해 다음과 같은 설정 클래스를 생성했습니다:

```java
package com.ssafy.trip.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 모든 API 엔드포인트에 대해 CORS 설정
        registry.addMapping("/api/**")
                // 허용할 오리진(출처) 설정 - Vue.js 개발 서버의 기본 포트는 5173
                .allowedOrigins("http://localhost:5173")
                // 허용할 HTTP 메서드 설정
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 인증 정보(쿠키, 인증 헤더 등) 허용
                .allowCredentials(true)
                // 허용할 헤더 설정
                .allowedHeaders("*")
                // 브라우저가 CORS 설정을 캐시하는 시간(초)
                .maxAge(3600);
    }
}
```

### 2. 프론트엔드 API 통신 설정

Vue.js 프론트엔드에서는 Axios를 사용하여 백엔드 API와 통신하는 것을 권장합니다.

1. **Axios 설치**
   ```bash
   npm install axios
   ```

2. **API 기본 설정**
   - `src/api/index.js` 파일을 생성하고 기본 설정을 추가합니다.
   - 백엔드 서버의 기본 URL을 설정합니다 (예: `http://localhost:8080/api`).
   - 필요한 헤더와 인증 정보 설정을 추가합니다.

3. **API 모듈 구성**
   - 각 기능별로 API 모듈을 분리하여 관리합니다 (예: 사용자 API, 숙소 API 등).
   - 각 API 함수는 Axios 인스턴스를 사용하여 백엔드와 통신합니다.

4. **컴포넌트에서 API 사용**
   - Vue 컴포넌트에서 API 모듈을 가져와 사용합니다.
   - 비동기 요청을 처리하기 위해 async/await 또는 Promise를 사용합니다.
   - 에러 처리를 적절히 구현합니다.

## 주의사항

1. **인증 토큰 관리**
   - JWT 등의 인증 토큰을 사용할 경우, 요청 헤더에 포함시켜야 합니다.
   - Axios 인터셉터를 사용하여 모든 요청에 토큰을 자동으로 추가할 수 있습니다.

2. **에러 처리**
   - API 요청 실패 시 적절한 에러 처리가 필요합니다.
   - 인증 오류(401), 권한 오류(403) 등 특정 에러 코드에 대한 처리 로직을 구현합니다.

3. **환경 변수 사용**
   - 개발/운영 환경에 따라 API 주소가 달라질 수 있으므로, 환경 변수를 사용하는 것이 좋습니다.
   - `.env.development`와 `.env.production` 파일을 사용하여 환경별 설정을 관리합니다.

이 설정을 통해 Vue.js 프론트엔드와 Spring Boot 백엔드 간의 원활한 통신이 가능합니다.