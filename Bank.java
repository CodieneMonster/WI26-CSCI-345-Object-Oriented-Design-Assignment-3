import java.util.Map;

public class Bank {

    // Upgrade costs by rank
    // 2: $4 / 5cr
    // 3: $10 / 10cr
    // 4: $18 / 15cr
    // 5: $28 / 20cr
    // 6: $40 / 25cr

    private static final Map<Integer, Integer> DOLLAR_COST = Map.of(
        2, 4,
        3, 10,
        4, 18,
        5, 28,
        6, 40
    );

    private static final Map<Integer, Integer> CREDIT_COST = Map.of(
        2, 5,
        3, 10,
        4, 15,
        5, 20,
        6, 25
    );

    public enum PaymentType {
        DOLLARS,
        CREDITS
    }

    public Bank() { }

    // Cost Lookup
    public int getDollarCost(int targetRank) {
        return DOLLAR_COST.getOrDefault(targetRank, -1);
    }

    public int getCreditCost(int targetRank) {
        return CREDIT_COST.getOrDefault(targetRank, -1);
    }

    private int getCost(int targetRank, PaymentType paymentType) {
        return (paymentType == PaymentType.DOLLARS)
                ? getDollarCost(targetRank)
                : getCreditCost(targetRank);
    }

    // Validation
    public boolean isValidTargetRank(int targetRank) {
        return targetRank >= 2 && targetRank <= 6;
    }

    public boolean canUpgrade(Player player, int targetRank, PaymentType paymentType) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null.");
        }

        if (!isValidTargetRank(targetRank)) return false;

        int currentRank = player.getRank();
        if (targetRank <= currentRank) return false;

        int cost = getCost(targetRank, paymentType);
        if (cost < 0) return false;

        if (paymentType == PaymentType.DOLLARS) {
            return player.getDollars() >= cost;
        } else {
            return player.getCredits() >= cost;
        }
    }

    // Upgrade Execution
    public void upgradePlayer(Player player, int targetRank, PaymentType paymentType) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null.");
        }

        if (!isValidTargetRank(targetRank)) {
            throw new IllegalArgumentException("Target rank must be between 2 and 6.");
        }

        int currentRank = player.getRank();
        if (targetRank <= currentRank) {
            throw new IllegalStateException(
                "Target rank must be higher than current rank (" + currentRank + ")."
            );
        }

        int cost = getCost(targetRank, paymentType);
        if (cost < 0) {
            throw new IllegalArgumentException("No cost defined for rank " + targetRank);
        }

        // Check affordability
        if (paymentType == PaymentType.DOLLARS) {
            if (player.getDollars() < cost) {
                throw new IllegalStateException(
                    "Not enough dollars. Need $" + cost + ", but have $" + player.getDollars()
                );
            }
            player.spendDollars(cost);
        } else {
            if (player.getCredits() < cost) {
                throw new IllegalStateException(
                    "Not enough credit. Need " + cost + "cr, have " + player.getCredits() + "cr"
                );
            }
            player.spendCredits(cost);
        }

        // Apply upgrade
        player.setRank(targetRank);
    }

    // Info Text
    public String upgradeInfo(Player player, int targetRank) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null.");
        }

        int currentRank = player.getRank();
        int dCost = getDollarCost(targetRank);
        int cCost = getCreditCost(targetRank);

        return "Upgrade: current rank " + currentRank +
               " -> target rank " + targetRank +
               " | Cost: $" + dCost + " or " + cCost + "cr";
    }
}