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

## Git 컨벤션

### 브랜치 네이밍

```
<type>/<short-description>

feat/riot-api-caching
fix/429-backoff
refactor/riot-fetcher
chore/gitignore-update
```

| type | 사용 시점 |
|------|----------|
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `refactor` | 동작 변경 없는 코드 개선 |
| `chore` | 빌드, 설정, 의존성 |
| `docs` | 문서, 주석 |
| `test` | 테스트 코드 |

### 커밋 메시지

```
<type>: <제목> (50자 이내)

<본문> — 필요한 경우에만, 무엇을 왜 변경했는지
```

- 제목은 한국어, 기술 용어(Redis, PUUID, KDA 등)는 영어 허용
- 제목 끝에 마침표 없음
- 본문은 `왜` 변경했는지 위주로 작성, 생략 가능

**예시**

```
feat: 소환사 즐겨찾기 localStorage 저장 기능 추가

fix: Riot API 429 응답 시 backoff 공유 안 되는 문제 수정

refactor: RiotFetcherImpl 매치 파싱 로직 분리

chore: RestTemplate connectTimeout 3초로 설정
```

### PR

**제목**: 커밋 메시지와 동일한 형식

```
feat: 소환사 즐겨찾기 기능 추가
```

**본문 템플릿**

```markdown
## 작업 내용
- 변경한 것을 bullet로 간략히

## 특이사항 (선택)
- 리뷰어에게 주의를 당부할 사항, 의도적인 trade-off 등
```

### Merge 전략

- **Merge commit** 방식 사용 (GitHub UI에서 수동 merge)
- feature 브랜치의 모든 커밋이 main에 그대로 남으므로, WIP 커밋("수정", "다시" 등)은 merge 전에 `git rebase -i`로 정리 후 merge

### develop 브랜치 도입 시 (추후)

프로덕션 배포가 본격화되면 `feature → develop → main` 구조로 전환하고, main에 대한 직접 push를 GitHub branch protection으로 차단할 예정.
