package control;

import java.util.*;

/** 
 * Encapsulates all checkers game rules validation. Implements Singleton pattern.
 */
public class Rules {
    // http://www.wcdf.net/rules.htm
    //SINGLETON implementation

    private static Rules instance;
    private char[][] localBoard;
    private boolean ai;
    private boolean isKing;

    /**
     * Stores arrays with the coordinates of each multiple jump
     */
	private ArrayList<int[]> jumpTree; 
    
    /**
     * Tracks the maximum jump sequence length encountered while recursively searching
     * all possible jumps from the current position. Used only temporarily during 
     * move generation logic.
     */
    private int maxFoundJumpLength;

    /**
     * Stores the depth of the deepest jump sequence possible from the current position,
     * as communicated by the move generation methods.
     */
    private int positionJumpLevel;

    
    private Rules(){}

    /**
     * Provides access to the singleton instance of the Rules class.
     */
    public static Rules getInstance(){
        if(instance == null)
            instance = new Rules();
        return instance;
    }

    /**
     * Configures the Rules instance with the game state specific to the current player's turn.
     * @param board current board state
     * @param ai    if the AI is the active player
     */
    public void setPropertiesForPlayer(char[][] board, boolean ai){
        localBoard = board;
        this.ai = ai;
    }

    
    /**
     * Checks if the piece at the given position has been crowned according to the game rules.
     * @param piece type of piece (red or black)
     * @param row where the play ends
     * @return boolean
     */
    public boolean isCrowned(char piece, int row){
        return  (piece == 'r' && row == 0) ||
                (piece == 'b' && row == 7);
    }

	/**
	 * Returns the "level or priority" of possible moves 
     * (0 = "normal moves" or 1,2,3... "length" of longest jump)
	 * @return int value
	 */

    public int getLevelOfPlays(){
        return positionJumpLevel;
    }

    /**
     * Generates all valid single moves and jump sequences for the active player's 
     * pieces based on the current board state.
     * @return ArrayList containing the legal moves, with each move 
     *          represented as an int array in the format [startRow, startCol, 
     *          endRow1, endCol1, ...endRowN, endColN]
     */
    public ArrayList<int[]> bestMoves(){
        ArrayList<int[]> moves = new ArrayList<>();
        int globalLevel = 0;            
        for(int r=0;r<8;r++){           
            for(int c=0;c<8;c++){
                if(isOwnPiece(r, c)){
                    isKing = recognize(r, c).equals("king");
                    AbstractMap.SimpleEntry<Integer, int[][]> possibilities = getPiecePossibleMoves(r, c);
                    int level = possibilities.getKey();
                    if((globalLevel==0) && (level==0)){
                        saveMoves(moves,possibilities.getValue(),false);
                    }
                    else if(level>0){
                        if(globalLevel == 0){
                            globalLevel = level; 
                            saveMoves(moves,possibilities.getValue(),true);
                        }
                        else{
                            globalLevel = Math.max(globalLevel,level);
                            saveMoves(moves,possibilities.getValue(),false);
                        }
                    }
                }
            }
        }
        positionJumpLevel = globalLevel;
        return moves;
    }


    //private methods ----------------------------------------


    /**
     * Saves the current player's last valid moves to the moves Arraylist.
     * @param moves ArrayList of all moves found so far
     * @param newMoves int[][] newly found moves
     * @param delete when the first jump is found, the whole list is cleared
     */
    private void saveMoves(ArrayList<int[]> moves,int[][] newMoves,boolean delete){
        if(delete){
            moves.clear();
        }
        Collections.addAll(moves, newMoves);
    }

    /**
     * Returns all valid single moves or possible jump sequences for the specified piece 
     * based on the current board state. If there's a jump all single moves are ignored (per the rules)
     * @see Rules#pairStructure(int, int, String, int[][])
     * @param row int
     * @param col int
     * @return {@code AbstractMap.SimpleEntry<Integer,int[][]>} where Integer is 
     *    max jump level (0 if no jumps are possible) and int[][] contains coordinate arrays for each move
     */

    private AbstractMap.SimpleEntry<Integer, int[][]> getPiecePossibleMoves(int row, int col){
        int[][] listOfMoves;
        int[][] listOfJumps;
        maxFoundJumpLength=0;

        listOfMoves = possibleMoves(row, col);

        if(isKing)
            listOfJumps = possibleJumps(row,col,"all");
        else if(ai)
            listOfJumps = possibleJumps(row,col,"down");
        else
            listOfJumps = possibleJumps(row,col,"up");

        if(listOfJumps != null){
            return pairStructure(row,col,"jumps",listOfJumps);
        }else{
            return pairStructure(row,col,"moves",listOfMoves);
        }
    }

