package com.sgm.hansimapi.domain.riot;

public class RiotId {

    private final String gameName;
    private final String tagLine;

    private RiotId(String gameName, String tagLine) {
        this.gameName = gameName;
        this.tagLine = tagLine;
    }

    /**
     * URL 경로 변수용 슬러그 파싱. '#'은 URL에 안전하지 않으므로 '-' 구분자만 허용.
     * 예: "페이커-KR1"
     */
    public static RiotId fromSlug(String slug) {
        if (slug == null || slug.contains("#") || !slug.contains("-")) {
            throw new IllegalArgumentException("Invalid riotId format: use 이름-태그 (예: 페이커-KR1)");
        }

        int lastDash = slug.lastIndexOf("-");
        String gameName = slug.substring(0, lastDash);
        String tagLine = slug.substring(lastDash + 1);
        return new RiotId(gameName, tagLine);
    }

    /**
     * Request body용 파싱. '#' 구분자(예: "페이커#KR1")와 '-' 슬러그(예: "페이커-KR1") 모두 허용.
     */
    public static RiotId from(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Riot ID는 비어있을 수 없습니다.");
        }
        if (input.contains("#")) {
            int idx = input.lastIndexOf("#");
            String gameName = input.substring(0, idx);
            String tagLine  = input.substring(idx + 1);
            if (gameName.isBlank() || tagLine.isBlank()) {
                throw new IllegalArgumentException("Invalid riotId format: use 이름#태그 (예: 페이커#KR1)");
            }
            return new RiotId(gameName, tagLine);
        }
        return fromSlug(input);
    }

    public String getGameName() {
        return gameName;
    }

    public String getTagLine() {
        return tagLine;
    }
}
