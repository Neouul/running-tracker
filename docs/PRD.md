# Product Requirement Document (PRD) - Running Tracker

## 1. 프로젝트 개요 (Overview)
- **제품명**: Running Tracker (가칭)
- **목적**: 사용자의 운동 경로를 실시간으로 기록하고, 안정적인 백그라운드 위치 추적을 제공하여 운동 성과를 관리하는 앱.
- **핵심 가치**: 사용자 경험의 연속성 (데이터 복원), 배터리 효율성, 트렌디한 디자인.

---

## 2. 사용자 시나리오 (User Scenarios)

1. **운동 시작**: 메인 화면에서 시작 버튼을 누르면 포그라운드 서비스가 실행되며 지도에 실시간 위치가 표시된다.
2. **운동 중 (포그라운드)**: 지도 위에 파란색 Polyline으로 이동 경로가 실시간 렌더링되며, 하단에는 페이스/거리/시간 등이 표시된다. 화면은 꺼지지 않는다.
3. **운동 중 (백그라운드)**: 앱을 최소화해도 상단 알림창에 "운동 중" 상태가 표시되며 계속해서 위치를 수집한다.
4. **시스템에 의한 중단 및 복구**: 안드로이드 시스템에 의해 위치 수집 서비스가 강제 종료되더라도, 시스템에 의해 재시작되면 기존 경로 데이터를 불러와 이어서 추적한다.
5. **운동 일시정지**: 화면 유지 기능이 해제되며, 위치 수집이 일시적으로 멈춘다.
6. **운동 종료**: 수집된 전체 경로와 운동 데이터를 Room DB에 영구 저장한다.

---

## 3. 기능 요구사항 (Functional Requirements)

### 3.1 위치 추적 (Location Tracking)
- **라이브러리**: Google Maps SDK for Android (`Google Maps Compose`).
- **서비스**: `Foreground Service`를 사용하여 백그라운드 작업 수행.
- **배터리 최적화**: `PRIORITY_BALANCED_POWER_ACCURACY` 수준의 위치 정확도 권장.
- **경로 유지**: 실시간 좌표 데이터를 Local DB(Room)에 캐싱하여 서비스 재시작 시 복구.

### 3.2 운동 데이터 계산
- **이동 거리**: 위도/경도 간의 거리합 계산.
- **평균 페이스**: 총 이동 거리 대비 총 소요 시간 계산.
- **칼로리 소모**: (이동 거리/시간/사용자 평균 체중) 기반 추산 알고리즘 적용.

### 3.3 UI/UX 및 디자인
- **레이아웃**: 상단(지도), 하단(콘텐츠 영역) 5:5 또는 6:4 분할 방식.
- **디자인 테마**: 화이트 배경 + 노랑/초록 그라데이션 포인트 (Neon/Vibrant 스타일).
- **특수 기능**: 운동 중 `KEEP_SCREEN_ON` 플래그 관리.

### 3.4 예외 처리
- **GPS 미작동**: 위치 서비스 사용 불가능 시 다이얼로그 또는 SnackBar로 사용자에게 알림.
- **권한 관리**: ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, POST_NOTIFICATIONS, FOREGROUND_SERVICE_LOCATION 등 처리.

---

## 4. 비기능 요구사항 (Non-functional Requirements)
- **안정성**: `START_STICKY`를 통한 서비스 복구 메커니즘 필수 구현.
- **성능**: 지도 위 Polyline 렌더링 시 메모리 누수 및 프레임 드랍 방지.

---

## 5. 데이터 구조 (Data Schema - Draft)

### `Run` Table (최종 기록)
- `id`: Int (Primary Key)
- `timestamp`: Long (운동 날짜)
- `img`: Bitmap (Map Snapshot - 선택사항)
- `avgSpeedInKMH`: Float
- `distanceInMeters`: Int
- `timeInMillis`: Long
- `caloriesBurned`: Int

### `TrackingPoint` Table (실시간 복구용)
- `runId`: Int
- `latitude`: Double
- `longitude`: Double
- `sequence`: Int

---

## 6. 마일스톤 (Milestones)
1. **MVP v1.0**: 위치 추적 + 지도 표시 + 기본 기록 저장 기능.
2. **v1.1**: 고도화된 소모 칼로리 계산 + 고도 정보 추가.
3. **v1.2**: 운동 기록 리스트 및 상세 보기 구현.
