import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game {

    private final int numPlayers;
    private final int totalDays;
    private int currentDay;

    private final Board board;
    private final Bank bank;
    private final Deck deck;
    private final ShotTracker shots;

    private final List<Player> players;
    private int activePlayerIndex;

    private boolean gameOver;

    private final Location trailers;
    private final Location castingOffice;

    public Game(int numPlayers) {
        if (numPlayers < 2 || numPlayers > 8) {
            throw new IllegalArgumentException("Deadwood requires 2 to 8 players.");
        }

        this.numPlayers = numPlayers;
        this.totalDays = (numPlayers <= 3) ? 3 : 4;
        this.currentDay = 1;

        this.board = new Board();
        this.bank = new Bank();
        this.deck = new Deck();
        this.shots = new ShotTracker();

        // Load XML
        loadFromXML();

        // Special locations must exist in board.xml
        this.trailers = board.getLocationByName("Trailers");
        this.castingOffice = board.getLocationByName("Casting Office");

        if (trailers == null) throw new IllegalStateException("Trailers not found in board.xml");
        if (castingOffice == null) throw new IllegalStateException("Casting Office not found in board.xml");

        // Create players at trailers
        this.players = new ArrayList<>();
        createPlayersAtTrailers(numPlayers);

        this.activePlayerIndex = 0;
        this.gameOver = false;

        // Deal scenes for day 1
        dealScenesForDay();
    }

    private void loadFromXML() {
        try {
            ParseXML parser = new ParseXML();
            parser.parseBoardXML("board.xml", board, shots, bank);
            parser.parseCardsXML("cards.xml", deck);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load XML: " + e.getMessage(), e);
        }
    }

    private void createPlayersAtTrailers(int n) {
        for (int i = 1; i <= n; i++) {
            Player p = new Player("Player" + i, 1, 0, 0, trailers);
            players.add(p);
        }
    }

    public void dealScenesForDay() {
        // If deck is empty (or low), reload cards from XML
        if (deck.isEmpty()) {
            reloadCardsFromXML();
        }

        deck.shuffle();

        for (Location loc : board.getAllLocations()) {
            if (loc instanceof Set) {
                Set set = (Set) loc;

                // Only sets get scene cards
                SceneCard card = deck.draw();
                set.setScene(card);

                // Reset shots to the initial count from board.xml
                shots.resetToInitial(set);
            }
        }
    }

    public void checkEndOfDay() {
        // Day ends when NO set has an active scene
        for (Location loc : board.getAllLocations()) {
            if (loc instanceof Set) {
                Set s = (Set) loc;
                if (s.hasActiveScene()) {
                    return; // still sets active, day continues
                }
            }
        }

        // If we get here, all sets are wrapped
        System.out.println("=== DAY " + currentDay + " ENDED ===");

        if (currentDay >= totalDays) {
            scoreAndEndGame();
        } else {
            startNextDay();
        }
    }

    // Clears any player role pointers still pointing into a wrapped set
    public void forcePlayersOffRolesInSet(Set set) {
        for (Player p : players) {
            if (p.getLocation() == set && p.isWorking()) {
                p.dropRole();
            }
        }
    }

    private void reloadCardsFromXML() {
        try {
            ParseXML parser = new ParseXML();

            // Clear deck before reloading
            deck.clear();

            parser.parseCardsXML("cards.xml", deck);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reload cards.xml: " + e.getMessage(), e);
        }
    }

    // Getters

    public int getNumPlayers() {
        return numPlayers;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public Board getBoard() {
        return board;
    }

    public Bank getBank() {
        return bank;
    }

    public Deck getDeck() {
        return deck;
    }

    public ShotTracker getShotTracker() {
        return shots;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Player getActivePlayer() {
        return players.get(activePlayerIndex);
    }

    public int getActivePlayerIndex() {
        return activePlayerIndex;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Location getTrailers() {
        return trailers;
    }

    public Location getCastingOffice() {
        return castingOffice;
    }

    // Turn / Day Control

    public void startNextDay() {
        if (currentDay >= totalDays) {
            throw new IllegalStateException("Cannot start next day. Game is already at final day.");
        }

        currentDay++;
        System.out.println("=== STARTING DAY " + currentDay + " ===");

        // Reset players to trailers + clear roles
        for (Player p : players) {
            p.resetForNewDay(trailers);
        }

        // Clear all sets (wrap + remove roles + remove currentScene)
        for (Location loc : board.getAllLocations()) {
            if (loc instanceof Set) {
                ((Set) loc).wrapScene();
            }
        }

        dealScenesForDay();
    }

    public void endTurn() {
        if (gameOver) return;
        activePlayerIndex = (activePlayerIndex + 1) % players.size();
    }

    public void endGameNow() {
        gameOver = true;
    }

    public void scoreAndEndGame() {
        System.out.println("=== GAME OVER: FINAL SCORES ===");

        int best = Integer.MIN_VALUE;
        Player winner = null;

        for (Player p : players) {
            int score = p.getDollars() + p.getCredits() + (5 * p.getRank());

            System.out.println(
                    p.getName()
                            + " => $"
                            + p.getDollars()
                            + ", "
                            + p.getCredits()
                            + "cr, rank "
                            + p.getRank()
                            + " | SCORE="
                            + score
            );

            if (score > best) {
                best = score;
                winner = p;
            }
        }

        if (winner != null) {
            System.out.println("WINNER: " + winner.getName() + " with " + best + " points!");
        }

        gameOver = true;
    }
}