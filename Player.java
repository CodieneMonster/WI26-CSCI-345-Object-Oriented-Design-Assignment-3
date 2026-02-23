public class Player {
    private final String name;

    private int rank;
    private int dollars;
    private int credits;
    private int rehearsalChips;

    private Location location; // current location
    private Role role; // current role (null if not working)

    public Player(String name, int startingRank, int startingDollars, int startingcredits, Location startLocation) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty.");
        }
        if (startingRank < 1) {
            throw new IllegalArgumentException("Starting rank must be at least 1.");
        }
        if (startingDollars < 0 || startingcredits < 0) {
            throw new IllegalArgumentException("Starting dollars and credits must be greater than or equal to zero.");
        }

        if (startLocation == null) {
            throw new IllegalArgumentException("Starting location required.");
        }

        this.name = name; // or name.trim() to remove leading/trailing whitespace
        this.rank = startingRank;
        this.dollars = startingDollars;
        this.credits = startingcredits;
        this.rehearsalChips = 0; // start with 0 rehearsal chips
        this.location = startLocation;
        this.role = null; // start with no role
    }

    // Getters for player attributes
    public String getName() {return name;}

    public int getRank() {return rank;}

    public int getDollars() {return dollars;}

    public int getCredits() {return credits;}

    public int getRehearsalChips() {return rehearsalChips;}

    public Location getLocation() {return location;}

    public Role getRole() {return role;}

    public boolean isWorking() {return role != null;}

    // player info string for display
    public String statusString() {
        String loc = (location == null) ? "(none)" : location.getName();
        String roleName = (role == null) ? "not working" : ("working \"" + role.getName() + "\"");
        return String.format("%s (rank %d, $%d, %dcr, rehearsal %d) @ %s, %s",
                name, rank, dollars, credits, rehearsalChips, loc, roleName);
    }

    // Movement 
    public void moveTo(Location destination) {
        if (destination == null) {throw new IllegalArgumentException("Destination location cannot be null.");}
        if (isWorking()) {throw new IllegalStateException("Cannot move while working on a role.");}
        if (location == null) {throw new IllegalStateException("Player has no current location");}
       
        if (!location.isAdjacent(destination)) {
            throw new IllegalArgumentException("Destination must be adjacent to current location.");
        }

        this.location = destination;
    }


    // move by name for parsing later
    public void moveToNeighborByName(String destinationName) {
        if (destinationName == null || destinationName.trim().isEmpty()) {throw new IllegalArgumentException("Destination name required.");}

        if (isWorking()) {throw new IllegalStateException("Cannot move while working on a role.");}

        if (location == null) {throw new IllegalStateException("Player has no current location.");}
        
        Location destination = location.getNeighborByName(destinationName.trim());
        if (destination == null) {throw new IllegalArgumentException("No adjacent location found with the name: " + destinationName);}

        moveTo(destination);
    }

    // Role for supporting work
    public void takeRole(Role r) {
        if (r == null) {throw new IllegalArgumentException("Role cannot be null.");}
        if (isWorking()) {throw new IllegalStateException("Player already working on a role.");}

        if (!r.isAvailable()) {throw new IllegalStateException("Role is not available.");}
        if (rank < r.getRankRequired()) {throw new IllegalStateException("Player rank too low for this role.");}

        r.assign(this);
        this.role = r;
        this.rehearsalChips = 0; // reset rehearsal chips when taking a new role
    }

    public void dropRole() {
        if (role == null) {return;}// not working, nothing to drop
        role.clear();
        role = null;
        rehearsalChips = 0; 
    }

    public void rehearse() {
        if (!isWorking()) {throw new IllegalStateException("Player must be working on a role to rehearse.");}
        rehearsalChips++;
    }

    public void addDollars(int amount) {
        if (amount < 0) {throw new IllegalArgumentException("Amount to add must be non-negative.");}
        dollars += amount;
    }

    public void addCredits(int amount) {
        if (amount < 0) {throw new IllegalArgumentException("Amount to add must be non-negative.");}
        credits += amount;
    }

    public void spendDollars(int amount) {
        if (amount < 0) {throw new IllegalArgumentException("Amount to spend must be non-negative.");}
        if (dollars < amount) {throw new IllegalStateException("Not enough dollars to spend.");}
        dollars -= amount;
    }

    public void spendCredits(int amount) {
        if (amount < 0) {throw new IllegalArgumentException("Amount to spend must be non-negative.");}
        if (credits < amount) {throw new IllegalStateException("Not enough credits to spend.");}
        credits -= amount;
    }
    // Rank changes (used by bank and upgrade later)
    public void setRank(int newRank) {
        if (newRank < 1) {throw new IllegalArgumentException("Rank must be at least 1.");}
        rank = newRank;
    }

    // Day reset
    public void resetForNewDay(Location trailers) {
        if (trailers == null) {throw new IllegalArgumentException("Trailer location required.");}
        dropRole();
        this.rehearsalChips = 0;
        this.location = trailers;
    }
}
