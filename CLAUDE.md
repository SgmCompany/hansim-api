# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# 빌드
./gradlew build

# 테스트 전체 실행
./gradlew test

# 단일 테스트 실행
./gradlew test --tests "com.sgm.hansimapi.패키지.클래스명"

# 로컬 실행 (local 프로파일)
./gradlew bootRun --args='--spring.profiles.active=local'

# 로컬 DB/Redis 실행
docker-compose up -d
```

## 환경 설정

로컬 실행 시 `application-local.yaml`이 활성화됩니다. 이 파일에는 MySQL 접속 정보(`localhost:3306/hansim`, user: `sgm`)와 Riot API Key가 포함되어 있습니다. `docker-compose.yaml`로 MySQL과 Redis를 띄운 뒤 실행하세요.

테스트는 H2 in-memory DB를 사용합니다(`application-test.yaml`).

## 아키텍처

레이어드 아키텍처를 따르며, 의존 방향은 항상 안쪽(도메인)을 향합니다.

```
presentation → application → domain ← infra
```

| 레이어 | 역할 |
|--------|------|
| `presentation` | REST 컨트롤러, 요청/응답 DTO |
| `application` | UseCase (비즈니스 흐름 조율), Command 객체 |
| `domain` | 핵심 도메인 모델, 인터페이스 정의 (외부 의존 없음) |
| `infra` | Riot API 호출(`RiotFetcherImpl`), JPA 엔티티 |
| `config` | Spring Bean 설정 (RestTemplate 등) |

### 핵심 흐름

`GET /api/v1/hansim/summary/{riotId}` 호출 시:
1. `SummaryController` → `SummaryCommand` 생성 (riotId `"이름-태그"` 형식으로 파싱)
2. `SummaryUseCase` → `RiotFetcher`로 puuid 조회 → 매치 목록 조회
3. `Summary.from(matches)` 로 승/패/승률 집계 후 반환

### 도메인 주요 개념

- **`RiotId`**: Riot ID(`name#tag` 또는 slug `name-tag`) 파싱 전담 값 객체
- **`TimeWindow`**: 조회 기간(Unix epoch ms). 기본값은 오늘(KST 06:00 ~ 익일 06:00), 최대 30일
- **`Match`**: 단일 게임 결과 (win/lose, KDA)
- **`Summary`**: 집계 결과 (totalGames, wins, losses, winRate)
- **`PlayerSummary`**: 큐별(노말/솔로랭크/자유랭크) 분리 통계
- **`QueueStat`**: 큐 단위 통계 + `hansimScore` (한심 점수)

### 주의 사항

- `RiotFetcher` 인터페이스는 `domain` 레이어에, 구현체 `RiotFetcherImpl`은 `infra` 레이어에 위치합니다 (의존성 역전).
- Riot API는 `asia.api.riotgames.com` 엔드포인트를 사용합니다.
- QueryDSL APT가 적용되어 있습니다. 엔티티 변경 후에는 `./gradlew compileJava`로 Q클래스를 재생성하세요.
