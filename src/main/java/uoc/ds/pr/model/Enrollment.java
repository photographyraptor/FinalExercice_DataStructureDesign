package uoc.ds.pr.model;

public class Enrollment implements Comparable<Enrollment> {
    private Player player;
    private boolean isSubtitute;

    public Enrollment(Player player, boolean isSubstitute) {
        this.player = player;
        this.isSubtitute = isSubstitute;
    }
    
    @Override
    public int compareTo(Enrollment enrollment) {
        return Double.compare(this.player.getLevel().ordinal(),
            enrollment.getPlayer().getLevel().ordinal());
    }

    public Player getPlayer() {
        return this.player;
    }
    
    public boolean getIsSubstitute() {
        return this.isSubtitute;
    }
}
