package control;

import java.util.ArrayList;

public class AI{

    // PUNTOS DE CADA ASPECTO DE LA FUNCION QUE EVALUA EL TABLERO (tarjet function)
    // PUNTOS POR POSICIONES
    int _POINT_FOR_WIN = 100;
    int _POINT_FOR_KING = 20;
    int _POINT_FOR_PIECE = 10;

    int _POINT_FOR_THREAT = 10;
    int _POINT_FOR_SIDES= 4;


    // NIVEL MAS PROFUNDO DE BUSQUEDA EN EL ARBOL (cantidad de jugadas que podra la IA preveer)
    int _TOP_RECURSION_LEVEL = 7;

    private int _pieces;
    private int _kings;
    private char[][] _gameBoard;
    private final Rules _rules;
    private ArrayList<int[]> _bestMovesList;
    private ArrayList<Integer> _decisionTree;

    /**
     * Constructor
     */
    public AI() {
        _rules = Rules.getInstance();
        _pieces = 12;
        _kings = 0;
    }
    /**
     * Setter para la cant de piezas (no se usa de momento, el juego no esta completo)
     * @param count int
     */
    private void setPieces(int count){
        _pieces = count;
    }
    /**
     * Setter para la cant de reyes (igual que el anterior)
     * @param count int
     */
    private void setKings(int count){
        _kings = count;
    }

    /**
     * Setter para el tablero
     * @param board tablero
     */
    public void setBoard(char[][] board){
        _gameBoard = copyBoard(board);
    }

    /**
     * Getter para el tablero
     * @return tablero
     */
    public char[][] getBoard() {
        return _gameBoard;
    }

    /**
     * Llama minimax y busca la mejor jugada por los puntajes(valor minimax) guardados de cada una,
     * si dos jugadas tienen el mismo puntaje, devuelve la primera que se encontró segun el orden de busqueda
     * @return la jugada en forma de arreglo (ejemplo [4,3,3,4] seria la jugada[[4,3],[3,4]])
     *
     */
    public int[] playAi(){
        _decisionTree = new ArrayList<>();
        int m = miniMax(_TOP_RECURSION_LEVEL,_gameBoard, true);
        if(_bestMovesList == null || _bestMovesList.size()==0)
            return null;
        int i = _decisionTree.indexOf(m);

        return _bestMovesList.get(i);
    }


    /* PSEUDOCODIGO DE MINIMAX (WIKIPEDIA) SE USA DE BASE PARA LA IMPLEMENTACION QUE LE SIGUE
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
     * @param depth profundida actula en el arbol (cuenta al reves)
     * @param board board
     * @param ai    juega la ia o no
     * @return valor minimax de cada estado
     */
    private int miniMax(int depth, char[][] board, boolean ai){
        _rules.setPropertiesForPlayer(board, ai);
        ArrayList<int[]> localArrayList = _rules.bestMoves();
        if(depth == _TOP_RECURSION_LEVEL){  //antes de iniciar la recursion guarda todas las
            _bestMovesList = localArrayList;  //jugadas posibles
        }
        if(localArrayList.isEmpty()){
            return ai?-_POINT_FOR_WIN:_POINT_FOR_WIN;
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
                    if (depth == _TOP_RECURSION_LEVEL) {
                        _decisionTree.add(val);
                    }
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
     * Target function para evaluar cada estado del tablero basado el los puntos definidos al inicio
     *
     * @param board board
     * @param ai    juega la ia o no
     * @return int valor
     */
    private int EvaluateBoard(char[][] board, boolean ai){
        int constant = (ai?1:-1);
        int constBl=1*constant;
        int constRd=-1*constant;

        // se cuenta desde el punto de vista de la ia
        int score=0;
        for (int r=0; r<8; r++){
            for (int c=0; c<8; c++){
                char cr = board[r][c];
                // puntea la pieza
                switch (cr) {
                    case 'r' -> score += (_POINT_FOR_PIECE * constRd);
                    case 'R' -> score += (_POINT_FOR_KING * constRd);
                    case 'b' -> score += (_POINT_FOR_PIECE * constBl);
                    case 'B' -> score += (_POINT_FOR_KING * constBl);
                    default -> {
                    }
                }
                // puntea la posicion
                if((cr=='r' || cr=='R')&&(c==0||c==7||r==0||r==7)){
                    score+=(_POINT_FOR_SIDES*constRd);
                }
                if((cr=='b' || cr=='B')&&(c==0||c==7||r==0||r==7)){
                    score+=(_POINT_FOR_SIDES*constBl);
                }
            }
        }
        ArrayList<int[]> localArrayList;
        _rules.setPropertiesForPlayer(board, false);
        localArrayList = _rules.bestMoves();
        int rVal = evalAlist(localArrayList)*_POINT_FOR_THREAT;
        localArrayList.clear();
        _rules.setPropertiesForPlayer(board, true);
        localArrayList = _rules.bestMoves();
        int bVal = evalAlist(localArrayList)*_POINT_FOR_THREAT;
        score+=rVal*constRd;
        score+=bVal*constBl;
        if(ai)
            return score;   //OJO probando devolver el mismo resultado con signo negativo
        else
            return -score;
    }

    /**
     * Cuenta cuantas piezas estan amenazadas por todas las jugadas posibles en el tablero actual
     * @param arlist pool de jugadas posibles
     * @return int value score
     */
    private int evalAlist(ArrayList<int[]> arlist){

        if(_rules.getLevelOfPlays()==0)
            return 0;
        else{
            int score=0;
            int temp;
            for (int[] ints : arlist) {
                temp = ints.length;
                switch (temp) {
                    case 4 -> score += 1;
                    case 6 -> score += 2;
                    case 2 -> score += 0;
                    default -> score += 3;  //tiene en cuenta la posiblilidad de que existan jumps mas largos q 3
                }
            }
            return score;
        }
    }

    /**
     * Realiza la jugada en el tablero
     * @param play la jugada
     * @param board  el tablero
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
        if((piece == 'b') && _rules.isCrowned(piece, prevR))
            piece = 'B';
        else if ((piece == 'r') && _rules.isCrowned(piece, prevR))
            piece = 'R';

        board[prevR][prevC] = piece;
    }

    
    /**
     * Elimina la pieza
     * @param board el tablero
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
     * Crea una copia del tablero para instanciar _board y para las sucesivas llamadas recursivas, necesario
     * para evitar problemas de actualizacion (referencias) generados con los metodos clone(), arraycopy(), copyOf(), etc.
     * @param board tablero original
     * @return copia
     */
    private char[][] copyBoard(char[][] board){
        char[][] temp = new char[8][8];
        for(int r=0;r<8;r++){
            temp[r] = board[r].clone();
            /*
            System.arraycopy(board[r], 0, temp[r], 0, 8);

             */
        }
        return temp;
    }
}
