# 🚀 Weady Server
<img width="600" height="400" alt="포스터" src="https://github.com/user-attachments/assets/e1ab7679-d35f-4544-b0ae-18c984559998" />

### 안녕하세요, 웨디(Weady) 백엔드 팀입니다. 👋

<br>  

---

<br> 

### 📖 프로젝트 소개
**웨디(Weady)** 는 오늘 하루를 어떻게 보낼지 제안하는 날씨 기반 일정 추천 iOS 애플리케이션입니다.

약 3,000여 지역의 단기예보 데이터를 분석하여, 날씨에 어울리는 옷차림과 장소, 코스를 추천합니다. 사용자는 날씨와 어울리는 맞춤형 일정을 한 번에 확인하고 선택할 수 있어, 계획 준비 시간을 줄이고 더 만족스러운 하루를 보낼 수 있습니다.

<br>

**주요 기능**
- **날씨 요약 및 옷차림 추천**: 기상청 단기예보 데이터(기온, 강수, 풍속 등)를 기반으로 오늘의 날씨와 적절한 옷차림을 제안합니다.
- **맞춤형 코스 추천**: 현재 날씨에 가장 어울리는 활동, 장소, 코스를 추천하여 계획의 번거로움을 줄여줍니다.
- **일정 기록 및 공유**: 추천받은 코스를 바탕으로 자신만의 하루 계획과 사진을 기록하고 공유하며 일상을 아카이빙할 수 있습니다.

<br>  

---

<br>

### 🧑‍💻 팀원 정보
|              백엔드 / 리드              |                 백엔드                  |                 백엔드                 |                 백엔드                 |                 백엔드                  |
|:----------------------------------:|:------------------------------------:|:-----------------------------------:|:-----------------------------------:|:------------------------------------:|
| <img src="https://github.com/user-attachments/assets/d63d51f0-85fe-4a7d-ba68-26fa030c80bb" width="120" alt="박장우"/>  |  <img src="https://github.com/user-attachments/assets/5c5740fb-221f-4444-b065-96a5330f5714" width=120px alt="김동완"/>  | <img src="https://github.com/user-attachments/assets/ebecfb9e-3930-4f9f-a83a-5dfc82adeb1b" width=120px alt="이은채"/> | <img src="https://github.com/user-attachments/assets/cb084e73-3c52-4121-8542-35732e6e379d" width=120px alt="이제원"/> | <img src="https://github.com/user-attachments/assets/a5158de6-7534-43cd-a9fe-b9da5b0e6972" width=120px alt="박지현"/>  |
| [박장우](https://github.com/pjw81226) | [김동완](https://github.com/ehddhks194) | [이은채](https://github.com/euuunchae) |   [이제원](https://github.com/nowOne2)       | [박지현](https://github.com/jlhyunii63) |

<br>  

---

<br>

### 🛠️ 기술 스택
- **Java**: `17`
- **Spring Boot**: `3.4.7`
- **MySQL**: `8.0`
- **Docker** | **Docker Hub** | **Github Actions** | **AWS EC2** | **AWS S3**
- **JPA** | **Swagger** | **Spirng Security** | **OAuth2.0** | **JWT** | **WebFlux**

<br>  

---

<br>

### 🏗️ 서버 아키텍처

<img width="801" height="601" alt="weady_acrhitecture drawio" src="https://github.com/user-attachments/assets/72afadb2-d70f-4dee-8226-ca63709b8963" />

<br>  

---

<br>

### 📝 ERD

<img width="8132" height="6038" alt="weadyERD" src="https://github.com/user-attachments/assets/b7135350-beaa-4845-8256-6dad7737be8e" />

(사진이 커서 살짝 깨져보일 수 있습니다! 확대하면 괜찮습니다.)

<br>  

---

<br>

### 📁 패키지 구조
```
src
└── main
    └── java
        └── com.weady.weady
            ├── common        # 공통 모듈 (API 응답 형식, 에러 처리 등)
            │   ├── apiResponse
            │   ├── config
            │   ├── constant
            │   ├── entity
            │   ├── error
            │   ├── external
            │   ├── security.jwt
            │   ├── util
            │   └── validation
            │
            ├── domain        # 핵심 비즈니스 로직
            │   ├── auth        # 인증/인가 (로그인, 회원가입)
            │   ├── board       # 웨디보드 (커뮤니티)
            │   ├── curation    # 큐레이션 (장소)
            │   ├── fashion     # 패션 (옷차림 추천)
            │   ├── location    # 지역
            │   ├── tags        # 태그, 카테고리
            │   ├── user        # 사용자
            │   ├── weadychive  # 웨디 기록 (아카이브)
            │   └── weather     # 날씨 (스케쥴러 포함)
            │
            └── WeadyApplication.java
```

<br>  

---

<br>

### 🌳 Branch 전략

<br>

| 브랜치                | 역할       | 설명                    |
|--------------------|----------|-----------------------------|
| `main`             | 배포·프로덕션용 | 배포 가능한 프로덕션 코드를 모아두는 브랜치입니다.  |
| `dev`              | 통합 개발    | 다음 버전 배포를 위한 개발 코드를 모아두는 브랜치입니다. |
| `feature/<issue-번호>-<설명>` | 단일 기능 구현 | 새로운 기능 개발을 위한 브랜치입니다. 개발이 완료되면 develop 브랜치로 병합합니다.              |
| `fix/<issue-번호>-<설명>` | 버그 픽스    |  버그를 수정하기 위한 브랜치입니다.              |
| `refactor<issue-번호>-<설명>` | 리펙토링     | 리펙토링을 위한 브랜치입니다.             |
| `release/<버전>` | 릴리스 준비   | 새로운 버전 배포를 준비하기 위한 브랜치입니다. |

예시) feature/101-invite-friends

<br>  

---

<br>

### ✍️ 커밋 컨벤션
**커밋을 할 때의 메세지는 다음의 규칙을 따라주세요!**  
조금 귀찮을 순 있어도 원할한 협업을 위해 꼭 지켜주세요.   
(언젠간 배우시게 될 부분이어서 연습을 위해 규칙을 정합니다!)

| `type`     | 용도                        |
| ---------- |---------------------------|
| `feat`     | 새로운 기능                    |
| `fix`      | 버그 수정                     |
| `docs`     | 문서 변경                     |
| `style`    | 포맷팅, 세미콜론 등 코드동작과 관련없는 변경 |
| `refactor` | 리팩터링                      |
| `perf`     | 성능 개선                     |
| `test`     | 테스트 코드 추가·수정              |
| `build`    | 빌드/패키지 매니저 설정             |
| `ci`       | CI 설정·스크립트                |
| `chore`    | 기타 잡무(린터 설정 등)            |
| `revert`   | 이전 커밋 되돌리기                |

<br>

| 파트              | 규칙                                                                                   |
|-----------------|--------------------------------------------------------------------------------------|
| **Header (필수)** | `type`, `subject`로 구성.<br>`subject`는 명령형 현재시제, 50자 이내, 마침표 ❌ <br> <br> 예시) `feat: JWT 리프레시 토큰 엔드포인트 추가`|

**예시:**
```
feat: JWT 리프레시 토큰 엔드포인트 추가
```
```
refactor: 사용자 조회 로직을 쿼리 빌더로 전환
```

