package com.sgm.hansimapi.domain.summary;

import com.sgm.hansimapi.domain.riot.Match;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * NINE_TO_SIX 근무 유형 기준 한심지수(HLS) 계산기.
 *
 * <p>알고리즘 (가중치 합 100):
 * <ul>
 *   <li>A. 볼륨 30 — 총 플레이 분(3h 만점 20) + 판수(6판 만점 10)</li>
 *   <li>B. 결과 20 — 패배/서렌/KDA 기반</li>
 *   <li>C. 심야 15 — KST 00~06 누적 분(60분 만점)</li>
 *   <li>D. 주말 10 — 토/일 누적 분(120분 만점)</li>
 *   <li>E. 세션 10 — 최장 연속 세션(120분 만점, 세션 간격 ≤30분)</li>
 *   <li>F. 연패 10 — 최대 연패(5연패 만점, 2연패부터 증가)</li>
 *   <li>G. 분노재큐 5 — 패배 후 5분 내 재큐 횟수(3회 만점)</li>
 * </ul>
 * 부정 지표 상한: B+C+D+F+G ≤ 60<br>
 * 최종: HLS = round(A + E + min(60, B+C+D+F+G))
 */
public class HlsCalculator {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 상한값 상수
    private static final double VOLUME_MINUTE_CAP  = 180.0; // 3시간
    private static final int    VOLUME_GAME_CAP    = 6;
    private static final double LATE_NIGHT_CAP_MIN = 60.0;  // 60분
    private static final double WEEKEND_CAP_MIN    = 120.0; // 120분
    private static final double SESSION_CAP_MIN    = 120.0; // 2시간
    private static final int    STREAK_CAP         = 4;     // 5연패(streak-1 기준 4)
    private static final int    TILT_CAP           = 3;     // 3회

    private static final long SESSION_GAP_MS  = 30L * 60 * 1000;  // 30분
    private static final long TILT_GAP_MS     = 5L  * 60 * 1000;  // 5분
    private static final int  LOW_PLAY_MIN    = 10;                // 저활동 컷 (10분 미만 → HLS=0)

    private HlsCalculator() {}

    public static HlsResult calculate(List<Match> matches) {
        if (matches.isEmpty()) return zero();

        // 저활동 컷: 총 플레이 10분 미만 → HLS=0
        int totalMinutes = matches.stream().mapToInt(m -> m.getGameDuration() / 60).sum();
        if (totalMinutes < LOW_PLAY_MIN) return zero();

        // 최신순이 아닌 오래된 순으로 정렬 (세션/연패/분노재큐 계산 기준)
        List<Match> sorted = matches.stream()
                .sorted(Comparator.comparingLong(Match::getGameStart))
                .toList();

        int volume       = calcVolume(sorted, totalMinutes);
        int result       = calcResult(sorted);
        int lateNight    = calcLateNight(sorted);
        int weekend      = calcWeekend(sorted);
        int session      = calcSession(sorted);
        int losingStreak = calcLosingStreak(sorted);
        int tilt         = calcTiltRequeue(sorted);

        int negative = Math.min(60, result + lateNight + weekend + losingStreak + tilt);
        int total    = (int) Math.round(volume + session + negative);

        return new HlsResult(total, volume, result, lateNight, weekend, session, losingStreak, tilt);
    }

    /** A. 볼륨 (30): 분 점수(20) + 판수 점수(10) */
    private static int calcVolume(List<Match> sorted, int totalMinutes) {
        double minuteScore = Math.min(totalMinutes, VOLUME_MINUTE_CAP) / VOLUME_MINUTE_CAP * 20;
        double gameScore   = Math.min(sorted.size(), VOLUME_GAME_CAP) / (double) VOLUME_GAME_CAP * 10;
        return (int) Math.round(minuteScore + gameScore);
    }

    /**
     * B. 결과 (20): 게임별 rawScore 평균을 20점으로 환산.
     * rawScore per game:
     *   - 승=0, 패=1 (base)
     *   - 얼리서렌 +0.5, 일반서렌 +0.3
     *   - gameDuration < 20분 이면서 패배 → 스톰프 +0.5
     *   - KDA≥3 → -0.2, KDA≤1 → +0.2
     *   - clamp [0, 1.5]
     */
    private static int calcResult(List<Match> sorted) {
        double sum = 0;
        for (Match m : sorted) {
            double raw = m.isWin() ? 0.0 : 1.0;

            if (!m.isWin() && m.getGameDuration() < 20 * 60) raw += 0.5; // 20분 이내 패배(압살)

            if (m.isGameEndedInEarlySurrender())     raw += 0.5;
            else if (m.isGameEndedInSurrender())     raw += 0.3;

            double kda = (m.getKills() + m.getAssists()) / Math.max(1.0, m.getDeaths());
            if (kda >= 3) raw -= 0.2;
            else if (kda <= 1) raw += 0.2;

            sum += Math.min(1.5, Math.max(0.0, raw));
        }
        double ratio = sum / (sorted.size() * 1.5);
        return (int) Math.round(ratio * 20);
    }

    /** C. 심야 (15): KST 00:00~06:00 구간 누적 분. 60분 만점 → 15점 */
    private static int calcLateNight(List<Match> sorted) {
        long totalMs = sorted.stream()
                .mapToLong(m -> overlapMs(m.getGameStart(), m.getGameDuration(), 0, 6))
                .sum();
        double minutes = totalMs / 60_000.0;
        return (int) Math.round(Math.min(minutes, LATE_NIGHT_CAP_MIN) / LATE_NIGHT_CAP_MIN * 15);
    }

