public abstract  class Role {
    private final String name;
    private final int rankRequired;
    private Player occupiedBy;

    // Area
    private Area area;

    public void setArea(Area area) {
        this.area = area;
    }

    public Area getArea() {
        return area;
    }

    protected Role(String name, int rankRequired) {
        if (name == null || name.isBlank()) {throw new IllegalArgumentException("Role name required");}
        if (rankRequired < 1) {throw new IllegalArgumentException("Role rank must be at least 1");}
        this.name = name.trim();
        this.rankRequired = rankRequired;
        this.occupiedBy = null; // start unoccupied
    }

    public String getName() {return name;}
    public int getRankRequired() {return rankRequired;}

    public boolean isAvailable() {return occupiedBy == null;}
    public Player getOccupiedBy() {return occupiedBy;}

        public boolean isPlayerQualified(Player p) {
        if (p == null) return false;
        return p.getRank() >= rankRequired;
    }


    public void assign(Player player) {
        if (player == null) {throw new IllegalArgumentException("Player cannot be null");}
        if (!isAvailable()) {throw new IllegalStateException("Role is already occupied");}

        occupiedBy = player;
    }

    public void clear() {occupiedBy = null;}

    @Override
    public String toString() {
        String occ = (occupiedBy == null) ? "available" : ("occupied by " + occupiedBy.getName());
        return String.format("%s (rank %d, %s)", name, rankRequired, occ);
    }
}
