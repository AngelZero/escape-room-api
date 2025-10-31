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
                "El Nido. Un 'eco' parpadea con una pista encriptada ZWxhdnJvYg==.",
                "Decodifica → POST /door con la clave (query o JSON).",
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
            p.getKeys().add("borlave"); // ← entrega Key 1
            return ResponseEntity.ok(new ClueResponse(
                    "Fragmento 1 aceptado. El 'eco' susurra: 'Accede al pasillo donde perdí mi conexión...'",
                    "Ve a GET /hallway para la siguiente pista.",
                    "/hallway"
            ));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error","Clave incorrecta. El 'eco' se ríe."));
    }

    // 3) GET /hallway (protegido por doorUnlocked)
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
                    "Resuelve con GET /hallway?answer=31337",
                    "/escape"
            ));
        }

        if (service.collectHallwayKey(answer)) {
            p.getKeys().add("31337"); // ← entrega Key 2
            return ResponseEntity.ok(new ClueResponse(
                    "Clave 2 aceptada. La llave maestra está casi lista.",
                    "Termina con POST /escape (sin body).",
                    "/escape"
            ));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error","Respuesta incorrecta."));
    }


    // 4) POST /escape → verificar llaves y “ganar”
    @PostMapping("/escape")
    public ResponseEntity<?> escape(@RequestHeader("X-Player-Id") @NotBlank String playerId) {
        PlayerProgress p = service.progressFor(playerId);
        if (p.hasAllKeys()) {
            return ResponseEntity.ok(Map.of(
                    "status","escaped",
                    "message","Llave maestra reconstruida. El virus 'vudú' ha sido purgado. ¡Eres libre!"
            ));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status","stuck",
                "missing","Needs keys",
                "hint","Visit /room → /door → /hallway"
        ));
    }
}
