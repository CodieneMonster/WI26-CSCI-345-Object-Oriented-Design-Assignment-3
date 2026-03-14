import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class DeadwoodFrame extends JFrame {
    private final Game game;

    public DeadwoodFrame(int numPlayers) {
        super("Deadwood");

        this.game = new Game(numPlayers);

        setLayout(new BorderLayout());

        add(new JLabel("Deadwood GUI loading..."), BorderLayout.EAST);
        add(new BoardPanel(game), BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}