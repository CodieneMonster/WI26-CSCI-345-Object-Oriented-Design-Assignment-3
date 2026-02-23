public class OffCardRole extends Role {
    private final int successDollars;
    private final int successCredits;
    private final int failDollars;
    private final int failCredits;
    private final String line;

    public OffCardRole(String name, int rankRequired,
                       int successDollars, int successCredits,
                       int failDollars, int failCredits, String line) {
        
        super(name, rankRequired);
        if (successDollars < 0 || successCredits < 0 || failDollars < 0 || failCredits < 0) {
            throw new IllegalArgumentException("Payout values must be non-negative.");
        }

        this.successDollars = successDollars;
        this.successCredits = successCredits;
        this.failDollars = failDollars;
        this.failCredits = failCredits;
        this.line = (line == null) ? "" : line.trim();
    }

    public OffCardRole(String name, int rankRequired,
                       int successDollars, int successCredits,
                       int failDollars, int failCredits) {
        this(name, rankRequired, successDollars, successCredits, failDollars, failCredits, "");
    }

    public int getSuccessDollars() { return successDollars; }
    public int getSuccessCredits() { return successCredits; }
    public int getFailDollars() { return failDollars; }
    public int getFailCredits() { return failCredits; }
    public String getLine() { return line; }

    public void applySuccessPayout(Player p) {
        if (p == null) throw new IllegalArgumentException("Player cannot be null.");
        if (successDollars > 0) p.addDollars(successDollars);
        if (successCredits > 0) p.addCredits(successCredits);
    }

    public void applyFailPayout(Player p) {
        if (p == null) throw new IllegalArgumentException("Player cannot be null.");
        if (failDollars > 0) p.addDollars(failDollars);
        if (failCredits > 0) p.addCredits(failCredits);
    }

    @Override
    public String toString() {
        String base = super.toString();
        String pay = String.format(" [OffCard: success +$%d +%dcr, fail +$%d +%dcr]",
                successDollars, successCredits, failDollars, failCredits);
        if (line.isEmpty()) return base + pay;
        return base + pay + " | line: \"" + line + "\"";
    }
}
