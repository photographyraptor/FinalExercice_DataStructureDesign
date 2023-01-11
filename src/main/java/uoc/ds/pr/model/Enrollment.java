package uoc.ds.pr.model;

public class Enrollment {
    private Player player;
    private boolean isSubtitute;

    public Enrollment(Player player, boolean isSubstitute) {
        this.player = player;
        this.isSubtitute = isSubstitute;
    }

    public Player getPlayer() {
        return this.player;
    }
    
    public boolean getIsSubstitute() {
        return this.isSubtitute;
    }
}
