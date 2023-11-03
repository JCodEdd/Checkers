package control;


import java.util.Arrays;

public class Play {
    public static void main(String[] args) {

        String[] test = { "1 0 b","1 2 b","0 5 b","2 1 r", "1 6 r", "4 3 r", "3 6 r", "5 6 r" 
    };
        Game game = new Game();
        System.out.println(Arrays.toString(game.playwithBoard(test)));
        System.out.println();
        System.out.println("Tablero despues de la jugada");
        game.printBoard();

    }
}