    /**
     * Groups the valid moves or sequences (jumps) found for a piece into a SimpleEntry structure
     * representing the possible gameplay rounds.
     * 
     * @param row int
     * @param col int
     * @param mode string [jumps o moves]
     * @param actions array of possible moves/jumps
     * @return SimpleEntry containing max jump length and array of moves/jumps
     */
    private AbstractMap.SimpleEntry<Integer, int[][]> pairStructure(int row, int col, String mode, int[][] actions){
        
        ArrayList<int[]> tempArrayList = new ArrayList<>();
        if(mode.equals("moves")){
            if(actions == null){
                return new AbstractMap.SimpleEntry<>(-1,null);
            }else{
                for (int[] action : actions) {
                    if (!(action[0] == -1 && action[1] == -1)) {
                        tempArrayList.add(action);
                    }
                }
            }
        }
        else{      //mode is jumps
            jumpTree = new ArrayList<>();
            testForMultipleJumps(new int[0],actions);
            tempArrayList.addAll(jumpTree);
        }

        int [][] returnStructure = new int[tempArrayList.size()][];
        for(int i=0;i<tempArrayList.size();i++){
            int[] temp = new int[tempArrayList.get(i).length+2];
            copyArrayMovesStructure(tempArrayList.get(i),temp,row,col);
            returnStructure[i] = temp;
        }
        return new AbstractMap.SimpleEntry<>(maxFoundJumpLength,returnStructure);
    }

    /**
     * Copies the coordinates from the old array into a new array, prepending the start row/col
     * @see Rules#enlargeArray(int[], int[], int[])
     * @param old old array
     * @param actual new array
     * @param row start row
     * @param col start col
     */
    private void copyArrayMovesStructure(int[] old, int[] actual, int row, int col){
        int i=0;
        actual[i++] = row; actual[i++] = col;
        for (int val : old){
            actual[i++] = val;
        }
    }

    /**
     * Recursively checks all possibilities for multiple jumps from the current position.
     * Stores full jump sequences found in jumpTree.
     * 
     * @param path visited places
     * @param from possible moves
     */
    private void testForMultipleJumps(int[] path,int[][] from){

        if(from == null){   //no more possible jumps means the end of current path
            int length = path.length/2 ; //jump length(1, 2, 3...)
            maxFoundJumpLength = Math.max(length, maxFoundJumpLength);
            jumpTree.add(path);
        }
        else{
            if(path.length > 2)
                checkPath(path, from);

            String[] directions = {"up","up","down","down"};

            for(int i=0;i<4;i++){
                if(!(from[i][0]==-1 && from[i][1]==-1)){
                    int[] temp = new int[path.length + 2];
                    enlargeArray(path, temp, from[i]);
                    if(isKing) {
                        testForMultipleJumps(temp , possibleJumps(from[i][0],from[i][1],"all"));
                    }
                    else
                        testForMultipleJumps(temp , possibleJumps(from[i][0],from[i][1],directions[i]));
                }
                if(i == 3 && checkInvalid(from))
                    testForMultipleJumps(path, null);
            }
        }
    }

    /**
     * Validates the path array does not contain duplicate jumps in the same direction.
     * 
     * @param path 
     * @param check 
     */
    private void checkPath(int[] path, int[][] check){
        int l = path.length;
        int last = l-3;
        int lastB = l-4;

        for(int i=0; i<4; i++){
            if ((path[lastB] == check[i][0]) && (path[last] == check[i][1])){
                check[i][0] = -1;
                check[i][1] = -1;
                return;
            }
        }

    }

    /**
     * Checks if the provided moves array contains only invalid (-1,-1) entries
     * @param from array
     */
    private boolean checkInvalid(int[][] from){
        int invalNum = 0;
        for (int[] pos : from) {
            if ((pos[0] == -1) && (pos[1] == -1))
                invalNum++;

        }
        return invalNum == 4;
    }


    /**
     * Enlarges the path array by adding the add array elements to the to array.
     * @param from array to enlarge (path)
     * @param to array
     * @param add array
     */
    private void enlargeArray(int[]from,int[]to,int[]add){
        if(from != null && to!=null && add!=null){
            int i;
            for(i=0;i<from.length;i++){
                to[i]=from[i];
            }
            to[i++]=add[0];
            to[i]=add[1];
        }
    }

    /**
     * Enum type for jump directions. If isKing, then all directions are valid, 
     * otherwise only up or down depending of the piece color.
     */
    private enum Direction{down,up,all}

