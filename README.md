## Haunted Escape Room — API (Spring Boot)

Minimal REST API for a small puzzle flow using per-player progress and keys.

### Requirements

* Java 17+
* Maven 3.9+
* (Optional) Postman for the included collection
* Use header `X-Player-Id` to identify the player session

### Run

```bash
mvn spring-boot:run
# or:
mvn clean package
java -jar target/*.jar
```

Server runs on `http://localhost:8080`.

### Endpoints

* **GET `/room`**
  Returns the first clue (Base64).
  Response shape: `{"message","hint","next"}`

* **POST `/door`**
  Validates **Key 1** (`borlave`). Accepts `key` as query or JSON.
  On success: unlocks the door and awards Key 1.

* **GET `/hallway`**
  Requires the door to be unlocked.

    * Without `answer`: returns the hallway clue (port “Elite” → 31337).
    * With `answer=31337`: awards Key 2.

* **POST `/escape`**
  Finishes the game if **both keys** were awarded.

### Headers

Every request must include:

```
X-Player-Id: <any-id>   # e.g., demo1
```

### Sample Flow (cURL)

```bash
# 1) First clue
curl -H "X-Player-Id: demo1" http://localhost:8080/room

# 2) Door (Key 1: borlave)
curl -X POST -H "X-Player-Id: demo1" "http://localhost:8080/door?key=borlave"

# 3) Hallway (see clue first)
curl -H "X-Player-Id: demo1" http://localhost:8080/hallway

# 3b) Hallway (answer Key 2: 31337)
curl -H "X-Player-Id: demo1" "http://localhost:8080/hallway?answer=31337"

# 4) Escape (requires both keys)
curl -X POST -H "X-Player-Id: demo1" http://localhost:8080/escape
```

### Error Handling

* 404 and other errors are returned as JSON (via `GlobalExceptionHandler`).
* Trying to access `/hallway` before unlocking the door returns **403 FORBIDDEN**.
* Wrong keys/answers return **401 UNAUTHORIZED**.