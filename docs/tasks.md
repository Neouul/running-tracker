# Running Tracker 프로젝트 태스크 리스트

## 1. 프로젝트 초기 설정 및 인프라 (Setup)

- [ ] `build.gradle.kts` 의존성 추가 (Google Maps, Room, Koin/Hilt, Compose Lifecycle)
- [ ] `AndroidManifest.xml` 권한 및 서비스 선언
    - [ ] 위치 권한 (Fine, Coarse, Background)
    - [ ]포그라운드 서비스 및 알림 권한
- [ ] BaseApplication 클래스 생성 및 DI 라이브러리 초기화 설정

## 2. 데이터 레이어 구현 (Data Layer)

- [ ] Room 데이터베이스 엔티티 정의
    - [ ] `Run`: 완료된 운동 기록 저장
    - [ ] `TrackingPoint`: 실시간 복구용 좌표 데이터 저장 
- [ ] Room DAO 및 Database 클래스 생성
- [ ] 메인 Repository 구현 (데이터 저장 및 복구 로직 포함)

## 3. 포그라운드 서비스 구현 (Tracking Service)

- [ ] `TrackingService` 기본 구조 생성 및 `Notification` 구현
- [ ] `FusedLocationProviderClient`를 이용한 위치 수집 로직 구현
    - [ ] 배터리 효율을 고려한 LocationRequest 설정
- [ ] 서비스 내 실시간 데이터(StateFlow) 관리 및 좌표 캐싱 (Room 연동)
- [ ] `START_STICKY` 및 서비스 재시작 시 데이터 복구 로직 구현

## 4. UI/UX 개발 (Jetpack Compose)

- [ ] 메인 레이아웃 구성 (상단 Map / 하단 Info Card 6:4 분할)
- [ ] Google Maps 연동 및 실시간 Polyline 렌더링 구현
- [ ] 하단 대시보드 UI (페이스, 시간, 거리, 칼로리) 구현
    - [ ] 화이트 톤 + 노랑/초록 그라데이션 스타일 적용
- [ ] 운동 제어 버튼 시스템 (시작/정지/종료) 및 상태 관리
- [ ] 화면 유지(KEEP_SCREEN_ON) 로직 연동

## 5. 예외 처리 및 폴리싱 (Exception Handling)

- [ ] GPS 비활성화 상태 탐지 및 알림 UI
- [ ] 위치 권한 거부 시 대응 로직
- [ ] 다크 모드/라이트 모드 테마 최적화 (화이트톤 중심)
- [ ] 최종 테스트 및 데이터 저장 확인

## 6. 사후 관리 및 아카이빙

- [ ] 코드 정리 및 final 빌드 확인