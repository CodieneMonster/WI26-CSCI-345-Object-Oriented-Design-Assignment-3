import java.util.Locale;

public class GameController {
    private final Game game;

    public GameController(Game game) {
        if (game == null) {throw new IllegalArgumentException("Game cannot be null.");}
        this.game = game;
    }

    public boolean handleCommand(String inputLine) {
        if (inputLine == null) return true;

        String line = inputLine.trim();
        if (line.isEmpty()) return true;

        String[] parts = line.split("\\s+");
        String cmd = parts[0].toLowerCase(Locale.ROOT);

        try {
            boolean consumedTurn = false;

            switch (cmd) {
                case "help":
                    printHelp();
                    return true;

                case "status":
                    cmdStatus();
                    return true;

                case "who":
                    cmdWho();
                    return true;

                case "where":
                    cmdWhere(line);
                    return true;

                case "board":
                    cmdBoard();
                    return true;

                case "move":
                    consumedTurn = cmdMove(line);
                    break;

                case "work":
                    consumedTurn = cmdWork(line);
                    break;

                case "act":
                    consumedTurn = cmdAct();
                    break;

                case "rehearse":
                    consumedTurn = cmdRehearse();
                    break;

                case "upgrade":
                    consumedTurn = cmdUpgrade(parts);
                    break;

                case "end":
                    cmdEnd();       // passing turn
                    return true;

                case "endgame":
                    cmdEndGame();
                    return false;

                case "endday": // helper for testing
                    game.startNextDay();
                    System.out.println("Day advanced. Current day: " + game.getCurrentDay());
                    return true;

                default:
                    System.out.println("Unkown command: " + cmd + "(type 'help' for list of commands)");
                    return true;
            }

            // If we got here, it was one of the "turn action" commands.
            if (consumedTurn) {
                Player current = game.getActivePlayer();
                game.endTurn();
                Player next = game.getActivePlayer();
                System.out.println(current.getName() + " ended their turn.");
                System.out.println("Next active player: " + next.getName());
            }

            return true;
        } catch (Exception e) {
            System.out.println("Command failed: " + e.getMessage());
            return true;
        }
    }


    // commands
    private void cmdWho() {
        Player p = game.getActivePlayer();
        String working = p.isWorking() ? "working" + p.getRole().getName() : "not working a role";
                System.out.println("Active player: " + p.getName()
                + " (rank " + p.getRank()
                + ", $" + p.getDollars()
                + ", " + p.getCredits() + "cr"
                + ", rehearsal=" + p.getRehearsalChips()
                + ") | " + working);
    }

    private void cmdStatus() {
        System.out.println("Day " + game.getCurrentDay() + " / " + game.getTotalDays());

        int active = 0;
        for (Location loc : game.getBoard().getAllLocations()) {
            if (loc instanceof Set) {
                if (((Set) loc).hasActiveScene()) active++;
            }
        }
        System.out.println("Active sets remaining: " + active);
    }

    private void cmdWhere(String line) {
        Player p = game.getActivePlayer();
        Location loc = p.getLocation();

        System.out.println("Active player location:" + loc.getName());
        if (loc instanceof Set) {
            Set set = (Set) loc;
            System.out.println(set.rolesSummary(p));
            
            int shotsRemaining = game.getShotTracker().getShotsRemaining(set);
            System.out.println("Shots remaining: " + shotsRemaining);

            if (!set.hasActiveScene()) {
                System.out.println("(This set is wrapped / no active scene.)");
            }
        }

        System.out.println("All player locations:");
        game.getBoard().printAllPlayerLocations(game.getPlayers(), p);
    }

    private boolean cmdMove(String fullLine) {
        String destName = readRemainder(fullLine, "move");

        if (destName.isBlank()) {
            System.out.println("Usage: move <location>");
            return false; // did not spend turn
        }

        Player p = game.getActivePlayer();
        if (p.isWorking()) {
            throw new IllegalStateException("Cannot move while working on a role.");
        }

        Location from = p.getLocation();
        game.getBoard().movePlayer(p, destName);
        Location to = p.getLocation();

        System.out.println("Moved from " + from.getName() + " to " + to.getName());
        return true; // spent turn
    }

    private void cmdBoard() {
        Player p = game.getActivePlayer();
        Location loc = p.getLocation();
        System.out.println("You are at: " + loc.getName());
        System.out.println("Adjacent locations:");
        for (Location nb : loc.getNeighbors()) {
            System.out.println(" - " + nb.getName());
        }
    }

    private boolean cmdWork(String fullLine) {
        String roleName = readRemainder(fullLine, "work");
        if (roleName.isBlank()) {
            System.out.println("Usage: work <role name>");
            return false;
        }

        Player p = game.getActivePlayer();
        Location loc = p.getLocation();
        if (!(loc instanceof Set)) {
            throw new IllegalStateException("You must be at a set to work a role.");
        }

        Set set = (Set) loc;
        Role role = set.assignRoleToPlayer(p, roleName);
        System.out.println("You are now working: " + role.getName() + "(rank " + role.getRankRequired() + ")");
        return true;
    }

