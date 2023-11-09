package control;



public class Play {
    public static void main(String[] args) {

        Game game = new Game();

    //    String[] sampleBoardState = { "1 0 b","1 2 b","0 5 b","2 1 r", "1 6 r", "4 3 r", "3 6 r", "5 6 r"  };
    //    game.playWithBoard(sampleBoardState);
        
        game.playWithRandomBoard();

    }
}
