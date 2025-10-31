package escape.service;

import escape.model.PlayerProgress;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private final Map<String, PlayerProgress> store = new ConcurrentHashMap<>();

    public PlayerProgress progressFor(String playerId) {
        return store.computeIfAbsent(playerId, id -> new PlayerProgress());
    }

    public String firstClue() {
        return "An 'echo' flickers... 'The first fragment is encoded in Base64: ZWxhdnJvYg=='.";
    }

    // Key 1: "borvale"
    public boolean tryDoor(String key) {
        return key != null && "borvale".equalsIgnoreCase(key.trim());
    }

    public String hallwayClue() {
        return "The echo says: 'The final key is the port number I always used for my attacks..."
                + " the one that opens all backdoors.' (Hint: it's 'Elite' in leetspeak).";
    }

    // Key 2: "31337"
    public boolean collectHallwayKey(String answer) {
        return answer != null && "31337".equals(answer.trim());
    }
}
