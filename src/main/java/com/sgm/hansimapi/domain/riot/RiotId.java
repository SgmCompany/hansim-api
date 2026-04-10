package com.sgm.hansimapi.domain.riot;

public class RiotId {

    private final String gameName;
    private final String tagLine;

    private RiotId(String gameName, String tagLine) {
        this.gameName = gameName;
        this.tagLine = tagLine;
    }

    // * FE에서 "용쿠리몽#KR1"을 "용쿠리몽-KR1"으로 요청
    public static RiotId fromSlug(String slug) {
        if (slug == null || !slug.contains("-")) {
            throw new IllegalArgumentException("Invalid slug format");
        }

        String[] parts = slug.split("-");
        return new RiotId(parts[0], parts[1]);
    }

    public String getGameName() {
        return gameName;
    }

    public String getTagLine() {
        return tagLine;
    }
}
