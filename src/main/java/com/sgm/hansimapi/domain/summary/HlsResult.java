package com.sgm.hansimapi.domain.summary;

/** NINE_TO_SIX 기준 한심지수(HLS) 계산 결과 */
public class HlsResult {

    /** 최종 HLS (0~100) */
    private final int total;

    /** A. 볼륨 (0~30): 총 플레이 시간 + 판수 */
    private final int volume;

    /** B. 결과 (0~20): 패배/서렌/KDA 기반 */
    private final int result;

    /** C. 심야 (0~15): KST 00~06시 누적 분수 */
    private final int lateNight;

    /** D. 주말 (0~10): 토/일 누적 분수 */
    private final int weekend;

    /** E. 세션 (0~10): 최장 연속 플레이 */
    private final int session;

    /** F. 연패 (0~10): 최대 연속 패배 */
    private final int losingStreak;

    /** G. 분노재큐 (0~5): 패배 후 5분 내 재입장 횟수 */
    private final int tilt;

    public HlsResult(int total, int volume, int result, int lateNight,
                     int weekend, int session, int losingStreak, int tilt) {
        this.total        = total;
        this.volume       = volume;
        this.result       = result;
        this.lateNight    = lateNight;
        this.weekend      = weekend;
        this.session      = session;
        this.losingStreak = losingStreak;
        this.tilt         = tilt;
    }

    public int getTotal()        { return total; }
    public int getVolume()       { return volume; }
    public int getResult()       { return result; }
    public int getLateNight()    { return lateNight; }
    public int getWeekend()      { return weekend; }
    public int getSession()      { return session; }
    public int getLosingStreak() { return losingStreak; }
    public int getTilt()         { return tilt; }
}
