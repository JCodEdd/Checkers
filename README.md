# Checkers
Java AI player for the checkers game that uses Minimax algorithm to determine the best move from the current board position.

## How It Works
* The **Rules** class encapsulates all checkers gameplay rules and validation. It generates all valid moves from any board position.
* The **AI** class implements the Minimax algorithm recursively to assign numeric scores to board positions up to a given search depth. It retrieves child nodes from Rules and selects the highest scoring move.
* The **Game** class manages the game board state and provides utilities to initialize boards and print the board state.

## Getting Started
### To test the AI functionality:
1. Run the Play class main method.
1. You can try sample games using:
   * A hardcoded starting position.
   * A randomly generated board.
1. The board states before and after the move will be printed.

## Key Classes
* **Rules** - Encapsulates checkers rules validation and move generation.
* **AI** - Implements Minimax search algorithm to evaluate board positions.
* **Game** - Manages game state and provides testing utilities.
* **Play** - Executable driver class for testing.

## Future Improvements
* Add graphical board representation.
* Allow human player interaction.
* Expand AI evaluation metrics.
