import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class MazeProjectFinal extends JFrame {

    private static final int ROWS = 21;
    private static final int COLS = 31;
    private static final int BASE_CELL_SIZE = 40;

    private static final int HERO_SIZE = 48;
    private final int WALL_THICKNESS = 10;

    private final double TREE_SCALE_WIDTH = 1.3;
    private final double TREE_SCALE_HEIGHT = 1.4;

    private int currentDelay = 200;

    private final Color WALL_COLOR_FALLBACK = new Color(50, 50, 50);
    private final Color FALLBACK_GRASS = new Color(34, 139, 34);
    private final Color FALLBACK_YOUNG_GRASS = new Color(144, 238, 144);
    private final Color FALLBACK_WATER = new Color(65, 105, 225);
    private final Color FALLBACK_MUD = new Color(101, 67, 33);

    private final Color SEARCH_COLOR = new Color(255, 215, 0, 100);
    private final Color PATH_BFS = Color.CYAN;
    private final Color PATH_DFS = Color.ORANGE;
    private final Color PATH_DIJKSTRA = Color.MAGENTA;
    private final Color PATH_ASTAR = Color.GREEN;

    private MazePanel canvas;
    private JTextArea infoArea;
    private JLabel speedLabel;

    public MazeProjectFinal() {
        setTitle("RPG Maze: Final Fixed (Audio & Crash Fix)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        canvas = new MazePanel();
        add(canvas, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.setBackground(new Color(34, 49, 29));

        JLabel infoTitle = new JLabel("  Forest Guide  ");
        infoTitle.setForeground(new Color(255, 215, 0));
        infoTitle.setFont(new Font("Serif", Font.BOLD, 22));
        infoTitle.setBorder(new EmptyBorder(15, 10, 15, 10));
        rightPanel.add(infoTitle, BorderLayout.NORTH);

        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(new Color(50, 70, 50));
        infoArea.setForeground(new Color(230, 230, 230));
        infoArea.setFont(new Font("Serif", Font.PLAIN, 15));
        infoArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        infoArea.setText("Status: Audio Fixed.\n\nSuara akan berhenti otomatis saat sampai tujuan.\n\nKlik 'Generate Map' untuk mulai.");

        rightPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.setBackground(new Color(34, 49, 29));
        bottomContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel sliderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sliderPanel.setBackground(new Color(34, 49, 29));

        JLabel lblSpeedIcon = new JLabel("Kecepatan: ");
        lblSpeedIcon.setForeground(Color.WHITE);

        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 10, 500, currentDelay);
        speedSlider.setBackground(new Color(34, 49, 29));
        speedSlider.setInverted(true);

        speedLabel = new JLabel(getSpeedText(currentDelay));
        speedLabel.setForeground(Color.YELLOW);
        speedLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        speedLabel.setPreferredSize(new Dimension(150, 20));

        speedSlider.addChangeListener(e -> {
            currentDelay = speedSlider.getValue();
            speedLabel.setText(getSpeedText(currentDelay));
            canvas.updateTimerDelay(currentDelay);
        });

        sliderPanel.add(lblSpeedIcon);
        sliderPanel.add(speedSlider);
        sliderPanel.add(speedLabel);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 8, 8));
        buttonPanel.setBackground(new Color(34, 49, 29));
        buttonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));

        JButton btnGen = styleButton("Generate Map", new Color(80, 80, 80), Color.WHITE);
        JButton btnBFS = styleButton("BFS", new Color(0, 100, 100), Color.WHITE);
        JButton btnDFS = styleButton("DFS", new Color(139, 69, 19), Color.WHITE);
        JButton btnDijkstra = styleButton("Dijkstra", new Color(128, 0, 128), Color.WHITE);
        JButton btnAStar = styleButton("A* (Smart)", new Color(0, 100, 0), Color.WHITE);

        btnGen.addActionListener(e -> {
            updateInfo("GENERATE MAP", "Abu-Abu", "Hutan Maze Random.\nMedan: Rumput(1), Air(5), Lumpur(10)");
            canvas.generateMaze();
        });

        btnBFS.addActionListener(e -> { updateInfo("BFS", "CYAN", "Mencari jalan terpendek."); canvas.solveBFS(); });
        btnDFS.addActionListener(e -> { updateInfo("DFS", "ORANYE", "Eksplorasi nekat."); canvas.solveDFS(); });
        btnDijkstra.addActionListener(e -> { updateInfo("DIJKSTRA", "MAGENTA", "Mencari jalan termurah."); canvas.solveDijkstra(); });
        btnAStar.addActionListener(e -> { updateInfo("A*", "HIJAU", "Cerdas & Cepat."); canvas.solveAStar(); });

        buttonPanel.add(btnGen);
        buttonPanel.add(btnBFS);
        buttonPanel.add(btnDFS);
        buttonPanel.add(btnDijkstra);
        buttonPanel.add(btnAStar);

        bottomContainer.add(sliderPanel);
        bottomContainer.add(Box.createRigidArea(new Dimension(0, 5)));
        bottomContainer.add(buttonPanel);

        add(bottomContainer, BorderLayout.SOUTH);

        setSize(1280, 800);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private String getSpeedText(int delay) {
        if (delay <= 50) return "Mode Berlari";
        if (delay <= 150) return "Jogging";
        if (delay <= 300) return "Berjalan";
        return "Merangkak";
    }

    private void updateInfo(String mode, String colorName, String desc) {
        infoArea.setText("MODE: " + mode + "\n" + "JEJAK: " + colorName + "\n\n" + desc);
        infoArea.setCaretPosition(0);
    }

    public void appendResult(int steps, int cost) {
        infoArea.append("\n\n--------------------------\n");
        infoArea.append("LAPORAN MISI:\n");
        infoArea.append("- Jarak: " + steps + "\n");
        infoArea.append("- Energi: " + cost + "\n");
        infoArea.setCaretPosition(infoArea.getDocument().getLength());
    }

    private JButton styleButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Serif", Font.BOLD, 12));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MazeProjectFinal::new);
    }

    static class Cell {
        int r, c;
        int type = 0;
        int treeVariant = 0;
        boolean visited = false;
        public Cell(int r, int c) { this.r = r; this.c = c; }
    }

    static class Node {
        Cell cell; int cost;
        public Node(Cell cell, int cost) { this.cell = cell; this.cost = cost; }
    }

    class MazePanel extends JPanel {
        private Cell[][] grid;
        private Cell startCell, endCell;

        private List<Cell> searchHistory;
        private List<Cell> pathSolution;
        private Color currentPathColor = Color.WHITE;

        private int animIndex = 0;
        private boolean showSolution = false;
        private Timer timer;
        private Clip audioClip;

        private boolean showWinPopup = false;
        private Image imgKnightWin;

        private Image imgKnightUp, imgKnightDown, imgKnightLeft, imgKnightRight;
        private Image imgGrassPath, imgGrassForest, imgWater, imgMud;
        private Image imgTreasure;
        private Image imgWallH, imgWallV, imgWallCorner;
        private Image[] imgTrees = new Image[3];

        private boolean imagesLoaded = false;

        public MazePanel() {
            setBackground(WALL_COLOR_FALLBACK);
            loadAndResizeImages();
            initGrid();
            generateMaze();

            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    repaint();
                }
            });
        }

        private void playSound(String filename, boolean loop) {
            try {
                stopSound();
                File soundFile = new File(filename);
                if (soundFile.exists()) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                    audioClip = AudioSystem.getClip();
                    audioClip.open(audioIn);

                    if (loop) {
                        audioClip.loop(Clip.LOOP_CONTINUOUSLY);
                    } else {
                        audioClip.start();
                    }
                }
            } catch (Exception e) {}
        }

        private void stopSound() {
            if (audioClip != null) {
                audioClip.stop();
                audioClip.close();
                audioClip = null;
            }
        }

        private void loadAndResizeImages() {
            try {
                int treeW = (int)(BASE_CELL_SIZE * TREE_SCALE_WIDTH);
                int treeH = (int)(BASE_CELL_SIZE * TREE_SCALE_HEIGHT);

                imgKnightUp = loadSmart("knight up", HERO_SIZE, HERO_SIZE);
                imgKnightDown = loadSmart("knight down", HERO_SIZE, HERO_SIZE);
                imgKnightLeft = loadSmart("knight left", HERO_SIZE, HERO_SIZE);
                imgKnightRight = loadSmart("knight right", HERO_SIZE, HERO_SIZE);

                imgTreasure = loadSmart("harta", BASE_CELL_SIZE, BASE_CELL_SIZE);

                imgKnightWin = loadSmart("knight win", 220, 220);

                imgGrassPath = loadSmart("rumput", BASE_CELL_SIZE, BASE_CELL_SIZE);
                imgGrassForest = loadSmart("rumput muda", BASE_CELL_SIZE, BASE_CELL_SIZE);

                imgWater = loadSmart("air", BASE_CELL_SIZE, BASE_CELL_SIZE);
                imgMud = loadSmart("lumpur", BASE_CELL_SIZE, BASE_CELL_SIZE);

                imgTrees[0] = loadSmart("pohon 1", treeW, treeH);
                imgTrees[1] = loadSmart("pohon 2", treeW, treeH);
                imgTrees[2] = loadSmart("pohon 3", treeW, treeH);

                File fWall = findFileSmart("dinding pipih");
                if (fWall != null) {
                    BufferedImage biWall = ImageIO.read(fWall);
                    imgWallH = biWall.getScaledInstance(BASE_CELL_SIZE, WALL_THICKNESS, Image.SCALE_SMOOTH);
                    BufferedImage rotatedWall = rotateImage(biWall, 90);
                    imgWallV = rotatedWall.getScaledInstance(WALL_THICKNESS, BASE_CELL_SIZE, Image.SCALE_SMOOTH);
                    imgWallCorner = biWall.getScaledInstance(WALL_THICKNESS, WALL_THICKNESS, Image.SCALE_SMOOTH);
                }
                imagesLoaded = true;
            } catch (Exception e) {
                imagesLoaded = false;
            }
        }

        private File findFileSmart(String baseName) {
            File f = new File(baseName + ".png");
            if (f.exists()) return f;
            f = new File(baseName + ".jpg");
            if (f.exists()) return f;
            f = new File(baseName + ".jpeg");
            if (f.exists()) return f;
            return null;
        }

        private Image loadSmart(String baseName, int w, int h) {
            File f = findFileSmart(baseName);
            if (f == null) return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            try {
                BufferedImage original = ImageIO.read(f);
                return original.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            } catch (IOException e) { return null; }
        }

        public BufferedImage rotateImage(BufferedImage img, double angle) {
            double rads = Math.toRadians(angle);
            double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
            int w = img.getWidth();
            int h = img.getHeight();
            int newWidth = (int) Math.floor(w * cos + h * sin);
            int newHeight = (int) Math.floor(h * cos + w * sin);
            BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = rotated.createGraphics();
            AffineTransform at = new AffineTransform();
            at.translate((newWidth - w) / 2, (newHeight - h) / 2);
            at.rotate(rads, w / 2, h / 2);
            g2d.setTransform(at);
            g2d.drawImage(img, 0, 0, null);
            g2d.dispose();
            return rotated;
        }

        public void updateTimerDelay(int newDelay) {
            if (timer != null) timer.setDelay(newDelay);
        }

        private void initGrid() {
            grid = new Cell[ROWS][COLS];
            Random rTree = new Random();
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    grid[r][c] = new Cell(r, c);
                    grid[r][c].treeVariant = rTree.nextInt(3);
                }
            }
            startCell = grid[1][1];
            endCell = grid[ROWS - 2][COLS - 2];
        }

        public void generateMaze() {
            stopAnimation();
            initGrid();
            ArrayList<Cell> frontier = new ArrayList<>();
            Random rand = new Random();
            startCell.type = 1;
            addFrontier(startCell, frontier);

            while (!frontier.isEmpty()) {
                Cell current = frontier.remove(rand.nextInt(frontier.size()));
                List<Cell> pathNeighbors = getPathNeighbors(current);
                if (!pathNeighbors.isEmpty()) {
                    Cell neighbor = pathNeighbors.get(rand.nextInt(pathNeighbors.size()));
                    connect(current, neighbor);
                }
                addFrontier(current, frontier);
            }
            createLoops();
            assignWeights();
            endCell.type = 1;
            repaint();
        }

        private void createLoops() {
            Random rand = new Random();
            for (int r = 1; r < ROWS - 1; r++) {
                for (int c = 1; c < COLS - 1; c++) {
                    if (grid[r][c].type == 0) {
                        if (rand.nextInt(100) < 15) grid[r][c].type = 1;
                    }
                }
            }
        }

        private void assignWeights() {
            Random rand = new Random();
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (grid[r][c].type != 0 && grid[r][c] != startCell && grid[r][c] != endCell) {
                        int chance = rand.nextInt(100);
                        if (chance < 60) grid[r][c].type = 1;
                        else if (chance < 85) grid[r][c].type = 5;
                        else grid[r][c].type = 10;
                    }
                }
            }
        }

        private void addFrontier(Cell cell, ArrayList<Cell> frontier) {
            int[][] dirs = {{0, 2}, {0, -2}, {2, 0}, {-2, 0}};
            for (int[] d : dirs) {
                int nr = cell.r + d[0];
                int nc = cell.c + d[1];
                if (isValid(nr, nc) && grid[nr][nc].type == 0) {
                    grid[nr][nc].type = 1; grid[nr][nc].type = 0;
                    if (!frontier.contains(grid[nr][nc])) frontier.add(grid[nr][nc]);
                }
            }
        }

        private List<Cell> getPathNeighbors(Cell cell) {
            List<Cell> n = new ArrayList<>();
            int[][] dirs = {{0, 2}, {0, -2}, {2, 0}, {-2, 0}};
            for (int[] d : dirs) {
                int nr = cell.r + d[0];
                int nc = cell.c + d[1];
                if (isValid(nr, nc) && grid[nr][nc].type != 0) n.add(grid[nr][nc]);
            }
            return n;
        }

        private List<Cell> getDirectNeighbors(Cell c) {
            List<Cell> list = new ArrayList<>();
            int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] d : dirs) {
                int nr = c.r + d[0];
                int nc = c.c + d[1];
                if (isValid(nr, nc)) list.add(grid[nr][nc]);
            }
            return list;
        }

        private void connect(Cell a, Cell b) {
            a.type = 1;
            int midR = (a.r + b.r) / 2;
            int midC = (a.c + b.c) / 2;
            grid[midR][midC].type = 1;
        }

        private boolean isValid(int r, int c) {
            return r >= 0 && r < ROWS && c >= 0 && c < COLS;
        }

        public void solveBFS() {
            currentPathColor = PATH_BFS;
            prepareSearch();
            Queue<Cell> queue = new LinkedList<>();
            Map<Cell, Cell> parentMap = new HashMap<>();

            queue.add(startCell);
            startCell.visited = true;
            boolean found = false;

            while (!queue.isEmpty()) {
                Cell current = queue.poll();
                searchHistory.add(current);
                if (current == endCell) { found = true; break; }
                for (Cell neighbor : getDirectNeighbors(current)) {
                    if (!neighbor.visited && neighbor.type != 0) {
                        neighbor.visited = true;
                        parentMap.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
            finishSearch(found, parentMap);
        }

        public void solveDFS() {
            currentPathColor = PATH_DFS;
            prepareSearch();
            Stack<Cell> stack = new Stack<>();
            Map<Cell, Cell> parentMap = new HashMap<>();

            stack.push(startCell);
            startCell.visited = true;
            boolean found = false;

            while (!stack.isEmpty()) {
                Cell current = stack.pop();
                searchHistory.add(current);
                if (current == endCell) { found = true; break; }
                for (Cell neighbor : getDirectNeighbors(current)) {
                    if (!neighbor.visited && neighbor.type != 0) {
                        neighbor.visited = true;
                        parentMap.put(neighbor, current);
                        stack.push(neighbor);
                    }
                }
            }
            finishSearch(found, parentMap);
        }

        public void solveDijkstra() {
            currentPathColor = PATH_DIJKSTRA;
            prepareSearch();
            PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));
            Map<Cell, Integer> dist = new HashMap<>();
            Map<Cell, Cell> parentMap = new HashMap<>();

            for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) dist.put(grid[r][c], Integer.MAX_VALUE);
            dist.put(startCell, 0);
            pq.add(new Node(startCell, 0));
            boolean found = false;

            while (!pq.isEmpty()) {
                Node current = pq.poll();
                Cell currCell = current.cell;
                if (currCell.visited) continue;
                currCell.visited = true;
                searchHistory.add(currCell);
                if (currCell == endCell) { found = true; break; }
                for (Cell neighbor : getDirectNeighbors(currCell)) {
                    if (!neighbor.visited && neighbor.type != 0) {
                        int newDist = dist.get(currCell) + neighbor.type;
                        if (newDist < dist.get(neighbor)) {
                            dist.put(neighbor, newDist);
                            parentMap.put(neighbor, currCell);
                            pq.add(new Node(neighbor, newDist));
                        }
                    }
                }
            }
            finishSearch(found, parentMap);
        }

        public void solveAStar() {
            currentPathColor = PATH_ASTAR;
            prepareSearch();
            PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));
            Map<Cell, Integer> gScore = new HashMap<>();
            Map<Cell, Cell> parentMap = new HashMap<>();

            for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) gScore.put(grid[r][c], Integer.MAX_VALUE);
            gScore.put(startCell, 0);
            pq.add(new Node(startCell, heuristic(startCell, endCell)));
            boolean found = false;

            while (!pq.isEmpty()) {
                Node current = pq.poll();
                Cell currCell = current.cell;
                if (currCell.visited) continue;
                currCell.visited = true;
                searchHistory.add(currCell);
                if (currCell == endCell) { found = true; break; }
                for (Cell neighbor : getDirectNeighbors(currCell)) {
                    if (!neighbor.visited && neighbor.type != 0) {
                        int tentativeG = gScore.get(currCell) + neighbor.type;
                        if (tentativeG < gScore.get(neighbor)) {
                            gScore.put(neighbor, tentativeG);
                            parentMap.put(neighbor, currCell);
                            int fScore = tentativeG + heuristic(neighbor, endCell);
                            pq.add(new Node(neighbor, fScore));
                        }
                    }
                }
            }
            finishSearch(found, parentMap);
        }

        private int heuristic(Cell a, Cell b) {
            return Math.abs(a.r - b.r) + Math.abs(a.c - b.c);
        }

        private void prepareSearch() {
            stopAnimation();
            showWinPopup = false;
            for(int r=0; r<ROWS; r++) for(int c=0; c<COLS; c++) grid[r][c].visited = false;
            searchHistory = new ArrayList<>();
            pathSolution = new ArrayList<>();
        }

        private void finishSearch(boolean found, Map<Cell, Cell> parentMap) {
            if (found) {
                Cell curr = endCell;
                while (curr != null) {
                    pathSolution.add(0, curr);
                    curr = parentMap.get(curr);
                }
                startAnimation();
            } else {
                JOptionPane.showMessageDialog(this, "Tidak ada rute ke Harta Karun!");
            }
        }

        private void startAnimation() {
            animIndex = 0;
            showSolution = false;
            playSound("grass step.wav", true);

            timer = new Timer(currentDelay, (ActionEvent e) -> {
                if (searchHistory == null) { timer.stop(); stopSound(); return; }

                if (!showSolution) {
                    if (animIndex < searchHistory.size()) {
                        animIndex++;
                        repaint();
                    } else {
                        showSolution = true;
                        animIndex = 0;
                        stopSound();
                        playSound("knight step.wav", true);
                        repaint();
                    }
                } else {
                    if (pathSolution != null && animIndex < pathSolution.size() - 1) {
                        animIndex++;
                        repaint();
                    } else {
                        timer.stop();
                        stopSound();

                        if (pathSolution != null) {
                            int totalCost = 0;
                            for(Cell c : pathSolution) totalCost += c.type;
                            appendResult(pathSolution.size(), totalCost);
                        }

                        showWinPopup = true;
                        playSound("win sound.wav", false);

                        repaint();
                    }
                }
            });
            timer.start();
        }

        private void stopAnimation() {
            if (timer != null) timer.stop();
            stopSound();
            searchHistory = null;
            pathSolution = null;
            showSolution = false;
            showWinPopup = false;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            AffineTransform originalTransform = g2.getTransform();

            int panelW = getWidth();
            int panelH = getHeight();
            int mazeW = COLS * BASE_CELL_SIZE;
            int mazeH = ROWS * BASE_CELL_SIZE;

            double scaleX = (double) panelW / mazeW;
            double scaleY = (double) panelH / mazeH;
            double scale = Math.min(scaleX, scaleY);

            int drawW = (int) (mazeW * scale);
            int drawH = (int) (mazeH * scale);
            int startX = (panelW - drawW) / 2;
            int startY = (panelH - drawH) / 2;

            g2.translate(startX, startY);
            g2.scale(scale, scale);

            int treeDrawW = (int)(BASE_CELL_SIZE * TREE_SCALE_WIDTH);
            int treeDrawH = (int)(BASE_CELL_SIZE * TREE_SCALE_HEIGHT);
            int treeOffsetX = (treeDrawW - BASE_CELL_SIZE) / 2;
            int treeOffsetY = (treeDrawH - BASE_CELL_SIZE);

            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    int x = c * BASE_CELL_SIZE;
                    int y = r * BASE_CELL_SIZE;
                    Cell cell = grid[r][c];

                    if (cell.type == 0) {
                        if (imagesLoaded && imgGrassForest != null) {
                            g2.drawImage(imgGrassForest, x, y, null);
                        } else {
                            g2.setColor(FALLBACK_YOUNG_GRASS);
                            g2.fillRect(x, y, BASE_CELL_SIZE, BASE_CELL_SIZE);
                        }
                    } else {
                        if (imagesLoaded && imgGrassPath != null) {
                            g2.drawImage(imgGrassPath, x, y, null);
                        } else {
                            g2.setColor(FALLBACK_GRASS);
                            g2.fillRect(x, y, BASE_CELL_SIZE, BASE_CELL_SIZE);
                        }
                    }

                    if (cell.type == 5 && imagesLoaded && imgWater != null) g2.drawImage(imgWater, x, y, null);
                    else if (cell.type == 10 && imagesLoaded && imgMud != null) g2.drawImage(imgMud, x, y, null);
                }
            }

            if (imagesLoaded && imgTreasure != null) {
                g2.drawImage(imgTreasure, endCell.c * BASE_CELL_SIZE, endCell.r * BASE_CELL_SIZE, null);
            }

            g2.setColor(WALL_COLOR_FALLBACK);
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    int x = c * BASE_CELL_SIZE;
                    int y = r * BASE_CELL_SIZE;

                    if (grid[r][c].type != 0) {
                        boolean topIsWall = (r > 0 && grid[r-1][c].type == 0);
                        boolean bottomIsWall = (r < ROWS-1 && grid[r+1][c].type == 0);
                        boolean leftIsWall = (c > 0 && grid[r][c-1].type == 0);
                        boolean rightIsWall = (c < COLS-1 && grid[r][c+1].type == 0);

                        boolean topLeftIsWall = (r > 0 && c > 0 && grid[r-1][c-1].type == 0);
                        boolean topRightIsWall = (r > 0 && c < COLS-1 && grid[r-1][c+1].type == 0);
                        boolean bottomLeftIsWall = (r < ROWS-1 && c > 0 && grid[r+1][c-1].type == 0);
                        boolean bottomRightIsWall = (r < ROWS-1 && c < COLS-1 && grid[r+1][c+1].type == 0);

                        if (topIsWall) {
                            if(imgWallH != null) g2.drawImage(imgWallH, x, y, null);
                            else g2.fillRect(x, y, BASE_CELL_SIZE, WALL_THICKNESS);
                        }
                        if (bottomIsWall) {
                            if(imgWallH != null) g2.drawImage(imgWallH, x, y + BASE_CELL_SIZE - WALL_THICKNESS, null);
                            else g2.fillRect(x, y + BASE_CELL_SIZE - WALL_THICKNESS, BASE_CELL_SIZE, WALL_THICKNESS);
                        }
                        if (leftIsWall) {
                            if(imgWallV != null) g2.drawImage(imgWallV, x, y, null);
                            else g2.fillRect(x, y, WALL_THICKNESS, BASE_CELL_SIZE);
                        }
                        if (rightIsWall) {
                            if(imgWallV != null) g2.drawImage(imgWallV, x + BASE_CELL_SIZE - WALL_THICKNESS, y, null);
                            else g2.fillRect(x + BASE_CELL_SIZE - WALL_THICKNESS, y, WALL_THICKNESS, BASE_CELL_SIZE);
                        }

                        if (imgWallCorner != null) {
                            if (topIsWall && leftIsWall) g2.drawImage(imgWallCorner, x, y, null);
                            if (topIsWall && rightIsWall) g2.drawImage(imgWallCorner, x + BASE_CELL_SIZE - WALL_THICKNESS, y, null);
                            if (bottomIsWall && leftIsWall) g2.drawImage(imgWallCorner, x, y + BASE_CELL_SIZE - WALL_THICKNESS, null);
                            if (bottomIsWall && rightIsWall) g2.drawImage(imgWallCorner, x + BASE_CELL_SIZE - WALL_THICKNESS, y + BASE_CELL_SIZE - WALL_THICKNESS, null);

                            if (!topIsWall && !leftIsWall && topLeftIsWall) g2.drawImage(imgWallCorner, x, y, null);
                            if (!topIsWall && !rightIsWall && topRightIsWall) g2.drawImage(imgWallCorner, x + BASE_CELL_SIZE - WALL_THICKNESS, y, null);
                            if (!bottomIsWall && !leftIsWall && bottomLeftIsWall) g2.drawImage(imgWallCorner, x, y + BASE_CELL_SIZE - WALL_THICKNESS, null);
                            if (!bottomIsWall && !rightIsWall && bottomRightIsWall) g2.drawImage(imgWallCorner, x + BASE_CELL_SIZE - WALL_THICKNESS, y + BASE_CELL_SIZE - WALL_THICKNESS, null);
                        }
                    }
                }
            }

            if (searchHistory != null && !showSolution) {
                g2.setColor(SEARCH_COLOR);
                int limit = Math.min(animIndex, searchHistory.size());
                for (int i = 0; i < limit; i++) {
                    Cell c = searchHistory.get(i);
                    g2.fillRect(c.c * BASE_CELL_SIZE, c.r * BASE_CELL_SIZE, BASE_CELL_SIZE, BASE_CELL_SIZE);
                }
            }

            if (showSolution && pathSolution != null) {
                g2.setColor(currentPathColor);
                g2.setStroke(new BasicStroke(3));
                for (int i = 0; i < animIndex; i++) {
                    if (i + 1 >= pathSolution.size()) break;

                    Cell c1 = pathSolution.get(i);
                    Cell c2 = pathSolution.get(i+1);
                    g2.drawLine(c1.c * BASE_CELL_SIZE + BASE_CELL_SIZE/2, c1.r * BASE_CELL_SIZE + BASE_CELL_SIZE/2,
                            c2.c * BASE_CELL_SIZE + BASE_CELL_SIZE/2, c2.r * BASE_CELL_SIZE + BASE_CELL_SIZE/2);
                }

                if (imagesLoaded && !pathSolution.isEmpty() && animIndex < pathSolution.size()) {
                    Cell curr = pathSolution.get(animIndex);
                    if (curr != endCell) {
                        Image kImg = imgKnightDown;
                        if (animIndex < pathSolution.size() - 1) {
                            Cell next = pathSolution.get(animIndex + 1);
                            if (next.c > curr.c) kImg = imgKnightRight;
                            else if (next.c < curr.c) kImg = imgKnightLeft;
                            else if (next.r < curr.r) kImg = imgKnightUp;
                            else if (next.r > curr.r) kImg = imgKnightDown;
                        }
                        int offsetX = (BASE_CELL_SIZE - HERO_SIZE) / 2;
                        int offsetY = (BASE_CELL_SIZE - HERO_SIZE) - 5;
                        g2.setColor(new Color(0,0,0, 80));
                        g2.fillOval(curr.c * BASE_CELL_SIZE + 5, curr.r * BASE_CELL_SIZE + BASE_CELL_SIZE - 10, BASE_CELL_SIZE - 10, 8);
                        g2.drawImage(kImg, curr.c * BASE_CELL_SIZE + offsetX, curr.r * BASE_CELL_SIZE + offsetY, null);
                    }
                }
            }

            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    int x = c * BASE_CELL_SIZE;
                    int y = r * BASE_CELL_SIZE;
                    if (grid[r][c].type == 0) {
                        if (imagesLoaded && imgTrees[grid[r][c].treeVariant] != null) {
                            g2.drawImage(imgTrees[grid[r][c].treeVariant], x - treeOffsetX, y - treeOffsetY, null);
                        }
                    }
                }
            }

            if (showWinPopup) {
                g2.setTransform(originalTransform);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRect(0, 0, w, h);

                int boxW = 400;
                int boxH = 320;
                int boxX = (w - boxW) / 2;
                int boxY = (h - boxH) / 2;

                g2.setColor(new Color(255, 250, 240));
                g2.fillRoundRect(boxX, boxY, boxW, boxH, 25, 25);

                g2.setColor(new Color(101, 67, 33));
                g2.setStroke(new BasicStroke(5));
                g2.drawRoundRect(boxX, boxY, boxW, boxH, 25, 25);

                g2.setFont(new Font("SansSerif", Font.BOLD, 22));
                String msg = "Anda menemukan harta karun!";
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(msg);
                g2.setColor(new Color(80, 50, 20));
                g2.drawString(msg, boxX + (boxW - textW) / 2, boxY + 50);

                if (imgKnightWin != null) {
                    int imgW = imgKnightWin.getWidth(null);
                    int imgX = boxX + (boxW - imgW) / 2;
                    int imgY = boxY + 80;
                    g2.drawImage(imgKnightWin, imgX, imgY, null);
                } else {
                    g2.setColor(Color.BLACK);
                    g2.drawString("(Gambar 'knight win' tidak ditemukan)", boxX + 50, boxY + 150);
                }
            }
        }
    }
}