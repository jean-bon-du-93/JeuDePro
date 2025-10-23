package com.example.snake;

import java.util.LinkedList;

// Represents a snake in the game.
public class Snake {
    private final String playerId;
    private final LinkedList<Point> body = new LinkedList<>();
    private Direction direction;

    public Snake(String playerId, Point start, Direction initialDirection) {
        this.playerId = playerId;
        this.direction = initialDirection;
        body.add(start);
    }

    public void move() {
        Point head = body.getFirst();
        Point newHead;
        switch (direction) {
            case UP:
                newHead = new Point(head.x, head.y - 1);
                break;
            case DOWN:
                newHead = new Point(head.x, head.y + 1);
                break;
            case LEFT:
                newHead = new Point(head.x - 1, head.y);
                break;
            case RIGHT:
                newHead = new Point(head.x + 1, head.y);
                break;
            default:
                // This should never happen
                newHead = head;
                break;
        }
        body.addFirst(newHead);
        body.removeLast();
    }

    public void grow() {
        Point tail = body.getLast();
        body.addLast(tail); // Effectively duplicates the tail segment
    }

    public Point getHead() {
        return body.getFirst();
    }

    public LinkedList<Point> getBody() {
        return body;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction newDirection) {
        // Prevent the snake from reversing on itself
        if (direction == Direction.UP && newDirection == Direction.DOWN) return;
        if (direction == Direction.DOWN && newDirection == Direction.UP) return;
        if (direction == Direction.LEFT && newDirection == Direction.RIGHT) return;
        if (direction == Direction.RIGHT && newDirection == Direction.LEFT) return;
        this.direction = newDirection;
    }

    public boolean checkCollisionWithSelf() {
        Point head = getHead();
        for (int i = 1; i < body.size(); i++) {
            if (head.equals(body.get(i))) {
                return true;
            }
        }
        return false;
    }

    public String getPlayerId() {
        return playerId;
    }
}