    /**
     * Finds valid jump moves for a piece in the given direction from the provided position on the board.
     * @param row int
     * @param col int
     * @param direct String direction
     * @return int[][] the possible jumps in the format [[up,left], [up,right], [down,right], [down,left]]
     *          invalid jumps are represented by [-1,-1]
     */
    private int[][] possibleJumps(int row, int col, String direct){
        if ((row == col) && (col == -1))
            return null;

        Direction direction = Direction.valueOf(direct);
        boolean downJumps,upJumps;
        switch(direction){
            case all:
                downJumps = true;
                upJumps = true;
                break;
            case up:
                downJumps = false;
                upJumps = true;
                break;
            case down:
                downJumps = true;
                upJumps = false;
                break;
            default:
                return null;
        }
        int [][] listOfJumps ={{-1,-1},{-1,-1},{-1,-1},{-1,-1}};
        boolean areJumps = false;
        if( upJumps && isTheJumpPossible(row,col,-1,-1)){
            listOfJumps[0][0]=row-2;
            listOfJumps[0][1]=col-2;
            areJumps = true;
        }
        if( upJumps && isTheJumpPossible(row,col,-1,+1)){
            listOfJumps[1][0]=row-2;
            listOfJumps[1][1]=col+2;
            areJumps = true;
        }
        if( downJumps && isTheJumpPossible(row,col,+1,+1)){
            listOfJumps[2][0]=row+2;
            listOfJumps[2][1]=col+2;
            areJumps = true;
        }
        if( downJumps && isTheJumpPossible(row, col,+1,-1)){
            listOfJumps[3][0]=row+2;
            listOfJumps[3][1]=col-2;
            areJumps = true;
        }
        if(areJumps)
            return listOfJumps;
        else
            return null;
    }

    /**
     * Finds valid regular (non-jump) moves for a piece from the provided position on the board.
     * @see Rules#possibleJumps(int, int, String)
     * @param row int
     * @param col int
     * @return int[][] containing possible moves or null if none found
     */
    private int[][] possibleMoves(int row, int col){
        int [][] listOfMoves ={{-1,-1},{-1,-1},{-1,-1},{-1,-1}};
        boolean areMoves = false;
        if(isTheMovePossible(row, row-1,col-1)){
            listOfMoves[0][0]=row-1; // up-left
            listOfMoves[0][1]=col-1;
            areMoves = true;
        }
        if(isTheMovePossible(row, row-1,col+1)){
            listOfMoves[1][0]=row-1; // up-right
            listOfMoves[1][1]=col+1;
            areMoves = true;
        }
        if(isTheMovePossible(row, row+1,col+1)){
            listOfMoves[2][0]=row+1; // down-right
            listOfMoves[2][1]=col+1;
            areMoves = true;
        }
        if(isTheMovePossible(row, row+1,col-1)){
            listOfMoves[3][0]=row+1; // down-left
            listOfMoves[3][1]=col-1;
            areMoves = true;
        }
        if(areMoves)
            return listOfMoves;
        else
            return null;
    }

    /**
     * Checks if a regular (non-jump) move from the current position to the given row/col is valid.
     * @param previousRow int
     * @param row int
     * @param col int
     */
    private Boolean isTheMovePossible(int previousRow, int row, int col){
        return ((isInRange(row) && isInRange(col) && recognize(row,col).equals("empty")) &&
                 isMovingFwd(previousRow, row));
    }

    /**
     * Checks if a jump move from the current position in the given direction is valid according to the rules.
     * @param row int
     * @param col int
     * @param verticalDirection (+1 or -1)
     * @param horizontalDirection (+1 or -1)
     */
    private Boolean isTheJumpPossible(int row,int col,int verticalDirection,int horizontalDirection){
        int tempRow =row+verticalDirection;
        int tempCol =col+horizontalDirection;

        if (!(isInRange(tempRow) && isInRange(tempCol))) {
            return false;
        }
        boolean firstCheck = isOpponentsPiece(tempRow, tempCol);

        tempRow+=verticalDirection;
        tempCol+=horizontalDirection;

        boolean forward = isMovingFwd(row, tempRow);

        if (!(isInRange(tempRow) && isInRange(tempCol))) {
            return false;
        }
        boolean secondCheck = (localBoard[tempRow][tempCol] == 'e');
        return firstCheck && forward && secondCheck;
    }


    /**
     * Checks if a move represents forward movement for the given player/piece.
     * Or if it's a king and can move backward
     * @param prevRow 
     * @param row 
     * @return boolean
     */
    private Boolean isMovingFwd(int prevRow, int row){
        return (isKing || (!ai && prevRow>row) ||
                (ai && prevRow<row));
    }

    /**
     * Checks if row or column is in range for the board.
     * @param a int
     */
    private Boolean isInRange(int a){
        return a < 8 && a >= 0;
    }

    private String recognize(int row,int col){
        char piece = localBoard[row][col];
        return switch (piece) {
            case 'b', 'r' -> "basic";
            case 'B', 'R' -> "king";
            default -> "empty";
        };
    }

    private boolean isOpponentsPiece(int row,int col){
        return ( (ai && (localBoard[row][col] == 'r' || localBoard[row][col] == 'R')) ||
                 (!ai && (localBoard[row][col] == 'b' || localBoard[row][col] == 'B')));
    }

    private boolean isOwnPiece(int row, int col){
        return ( (ai && (localBoard[row][col] == 'b' || localBoard[row][col] == 'B')) ||
                 (!ai && (localBoard[row][col] == 'r' || localBoard[row][col] == 'R')));
    }

}
