package com.example.snake;

import io.javalin.websocket.WsContext;
import java.util.concurrent.ConcurrentHashMap;

// Manages game instances and player assignments.
public class GameManager {
    private final ConcurrentHashMap<String, Game> games = new ConcurrentHashMap<>();
    private Game waitingGame = null;

    public synchronized void joinGame(WsContext ctx) {
        if (waitingGame == null) {
            waitingGame = new Game();
        }
        waitingGame.addPlayer(ctx);
        games.put(ctx.getSessionId(), waitingGame);

        if (waitingGame.isFull()) {
            waitingGame = null; // Reset for the next pair of players
        }
    }

    public void handleInput(WsContext ctx, String message) {
        Game game = games.get(ctx.getSessionId());
        if (game != null) {
            game.handleInput(ctx, message);
        }
    }

    public void leaveGame(WsContext ctx) {
        Game game = games.remove(ctx.getSessionId());
        if (game != null) {
            game.removePlayer(ctx);
            // If the leaving player was in the waiting game, nullify it
            if (game == waitingGame) {
                waitingGame = null;
            }
        }
    }
}
