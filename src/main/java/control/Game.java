package control;


import java.util.Arrays;


/**
 * De momento los metodos de esta clase son de apoyo para poder probar la funcionalidad de las demas clases
 */
public class Game {
    private final AI _ai;
    private char[][] _board;

    /**
     * Genera un tablero inicializando las posiciones invalidas en '\0' y el resto en 'e' (empty), luego
     * inserta en las posiciones indicadas las fichas que desee el usuario
     * @param positions posiciones ocupadas en el tablero (ejemplo: ["0 1 b", "1 2 r", "3 4 r"])
     * @return tablero inicializado
     */
    private char[][] boardGenerator(String[] positions){

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
     * Constructor
     */
    public Game() {
        _ai = new AI();
    }

    /**
     * Usa las posiciones que se deseen inicializar para crear el tablero y buscar la mejor jugada para la ia,
     * luego actualiza el tablero actual con dicha jugada, llama a printBoard() antes de actualizar
     * para poder observar la jugada
     * @param positions posiciones ocupadas en el tablero
     * @return mejor jugada encontrada por la ia
     * @see AI#playAi()
     */
    public int[] playwithBoard(String[] positions){
        _board = boardGenerator(positions);
        _ai.setBoard(_board);
        int[] play = _ai.playAi();
        System.out.println("Tablero antes de la jugada");
        printBoard();
        System.out.println();
        updateBoard(play);
        return play;
    }

    /**
     * Actualiza el tablero actual de acuerdo a la jugada que se le pase
     * @param play jugada
     */
    private void updateBoard(int[] play){
        _ai.makeTheMove(play, _board);
    }

    /**
     * Imprime el tablero fila por fila, crea una copia auxiliar y reemplaza las casillas vacias ('e') o
     * las no validas en el juego ('\0') con (' '), luego cambia las fichas de la ia ('b') con ('□')
     * y las del rival ('r') con ('■'), por ultimo reemplaza algunos caracteres por otros para "simular" mejor un tablero
     */
    public void printBoard(){
        char[][] boardCopy = _board.clone();
        char[] aux;

        for(int i=0; i<8; i++){                   //hay que tomar un camino algo mas  largo que el usual para actualizar
            for(int j=0; j<8; j++){               //la copia del tablero, de lo contrario se actualiza tambien el tablero
                if(boardCopy[i][j] == 'e' || boardCopy[i][j] == '\0'){
                    aux = boardCopy[i].clone();
                    aux[j] = ' ';
                    boardCopy[i] = aux;
                }
                if(boardCopy[i][j] == 'r'){
                    aux = boardCopy[i].clone();
                    aux[j] = '\u25A0';
                    boardCopy[i] = aux;
                }
                if(boardCopy[i][j] == 'b'){
                    aux = boardCopy[i].clone();
                    aux[j] = '\u25A2';
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

