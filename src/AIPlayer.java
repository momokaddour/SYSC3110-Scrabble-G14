/**
 * This class is part of the "Scrabble" application.
 *
 * AIPlayer class manages the AIPlayer as well as allows a way for the Game to interact with the hand. Contains storage
 * for the AIPlayer's number, status and current points. Allows for the ability to control the Hand through an exchange
 * method.
 *
 * AIPlayer also contains the primary algorithm that determines what play the AI will make and where the AI will make
 * it. This is done through analyzing the current state of the board.
 *
 * @author Mohamed Kaddour
 * @date 2022.11.22
 */
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;

public class AIPlayer extends Player implements Serializable {

    private Board board;

    private HashMap<String, String> possiblePlays;
    private HashMap<String, Boolean> playableCoordinates;
    private Game game;

    /**
     * Constructor to initialize player points to 0 when player is created. Constructor to initialize
     * hand and pass in an instance of the game as well.
     *
     * @param playerNumber the number assigned to the player
     * @param game of type Game
     */
    public AIPlayer(int playerNumber, Game game) {
        super(playerNumber);

        this.board = new Board();
        this.setAI(true);
        this.possiblePlays = new HashMap<>();
        this.playableCoordinates = new HashMap<String, Boolean>();
        this.game = game;
    }

    /**
     * Analyzes the current state of the board by attempting all possible combinations of up to 3 letters
     * using attemptAMove().
     * @param board Board the current state of the board.
     * */
    public void analyzeBoard(Board board) throws FileNotFoundException {
        this.board.setCells(board.getCells());
        this.board.setSquares(board.getSquares());
        this.board.setTiles(board.getTiles());
        this.board.setFirstPlay(board.isFirstPlay());
        this.board.updateBoardPattern(board.getBoardPattern());

        if (board.boardIsEmpty())
        {
            this.playableCoordinates.put("8H", true);
        }
        else {
            this.playableCoordinates = this.board.getAIPlayableCoordinates();
        }
        ArrayList<Tile> hand = this.getHand().getHand();

        for (int i = 0; i < hand.size(); i++)
        {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(hand.get(i).getLetter());

            for (String s : this.playableCoordinates.keySet())
            {
                boolean direction = this.playableCoordinates.get(s);
                attemptAiMove(stringBuilder.toString(), changeStartingCoordinatesToVertical(s, direction),
                        direction);
            }
        }

        for (int i = 0; i < hand.size(); i++)
        {
            for (int j = 0; j < hand.size(); j++)
            {
                if (i != j) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(hand.get(i).getLetter());
                    stringBuilder.append(hand.get(j).getLetter());

                    for (String s : this.playableCoordinates.keySet()) {
                        boolean direction = this.playableCoordinates.get(s);
                        attemptAiMove(stringBuilder.toString(), changeStartingCoordinatesToVertical(s, direction),
                                direction);
                    }
                }
            }
        }

        for (int i = 0; i < hand.size(); i++)
        {
            for (int j = 0; j < hand.size(); j++)
            {
                for (int k = 0; k < hand.size(); k++) {
                    if ((i != j) && (i != k ) && (j != k)) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(hand.get(i).getLetter());
                        stringBuilder.append(hand.get(j).getLetter());
                        stringBuilder.append(hand.get(k).getLetter());

                        for (String s : this.playableCoordinates.keySet()) {
                            boolean direction = this.playableCoordinates.get(s);
                            attemptAiMove(stringBuilder.toString(), changeStartingCoordinatesToVertical(s, direction),
                                    direction);
                        }
                    }
                }
            }
        }
    }

    /**
     * Attempts a move using the PlayMove object to see if the play itself is actually valid. If the play is
     * valid, then the word and direction are stored.
     * @param currentAttempt the current word to be attempted
     * @param placementAttempt the coordinates to attempt the word at
     * @param placementDirection the direction in which to play at
     * */
    private void attemptAiMove(String currentAttempt, String placementAttempt, boolean placementDirection)
            throws FileNotFoundException {
        ArrayList<Character> currentAttemptArrayList = new ArrayList<>();
        for (Character c : currentAttempt.toCharArray()) { currentAttemptArrayList.add(c); }

        PlayMove playMove = new PlayMove(placementAttempt, mapCharToTile(currentAttemptArrayList),
                this.board, placementDirection);
        if (playMove.placeTile()) {
            if (playMove.checkWord()) {
                possiblePlays.put(currentAttempt, placementAttempt);
            }
        }
    }

    /**
     * Analyzes the hashmap of all possible plays and then picks the word with the higest score to play on the actual
     * board.
     * */
    public void playHighestMove() throws FileNotFoundException {
        ArrayList<String> possiblePlays = new ArrayList<>();
        for (Object s : this.possiblePlays.keySet().toArray()) {possiblePlays.add((String)s);}
        String bestWord = " ";
        int bestWordPoints = 0;
        int currentWordPoints = 0;
        for (int i = 0; i < possiblePlays.size(); i++)
        {
            currentWordPoints = Board.calculateWordScore(possiblePlays.get(i));
            if(currentWordPoints > bestWordPoints)
            {
                bestWordPoints = currentWordPoints;
                bestWord = possiblePlays.get(i);
            }
        }

        for (Character c : bestWord.toCharArray()){this.game.addToRemoveTilesFromHand(c, false);}

        if (bestWord != null) {
            this.game.processCommand(new Command("play", bestWord, this.possiblePlays.get(bestWord)));
        }
        else
        {
            this.game.processCommand(new Command("pass", null, null));
        }

        this.possiblePlays.clear();
    }

    /**
     * Changes the starting coordinates to vertical (switching letter and number) if the play is deemed to be vertical
     * @param coordinates the coordinates to modify
     * @param direction check the direction
     * */
    private String changeStartingCoordinatesToVertical(String coordinates, boolean direction) {

        if (!direction) {
            String num = " ";
            char letter = ' ';
            if (coordinates.length() == 3) {
                num = coordinates.substring(0, 2);
                letter = coordinates.charAt(2);
            } else {
                num = coordinates.substring(0, 1);
                letter = coordinates.charAt(1);
            }

            StringBuilder sb = new StringBuilder();

            sb.append(letter);
            sb.append(num);

            return sb.toString();
        }
        else
        {
            return coordinates;
        }
    }

    /**
     * Helper method to map the chars within the word to be played to the Tiles within the AIPlayer's hand.
     * @param currentAttemptArrayList the char array to be mapped
     * @return ArrayList<Tile>
     * */
    private ArrayList<Tile> mapCharToTile(ArrayList<Character> currentAttemptArrayList)
    {
        ArrayList<Tile> returnArray = new ArrayList<>();

        for (Character c : currentAttemptArrayList)
        {
            for (Tile t : this.getHand().getHand())
            {
                if (t.getLetter().equals(Character.toString(c)))
                {
                    returnArray.add(t);
                    break;
                }
            }
        }

        return returnArray;
    }
}
