package control;

import java.util.*;

public class Rules {
    // http://www.wcdf.net/rules.htm
    //SINGLETON impl
    private static Rules _instance;
    private char[][] _localBoard;
    private boolean _ai;                //signals ai turn or not
    private ArrayList<int[]> _jumpTree; //arrayList que guarda los arreglos con las coordenanas de cada salto multiple
    private int _longestJump;

    /**
     * Para cada pieza, antes de generar las jugadas, se chequea si es rey, si lo es,
     * se marca este campo como true para cada una de las validaciones/chequeos que vienen
     * despues. Asi se evita hacer el mismo chequeo varias veces o pasar un argumento extra
     * a cada metodo.
     */
    private boolean _isKing;

    private int _jumpBasedTree;

    /**
     * Constructor privado
     */
    private Rules(){}

    /**
     * Getter para la instancia de esta clase.
     * La implementacion de esta clase es de instancia unica
     * @return instancia de la clase
     */
    public static Rules getInstance(){
        if(_instance == null)
            _instance = new Rules();
        return _instance;
    }

    /**
     * Setea las propiedades esenciales para cada "jugador"
     * @param board tablero
     * @param ai    es la ia o no
     */
    public void setPropertiesForPlayer(char[][] board, boolean ai){
        _localBoard = board;
        _ai = ai;
    }

    /**
     * Usa el tablero y el jugador actuales para buscar todas las posibles jugadas
     * de cada pieza del jugador siguiendo las reglas.
     * @return ArrayList con las jugadas, ejemplo:[4,3,3,4] (el 1er par de elementos es la posicion original de
     *                                                          la pieza y cada par de los siguientes es la direccion
     *                                                          de una casilla de la jugada)
     */
    public ArrayList<int[]> bestMoves(){
        ArrayList<int[]> moves = new ArrayList<>();
        int globalLevel = 0;            // se usa para detectar cuando existen jumps posibles y por tanto
        for(int r=0;r<8;r++){           // eliminar las jugadas guardadas hasta el momento
            for(int c=0;c<8;c++){
                if(ownPiece(r, c)){
                    _isKing = recognize(r, c).equals("king");
                    AbstractMap.SimpleEntry<Integer, int[][]> possibilities = findAllPossibleMoves(r, c);
                    int level = possibilities.getKey();
                    if(level==globalLevel){
                        saveMoves(moves,possibilities.getValue(),false);
                    }
                    if(level>globalLevel){
                        if(globalLevel == 0){ //si cae en este caso y global level es 0 solo se han encontrado hasta
                            globalLevel = 1;  //el momento jugadas normales y me acaban de devolver un jump
                            saveMoves(moves,possibilities.getValue(),true);
                        }
                        else{
                            saveMoves(moves,possibilities.getValue(),false);
                        }
                    }
                }
            }
        }
        _jumpBasedTree = globalLevel;
        return moves;
    }


    //private methods ----------------------------------------


    /**
     * Toma las jugadas posibles y las copia en el Arraylist de las jugadas
     * @param moves ArrayList con todas las jugadas
     * @param newMoves int[][]
     * @param delete cuando aparece el 1er jump posible se eliminan todas las jugadeas "normales"
     */
    private void saveMoves(ArrayList<int[]> moves,int[][] newMoves,boolean delete){
        if(delete){
            moves.clear();
        }
        Collections.addAll(moves, newMoves);
    }

    /**
     * Busca todas las posibles jugadas de la pieza en la casilla especificada.
     * Si existe posibilidad de un "jump" todas las jugadas son ignoradas (segun las reglas)
     *
     * @param row row int
     * @param col col int
     * @return SimpleEntry <Integer,int[][]> Integer es el nivel de la jugada indica la jugada a realizar y
     * el arreglo son las coordenadas de cada posicion de la jugada
     */