    private boolean  cmdAct() {
        Player p = game.getActivePlayer();
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

        int roll = (int)(Math.random() * 6) + 1;
        int total = roll + p.getRehearsalChips();

        System.out.println("Rolled: " + roll + " + rehearsal(" + p.getRehearsalChips()
                + ") = " + total + " vs budget " + budget);

        Role role = p.getRole();
        if (total >= budget) {
            System.out.println("SUCCESS!");

            // payout using Role object's values
            if (role instanceof OnCardRole) {
                OnCardRole r = (OnCardRole) role;
                if (r.getSuccessDollars() > 0) p.addDollars(r.getSuccessDollars());
                if (r.getSuccessCredits() > 0) p.addCredits(r.getSuccessCredits());
            } else if (role instanceof OffCardRole) {
                OffCardRole r = (OffCardRole) role;
                if (r.getSuccessDollars() > 0) p.addDollars(r.getSuccessDollars());
                if (r.getSuccessCredits() > 0) p.addCredits(r.getSuccessCredits());
            }
            System.out.println("Now: $" + p.getDollars() + ", " + p.getCredits() + "cr");

            // remove shot
            game.getShotTracker().removeShot(set);
            System.out.println("Shots remaining: " + game.getShotTracker().getShotsRemaining(set));

            // wrap check
            if (game.getShotTracker().isWrapped(set)) {
                System.out.println("SCENE WRAPPED at " + set.getName() + "!");
                set.wrapScene();
                game.forcePlayersOffRolesInSet(set);
                game.checkEndOfDay();
            }
        } else {
            System.out.println("FAIL.");

            // fail payout
            if (role instanceof OnCardRole) {
                OnCardRole r = (OnCardRole) role;
                if (r.getFailDollars() > 0) p.addDollars(r.getFailDollars());
                if (r.getFailCredits() > 0) p.addCredits(r.getFailCredits());
            } else if (role instanceof OffCardRole) {
                OffCardRole r = (OffCardRole) role;
                if (r.getFailDollars() > 0) p.addDollars(r.getFailDollars());
                if (r.getFailCredits() > 0) p.addCredits(r.getFailCredits());
            }
            System.out.println("Now: $" + p.getDollars() + ", " + p.getCredits() + "cr");
        }
        return true;
    }

    private boolean cmdRehearse() {
        Player p = game.getActivePlayer();

        if (!p.isWorking()) {
            throw new IllegalStateException("You must be working on a role to rehearse.");
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
        System.out.println("Rehearsal successful. You now have " + p.getRehearsalChips() + " rehearsal chips.");
        return true;
    }

    private boolean cmdUpgrade(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Usage: upgrade <targetRank> <dollars|credits>");
            return false;
        }

        Player p = game.getActivePlayer();
        if (!p.getLocation().getName().equalsIgnoreCase("Casting Office")) {
            throw new IllegalStateException("You must be at the Casting Office to upgrade your rank.");
        }

        int targetRank;
        try {
            targetRank = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Target rank must be a number.");
        }

        Bank.PaymentType payType = parsePaymentType(parts[2]);
        System.out.println(game.getBank().upgradeInfo(p, targetRank));
        int beforeRank = p.getRank();

        game.getBank().upgradePlayer(p, targetRank, payType);
        System.out.println("Upgrade successful: rank " + beforeRank + " -> " + p.getRank()
                + " | now $" + p.getDollars() + ", " + p.getCredits() + "cr");
        return true;
    }

      private void cmdEnd() {
        Player current = game.getActivePlayer();
        game.endTurn();
        Player next = game.getActivePlayer();

        System.out.println(current.getName() + " ended their turn.");
        System.out.println("Next active player: " + next.getName());
    }

    private void cmdEndGame() {
        game.endGameNow();
        System.out.println("Game ended (endgame).");
    }

    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  status");
        System.out.println("  who");
        System.out.println("  where");
        System.out.println("  board");
        System.out.println("  move <location>");
        System.out.println("  work <roleName>");
        System.out.println("  act");
        System.out.println("  rehearse");
        System.out.println("  upgrade <targetRank> <dollars|credits>");
        System.out.println("  endday");
        System.out.println("  end");
        System.out.println("  endgame");
    }

    // Helpers
    private static String readRemainder(String fullLine, String commandWord) {
        String s = fullLine.trim();
        if (s.length() <= commandWord.length()) return "";
        String remainder = s.substring(commandWord.length()).trim();
        return remainder;
    }

    private static Bank.PaymentType parsePaymentType(String s) {
        String key = s.trim().toLowerCase(Locale.ROOT);
        if (key.equals("dollars") || key.equals("dollar") || key.equals("$")) {
            return Bank.PaymentType.DOLLARS;
        }
        if (key.equals("credits") || key.equals("credit") || key.equals("cr")) {
            return Bank.PaymentType.CREDITS;
        }
        throw new IllegalArgumentException("Payment type must be 'dollars' or 'credits'.");
    }
}