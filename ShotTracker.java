import java.util.HashMap;
import java.util.Map;

public class ShotTracker {
    private final Map<Set, Integer> initialShots;
    private final Map<Set, Integer> shotsRemaining;

    public ShotTracker() {
        this.initialShots = new HashMap<>();
        this.shotsRemaining = new HashMap<>();
    }

    // Register a set with its initial shot count (called by ParseXML)
    public void registerSet(Set set, int initial) {
        if (set == null) {
            throw new IllegalArgumentException("Set cannot be null.");
        }
        if (initial < 0) {
            throw new IllegalArgumentException("Shot count cannot be negative.");
        }

        initialShots.put(set, initial);
        shotsRemaining.put(set, initial);
    }

    // Get current shots remaining
    public int getShotsRemaining(Set set) {
        return shotsRemaining.getOrDefault(set, 0);
    }

    // NEW: get initial shot count
    public int getInitialShots(Set set) {
        return initialShots.getOrDefault(set, 0);
    }

    // Remove one shot (for act success)
    public void removeShot(Set set) {
        int remaining = getShotsRemaining(set);

        if (remaining <= 0) {
            throw new IllegalStateException("No shots remaining for set " + set.getName());
        }

        shotsRemaining.put(set, remaining - 1);
    }

    // Check if scene wrapped
    public boolean isWrapped(Set set) {
        return getShotsRemaining(set) == 0;
    }

    // Reset shots (for new day)
    public void resetSet(Set set, int shotCount) {
        if (set == null) throw new IllegalArgumentException("Set cannot be null.");
        if (shotCount < 0) throw new IllegalArgumentException("Shot count cannot be negative.");
        shotsRemaining.put(set, shotCount);
    }

    // Reset to the ORIGINAL count (start of new day or new scene)
    public void resetSet(Set set) {
        shotsRemaining.put(set, getInitialShots(set));
    }

    public void resetToInitial(Set set) {
        if (set == null) throw new IllegalArgumentException("Set cannot be null.");
        shotsRemaining.put(set, getInitialShots(set));
    }

        // Reset all sets for new day
    public void resetAll() {
        for (Set s : initialShots.keySet()) {
            resetSet(s);
        }
    }

    // Remove set completely
    public void clearSet(Set set) {
        shotsRemaining.remove(set);
        initialShots.remove(set);
    }

    // Debug helper
    public void printShots() {
        for (Map.Entry<Set, Integer> entry : shotsRemaining.entrySet()) {
            System.out.println(entry.getKey().getName() + " shots remaining: " + entry.getValue());
        }
    }
}