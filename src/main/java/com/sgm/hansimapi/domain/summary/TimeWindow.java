package com.sgm.hansimapi.domain.summary;

import java.time.*;
import java.time.temporal.ChronoUnit;

/**
 * 조회 기간을 나타내는 값 객체.
 * 하루의 경계는 KST 기준 오전 6시 (리그오브레전드 일일 초기화 시간)
 */
public class TimeWindow {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final String[] DAY_NAMES = {"월", "화", "수", "목", "금", "토", "일"};

    private final long start;
    private final long end;

    private TimeWindow(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public static TimeWindow from(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            return daily();
        }

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("startDate는 endDate보다 이전이어야 합니다.");
        }
        if (end.isAfter(LocalDate.now(KST))) {
            throw new IllegalArgumentException("미래 날짜는 조회할 수 없습니다.");
        }
        if (ChronoUnit.DAYS.between(start, end) > 30) {
            throw new IllegalArgumentException("최대 30일까지 조회 가능합니다.");
        }

        ZonedDateTime startDt = start.atTime(6, 0).atZone(KST);
        ZonedDateTime endDt   = end.plusDays(1).atTime(6, 0).atZone(KST);

        return new TimeWindow(startDt.toInstant().toEpochMilli(), endDt.toInstant().toEpochMilli());
    }

    public static TimeWindow daily() {
        LocalDate today = LocalDate.now(KST);
        ZonedDateTime start = today.atTime(6, 0).atZone(KST);
        ZonedDateTime end   = start.plusDays(1);
        return new TimeWindow(start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli());
    }

    public String getPeriodStr() {
        ZonedDateTime startKst = Instant.ofEpochMilli(start).atZone(KST);
        ZonedDateTime endKst   = Instant.ofEpochMilli(end).atZone(KST);
        return formatKst(startKst) + " ~ " + formatKst(endKst) + " (KST)";
    }

    private String formatKst(ZonedDateTime dt) {
        String day = DAY_NAMES[dt.getDayOfWeek().getValue() - 1];
        int hour = dt.getHour();
        String ampm = hour < 12 ? "오전" : "오후";
        int displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
        return String.format("%s (%s) %s %d시", dt.toLocalDate(), day, ampm, displayHour);
    }

    public long getStart() { return start; }
    public long getEnd()   { return end; }
}
