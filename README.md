# Deadwood Game

CSCI 345 – Object-Oriented Design  
Assignment 2

## Overview

This project is a Java implementation of the **Deadwood** board game.  
It includes both:

- a **console/text-based version**
- a **GUI version built with Java Swing**

The game loads board data, scene cards, roles, shots, and areas from XML files, then builds the game world dynamically at runtime. Players can move around the board, take roles, rehearse, act, upgrade their rank, and compete for the highest final score.


## Key classes structure 

│
├── Core Game
│   ├── Game
│   ├── Player
│   ├── Board
│   └── Deck
│
├── Roles
│   ├── Role
│   ├── OnCardRole
│   └── OffCardRole
│
├── GUI
│   ├── DeadwoodGUI
│   ├── DeadwoodFrame
│   └── BoardPanel
│
└── XML
    └── ParseXML
## Project Structure

### Core Game Classes

- `Deadwood.java` → Main console program (runs the text-based game)
- `Game.java` → Stores game state and controls turn progression
- `GameController.java` → Command interpreter for console gameplay
- `ParseXML.java` → Loads board and scene data from XML files
- `Board.java` → Represents the game board and all locations
- `Location.java` / `Set.java` → Models board locations and film sets
- `Player.java` → Stores player state (rank, money, credits, role, location)
- `Role.java` / `OnCardRole.java` / `OffCardRole.java` → Role hierarchy for on-card and off-card roles
- `SceneCard.java` → Represents scene card information
- `Deck.java` → Manages the scene card deck
- `ShotTracker.java` → Tracks shot counters on sets
- `Bank.java` → Handles player rank upgrades and payments

---
### GUI Classes

- `DeadwoodGUI.java` → Entry point for the graphical version of the game
- `DeadwoodFrame.java` → Main game window
- `BoardPanel.java` → Renders the game board, cards, and player pieces
- `BoardLayersListener.java` → Handles mouse interaction with the board

---

### Board / Layout Support

- `Area.java` → Stores coordinate and area data used to position elements on the board

---
## Features

- Supports **2 to 8 players**
- Loads board and card data from:
  - `board.xml`
  - `cards.xml`
- Includes a **text command interface**
- Includes a **graphical interface**
- Tracks:
  - player rank
  - dollars
  - credits
  - rehearsal chips
  - current location
  - active roles
  - current day
- Supports:
  - movement between adjacent locations
  - taking on-card and off-card roles
  - acting and rehearsing
  - upgrading at the Casting Office
  - scene wrapping
  - end-of-day progression
  - final scoring and winner display

## Game Rules Implemented

- Players begin in the **Trailers**
- Rank upgrades happen at the **Casting Office**
- A player cannot move if they are currently working a role
- A player may only work a role if:
  - they are at that set
  - the scene is still active
  - the role is open
  - their rank is high enough
- Acting uses a die roll plus rehearsal chips
- Rehearsing increases rehearsal chips up to `budget - 1`
- When all shots are removed from a set, the scene wraps
- The game ends after:
  - **3 days** for 2–3 players
  - **4 days** for 4–8 players
- Final score is calculated as:

```text
score = dollars + credits + (5 × rank)


How to Compile:
-Open a terminal inside the project directory and run:
  javac *.java

How to Run the Full Game VIA TEXT JOURNEY:

The main entry point is:
java Deadwood <numPlayers>

Example:
java Deadwood 2

Expected startup output:
Deadwood started with 2 players.
Type 'help' for commands.
>

Available commands inside the game prompt:
help
status
who
where
board
move <location>
work <roleName>
rehearse
upgrade <targetRank> <dollars|credits>
end
endday
endgame

Example session:
> who
> status
> where
> move Ranch
> work Sheriff
> rehearse
> end



How to Run the Full Game VIA GUI:

The main entry point is:
Example:
javac *.java
java DeadwoodGUI
after that you can play the game 
Expected startup output: based on number of players chosen



Design Notes:
-GameController separates command parsing from game logic (Single Responsibility Principle).
-Game owns all major systems (Board, Bank, Deck, ShotTracker, Players).
-ParseXML builds the world dynamically from XML instead of hardcoding data.
-Roles use inheritance (Role → OnCardRole / OffCardRole) to model different behaviors.

This structure improves maintainability, testing, and extensibility.