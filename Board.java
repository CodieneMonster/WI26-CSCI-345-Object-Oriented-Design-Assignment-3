import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {

    private final Map<String, Location> locations;

    public Board() {
        this.locations = new HashMap<>();
    }

    // Location Management
    public void addLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        locations.put(location.getName().toLowerCase(), location);
    }

    public Location getLocationByName(String name) {
        if (name == null) return null;
        return locations.get(name.trim().toLowerCase());
    }

    public Collection<Location> getAllLocations() {
        return Collections.unmodifiableCollection(locations.values());
    }

    // Movement
    public boolean movePlayer(Player player, String destinationName) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        Location destination = getLocationByName(destinationName);
        if (destination == null) {
            throw new IllegalArgumentException("Destination not found: " + destinationName);
        }

        Location current = player.getLocation();
        if (!current.isAdjacent(destination)) {
            throw new IllegalArgumentException(
                "cant move: " + destination.getName() + " is not adjacent to " + current.getName()
            );
        }

        player.moveTo(destination);
        return true;
    }

    // Display
    public void printAllPlayerLocations(List<Player> players, Player activePlayer) {
        for (Player p : players) {
            String activeMarker = (p == activePlayer) ? " (active)" : "";
            System.out.println(p.getName() + " is at " + p.getLocation().getName() + activeMarker);
        }
    }
}