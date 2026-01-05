# 러닝 트래커 앱 요구사항 정의 (Requirements) - Updated

## 1. 핵심 기능 (Key Features)

- **운동 추적 시작/종료**: 
  - 시작 시 포그라운드 서비스 실행 및 상단 알림 ("운동 중") 표시.
  - 종료 시 데이터 계산 및 Room DB 저장.
- **실시간 경로 표시**: 
  - **Google Maps**를 사용하며, 수집된 좌표를 Polyline으로 실시간 렌더링.
- **백그라운드 위치 수집**: 
  - 앱이 백그라운드에 있어도 위치 수집 지속.
  - **배터리 효율**을 고려한 수집 정책 적용.
- **화면 유지 제어**: 
  - 운동 중 화면 켜짐 유지 (`KEEP_SCREEN_ON`), 일시정지/종료 시 해제.
- **데이터 보존 및 복구 전략**: 
  - 서비스 강제 종료 후 시스템 복구 시 (`START_STICKY`), **기존 경로 데이터를 유지하여 즉시 재렌더링**해야 함.
- **GPS 상태 대응**: 
  - GPS 비활성화 시 사용자 알림 처리.

---

## 2. 결정된 세부 사항

- **지도**: Google Maps (API Key 필요 - 추후 설정 필요)
- **데이터 측정 항목**: 시간, 이동 거리, **평균 페이스, 칼로리 소모량**
- **UI 구성**:
  - 상단: Google Map
  - 하단: 운동 정보(페이스, 칼로리 등) + 컨트롤 버튼(시작/정지/종료)
- **디자인 컨셉**: 
  - 화이트톤 베이스의 깔끔한 디자인.
  - **활기찬 노란색(Yellow) & 초록색(Green) 그라데이션** 포인트 (트렌디한 스타일).

---

## 3. 기술 스택
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Map Library**: Google Maps Compose SDK
- **Database**: Room (운동 기록 저장 및 복구용)
- **Service**: Foreground Service with Location Permissions
- **Architecture**: MVVM / Clean Architecture
