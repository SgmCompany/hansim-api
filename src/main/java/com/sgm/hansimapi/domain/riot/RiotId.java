package com.sgm.hansimapi.domain.riot;

public class RiotId {

    private final String gameName;
    private final String tagLine;

    private RiotId(String gameName, String tagLine) {
        this.gameName = gameName;
        this.tagLine = tagLine;
    }

    // * FE에서 "용쿠리몽#KR1"을 "용쿠리몽-KR1"으로 변환하여 요청. "#" 형식은 허용하지 않음
    public static RiotId fromSlug(String slug) {
        if (slug == null || slug.contains("#") || !slug.contains("-")) {
            throw new IllegalArgumentException("Invalid riotId format: use 이름-태그 (예: 페이커-KR1)");
        }

        int lastDash = slug.lastIndexOf("-");
        String gameName = slug.substring(0, lastDash);
        String tagLine = slug.substring(lastDash + 1);
        return new RiotId(gameName, tagLine);
    }

    public String getGameName() {
        return gameName;
    }

    public String getTagLine() {
        return tagLine;
    }
}
