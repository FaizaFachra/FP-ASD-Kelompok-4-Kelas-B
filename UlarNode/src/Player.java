import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class Player {
    private String name;
    private Color color;
    private Node currentPosition;
    private int score;
    private int wins;


    private Map<Integer, Integer> shortcutMemory;

    public Player(String name, Color color, Node startPosition) {
        this.name = name;
        this.color = color;
        this.currentPosition = startPosition;
        this.score = 0;
        this.wins = 0;
        this.shortcutMemory = new HashMap<>();
    }

    public String getName() { return name; }
    public Color getColor() { return color; }

    public Node getPosition() { return currentPosition; }
    public void setPosition(Node position) { this.currentPosition = position; }

    public int getScore() { return score; }
    public void addScore(int points) { this.score += points; }
    public void resetScore() { this.score = 0; }

    public int getWins() { return wins; }
    public void addWin() { this.wins++; }
    public void setWins(int wins) { this.wins = wins; }


    public void rememberShortcut(int startId, int endId) {
        if (endId > startId) shortcutMemory.put(endId, startId);
    }

    public int checkShortcutDown(int currentId) {
        if (shortcutMemory.containsKey(currentId)) {
            int destination = shortcutMemory.get(currentId);
            shortcutMemory.remove(currentId);
            return destination;
        }
        return -1;
    }

    @Override
    public String toString() {
        return name;
    }
}