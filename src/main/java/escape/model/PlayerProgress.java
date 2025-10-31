package escape.model;

import java.util.HashSet;
import java.util.Set;

public class PlayerProgress {
    private boolean doorUnlocked = false;
    private final Set<String> keys = new HashSet<>();

    public boolean isDoorUnlocked() { return doorUnlocked; }
    public void setDoorUnlocked(boolean val) { this.doorUnlocked = val; }

    public Set<String> getKeys() { return keys; }
    public boolean hasAllKeys() { return keys.contains("borlave") && keys.contains("31337"); }
}
