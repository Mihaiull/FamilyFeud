
# Family Feud Multiplayer Web Game (Backend)

This project is the backend for a modern, multiplayer web game inspired by Family Feud. It features a robust Java/Spring Boot API, real-time WebSocket updates, and a browser-based test client for development. The backend is production-ready—frontend and full game experience coming soon!

---


## Features
- **Multiplayer Game Logic:** Create, join, and play Family Feud-style matches with teams
- **Real-Time Sync:** WebSocket broadcasting for all game state changes (instant updates for all players)
- **REST API:** Full-featured endpoints for every game action
- **Test Client:** `ws-test.html` for live game state monitoring and message sending
- **Battle-Tested:** Integration & unit tests for controllers and services

---


## Quick Start (Backend Only)


### 1. Prerequisites
- Java 17+
- Maven
- (Optional) PostgreSQL (or use H2 for dev)


### 2. Build & Run
```sh
mvn clean package
java -jar target/*.jar
```


### 3. REST API Usage
- See `src/main/java/com/feud/controller/` for endpoints
- Example: Create a game
  ```sh
  curl -X POST http://localhost:8080/games -H 'Content-Type: application/json' -d '{"topic":"Animals"}'
  ```


### 4. Real-Time WebSocket Test Client
- Open `ws-test.html` in your browser
- Enter a game code and connect
- See live updates as you POST to the API
- Send test messages to `/app/game/{code}`

---


## Project Structure
```
├── src/main/java/com/feud/
│   ├── controller/    # REST controllers
│   ├── model/         # Entities (Game, Player, etc.)
│   ├── service/       # Business logic
│   ├── websocket/     # WebSocket config & broadcaster
│   └── ...
├── src/test/java/com/feud/  # Integration & unit tests
├── ws-test.html       # WebSocket test client
├── pom.xml            # Maven build
```

---


## WebSocket API
- **Endpoint:** `ws://localhost:8080/ws` (SockJS/STOMP)
- **Subscribe:** `/topic/game/{code}`
- **Send:** `/app/game/{code}` (for custom messages)

---


## Roadmap & Contributing

### Coming Soon
- **Frontend Web App:** Play Family Feud in your browser with friends!
- **Lobby & Matchmaking:** Easy game setup and team selection
- **Live Animations & UI:** Modern, responsive, and fun
- **Persistent Storage:** PostgreSQL for production, H2 for dev
- **Docker Support:** Easy deployment

### Contributing
1. Fork & clone
2. Create a feature branch
3. Make changes, add tests
4. Push and open a pull request

---


## License
MIT

---


## Credits
- Inspired by Family Feud

---


## Screenshots

_Screenshots and frontend previews coming soon!_

---


## Contact
- GitHub: [Mihaiull](https://github.com/Mihaiull)
