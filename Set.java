import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Set extends Location {

    // Off-card roles (extras) belong to the Set
    private final List<OffCardRole> offCardRoles = new ArrayList<>();

    // On-card roles belong to the SceneCard
    private SceneCard currentScene;

    private boolean wrapped;

    public Set(String name) {
        super(name);
        this.currentScene = null;
        this.wrapped = false;
    }

    // Scene state 

    public void setScene(SceneCard scene) {
        // clear old role occupancy (players leave roles when a new scene is dealt)
        clearAllRoleOccupancy();

        this.currentScene = scene;
        this.wrapped = false;
    }

    public SceneCard getScene() {
        return currentScene;
    }

    public boolean hasActiveScene() {
        return currentScene != null && !wrapped;
    }

    public void wrapScene() {
        this.wrapped = true;
        clearAllRoleOccupancy();
        this.currentScene = null;
    }

    

    // Role management 

    public void addOffCardRole(OffCardRole role) {
        if (role == null) throw new IllegalArgumentException("OffCardRole cannot be null.");
        offCardRoles.add(role);
    }

    public List<OffCardRole> getOffCardRoles() {
        return Collections.unmodifiableList(offCardRoles);
    }

    public List<OnCardRole> getOnCardRoles() {
        if (currentScene == null) return List.of();
        return currentScene.getOnCardRoles();
    }



    // Combined view for console/help + work command
    public List<Role> getAllRoles() {
        List<Role> all = new ArrayList<>();
        all.addAll(offCardRoles);
        if (currentScene != null) {
            all.addAll(currentScene.getOnCardRoles());
        }
        return all;
    }

    public Role getRoleByName(String roleName) {
        if (roleName == null || roleName.isBlank()) return null;
        String key = roleName.trim();

        for (OffCardRole r : offCardRoles) {
            if (r.getName().equalsIgnoreCase(key)) return r;
        }
        if (currentScene != null) {
            for (OnCardRole r : currentScene.getOnCardRoles()) {
                if (r.getName().equalsIgnoreCase(key)) return r;
            }
        }
        return null;
    }

    // Work command support

    public Role assignRoleToPlayer(Player player, String roleName) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null.");
        if (roleName == null || roleName.isBlank()) throw new IllegalArgumentException("Role name required.");

        // Must have an active scene to work ANY role at this set (off-card or on-card)
        if (player.getLocation() != this) {
            throw new IllegalStateException("Player must be at this set to work a role here.");
        }

        if (!hasActiveScene()) {
            throw new IllegalStateException("No active scene here. Cannot work a role.");
        }

        Role role = getRoleByName(roleName);
        if (role == null) {
            throw new IllegalArgumentException("No role named '" + roleName + "' at set " + getName());
        }

        player.takeRole(role);
        return role;
    }

    public void clearAllRoleOccupancy() {
        for (OffCardRole r : offCardRoles) r.clear();
        if (currentScene != null) {
            for (OnCardRole r : currentScene.getOnCardRoles()) r.clear();
        }
    }

    //  Console-friendly info 
    public String rolesSummary(Player viewer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Roles at ").append(getName()).append(":\n");

        for (Role r : getAllRoles()) {
            String status = r.isAvailable() ? "OPEN" : "TAKEN";
            String qual = (viewer != null && r.isPlayerQualified(viewer)) ? "QUALIFY" : "NO";

            sb.append(" - ").append(r.getName())
              .append(" (rank ").append(r.getRankRequired()).append(") ")
              .append(status).append(", ").append(qual);

            if (r instanceof OnCardRole) sb.append(" [OnCard]");
            if (r instanceof OffCardRole) sb.append(" [OffCard]");
            sb.append("\n");
        }

        if (currentScene != null) {
            sb.append("Scene: ").append(currentScene.getTitle())
              .append(" | budget=").append(currentScene.getBudget())
              .append("\n");
        } else {
            sb.append("Scene: (none)\n");
        }

        return sb.toString();
    }
}
