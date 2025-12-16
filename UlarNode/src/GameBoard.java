import java.util.*;
import java.awt.Point;

public class GameBoard {
    private final List<Node> nodes;
    private final int totalNodes = 64;
    private final List<int[]> randomLinks;

    public GameBoard() {
        nodes = new ArrayList<>();
        randomLinks = new ArrayList<>();

        Random rand = new Random();

        for (int i = 0; i < totalNodes; i++) {
            int id = i + 1;
            boolean star = (id % 5 == 0);
            Node node = new Node(id, star);


            if (id != 1 && id != totalNodes && !star) {
                if (rand.nextInt(100) < 30) {
                    int score = (rand.nextInt(5) + 1) * 10;
                    node.setBonusScore(score);
                }
            }

            nodes.add(node);
        }

        buildGraphConnections();
        createRandomLinks();
    }

    private void buildGraphConnections() {
        for (int i = 0; i < totalNodes - 1; i++) {
            Node current = nodes.get(i);
            Node next = nodes.get(i + 1);

            current.addNeighbor(next);
            next.addNeighbor(current);
        }
    }

    private void createRandomLinks() {
        Random rand = new Random();
        int linksCreated = 0;

        while (linksCreated < 5) {
            int id1 = rand.nextInt(totalNodes) + 1;
            int id2 = rand.nextInt(totalNodes) + 1;

            if (id1 != id2 && Math.abs(id1 - id2) > 5) {
                Node n1 = getNodeById(id1);
                Node n2 = getNodeById(id2);

                n1.addNeighbor(n2);
                n2.addNeighbor(n1);

                randomLinks.add(new int[]{id1, id2});
                linksCreated++;
            }
        }
    }

    public Node getHighestNodeAtDistance(Node start, int exactSteps) {
        Map<Node, Integer> distanceMap = new HashMap<>();
        Queue<Node> queue = new LinkedList<>();

        queue.add(start);
        distanceMap.put(start, 0);

        Node bestNode = null;

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            int dist = distanceMap.get(current);

            if (dist == exactSteps) {
                if (bestNode == null || current.getId() > bestNode.getId()) {
                    bestNode = current;
                }
                continue;
            }

            if (dist < exactSteps) {
                for (Node neighbor : current.getNeighbors()) {
                    if (!distanceMap.containsKey(neighbor)) {
                        distanceMap.put(neighbor, dist + 1);
                        queue.add(neighbor);
                    }
                }
            }
        }

        if (bestNode == null) {
            return getNodeById(start.getId() + 1);
        }

        return bestNode;
    }

    public List<Node> getShortestPath(Node start, Node end) {
        if (start == end) return new ArrayList<>();

        Queue<Node> queue = new LinkedList<>();
        Map<Node, Node> parentMap = new HashMap<>();
        Set<Node> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);
        parentMap.put(start, null);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current == end) break;

            for (Node neighbor : current.getNeighbors()) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    parentMap.put(neighbor, current);
                }
            }
        }

        List<Node> path = new LinkedList<>();
        Node curr = end;
        while (curr != null) {
            path.add(0, curr);
            curr = parentMap.get(curr);
        }

        if (!path.isEmpty() && path.get(0) == start) {
            path.remove(0);
        }

        return path;
    }

    public Node getNodeById(int id) {
        if (id < 1) return nodes.get(0);
        if (id > totalNodes) return nodes.get(totalNodes - 1);
        return nodes.get(id - 1);
    }

    public List<Node> getNodes() { return nodes; }
    public int getTotalNodes() { return totalNodes; }
    public List<int[]> getRandomLinks() { return randomLinks; }
}