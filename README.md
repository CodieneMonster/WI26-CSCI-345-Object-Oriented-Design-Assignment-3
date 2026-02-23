Deadwood Game – Assignment 2
CSCI 345 – Object Oriented Design

Overview:
This project implements a console-based version of the Deadwood board game.
The system loads game data from XML files, builds the game world (board, scenes, roles, players), and allows users to interact with the game through text commands.

The main gameplay loop is controlled through Deadwood.java, which uses:
-Game — core game state and logic
-GameController — command interpreter
-ParseXML — XML loader for board and cards

Project Structure:
Key classes:

Deadwood.java          → Main program (runs the game)
Game.java              → Game state and turn logic
GameController.java    → Command parser
ParseXML.java          → XML loader
Board.java             → Game board
Location.java / Set.java
Player.java
Role.java / OnCardRole.java / OffCardRole.java
SceneCard.java
Deck.java
ShotTracker.java
Bank.java

How to Compile:
-Open a terminal inside the project directory and run:
  javac *.java

How to Run the Full Game:

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

Design Notes:
-GameController separates command parsing from game logic (Single Responsibility Principle).
-Game owns all major systems (Board, Bank, Deck, ShotTracker, Players).
-ParseXML builds the world dynamically from XML instead of hardcoding data.
-Roles use inheritance (Role → OnCardRole / OffCardRole) to model different behaviors.

This structure improves maintainability, testing, and extensibility.