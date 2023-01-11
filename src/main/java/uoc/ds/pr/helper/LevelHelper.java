package uoc.ds.pr.helper;

import uoc.ds.pr.SportEvents4Club;

public class LevelHelper {
    public static SportEvents4Club.Level getLevel(int val) {
        if (val >= 15) {
            return SportEvents4Club.Level.LEGEND;
        }
        if (val >=  10) {
            return SportEvents4Club.Level.MASTER;
        }
        if (val >= 5) {
            return SportEvents4Club.Level.EXPERT;
        }
        if (val >= 2) {
            return SportEvents4Club.Level.PRO;
        }
        return SportEvents4Club.Level.ROOKIE;
    }
}
