package control;


import java.util.Arrays;
import java.util.Random;


/**
 * Manages board state and allows using sample game states to test AI and Rules functionality.
 */
public class Game {
    private final AI ai;
    private char[][] board;

    
    /**
     * Constructor
     */
    public Game() {
        ai = new AI();
    }
    
    /**
     * Simulates an ai turn in given board state. Prints the board, gets the best ai move,
     * updates the board with it and prints the board again.
     * @param positions board state
     * @see AI#playAi()
     */
    public void playWithBoard(String[] positions){
        board = generateBoardFromInput(positions);
        ai.setBoard(board);
        int[] play = ai.playAi();
        System.out.println("Before the move");
        printBoard();
        System.out.println();
        updateBoard(play);
        System.out.println("After the move");;
        printBoard();
    }

    /**
     * Simulates an ai turn in randomly generated board state. Prints the board, gets 
     * the best ai move, updates the board with it and prints the board again.
     * @see AI#playAi()
     */
    public void playWithRandomBoard(){
        board = generateRandomBoard();
        ai.setBoard(board);
        int[] play = ai.playAi();
        System.out.println("Before the move");
        printBoard();
        System.out.println();
        updateBoard(play);
        System.out.println("After the move");;
        printBoard();
    }

    /**
     * Generates the initial game board state by placing starting pieces on an 
     * empty board based on given positions.
     * @param positions  Strings representing row,col,piece for starting positions
     * @return initialized game board
     */
    private char[][] generateBoardFromInput(String[] positions){
        char[][] board  = new char[8][8];
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                if( (i+j) % 2 == 0)
                    board[i][j] = '\0';
                else
                    board[i][j] = 'e';
            }
        }
        for (String position : positions) {
            int row = Integer.parseInt(position.substring(0,1));
            int col = Integer.parseInt(position.substring(2,3));
            if(board[row][col] == 'e')
                board[row][col] = position.charAt(4);
        }
        return board;
    }
    
    /**
     * Generates a random game board with around 5 pieces for each player.
     * @return randomly initialized game board
     */
    private char[][] generateRandomBoard(){
        char[][] board  = new char[8][8];
        char[] pieceType = {'r','R','b','B'};
        int rCount=0,bCount=0;
        Random random = new Random();

        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                if( (i+j) % 2 == 0)
                    board[i][j] = '\0';
                else{
                    board[i][j] = 'e';
                }
            }
        }

        while (rCount <= 4 || bCount <= 4) {
            int randRow = random.nextInt(8);
            int randCol = random.nextInt(8);
            if( (randRow+randCol) % 2 == 0)
                    continue;
            char nextPiece = pieceType[random.nextInt(4)];

            if(rCount>5 && (nextPiece=='r' || nextPiece=='R'))
                continue;
            else if (bCount>5 && (nextPiece=='b' || nextPiece=='B'))
                continue;
 
            switch (nextPiece){
                case 'r', 'R' -> rCount++;
                case 'b', 'B' -> bCount++;
            }
            board[randRow][randCol] = nextPiece;
        }
        
        return board;
    }

    /**
     * Updates the board with the given move.
     * @param play jugada
     */
    private void updateBoard(int[] play){
        ai.makeTheMove(play, board);
    }

    /**
     * Prints the board in a basic visual representation.
     */
    private void printBoard(){
        char[][] boardCopy = board.clone();
        char[] aux;

        for(int i=0; i<8; i++){                   
            for(int j=0; j<8; j++){              
                if(boardCopy[i][j] == 'e' || boardCopy[i][j] == '\0'){
                    aux = boardCopy[i].clone();
                    aux[j] = ' ';
                    boardCopy[i] = aux;
                }
            }
        }
        System.out.println("+----+-----+-----+-----+-----+-----+-----+----+");

        for (char[] row : boardCopy) {
            System.out.println(Arrays.toString(row).replaceAll(",", "  | ").
                    replaceAll("]", " |").replaceAll("\\[" , "| "));
            System.out.println("+----+-----+-----+-----+-----+-----+-----+----+");
        }


    }


}

