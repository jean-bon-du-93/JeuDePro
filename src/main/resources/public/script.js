const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');
const statusEl = document.getElementById('status');

const CELL_SIZE = 20;
const BOARD_WIDTH = 40;
const BOARD_HEIGHT = 30;

canvas.width = BOARD_WIDTH * CELL_SIZE;
canvas.height = BOARD_HEIGHT * CELL_SIZE;

let sessionId = '';
const socket = new WebSocket(`ws://${window.location.host}/websocket`);

socket.onopen = () => {
    statusEl.textContent = 'Waiting for another player...';
};

socket.onmessage = (event) => {
    const gameState = JSON.parse(event.data);

    // The server might send the session ID upon connection or as part of a message
    if (gameState.sessionId) {
        sessionId = gameState.sessionId;
    }

    switch (gameState.type) {
        case 'update':
            statusEl.textContent = 'Game in progress...';
            drawGame(gameState);
            break;
        case 'game-over':
            let winnerText;
            if (gameState.winner === 'draw') {
                winnerText = "It's a draw!";
            } else if (gameState.winner === sessionId) {
                winnerText = 'You win!';
            } else {
                winnerText = 'You lose!';
            }
            statusEl.textContent = `Game Over! ${winnerText}`;
            break;
    }
};

socket.onclose = () => {
    statusEl.textContent = 'Disconnected from server. Please refresh.';
};

socket.onerror = (error) => {
    console.error('WebSocket Error:', error);
    statusEl.textContent = 'Connection error.';
};

function drawGame(state) {
    // Clear canvas
    ctx.fillStyle = '#000';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // Draw snakes
    state.snakes.forEach((snake, index) => {
        // Simple color differentiation
        ctx.fillStyle = index === 0 ? '#00FF00' : '#0000FF';
        snake.body.forEach(segment => {
            drawCell(segment.x, segment.y);
        });
    });

    // Draw food
    if (state.food) {
        ctx.fillStyle = '#FF0000';
        drawCell(state.food.x, state.food.y);
    }
}

function drawCell(x, y) {
    ctx.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
}

document.addEventListener('keydown', (event) => {
    let direction = null;
    switch (event.key) {
        case 'ArrowUp': direction = 'UP'; break;
        case 'ArrowDown': direction = 'DOWN'; break;
        case 'ArrowLeft': direction = 'LEFT'; break;
        case 'ArrowRight': direction = 'RIGHT'; break;
    }

    if (direction && socket.readyState === WebSocket.OPEN) {
        socket.send(direction);
    }
});
