package com.example.snake;

import io.javalin.Javalin;

public class Main {

    private static final GameManager gameManager = new GameManager();

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(getHerokuAssignedPort());

        app.ws("/websocket", ws -> {
            ws.onConnect(ctx -> {
                System.out.println("Player connected: " + ctx.getSessionId());
                gameManager.joinGame(ctx);
            });

            ws.onClose(ctx -> {
                System.out.println("Player disconnected: " + ctx.getSessionId());
                gameManager.leaveGame(ctx);
            });

            ws.onMessage(ctx -> {
                gameManager.handleInput(ctx, ctx.message());
            });
        });
    }

    private static int getHerokuAssignedPort() {
        String herokuPort = System.getenv("PORT");
        if (herokuPort != null) {
            return Integer.parseInt(herokuPort);
        }
        return 7070;
    }
}
