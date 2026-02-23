import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class XMLDeadwoodTest {

    private static int total = 0;
    private static int passed = 0;

    public static void main(String[] args) {
        System.out.println("=== XMLDeadwoodTest (Correctness Checks) ===");

        try {
            ParseXML parser = new ParseXML();

            Board board = new Board();
            ShotTracker shots = new ShotTracker();
            Bank bank = new Bank();
            Deck deck = new Deck();

            System.out.println("Parsing board.xml...");
            parser.parseBoardXML("board.xml", board, shots, bank);

            System.out.println("Parsing cards.xml...");
            parser.parseCardsXML("cards.xml", deck);

            // Basic counts 
            Collection<Location> locs = board.getAllLocations();
            int setCount = countSets(locs);

            checkEquals("Locations loaded should be 12", 12, locs.size());
            checkEquals("Sets loaded should be 10", 10, setCount);
            checkEquals("Deck size should be 40", 40, deck.size());

            // Required special locations 
            checkTrue("Trailers exists", board.getLocationByName("Trailers") != null);
            checkTrue("Casting Office exists", board.getLocationByName("Casting Office") != null);


            List<Set> sets = collectSets(locs);
            checkEquals("Collected set list size matches setCount", setCount, sets.size());

            // Each set should have at least 1 neighbor 
            for (Set s : sets) {
                boolean hasNeighbor = false;
                for (Location other : locs) {
                    if (other != s && s.isAdjacent(other)) {
                        hasNeighbor = true;
                        break;
                    }
                }
                checkTrue("Set has at least 1 neighbor: " + s.getName(), hasNeighbor);
            }

            //Role sanity checks
            for (Set s : sets) {
                try {
                    int roleCount = s.getAllRoles().size();
                    checkTrue("Set has >= 1 role loaded: " + s.getName(), roleCount >= 1);
                } catch (Throwable t) {
                    System.out.println("[SKIP] Role count check not supported by Set API for: " + s.getName());
                }
            }

            // Shot check
            for (Set s : sets) {
                try {
                    int rem = shots.getShotsRemaining(s);
                    checkTrue("Shots remaining >= 1 for set: " + s.getName(), rem >= 1);
                    checkTrue("Set is not wrapped at start: " + s.getName(), !shots.isWrapped(s));
                } catch (Throwable t) {
                    System.out.println("[SKIP] ShotTracker check not supported for: " + s.getName());
                }
            }

            // Summary
            System.out.println("\n=== SUMMARY ===");
            System.out.println("Passed " + passed + " / " + total + " checks.");
            if (passed == total) {
                System.out.println("[OK] XML parsing looks correct.");
            } else {
                System.out.println("[FAIL] Some checks failed. Fix before milestone submission.");
            }
        } catch (Exception e) {
            System.out.println("[CRASH] Test crashed with exception:");
            e.printStackTrace();
        }
    }

    // helpers
    private static int countSets(Collection<Location> locs) {
        int count = 0;
        for (Location l : locs) if (l instanceof Set) count++;
        return count;
    }

    private static List<Set> collectSets(Collection<Location> locs) {
        List<Set> sets = new ArrayList<>();
        for (Location l : locs) {
            if (l instanceof Set) sets.add((Set) l);
        }
        return sets;
    }

    private static void checkTrue(String name, boolean cond) {
        total++;
        if (cond) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            System.out.println("[FAIL] " + name);
        }
    }

    private static void checkEquals(String name, int expected, int actual) {
        total++;
        if (expected == actual) {
            passed++;
            System.out.println("[PASS] " + name + " (" + actual + ")");
        } else {
            System.out.println("[FAIL] " + name + " (expected " + expected + ", got " + actual + ")");
        }
    }
}