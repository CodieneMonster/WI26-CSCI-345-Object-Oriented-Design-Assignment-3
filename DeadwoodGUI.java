import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class DeadwoodGUI extends JFrame {

    private final Game game;
    private final BoardPanel boardPanel;
    private final JLabel infoLabel;
    private final JButton endTurnButton;
    private final JButton moveButton;
    private final JButton workButton;
    private final JButton actButton;
    private final JButton rehearseButton;
    private final JButton upgradeButton;

    public DeadwoodGUI(int numPlayers) {
        super("Deadwood");

        this.game = new Game(numPlayers);
        this.boardPanel = new BoardPanel(game);
        this.infoLabel = new JLabel();
        this.endTurnButton = new JButton("End Turn");

        this.moveButton = new JButton("Move");
        this.workButton = new JButton("Work");
        this.actButton = new JButton("Act");
        this.rehearseButton = new JButton("Rehearse");
        this.upgradeButton = new JButton("Upgrade");


        setLayout(new BorderLayout());
        add(boardPanel, BorderLayout.CENTER);
        add(infoLabel, BorderLayout.EAST);
        add(endTurnButton, BorderLayout.SOUTH);

        endTurnButton.addActionListener(e -> {
            game.endTurn();
            refreshGUI();
        });

        moveButton.addActionListener(e -> {
            Player p = game.getActivePlayer();
            Location current = p.getLocation();
            List<Location> neighbors = new java.util.ArrayList<>(current.getNeighbors());

            String[] options = new String[neighbors.size()];
            for (int i = 0; i < neighbors.size(); i++) {
                options[i] = neighbors.get(i).getName();
            }

            String choice = (String) JOptionPane.showInputDialog(
                    this,
                    "Choose a location:",
                    "Move",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options.length > 0 ? options[0] : null
            );

            if (choice != null) {
                try {
                    game.getBoard().movePlayer(p, choice);
                    refreshGUI();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            }
        });

        workButton.addActionListener(e -> {
            Player p = game.getActivePlayer();
            Location loc = p.getLocation();

            if (!(loc instanceof Set)) {
                JOptionPane.showMessageDialog(this, "You must be at a set to work a role.");
                return;
            }

            Set set = (Set) loc;

            java.util.List<Role> roles = set.getAllRoles();
            java.util.List<String> openRoles = new java.util.ArrayList<>();

            for (Role r : roles) {
                if (r.isAvailable() && r.isPlayerQualified(p) && set.hasActiveScene()) {
                    openRoles.add(r.getName());
                }
            }

            if (openRoles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No available roles you qualify for here.");
                return;
            }

            String choice = (String) JOptionPane.showInputDialog(
                    this,
                    "Choose a role:",
                    "Work",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    openRoles.toArray(),
                    openRoles.get(0)
            );

            if (choice != null) {
                try {
                    set.assignRoleToPlayer(p, choice);
                    game.endTurn();
                    refreshGUI();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            }
        });

       actButton.addActionListener(e -> {
            Player p = game.getActivePlayer();

            try {
                if (!p.isWorking()) {
                    throw new IllegalStateException("You must be working on a role to act.");
                }

                if (!(p.getLocation() instanceof Set)) {
                    throw new IllegalStateException("You must be at a set to act.");
                }

                Set set = (Set) p.getLocation();
                if (!set.hasActiveScene() || set.getScene() == null) {
                    throw new IllegalStateException("No active scene here.");
                }

                SceneCard scene = set.getScene();
                int budget = scene.getBudget();

                int roll = (int) (Math.random() * 6) + 1;
                int total = roll + p.getRehearsalChips();

                Role role = p.getRole();
                String message;

                if (total >= budget) {
                    if (role instanceof OnCardRole) {
                        OnCardRole r = (OnCardRole) role;
                        if (r.getSuccessDollars() > 0) p.addDollars(r.getSuccessDollars());
                        if (r.getSuccessCredits() > 0) p.addCredits(r.getSuccessCredits());
                    } else if (role instanceof OffCardRole) {
                        OffCardRole r = (OffCardRole) role;
                        if (r.getSuccessDollars() > 0) p.addDollars(r.getSuccessDollars());
                        if (r.getSuccessCredits() > 0) p.addCredits(r.getSuccessCredits());
                    }

                    game.getShotTracker().removeShot(set);

                    message = "SUCCESS!\nRolled: " + roll +
                            "\nTotal: " + total +
                            "\nShots remaining: " + game.getShotTracker().getShotsRemaining(set);

                    if (game.getShotTracker().isWrapped(set)) {
                        set.wrapScene();
                        game.forcePlayersOffRolesInSet(set);
                        game.checkEndOfDay();
                        message += "\nScene wrapped!";
                    }
                } else {
                    if (role instanceof OnCardRole) {
                        OnCardRole r = (OnCardRole) role;
                        if (r.getFailDollars() > 0) p.addDollars(r.getFailDollars());
                        if (r.getFailCredits() > 0) p.addCredits(r.getFailCredits());
                    } else if (role instanceof OffCardRole) {
                        OffCardRole r = (OffCardRole) role;
                        if (r.getFailDollars() > 0) p.addDollars(r.getFailDollars());
                        if (r.getFailCredits() > 0) p.addCredits(r.getFailCredits());
                    }

                    message = "FAIL.\nRolled: " + roll + "\nTotal: " + total;
                }

                JOptionPane.showMessageDialog(this, message);
                game.endTurn();
                refreshGUI();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });



        rehearseButton.addActionListener(e -> {
            Player p = game.getActivePlayer();

            try {
                if (!p.isWorking()) {
                    throw new IllegalStateException("You must be working on a role to rehearse.");
                }

                if (!(p.getLocation() instanceof Set)) {
                    throw new IllegalStateException("You must be at a set to rehearse.");
                }

                Set set = (Set) p.getLocation();
                SceneCard scene = set.getScene();

                if (scene == null) {
                    throw new IllegalStateException("No active scene here.");
                }

                int budget = scene.getBudget();
                if (p.getRehearsalChips() >= budget - 1) {
                    throw new IllegalStateException("Cannot rehearse anymore. Max rehearsal chips reached.");
                }

                p.rehearse();
                JOptionPane.showMessageDialog(this,
                        "Rehearsal successful.\nChips: " + p.getRehearsalChips());

                game.endTurn();
                refreshGUI();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        upgradeButton.addActionListener(e -> {
            Player p = game.getActivePlayer();

            try {
                if (!p.getLocation().getName().equalsIgnoreCase("Casting Office")) {
                    throw new IllegalStateException("You must be at the Casting Office to upgrade.");
                }

                String[] rankOptions = {"2", "3", "4", "5", "6"};
                String chosenRank = (String) JOptionPane.showInputDialog(
                        this,
                        "Choose target rank:",
                        "Upgrade Rank",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        rankOptions,
                        rankOptions[0]
                );

                if (chosenRank == null) {
                    return;
                }

                int targetRank = Integer.parseInt(chosenRank);

                String[] payOptions = {"dollars", "credits"};
                String chosenPayType = (String) JOptionPane.showInputDialog(
                        this,
                        "Choose payment type:",
                        "Upgrade Payment",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        payOptions,
                        payOptions[0]
                );

                if (chosenPayType == null) {
                    return;
                }

                Bank.PaymentType payType;
                if (chosenPayType.equalsIgnoreCase("dollars")) {
                    payType = Bank.PaymentType.DOLLARS;
                } else {
                    payType = Bank.PaymentType.CREDITS;
                }

                String info = game.getBank().upgradeInfo(p, targetRank);
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        info + "\nProceed?",
                        "Confirm Upgrade",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                game.getBank().upgradePlayer(p, targetRank, payType);

                JOptionPane.showMessageDialog(
                        this,
                        "Upgrade successful!\nNew rank: " + p.getRank()
                        + "\nDollars: $" + p.getDollars()
                        + "\nCredits: " + p.getCredits() + "cr"
                );

                refreshGUI();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });


        updateInfoPanel();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(moveButton);
        buttonPanel.add(workButton);
        buttonPanel.add(actButton);
        buttonPanel.add(rehearseButton);
        buttonPanel.add(upgradeButton);
        buttonPanel.add(endTurnButton);

        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void updateInfoPanel() {
        Player p = game.getActivePlayer();

        String text = "<html>"
                + "<h2>Deadwood</h2>"
                + "Day: " + game.getCurrentDay() + " / " + game.getTotalDays() + "<br><br>"
                + "<h2>Current Player</h2>"
                + "Name: " + p.getName() + "<br>"
                + "Rank: " + p.getRank() + "<br>"
                + "Dollars: $" + p.getDollars() + "<br>"
                + "Credits: " + p.getCredits() + "<br>"
                + "Rehearsal: " + p.getRehearsalChips() + "<br>"
                + "Location: " + p.getLocation().getName() + "<br>"
                + "</html>";

        infoLabel.setText(text);
        infoLabel.setPreferredSize(new Dimension(220, 200));
    }

    public void refreshGUI() {
        updateInfoPanel();
        boardPanel.repaint();
        checkForWinnerPopup();
    }


    private static void showStartDialog() {
        String[] options = {"2", "3", "4", "5", "6", "7", "8"};

        String choice = (String) JOptionPane.showInputDialog(
                null,
                "How many players?",
                "Start Deadwood",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == null) {
            return;
        }

        int numPlayers = Integer.parseInt(choice);
        new DeadwoodGUI(numPlayers);
    }

    private void checkForWinnerPopup() {
        if (!game.isGameOver()) {
            return;
        }

        Player winner = null;
        int bestScore = Integer.MIN_VALUE;

        StringBuilder sb = new StringBuilder();
        sb.append("Final Scores:\n\n");

        for (Player p : game.getPlayers()) {
            int score = p.getDollars() + p.getCredits() + (5 * p.getRank());

            sb.append(p.getName())
            .append(" -> Score: ")
            .append(score)
            .append(" ($")
            .append(p.getDollars())
            .append(", ")
            .append(p.getCredits())
            .append("cr, rank ")
            .append(p.getRank())
            .append(")\n");

            if (score > bestScore) {
                bestScore = score;
                winner = p;
            }
        }

        if (winner != null) {
            sb.append("\nWinner: ")
            .append(winner.getName())
            .append(" with ")
            .append(bestScore)
            .append(" points!");
        }

        JOptionPane.showMessageDialog(
                this,
                sb.toString(),
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE
        );

        dispose();
        showStartDialog();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String[] options = {"2", "3", "4", "5", "6", "7", "8"};

            String choice = (String) JOptionPane.showInputDialog(
                    null,
                    "How many players?",
                    "Start Deadwood",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == null) {
                return; // user closed the window
            }

            int numPlayers = Integer.parseInt(choice);
            new DeadwoodGUI(numPlayers);
        });
    }
}
