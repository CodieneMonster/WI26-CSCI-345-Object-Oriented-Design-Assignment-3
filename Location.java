import java.util.Collections;
import java.util.HashSet;

public class Location {
    private final String name;
    private final java.util.Set<Location> neighbors = new HashSet<>();

    protected Location(String name) {
        if (name == null || name.isBlank()) {throw new IllegalArgumentException("Location name required");}
        this.name = name.trim();
    }

    public String getName() {return name;}

    public void addNeighbor(Location other) {
        if (other == null) {throw new IllegalArgumentException("Neighbor cant be null");}
        if (other == this) { return; } // no self loops
        neighbors.add(other);
        other.neighbors.add(this); // bidirectionalilty
    }

    public boolean isAdjacent(Location other) {
        return neighbors.contains(other);
    }

    public Location getNeighborByName(String neighborName) {
        if (neighborName == null || neighborName.isBlank()) {return null;}
        for (Location loc : neighbors) {
            if (loc.getName().equalsIgnoreCase(neighborName)) {
                return loc;
            }
        }
        return null; // not found
    }

    public java.util.Set<Location> getNeighbors() {
        return Collections.unmodifiableSet(neighbors);
    }
    
    @Override
    public String toString() {
        return name;
    }
}
