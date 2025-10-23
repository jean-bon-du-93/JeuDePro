package com.example.snake;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import io.javalin.websocket.WsContext;

// The main game engine.
public class Game {
    public static final int BOARD_WIDTH = 40;
    public static final int BOARD_HEIGHT = 30;

    private final List<Snake> snakes = new ArrayList<>();
    private final List<WsContext> players = new ArrayList<>();
    private Point food;
    private boolean isRunning = false;
    private final ScheduledExecutorService gameLoop;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Game() {
        this.gameLoop = Executors.newSingleThreadScheduledExecutor();
    }

    public synchronized void addPlayer(WsContext ctx) {
        if (snakes.size() >= 2) {
            return; // Game is full
        }

        // Player 1 starts on the left, Player 2 on the right
        Point startPoint = (snakes.isEmpty())
            ? new Point(5, 15)
            : new Point(35, 15);
        Direction startDirection = (snakes.isEmpty())
            ? Direction.RIGHT
            : Direction.LEFT;

        Snake snake = new Snake(ctx.getSessionId(), startPoint, startDirection);
        snakes.add(snake);
        players.add(ctx);

        if (snakes.size() == 2) {
            startGame();
        }
    }

    public synchronized void removePlayer(WsContext ctx) {
        players.remove(ctx);
        snakes.removeIf(snake -> snake.getPlayerId().equals(ctx.getSessionId()));
        if (players.isEmpty()) {
            stopGame();
        }
    }

    public synchronized boolean isFull() {
        return snakes.size() >= 2;
    }

    private void startGame() {
        isRunning = true;
        spawnFood();
        gameLoop.scheduleAtFixedRate(this::tick, 0, 150, TimeUnit.MILLISECONDS);
        broadcastGameState();
    }

    private void stopGame() {
        isRunning = false;
        gameLoop.shutdownNow();
    }

    private void tick() {
        if (!isRunning) return;

        // Move snakes
        snakes.forEach(Snake::move);

        // Check for collisions and food
        checkCollisions();

        // Broadcast new state
        broadcastGameState();
    }

    private void spawnFood() {
        Random rand = new Random();
        int x, y;
        boolean onSnake;
        do {
            onSnake = false;
            x = rand.nextInt(BOARD_WIDTH);
            y = rand.nextInt(BOARD_HEIGHT);
            Point potentialFood = new Point(x, y);
            for (Snake snake : snakes) {
                if (snake.getBody().contains(potentialFood)) {
                    onSnake = true;
                    break;
                }
            }
        } while (onSnake);
        this.food = new Point(x, y);
    }

    private void checkCollisions() {
        List<Snake> aliveSnakes = new ArrayList<>(snakes);
        for (Snake snake : snakes) {
            Point head = snake.getHead();

            // Wall collision
            if (head.x < 0 || head.x >= BOARD_WIDTH || head.y < 0 || head.y >= BOARD_HEIGHT) {
                aliveSnakes.remove(snake);
                continue;
            }

            // Self-collision
            if (snake.checkCollisionWithSelf()) {
                aliveSnakes.remove(snake);
                continue;
            }

            // Head-on collision with other snakes
            for (Snake otherSnake : snakes) {
                if (snake == otherSnake) continue;
                // Simplified: if heads collide, both lose
                if (head.equals(otherSnake.getHead())) {
                    aliveSnakes.remove(snake);
                    aliveSnakes.remove(otherSnake);
                } else if (otherSnake.getBody().contains(head)) {
                     aliveSnakes.remove(snake);
                }
            }

            // Food collision
            if (head.equals(food)) {
                snake.grow();
                spawnFood();
            }
        }

        if (aliveSnakes.size() < 2) {
             endGame(aliveSnakes);
        }
    }

    private void endGame(List<Snake> winners) {
        stopGame();
        String winnerMessage = winners.isEmpty() ? "draw" : winners.get(0).getPlayerId();
        try {
            String gameOverMessage = objectMapper.writeValueAsString(new GameState("game-over", null, null, winnerMessage));
            for (WsContext player : players) {
                 if(player.session.isOpen()){
                    player.send(gameOverMessage);
                 }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void broadcastGameState() {
        try {
            String gameState = objectMapper.writeValueAsString(new GameState("update", snakes, food, null));
            for (WsContext player : players) {
                if(player.session.isOpen()){
                    player.send(gameState);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleInput(WsContext ctx, String message) {
        Snake snake = snakes.stream()
            .filter(s -> s.getPlayerId().equals(ctx.getSessionId()))
            .findFirst()
            .orElse(null);

        if (snake != null) {
            switch (message) {
                case "UP": snake.setDirection(Direction.UP); break;
                case "DOWN": snake.setDirection(Direction.DOWN); break;
                case "LEFT": snake.setDirection(Direction.LEFT); break;
                case "RIGHT": snake.setDirection(Direction.RIGHT); break;
            }
        }
    }

    // Simple class to structure the JSON message
    private static class GameState {
        public String type;
        public List<Snake> snakes;
        public Point food;
        public String winner;

        public GameState(String type, List<Snake> snakes, Point food, String winner) {
            this.type = type;
            this.snakes = snakes;
            this.food = food;
            this.winner = winner;
        }
    }
}
