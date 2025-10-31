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

    // --- Storyline "El Nido" (solo cambia el contenido, no la estructura) ---
    public String firstClue() {
        return "Un 'eco' parpadea... 'El primer fragmento está encriptado en Base64: ZWxhdnJvYg=='";
    }

    // Key 1: "borlave"
    public boolean tryDoor(String key) {
        return key != null && "borlave".equalsIgnoreCase(key.trim());
    }

    public String hallwayClue() {
        return "El 'eco' dice: 'La clave final es el número de puerto que siempre usaba para mis ataques... "
                + "el que abre todas las puertas traseras.' (Pista: es 'Elite' en leetspeak).";
    }

    // Key 2: "31337"
    public boolean collectHallwayKey(String answer) {
        return answer != null && "31337".equals(answer.trim());
    }
}