    private AbstractMap.SimpleEntry<Integer, int[][]> findAllPossibleMoves(int row, int col){
        int[][] listOfMoves;
        int[][] listOfJumps;
        _longestJump=0;

        listOfMoves = possibleMoves(row, col);

        if(_isKing)
            listOfJumps = possibleJumps(row,col,"all");
        else if(_ai)
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
     * Auxiliar para estructurar bien la jugada y filtrar casos incorrectos,
     * crea una estructura Pair<Integer,int[][]>
     * Integer - describe el nivel de la jugada, valores posible: [1,2,3... = "jumps",0 = "moves",-1 = null]
     * int[][] - guarda la jugada
     *
     * @param row     row
     * @param col     column
     * @param mode    string [jumps o moves]
     * @param actions jugadas encontradas en el metodo anterior
     * @return Pair
     */
    private AbstractMap.SimpleEntry<Integer, int[][]> pairStructure(int row, int col, String mode, int[][] actions){
        //Here to test
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
        else{      //si mode es "jumps"
            _jumpTree = new ArrayList<>();
            testForMultipleJumps(new int[0],actions);
            tempArrayList.addAll(_jumpTree);
        }

        int [][] returnStructure = new int[tempArrayList.size()][];
        for(int i=0;i<tempArrayList.size();i++){
            int[] temp = new int[tempArrayList.get(i).length+2];
            copyArrayMovesStructure(tempArrayList.get(i),temp,row,col);
            returnStructure[i] = temp;
        }
        return new AbstractMap.SimpleEntry<>(_longestJump,returnStructure);
    }

    /**
     * Inserta las coordenadas iniciales al array con los datos de una jugada
     * Similar a:
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
     * Chequea si hay jumps multiples posibles a realizar. Para cada jump dentro de las acciones posibles:
     * chequea si la direccion de la casilla del primer jump del arreglo (from[]) es valida,
     * en caso positivo registra esa casilla en path[],
     * llama el metodo que busca los jumps posibles a realizar estando en dicha casilla y con lo que
     * este devuelve mas la posicion actual se llama recursivamente,
     * cuando encuentra el final de un jump multiple guarda el recorrido completo del jump en _jumpTree y cambia
     * al paso recursivo siguiente al terminado (el primero de los pendientes).
     * @param path visited places
     * @param from possible moves
     */
    private void testForMultipleJumps(int[] path,int[][] from){

        if(from == null){   //cuando no hay mas jumps posibles llagamos al final del jump multiple actual
            int length = path.length/2 ; //longitud del salto (1, 2, 3...)
            _longestJump = Math.max(length, _longestJump);
            _jumpTree.add(path);
        }
        else{
            if(path.length > 2)
                checkPath(path, from);
            String[] directions = {"up","up","down","down"}; //el arreglo de posibles jumps tiene la estructura
                                      //[[arriba,izquierda],[arriba,derecha],[abajo,derecha],[abajo,izquierda]]

            for(int i=0;i<4;i++){
                if(!(from[i][0]==-1 && from[i][1]==-1)){
                    int[] temp = new int[path.length + 2];
                    enlargeArray(path, temp, from[i]);
                    if(_isKing) {
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
     * Chequea si alguna direccion del nuevo posible jump es la direccion de la jugada anterior (que seria la penultima
     * posicion registrada porque la ultima es la actual) para evitar saltar
     * sobre la misma ficha, si está la cambia por una posicion invalida, si no devuelve el arreglo sin cambios
     *
     * @param path  camino del jump
     * @param check nueva posicion
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
     * Chequea si el arrleglo de los jumps posibles solo contiene posicione invalidas ([-1,-1])
     * @param from arreglo de los jumps
     * @return contiene todas las jugadas invalidas o no
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
     * Añade los datos de una nueva posicion a path
     * @param from arreglo a agrandar (path)
     * @param to nuevo arreglo con los datos añadidos
     * @param add array datos que hay que agregarle a path
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
     * enum type para las direcciones posibles de un jump
     */
    private enum Direction{down,up,all}
    /**
     * Encuentra todos los posibles jumps en la direccion especificada
     * @param row int row
     * @param col int column
     * @param direct direccion en la cual la pieza se puede mover(en todas o solo hacia abajo)
     * @return int[][] the possible jumps in structure [[5,3,3,3],[row,col,row,col,row,col],[]] - jump from position [5,3] to position [3,3]
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
     * Busca los movimientos posibles para la ficha en la posicion dada
     * @param row int row
     * @param col int col
     * @return int[][] all moves (4 directions
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
     * Chequea si la jugada es posible
     * @param previousRow int
     * @param row int
     * @param col int
     * @return boolean
     */
    private Boolean isTheMovePossible(int previousRow, int row, int col){
        return ((isInRange(row) && isInRange(col) && recognize(row,col).equals("empty")) &&
                 isMovingFwd(previousRow, row));
    }

    /**
     * Chequea si el jump (accion de "comer" una pieza) es posible dados los argumentos
     * @param row int row
     * @param col int col
     * @param vert int direccion vertical (+1 / -1)
     * @param horiz int direccion horizontal (+1 / -1)
     * @return boolean
     */
    private Boolean isTheJumpPossible(int row,int col,int vert,int horiz){
        int tempRow =row+vert;
        int tempCol =col+horiz;
        // FIRST CHECK (hay una pieza del oponente en la casilla contigua?)
        // SECOND CHECK (hay una casilla vacia disponible para el jump?)
        boolean firstRange = isInRange(tempRow) && isInRange(tempCol);
        if(!firstRange){return false;}
        boolean firstCheck = opponentsPiece(tempRow, tempCol);
        tempRow+=vert;
        tempCol+=horiz;
        boolean forward = isMovingFwd(row, tempRow);
        boolean secondRange = isInRange(tempRow) && isInRange(tempCol);
        if(!secondRange){return false;}
        boolean secondCheck = (_localBoard[tempRow][tempCol] == 'e');
        return firstCheck && forward && secondCheck;
    }


    /**
     * Chequea si la pieza esta avanzando con la posible jugada (o si es rey y puede retroceder)
     * @param prevRow fila inicial
     * @param row fila final
     * @return boolean
     */
    private Boolean isMovingFwd(int prevRow, int row){
        return (_isKing || (!_ai && prevRow>row) ||
                (_ai && prevRow<row));
    }

    /**
     * Chequea si el argumento esta en rango del tablero
     * @param a int number
     * @return boolean decision
     */
    private Boolean isInRange(int a){
        return a < 8 && a >= 0;
    }

    /**
     * Reconoce el tipo de ficha que hay en la casilla
     * @param row int
     * @param col int
     * @return String
     */
    private String recognize(int row,int col){
        char reco = _localBoard[row][col];
        return switch (reco) {
            case 'b', 'r' -> "basic";
            case 'B', 'R' -> "king";
            default -> "empty";
        };
    }

    /**
     * Reconoce si la pieza en la posicion dada es del oponente
     * @param r int
     * @param c int
     * @return boolean
     */
    private boolean opponentsPiece(int r,int c){
        return ( (_ai && (_localBoard[r][c] == 'r' || _localBoard[r][c] == 'R')) ||
                 (!_ai && (_localBoard[r][c] == 'b' || _localBoard[r][c] == 'B')));
    }

    /**
     * Reconoce si la pieza en la posicion dada es de la IA(b) o del usuario(r)
     * @param r int
     * @param c int
     * @return boolean
     */
    private boolean ownPiece(int r, int c){
        return ( (_ai && (_localBoard[r][c] == 'b' || _localBoard[r][c] == 'B')) ||
                (!_ai && (_localBoard[r][c] == 'r' || _localBoard[r][c] == 'R')));
    }

    /**
     * Chequea si una pieza llega a la fila final del tablero y se convierte en rey
     * @param piece tipo de pieza (roja o negra)
     * @param row fila donde termina la jugada
     * @return boolean
     */
    public boolean isCrowned(char piece, int row){
        return (piece == 'r' && row == 0) ||
                (piece == 'b' && row == 7);
    }

    /**
     * Devuelve el "nivel o prioridad" de las jugadas posibles (0 = "jugadas normales"; 1,2,3... "longitud" del jump)
     * @return int value
     */
    public int getLevelOfPlays(){
        return _jumpBasedTree;
    }

}
