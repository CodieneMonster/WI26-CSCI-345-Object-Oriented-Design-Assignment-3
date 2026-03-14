import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class BoardPanel extends JPanel {
    private final Game game;
    private final Image boardImage;

    private final Image cardBackImage;
    private final Image shotImage;


    private final java.util.Set<String> revealedSets;
    private int lastRenderedDay;

    public BoardPanel(Game game) {
        this.game = game;
        this.revealedSets = new java.util.HashSet<>();
        this.lastRenderedDay = game.getCurrentDay();
        // this.boardImage = new ImageIcon("board.jpg").getImage();

        // setPreferredSize(new Dimension( boardImage.getWidth(null), boardImage.getHeight(null)));
    ImageIcon boardIcon = new ImageIcon("board.jpg");
    this.boardImage = boardIcon.getImage();

    ImageIcon cardBackIcon = new ImageIcon("cardback.png");
    this.cardBackImage = cardBackIcon.getImage();


    ImageIcon shotIcon = new ImageIcon("shot.png");
    this.shotImage = shotIcon.getImage();


    setPreferredSize(new Dimension(
            boardIcon.getIconWidth(),
            boardIcon.getIconHeight()
    ));
    }

    private boolean shouldRevealScene(Set set) {
        for (Player p : game.getPlayers()) {
            Location current = p.getLocation();

            // Reveal the set the player is standing in
            if (current.getName().equalsIgnoreCase(set.getName())) {
                return true;
            }

            // Reveal adjacent sets by NAME, not object reference
            for (Location neighbor : current.getNeighbors()) {
                if (neighbor.getName().equalsIgnoreCase(set.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. draw board background first
        g.drawImage(boardImage, 0, 0, this);

        // Reset revealed scenes when a new day starts
        if (game.getCurrentDay() != lastRenderedDay) {
            revealedSets.clear();
            lastRenderedDay = game.getCurrentDay();
        }

        // Permanently reveal sets for any player standing on them or adjacent to them
        for (Player p : game.getPlayers()) {
            Location current = p.getLocation();

            if (current instanceof Set) {
                revealedSets.add(current.getName().toLowerCase());
            }

            for (Location neighbor : current.getNeighbors()) {
                if (neighbor instanceof Set) {
                    revealedSets.add(neighbor.getName().toLowerCase());
                }
            }
        }

        // Draw scene cards or cardbacks
        for (Location loc : game.getBoard().getAllLocations()) {
            if (loc instanceof Set) {
                Set set = (Set) loc;
                Area area = set.getArea();

                if (area == null) continue;

                int cardX = area.getX();
                int cardY = area.getY();
                int cardW = 205;
                int cardH = 115;

                String setName = set.getName().toLowerCase();

                // Wrapped / inactive set -> show cardback
                if (!set.hasActiveScene() || set.getScene() == null) {
                    g.drawImage(cardBackImage, cardX, cardY, cardW, cardH, this);
                }

                // Active + revealed -> show front scene card
                else if (revealedSets.contains(setName)) {
                    SceneCard scene = set.getScene();
                    String imageName = scene.getImageName();

                    if (imageName != null && !imageName.isBlank()) {
                        ImageIcon cardIcon = new ImageIcon(imageName);

                        if (cardIcon.getIconWidth() > 0 && cardIcon.getIconHeight() > 0) {
                            g.drawImage(cardIcon.getImage(), cardX, cardY, cardW, cardH, this);
                        }
                    }
                }

                // else: unrevealed active set -> do nothing
                // board background art stays visible
            }
        }

        // shot counter
        for (Location loc : game.getBoard().getAllLocations()) {
            if (loc instanceof Set) {
                Set set = (Set) loc;

                // only active scenes should show remaining shots
                if (set.hasActiveScene() && set.getScene() != null) {
                    int shotsRemaining = game.getShotTracker().getShotsRemaining(set);
                    List<Area> shotAreas = set.getShotAreas();

                    for (int i = 0; i < shotsRemaining && i < shotAreas.size(); i++) {
                        Area shotArea = shotAreas.get(i);

                        if (shotArea != null) {
                            g.drawImage(
                                shotImage,
                                shotArea.getX(),
                                shotArea.getY(),
                                shotArea.getW(),
                                shotArea.getH(),
                                this
                            );
                        }
                    }
                }
            }
        }


        // 4. draw players
        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            Location loc = p.getLocation();
            Area area = loc.getArea();

            if (area != null) {
                int x = area.getX() + 10 + (i * 20);
                int y = area.getY() + 10;

                String dieFile = p.getColorCode() + p.getRank() + ".png";
                ImageIcon dieIcon = new ImageIcon(dieFile);

                if (dieIcon.getIconWidth() > 0 && dieIcon.getIconHeight() > 0) {
                    g.drawImage(dieIcon.getImage(), x, y, 40, 40, this);
                } 
            }
        }
    }
}