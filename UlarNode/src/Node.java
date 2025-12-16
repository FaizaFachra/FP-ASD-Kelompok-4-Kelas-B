import java.util.ArrayList;
import java.util.List;

public class Node {
    private final int id;
    private boolean isStar;
    private int bonusScore;
    private List<Node> neighbors;

    public Node(int id, boolean isStar) {
        this.id = id;
        this.isStar = isStar;
        this.bonusScore = 0;
        this.neighbors = new ArrayList<>();
    }

    public int getId() { return id; }
    public boolean isStar() { return isStar; }

    public int getBonusScore() { return bonusScore; }
    public void setBonusScore(int score) { this.bonusScore = score; }

    public void addNeighbor(Node node) {
        this.neighbors.add(node);
    }

    public List<Node> getNeighbors() {
        return neighbors;
    }
}