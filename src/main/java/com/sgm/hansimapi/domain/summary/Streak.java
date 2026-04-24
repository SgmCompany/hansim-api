package com.sgm.hansimapi.domain.summary;

import com.sgm.hansimapi.domain.riot.Match;

import java.util.List;

public class Streak {

    public enum Type { WIN, LOSE, NONE }

    private final Type type;
    private final int count;

    public Streak(Type type, int count) {
        this.type = type;
        this.count = count;
    }

    public static Streak from(List<Match> matches) {
        if (matches.isEmpty()) {
            return new Streak(Type.NONE, 0);
        }

        boolean firstWin = matches.get(0).isWin();
        Type type = firstWin ? Type.WIN : Type.LOSE;
        int count = 0;

        for (Match match : matches) {
            if (match.isWin() == firstWin) count++;
            else break;
        }

        return new Streak(type, count);
    }

    public Type getType() { return type; }
    public int getCount() { return count; }
}
