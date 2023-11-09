package control;

import java.util.ArrayList;

public class AI{

    // Point values for each aspect of the target function that evaluates the board.
    // Point values for piece positions.
    int POINT_FOR_WIN = 100;
    int POINT_FOR_KING = 20;
    int POINT_FOR_PIECE = 10;

    int POINT_FOR_THREAT = 10;
    int POINT_FOR_SIDES = 4;


    // Maximum depth of search in the game tree (number of moves the AI can anticipate).
    int TOP_RECURSION_LEVEL = 6;

    private char[][] gameBoard;
    private final Rules rules;
    private ArrayList<int[]> bestMovesList;
    private ArrayList<Integer> decisionTree;

    /**
     * Constructor
     */
    public AI() {
        rules = Rules.getInstance();
    }
    
    /**
     * Setter for the board
     * @param board tablero
     */
    public void setBoard(char[][] board){
        gameBoard = copyBoard(board);
    }

    /**
     * Getter for the board
     * @return tablero
     */
    public char[][] getBoard() {
        return gameBoard;
    }

    /**
     * Finds and returns the best move for the ai
     * @return best move in a int[] 
     */
    public int[] playAi(){
        decisionTree = new ArrayList<>();
        int m = miniMax(TOP_RECURSION_LEVEL, gameBoard, true);
        if(bestMovesList == null || bestMovesList.size()==0)
            return null;
        int i = decisionTree.indexOf(m);
        return bestMovesList.get(i);
    }


    /* PSEUDOCODE FOR MINIMAX FROM WIKIPEDIA USED AS AN INSPIRATION
    function minimax(node, depth, maximizingPlayer)
    if depth = 0 or node is a terminal node
        return the heuristic value of node
    if maximizingPlayer
        bestValue := -∞
        for each child of node
            val := minimax(child, depth - 1, FALSE)
            bestValue := max(bestValue, val)
        return bestValue
    else
        bestValue := +∞
        for each child of node
            val := minimax(child, depth - 1, TRUE)
            bestValue := min(bestValue, val)
        return bestValue

(* Initial call for maximizing player *)
minimax(origin, depth, TRUE)
 */

    /**
     * MINIMAX
     *
     * @param depth profundida actual en el arbol (cuenta al reves)
     * @param board board
     * @param ai    juega la ia o no
     * @return valor minimax de cada estado
     */
    private int miniMax(int depth, char[][] board, boolean ai){
        rules.setPropertiesForPlayer(board, ai);
        ArrayList<int[]> localArrayList = rules.bestMoves();
        if(depth == TOP_RECURSION_LEVEL){  
            bestMovesList = localArrayList;  
        }
        if(localArrayList.isEmpty()){
            return ai?-POINT_FOR_WIN:POINT_FOR_WIN;
        }
        if(depth==0){
            return EvaluateBoard(board, ai);
        }
        else{
            int baseValue = (ai)?Integer.MIN_VALUE:Integer.MAX_VALUE;
            for (int[] play : localArrayList) {
                char[][] boardC = copyBoard(board);
                makeTheMove(play, boardC);
                int val = miniMax(depth - 1, boardC, !ai);
                if (ai) {
                    if (depth == TOP_RECURSION_LEVEL) 
                        decisionTree.add(val);
                    if (val > baseValue)
                        baseValue = val;
                }
                else {
                    if (val < baseValue)
                        baseValue = val;
                }
            }
            return baseValue;
        }
    }

    /**
     * Calculates a score for the provided game board based on piece 
     * placement and threat assessment. Uses constant point values defined.
     *
     * @param board game board
     * @param ai    True if evaluating from the AI's perspective
     * @return integer score calculated for the board
     */
    private int EvaluateBoard(char[][] board, boolean ai){
        int constant = (ai?1:-1);
        int constBl=1*constant;
        int constRd=-1*constant;

        int score=0;
        for (int r=0; r<8; r++){
            for (int c=0; c<8; c++){
                char cr = board[r][c];
                // points for pieces
                switch (cr) {
                    case 'r' -> score += (POINT_FOR_PIECE * constRd);
                    case 'R' -> score += (POINT_FOR_KING * constRd);
                    case 'b' -> score += (POINT_FOR_PIECE * constBl);
                    case 'B' -> score += (POINT_FOR_KING * constBl);
                    default -> {
                    }
                }
                // points for positions
                if((cr=='r' || cr=='R')&&(c==0||c==7||r==0||r==7)){
                    score+=(POINT_FOR_SIDES*constRd);
                }
                if((cr=='b' || cr=='B')&&(c==0||c==7||r==0||r==7)){
                    score+=(POINT_FOR_SIDES*constBl);
                }
            }
        }
        ArrayList<int[]> localArrayList;
        rules.setPropertiesForPlayer(board, false);
        localArrayList = rules.bestMoves();
        int rVal = evaluateThreatLevelFromMoves(localArrayList)*POINT_FOR_THREAT;
        localArrayList.clear();
        rules.setPropertiesForPlayer(board, true);
        localArrayList = rules.bestMoves();
        int bVal = evaluateThreatLevelFromMoves(localArrayList)*POINT_FOR_THREAT;
        score+=rVal*constRd;
        score+=bVal*constBl;
        if(ai)
            return score; 
        else
            return -score;
    }

    /**
     * Evaluates the threat level posed by the given list of possible moves 
     * by counting threatened opponent pieces based on move lengths.
     * @param arlist The list of possible moves to evaluate
     * @return int The calculated threat score
     */
    private int evaluateThreatLevelFromMoves(ArrayList<int[]> arlist){

        if(rules.getLevelOfPlays()==0)
            return 0;
        else{
            int score=0;
            int temp;
            for (int[] ints : arlist) {
                temp = ints.length;
                switch (temp) {
                    case 4 -> score += 1;
                    case 6 -> score += 2;
                    default -> score += 3;  //possible jumps longer than 3 pieces
                }
            }
            return score;
        }
    }

    /**
     * Applies the given move to the board. Modifies board parameter in-place.
     * @param play
     * @param board 
     */
    public void makeTheMove(int[] play, char[][] board) {
        int i=0;
        int prevR=play[i++];
        int prevC=play[i++];
        int r,c;
        char piece = board[prevR][prevC];
        while(i+1<=play.length){
            r=play[i++];c=play[i++];
            clearTheWay(board, prevR, prevC, r, c);
            prevR=r;
            prevC=c;
        }
        if((piece == 'b') && rules.isCrowned(piece, prevR))
            piece = 'B';
        else if ((piece == 'r') && rules.isCrowned(piece, prevR))
            piece = 'R';

        board[prevR][prevC] = piece;
    }

    
    /**
     * Clears the pieces jumped over by the given move.
     * Elimina la pieza
     * @param board the game board
     * @param prevR previous row - start row
     * @param prevC previous column - start column
     * @param r actual row - end row
     * @param c actual column - end column
     */
    private void clearTheWay(char[][] board,int prevR,int prevC,int r,int c){
        board[prevR][prevC]='e';
        board[r][c]='e';
        int o = prevR-r;
        if(o>1 || o<-1){
            int jmpR=(prevR>r)?prevR-1:prevR+1;
            int jmpC=(prevC>c)?prevC-1:prevC+1;
            board[jmpR][jmpC]='e';
        }
    }

    /**
     * Makes a deep copy of the given game board.
     * @param board The board to copy
     * @return A duplicated two-dimensional char array Board
     */
    private char[][] copyBoard(char[][] board){
        char[][] temp = new char[8][8];
        for(int r=0;r<8;r++){
            temp[r] = board[r].clone();
        }
        return temp;
    }
}
