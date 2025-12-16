import java.io.*;
import java.util.*;

public class StatsManager {
    private static final String FILE_NAME = "game_stats.txt";


    private Map<String, Integer> winHistory;


    private Map<String, Integer> highScores;

    public StatsManager() {
        winHistory = new HashMap<>();
        highScores = new HashMap<>();
        loadStats();
    }

    public void recordGame(Player winner, List<Player> allPlayers) {
        winHistory.put(winner.getName(), winHistory.getOrDefault(winner.getName(), 0) + 1);

        for (Player p : allPlayers) {
            String name = p.getName();
            int currentScore = p.getScore();

            if (highScores.containsKey(name)) {
                int oldScore = highScores.get(name);
                if (currentScore > oldScore) {
                    highScores.put(name, currentScore);
                }
            } else {
                if (currentScore > 0) {
                    highScores.put(name, currentScore);
                }
            }
        }

        saveStats();
    }

    public void resetStats() {
        winHistory.clear();
        highScores.clear();
        File f = new File(FILE_NAME);
        if (f.exists()) {
            f.delete();
        }
    }

    public Object[][] getTopScoresData() {
        List<ScoreEntry> sortedScores = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : highScores.entrySet()) {
            sortedScores.add(new ScoreEntry(entry.getKey(), entry.getValue()));
        }

        sortedScores.sort((a, b) -> b.score - a.score);

        int size = Math.min(sortedScores.size(), 10);
        Object[][] data = new Object[size][2];

        for (int i = 0; i < size; i++) {
            data[i][0] = sortedScores.get(i).name;
            data[i][1] = sortedScores.get(i).score;
        }
        return data;
    }

    public Object[][] getTopWinsData() {
        List<Map.Entry<String, Integer>> sortedWins = new ArrayList<>(winHistory.entrySet());

        sortedWins.sort((a, b) -> b.getValue() - a.getValue());

        int size = Math.min(sortedWins.size(), 10);
        Object[][] data = new Object[size][2];

        for (int i = 0; i < size; i++) {
            Map.Entry<String, Integer> entry = sortedWins.get(i);
            data[i][0] = entry.getKey();
            data[i][1] = entry.getValue();
        }
        return data;
    }

    public int getPreviousWins(String playerName) {
        return winHistory.getOrDefault(playerName, 0);
    }

    private void saveStats() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            writer.println("[WINS]");
            for (Map.Entry<String, Integer> entry : winHistory.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue());
            }
            writer.println("[SCORES]");
            for (Map.Entry<String, Integer> entry : highScores.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadStats() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            boolean readingWins = false;
            boolean readingScores = false;

            while ((line = reader.readLine()) != null) {
                if (line.equals("[WINS]")) {
                    readingWins = true; readingScores = false; continue;
                } else if (line.equals("[SCORES]")) {
                    readingWins = false; readingScores = true; continue;
                }

                String[] parts = line.split("=");
                if (parts.length < 2) continue;

                if (readingWins) {
                    winHistory.put(parts[0], Integer.parseInt(parts[1]));
                } else if (readingScores) {
                    highScores.put(parts[0], Integer.parseInt(parts[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ScoreEntry {
        String name;
        int score;
        public ScoreEntry(String n, int s) { name = n; score = s; }
    }
}