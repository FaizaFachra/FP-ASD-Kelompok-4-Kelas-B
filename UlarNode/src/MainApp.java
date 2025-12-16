import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class MainApp extends JFrame {

    private JPanel mainContainer;
    private CardLayout cardLayout;

    private GameBoard board;
    private GamePanel gamePanel;
    private DicePanel dicePanel;

    private JLabel statusLabel;
    private JLabel diceResultLabel;
    private JLabel turnLabel;
    private JButton rollButton;
    private Random random;

    private LinkedList<Player> turnQueue;
    private List<Player> allPlayers;
    private Player currentPlayer;
    private Timer moveTimer;
    private List<Node> currentPath;
    private int pathIndex;

    private SoundManager soundManager;
    private StatsManager statsManager;

    private JTable scoreTable;
    private JTable winsTable;

    // --- WARNA & ASET ---
    public static final Color BG_COLOR = new Color(44, 62, 80);
    public static final Color BTN_COLOR = new Color(230, 126, 34);
    public static final Color TEXT_COLOR = new Color(52, 73, 94);
    public static final Color BLUE_BTN = new Color(52, 152, 219); // Biru untuk Start/Reset


    private final Color[] PLAYER_COLORS = {
            new Color(199, 21, 133), // P1: Pink Tua
            new Color(52, 152, 219), // P2: Biru
            new Color(204, 163, 0),  // P3: Kuning Tua
            new Color(204, 85, 0)    // P4: Orange Tua
    };

    private final String[] SHIP_FILES = {
            "kapal 1.png", "kapal 2.png", "kapal 3.png", "kapal 4.png"
    };

    public MainApp() {
        setTitle("Graph Snakes & Ladders - Ultimate Version");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        soundManager = new SoundManager();
        statsManager = new StatsManager();
        soundManager.playBackgroundMusic("bgm.wav");

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(createSetupPanel(), "SETUP");

        add(mainContainer);

        setSize(1000, 850);
        setLocationRelativeTo(null);

        random = new Random();
    }


    private JPanel createSetupPanel() {
        JPanel panel = new BackgroundPanel("background awal.jpg");
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("GAME SETUP", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new BorderLayout(0, 10));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(320, 0));

        JPanel tablesContainer = new JPanel(new GridLayout(2, 1, 0, 15));
        tablesContainer.setOpaque(false);

        JPanel scoreP = createStatsTable("TOP SCORES", new String[]{"Player", "Score"}, statsManager.getTopScoresData(), true);
        tablesContainer.add(scoreP);

        JPanel winsP = createStatsTable("TOP WINS", new String[]{"Player", "Wins"}, statsManager.getTopWinsData(), false);
        tablesContainer.add(winsP);

        statsPanel.add(tablesContainer, BorderLayout.CENTER);

        JButton resetBtn = new JButton("RESET STATISTICS");
        resetBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resetBtn.setBackground(BLUE_BTN);
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFocusPainted(false);
        resetBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete all history?", "Reset Statistics", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                statsManager.resetStats();
                refreshStatsTables();
            }
        });
        statsPanel.add(resetBtn, BorderLayout.SOUTH);

        panel.add(statsPanel, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel playerCountLabel = new JLabel("How many players?");
        playerCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        playerCountLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(playerCountLabel, gbc);

        JSpinner playerCountSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 4, 1));
        playerCountSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        playerCountSpinner.setPreferredSize(new Dimension(80, 30));
        gbc.gridx = 1;
        centerPanel.add(playerCountSpinner, gbc);

        JPanel playerNamesPanel = new JPanel();
        playerNamesPanel.setLayout(new BoxLayout(playerNamesPanel, BoxLayout.Y_AXIS));
        playerNamesPanel.setOpaque(false);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        centerPanel.add(playerNamesPanel, gbc);

        playerCountSpinner.addChangeListener(e -> {
            int count = (int) playerCountSpinner.getValue();
            updatePlayerNameFields(playerNamesPanel, count);
        });

        updatePlayerNameFields(playerNamesPanel, 2);
        panel.add(centerPanel, BorderLayout.CENTER);

        JButton startButton = new JButton("START ADVENTURE");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        startButton.setBackground(BLUE_BTN);
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setPreferredSize(new Dimension(200, 60));

        startButton.addActionListener(e -> {
            List<String> names = new ArrayList<>();
            for (Component comp : playerNamesPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel p = (JPanel) comp;
                    for (Component c : p.getComponents()) {
                        if (c instanceof JTextField) {
                            names.add(((JTextField) c).getText());
                        }
                    }
                }
            }
            startGame(names);
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(startButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updatePlayerNameFields(JPanel panel, int count) {
        panel.removeAll();
        for (int i = 0; i < count; i++) {
            JPanel playerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            playerPanel.setOpaque(false);

            ImageIcon shipIcon = loadShipIcon(i, 40, 40);
            JLabel iconLabel = new JLabel(shipIcon);

            JLabel label = new JLabel("Player " + (i + 1) + ": ");
            label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            label.setForeground(PLAYER_COLORS[i % PLAYER_COLORS.length]);

            JTextField textField = new JTextField(12);
            textField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            textField.setText("Player " + (i + 1));

            playerPanel.add(iconLabel);
            playerPanel.add(label);
            playerPanel.add(textField);
            panel.add(playerPanel);
        }
        panel.revalidate();
        panel.repaint();
    }

    private ImageIcon loadShipIcon(int index, int w, int h) {
        try {
            String fname = SHIP_FILES[index % SHIP_FILES.length];
            File f = new File(fname);
            if(!f.exists()) f = new File(fname.replace(".png", ".jpg"));

            if(f.exists()) {
                BufferedImage img = ImageIO.read(f);
                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch(Exception e) { e.printStackTrace(); }

        BufferedImage fallback = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics g = fallback.getGraphics();
        g.setColor(PLAYER_COLORS[index % PLAYER_COLORS.length]);
        g.fillRect(0, 0, w, h);
        g.dispose();
        return new ImageIcon(fallback);
    }

    private JPanel createStatsTable(String title, String[] cols, Object[][] data, boolean isScoreTable) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE), title,
                0, 0, new Font("Segoe UI", Font.BOLD, 14), Color.WHITE));

        JTable table = new JTable(new DefaultTableModel(data, cols));
        table.setEnabled(false);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        if (isScoreTable) this.scoreTable = table;
        else this.winsTable = table;

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void refreshStatsTables() {
        if (scoreTable != null) {
            scoreTable.setModel(new DefaultTableModel(
                    statsManager.getTopScoresData(), new String[]{"Player", "Score"}));
        }
        if (winsTable != null) {
            winsTable.setModel(new DefaultTableModel(
                    statsManager.getTopWinsData(), new String[]{"Player", "Wins"}));
        }
    }

    private void startGame(List<String> playerNames) {
        board = new GameBoard();
        turnQueue = new LinkedList<>();
        allPlayers = new ArrayList<>();

        for (int i = 0; i < playerNames.size(); i++) {
            String name = playerNames.get(i);
            int prevWins = statsManager.getPreviousWins(name);
            Player p = new Player(name, PLAYER_COLORS[i % PLAYER_COLORS.length], board.getNodeById(1));
            p.setWins(prevWins);
            turnQueue.add(p);
            allPlayers.add(p);
        }

        JPanel gameContainer = new JPanel(new BorderLayout());
        gameContainer.setBackground(BG_COLOR);

        gamePanel = new GamePanel(board);
        gamePanel.setPlayers(allPlayers);



        gameContainer.add(gamePanel, BorderLayout.CENTER);
        gameContainer.add(createControlPanel(), BorderLayout.SOUTH);

        mainContainer.add(gameContainer, "GAME");
        cardLayout.show(mainContainer, "GAME");

        updateTurnLabel();
    }


    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;


        rollButton = new JButton("ROLL DICE");
        rollButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        rollButton.setBackground(BTN_COLOR);
        rollButton.setForeground(Color.WHITE);
        rollButton.setFocusPainted(false);
        rollButton.setPreferredSize(new Dimension(140, 60));
        rollButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rollButton.addActionListener(e -> processDiceRoll());

        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        panel.add(rollButton, gbc);


        JPanel infoGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        infoGroup.setBackground(Color.WHITE);

        dicePanel = new DicePanel();
        infoGroup.add(dicePanel);

        JPanel textPanel = new JPanel(new GridBagLayout());
        textPanel.setBackground(Color.WHITE);
        GridBagConstraints tgbc = new GridBagConstraints();
        tgbc.gridx = 0; tgbc.gridy = GridBagConstraints.RELATIVE;
        tgbc.anchor = GridBagConstraints.WEST;
        tgbc.insets = new Insets(1, 0, 1, 0);

        turnLabel = new JLabel("Turn: -");
        turnLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        turnLabel.setForeground(TEXT_COLOR);
        textPanel.add(turnLabel, tgbc);

        diceResultLabel = new JLabel("Ready");
        diceResultLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        diceResultLabel.setForeground(TEXT_COLOR);
        textPanel.add(diceResultLabel, tgbc);

        statusLabel = new JLabel("Waiting...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
        textPanel.add(statusLabel, tgbc);

        infoGroup.add(textPanel);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(infoGroup, gbc);

        return panel;
    }

    private void updateTurnLabel() {
        if (!turnQueue.isEmpty()) {
            Player next = turnQueue.peek();

            int playerIndex = -1;
            for(int i=0; i<allPlayers.size(); i++) {
                if(allPlayers.get(i) == next) { playerIndex = i; break; }
            }

            turnLabel.setText("Turn: " + next.getName());
            turnLabel.setForeground(next.getColor());

            if(playerIndex != -1) {
                turnLabel.setIcon(loadShipIcon(playerIndex, 40, 40));
                turnLabel.setIconTextGap(15);
            }

            diceResultLabel.setText("Score: " + next.getScore());
        }
    }

    private boolean isPrime(int num) {
        if (num <= 1) return false;
        for (int i = 2; i <= Math.sqrt(num); i++) {
            if (num % i == 0) return false;
        }
        return true;
    }

    private void processDiceRoll() {
        if (turnQueue.isEmpty()) return;

        rollButton.setEnabled(false);
        rollButton.setBackground(Color.GRAY);

        currentPlayer = turnQueue.poll();
        int diceValue = random.nextInt(6) + 1;

        if (soundManager != null) {
            soundManager.playDiceSound("dice.wav");
        }

        dicePanel.roll(diceValue, () -> {
            calculateAndMovePlayer(diceValue);
        });
    }

    private void calculateAndMovePlayer(int diceValue) {
        int probabilityVar = random.nextInt(100);
        boolean isGreen = probabilityVar < 80;

        int currentId = currentPlayer.getPosition().getId();
        boolean startIsPrime = isPrime(currentId);

        currentPath = new ArrayList<>();

        if (startIsPrime) {
            if (soundManager != null) soundManager.playPrimeSound("prime.wav");

            Node startNode = board.getNodeById(currentId);
            Node bestTarget = board.getHighestNodeAtDistance(startNode, diceValue);
            List<Node> shortestPath = board.getShortestPath(startNode, bestTarget);

            for (int i = 0; i < shortestPath.size() - 1; i++) {
                Node a = shortestPath.get(i);
                Node b = shortestPath.get(i + 1);
                currentPath.add(a);
                if (Math.abs(b.getId() - a.getId()) > 1) {
                    currentPlayer.rememberShortcut(a.getId(), b.getId());
                }
            }
            if (!shortestPath.isEmpty()) {
                currentPath.add(shortestPath.get(shortestPath.size() - 1));
            }

            diceResultLabel.setText("Dice: " + diceValue + " (PRIME!)");
            gamePanel.showNotification("PRIME! Shortcut Active! (Var: " + probabilityVar + "%)");

        } else {
            if (isGreen) {
                int targetId = currentId + diceValue;

                if (targetId > board.getTotalNodes()) {
                    int excess = targetId - board.getTotalNodes();
                    targetId = board.getTotalNodes() - excess;

                    for (int i = currentId + 1; i <= board.getTotalNodes(); i++) {
                        currentPath.add(board.getNodeById(i));
                    }
                    for (int i = board.getTotalNodes() - 1; i >= targetId; i--) {
                        currentPath.add(board.getNodeById(i));
                    }

                    statusLabel.setText("Overshoot! Bounce Back!");
                } else {
                    for (int i = currentId + 1; i <= targetId; i++) {
                        currentPath.add(board.getNodeById(i));
                    }
                    statusLabel.setText("FORWARD (Green) - Var: " + probabilityVar + "%");
                }

                diceResultLabel.setText("Dice: " + diceValue);

            } else {
                int steps = diceValue;
                Node curr = currentPlayer.getPosition();

                while (steps > 0) {
                    int currId = curr.getId();
                    int shortcutDown = currentPlayer.checkShortcutDown(currId);

                    if (shortcutDown != -1) {
                        curr = board.getNodeById(shortcutDown);
                        gamePanel.showNotification("Oops! Falling back!");
                    } else {
                        int nextId = currId - 1;
                        if (nextId < 1) nextId = 1;
                        curr = board.getNodeById(nextId);
                    }

                    currentPath.add(curr);
                    steps--;
                    if (curr.getId() == 1) break;
                }

                diceResultLabel.setText("Dice: " + diceValue);
                statusLabel.setText("BACKWARD (Red) - Var: " + probabilityVar + "%");
            }
        }

        pathIndex = 0;
        startStepAnimation();
    }

    private void startStepAnimation() {
        if (currentPath.isEmpty()) {
            handleEndTurn(currentPlayer.getPosition());
            return;
        }

        moveTimer = new Timer(300, e -> {
            if (pathIndex < currentPath.size()) {
                Node next = currentPath.get(pathIndex);
                currentPlayer.setPosition(next);
                if (soundManager != null) soundManager.playStepSound("whoosh.wav");
                gamePanel.repaint();
                pathIndex++;
            } else {
                ((Timer)e.getSource()).stop();
                handleEndTurn(currentPlayer.getPosition());
            }
        });
        moveTimer.start();
    }

    private void handleEndTurn(Node finalPos) {
        if (finalPos.getBonusScore() > 0) {
            currentPlayer.addScore(finalPos.getBonusScore());
            if (soundManager != null) soundManager.playScoreSound("score.wav");
            gamePanel.showNotification("Bonus! +" + finalPos.getBonusScore());
            finalPos.setBonusScore(0);
        }

        if (finalPos.getId() == board.getTotalNodes()) {
            if (soundManager != null) soundManager.playVictorySound("victory.wav");
            statsManager.recordGame(currentPlayer, allPlayers);

            Object[] options = {"Play Again", "Exit"};
            int choice = JOptionPane.showOptionDialog(this,
                    "CONGRATULATIONS! " + currentPlayer.getName() + " WINS!\nDo you want to play again?",
                    "Game Over",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == JOptionPane.YES_OPTION) {
                soundManager.playBackgroundMusic("bgm.wav");
                refreshStatsTables();
                cardLayout.show(mainContainer, "SETUP");
            } else {
                System.exit(0);
            }
            return;
        }

        if (finalPos.isStar()) {
            if (soundManager != null) soundManager.playBonusSound("bonus.wav");
            gamePanel.showNotification("STAR! Roll Again!");
            turnQueue.addFirst(currentPlayer);
        } else {
            turnQueue.addLast(currentPlayer);
        }

        gamePanel.repaint();
        updateTurnLabel();

        rollButton.setEnabled(true);
        rollButton.setBackground(BTN_COLOR);
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}