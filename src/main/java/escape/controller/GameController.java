package escape.controller;

import escape.model.ClueResponse;
import escape.model.PlayerProgress;
import escape.service.GameService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
@Validated
public class GameController {

    private final GameService service;
    public GameController(GameService service) { this.service = service; }

    // 1) GET /room
    @GetMapping("/room")
    public ClueResponse room(@RequestHeader("X-Player-Id") @NotBlank String playerId) {
        return new ClueResponse(
                "The Nest. An echo flickers with an encoded hint: ZWxhdnJvYg==.",
                "Decode, then POST /door with the key (query or JSON).",
                "/door"
        );
    }

    // 2) POST /door
    @PostMapping("/door")
    public ResponseEntity<?> door(
            @RequestHeader("X-Player-Id") @NotBlank String playerId,
            @RequestParam(value = "key", required = false) String keyFromQuery,
            @RequestBody(required = false) Map<String,String> body
    ) {
        String key = keyFromQuery != null ? keyFromQuery : (body != null ? body.get("key") : null);
        if (key == null || key.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error","Missing 'key'"));
        }
        var p = service.progressFor(playerId);
        if (service.tryDoor(key)) {
            p.setDoorUnlocked(true);
            p.getKeys().add("borvale"); // ← award Key 1
            return ResponseEntity.ok(new ClueResponse(
                    "Fragment 1 accepted. The echo whispers: 'Enter the hallway where I lost my connection...'",
                    "Go to GET /hallway for the next hint.",
                    "/hallway"
            ));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error","Wrong key. The echo laughs."));
    }

    // 3) GET /hallway (protected by doorUnlocked)
    @GetMapping("/hallway")
    public ResponseEntity<?> hallway(@RequestHeader("X-Player-Id") @NotBlank String playerId,
                                     @RequestParam(value = "answer", required = false) String answer) {
        var p = service.progressFor(playerId);
        if (!p.isDoorUnlocked()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error","Door still locked. Solve /door first."));
        }

        if (answer == null) {
            return ResponseEntity.ok(new ClueResponse(
                    service.hallwayClue(),
                    "Solve with GET /hallway?answer=<number>",
                    "/escape"
            ));
        }

        if (service.collectHallwayKey(answer)) {
            p.getKeys().add("31337"); // ← award Key 2
            return ResponseEntity.ok(new ClueResponse(
                    "Key 2 accepted. The master key is almost ready.",
                    "Finish with POST /escape (no body).",
                    "/escape"
            ));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error","Wrong answer."));
    }

    // 4) POST /escape → verify keys and “win”
    @PostMapping("/escape")
    public ResponseEntity<?> escape(@RequestHeader("X-Player-Id") @NotBlank String playerId) {
        PlayerProgress p = service.progressFor(playerId);
        if (p.hasAllKeys()) {
            return ResponseEntity.ok(Map.of(
                    "status","escaped",
                    "message","Master key rebuilt. The 'voodoo' virus has been purged. You are free!"
            ));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status","stuck",
                "missing","Need keys",
                "hint","Visit /room → /door → /hallway"
        ));
    }
}
