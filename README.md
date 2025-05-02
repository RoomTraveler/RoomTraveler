# trip_Spring_Gwangju_04_범수_황성헌

### 코드를 Spring Boot와 Mybatis로 변경했습니다

# 클래스 다이어그램
![123](/uploads/d54102aefd6decd20383b31cb2549f0b/123.png)
![2](/uploads/28879903bdb9ad25a8573ec6975fd38b/2.png)

## 폴더 구조
trip_spring_gwangju_04_BeomSu_HwangSH/   
├── .gitignore   
├── mvnw   
├── mvnw.cmd   
├── pom.xml   
├── src/   
│   ├── main/   
│   │   ├── java/   
│   │   │   └── com/   
│   │   │       └── ssafy/   
│   │   │           └── trip/   
│   │   │               ├── TripApplication.java   
│   │   │               ├── controller/   
│   │   │               │   ├── MapController.java   
│   │   │               │   ├── MemberController.java   
│   │   │               │   └── ViewController.java   
│   │   │               ├── model/   
│   │   │               │   ├── dao/   
│   │   │               │   │   ├── member/   
│   │   │               │   │   │   └── MemberDao.java   
│   │   │               │   │   └── trip/   
│   │   │               │   │       └── MapDAO.java   
│   │   │               │   ├── dto/   
│   │   │               │   │   ├── MapDTO.java   
│   │   │               │   │   └── Member.java   
│   │   │               │   └── service/   
│   │   │               │       ├── member/   
│   │   │               │       │   ├── MemberService.java   
│   │   │               │       │   └── MemberServiceImpl.java   
│   │   │               │       └── trip/   
│   │   │               │           ├── MapService.java   
│   │   │               │           └── MapServiceImpl.java   
│   │   │               └── util/   
│   │   │                   └── CryptoConfig.java   
│   │   └── resources/   
│   │       ├── application.properties   
│   │       ├── mappers/   
│   │       │   ├── map.xml   
│   │       │   └── member.xml   
│   │       ├── sql/   
│   │           └── db.sql   
├── README.md   


## 실행 화면 - 지도를 네이버 지도로 변경
![스크린샷_2025-04-25_170114](/uploads/e2e6ee09df7d2be5d7de0bc23a727bcf/스크린샷_2025-04-25_170114.png)

### 주의
지도를 바꾸다보니 내부에 동작을 변경해야하는데 시간 안에 하지 못해 앞으로 관통 프로젝트를 통해 개선해 나갈 것 입니다.