    /** D. 주말 (10): KST 토/일 누적 분. 120분 만점 → 10점 */
    private static int calcWeekend(List<Match> sorted) {
        long totalMs = 0;
        for (Match m : sorted) {
            long gameStartMs = m.getGameStart();
            long gameEndMs   = gameStartMs + (long) m.getGameDuration() * 1000;

            LocalDate startDate = Instant.ofEpochMilli(gameStartMs).atZone(KST).toLocalDate();
            LocalDate endDate   = Instant.ofEpochMilli(gameEndMs).atZone(KST).toLocalDate();

            for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
                DayOfWeek dow = d.getDayOfWeek();
                if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                    long dayStart = d.atStartOfDay(KST).toInstant().toEpochMilli();
                    long dayEnd   = d.plusDays(1).atStartOfDay(KST).toInstant().toEpochMilli();
                    long oStart   = Math.max(gameStartMs, dayStart);
                    long oEnd     = Math.min(gameEndMs, dayEnd);
                    if (oEnd > oStart) totalMs += (oEnd - oStart);
                }
            }
        }
        double minutes = totalMs / 60_000.0;
        return (int) Math.round(Math.min(minutes, WEEKEND_CAP_MIN) / WEEKEND_CAP_MIN * 10);
    }

    /**
     * E. 세션 (10): 경기 간격 ≤30분을 같은 세션으로 묶어 최장 세션 계산.
     * 세션 길이 = 첫 경기 시작 ~ 마지막 경기 종료.
     * 120분 만점 → 10점
     */
    private static int calcSession(List<Match> sorted) {
        if (sorted.isEmpty()) return 0;

        List<long[]> sessions = new ArrayList<>(); // [sessionStartMs, sessionEndMs]
        long sessionStart = sorted.get(0).getGameStart();
        long sessionEnd   = sorted.get(0).getGameStart() + (long) sorted.get(0).getGameDuration() * 1000;

        for (int i = 1; i < sorted.size(); i++) {
            Match m     = sorted.get(i);
            long mStart = m.getGameStart();
            long mEnd   = mStart + (long) m.getGameDuration() * 1000;

            if (mStart - sessionEnd <= SESSION_GAP_MS) {
                // 같은 세션 연장
                sessionEnd = Math.max(sessionEnd, mEnd);
            } else {
                sessions.add(new long[]{sessionStart, sessionEnd});
                sessionStart = mStart;
                sessionEnd   = mEnd;
            }
        }
        sessions.add(new long[]{sessionStart, sessionEnd});

        long longestMs = sessions.stream()
                .mapToLong(s -> s[1] - s[0])
                .max()
                .orElse(0L);

        double minutes = longestMs / 60_000.0;
        return (int) Math.round(Math.min(minutes, SESSION_CAP_MIN) / SESSION_CAP_MIN * 10);
    }

    /**
     * F. 연패 (10): 최대 연속 패배 수.
     * 2연패부터 증가, 5연패 이상 만점(10점).
     */
    private static int calcLosingStreak(List<Match> sorted) {
        int maxStreak = 0;
        int cur       = 0;
        for (Match m : sorted) {
            if (!m.isWin()) { cur++; maxStreak = Math.max(maxStreak, cur); }
            else              cur = 0;
        }
        int streakScore = Math.max(0, maxStreak - 1); // 2연패부터
        return (int) Math.round(Math.min(streakScore, STREAK_CAP) / (double) STREAK_CAP * 10);
    }

    /**
     * G. 분노재큐 (5): 패배 직후 5분 내에 다음 게임이 시작된 횟수.
     * 3회 이상 만점(5점).
     */
    private static int calcTiltRequeue(List<Match> sorted) {
        int count = 0;
        for (int i = 0; i < sorted.size() - 1; i++) {
            Match cur  = sorted.get(i);
            Match next = sorted.get(i + 1);
            if (!cur.isWin()) {
                long curEnd = cur.getGameStart() + (long) cur.getGameDuration() * 1000;
                if (next.getGameStart() - curEnd <= TILT_GAP_MS) count++;
            }
        }
        return (int) Math.round(Math.min(count, TILT_CAP) / (double) TILT_CAP * 5);
    }

    /**
     * 특정 시간대(hourFrom ~ hourTo KST)와 게임 시간의 겹치는 구간(ms)을 계산.
     * 게임이 자정을 넘길 수 있으므로 날짜 단위로 순회.
     */
    private static long overlapMs(long gameStartMs, int gameDurationSec, int hourFrom, int hourTo) {
        long gameEndMs  = gameStartMs + (long) gameDurationSec * 1000;
        LocalDate start = Instant.ofEpochMilli(gameStartMs).atZone(KST).toLocalDate();
        LocalDate end   = Instant.ofEpochMilli(gameEndMs).atZone(KST).toLocalDate();

        long overlap = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            long winStart = d.atTime(hourFrom, 0).atZone(KST).toInstant().toEpochMilli();
            long winEnd   = d.atTime(hourTo,   0).atZone(KST).toInstant().toEpochMilli();
            long oStart   = Math.max(gameStartMs, winStart);
            long oEnd     = Math.min(gameEndMs,   winEnd);
            if (oEnd > oStart) overlap += (oEnd - oStart);
        }
        return overlap;
    }

    private static HlsResult zero() {
        return new HlsResult(0, 0, 0, 0, 0, 0, 0, 0);
    }
}
