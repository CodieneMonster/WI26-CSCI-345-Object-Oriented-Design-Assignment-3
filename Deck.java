import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Deck {

    private final List<SceneCard> cards;
    private final Random rng;

    public Deck() {
        this.cards = new ArrayList<>();
        this.rng = new Random();
    }

    // Add Cards
    public void add(SceneCard card) {
        if (card == null) {
            throw new IllegalArgumentException("Cannot add null SceneCard.");
        }
        cards.add(card);
    }

    public void addAll(List<SceneCard> sceneCards) {
        if (sceneCards == null) return;

        for (SceneCard c : sceneCards) {
            add(c);
        }
    }

    // Basic Info
    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    // Shuffle
    public void shuffle() {
        Collections.shuffle(cards, rng);
    }

    // Draw / Peek
    public SceneCard draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Deck is empty. Cannot draw.");
        }
        return cards.remove(0);
    }

    public SceneCard peek() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Deck is empty. Cannot peek.");
        }
        return cards.get(0);
    }

    // Utility
    public void clear() {
        cards.clear();
    }

    public void printDeck() {
        System.out.println("Deck (" + cards.size() + " cards):");
        for (SceneCard c : cards) {
            System.out.println(" - " + c.getTitle() + " (budget " + c.getBudget() + ")");
        }
    }
}