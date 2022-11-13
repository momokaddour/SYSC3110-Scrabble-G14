import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class GameCommandPanelController implements ActionListener{

    private ArrayList<Tile> hypotheticalHand;
    private Player player;
    private ArrayList<JToggleButton> tileButtons;
    private ArrayList<Tile> tilesToExchange;
    private Game game;
    private JFrame exchangeFrame;

    public GameCommandPanelController (Player player,
                                       ArrayList<JToggleButton> tileButtons,
                                       ArrayList<Tile> tilesToExchange,
                                       Game game,
                                       JFrame exchangeFrame)
    {
        this.player = player;
        this.tileButtons = tileButtons;
        this.tilesToExchange = tilesToExchange;
        this.game = game;
        this.exchangeFrame = exchangeFrame;
    }

    private void processToggleButtonAction(ActionEvent e) {
        this.hypotheticalHand = this.player.getHand().getHand();

        JToggleButton button = (JToggleButton) e.getSource();
        final String[] split = button.getActionCommand().split(":");
        int col = Integer.parseInt(split[1]);
        if (tileButtons.get(col).getText().equals(button.getText())) {
            if (button.isSelected()) {
                tilesToExchange.add(hypotheticalHand.get(col));
                this.game.addToExchangeTilesFromHand(button.getText().charAt(0));
            } else {
                tilesToExchange.remove(hypotheticalHand.get(col));
                this.game.removeFromExchangeTilesFromHand(button.getText().charAt(0));
            }

        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(GameCommandPanel.PLAY)) {
            try {
                this.game.processCommand(new Command("play", " ", this.game.getStartingCoordinates()));
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
            this.game.nextPlayer();
            /**
             * Play the move set onto board
             */
        } else if (e.getActionCommand().equals(GameCommandPanel.EXCHANGE_BUTTON)) {
            exchangeFrame.setVisible(true);
        } else if (e.getActionCommand().equals(GameCommandPanel.SHUFFLE)) {
            try {
                this.game.processCommand(new Command("shuffle", null, null));
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
            /**
             * Shuffle Hand change order of tiles within hand
             */
        } else if (e.getActionCommand().equals(GameCommandPanel.PASS)) {
            this.game.nextPlayer();
            /**
             * Pass Turn go to next player
             */
        } else if (e.getActionCommand().contains("Letter")) {
            processToggleButtonAction(e);
        } else if (e.getActionCommand().contains(GameCommandPanel.EXCHANGE_COMMAND)) {
            try {
                this.game.processCommand(new Command("exchange",
                        Game.convertCharArrayListToString(this.game.getExchangeTilesFromHand()), null));
                this.exchangeFrame.dispose();
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }

            this.game.clearRemoveFromExchangeTilesFromHand();
        }
    }
}
