import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class GamePanel extends JPanel {
    private final GameBoard board;
    private List<Player> players;

    private BufferedImage backgroundImage;
    private BufferedImage[] shipImages;

    private BufferedImage imgNodeBiasa;
    private BufferedImage imgBonusTurn;
    private BufferedImage imgFinish;

    private final Color LINK_COLOR = new Color(255, 140, 0);
    private final Color SCORE_TEXT_COLOR = new Color(50, 255, 50);
    private final Color STAR_COLOR = new Color(241, 196, 15);
    private final Color ID_TEXT_COLOR = Color.BLACK;

    private String notificationMessage = null;
    private Timer notificationTimer;
    private Color notificationColor = new Color(0, 0, 0, 180);

    public GamePanel(GameBoard board) {
        this.board = board;
        this.players = new ArrayList<>();
        setOpaque(false);

        loadImages();

        notificationTimer = new Timer(3000, e -> {
            notificationMessage = null;
            repaint();
        });
        notificationTimer.setRepeats(false);
    }

    private void loadImages() {
        backgroundImage = loadImageSafe("background game.jpg");

        imgNodeBiasa = loadImageSafe("node papan.png");
        imgBonusTurn = loadImageSafe("bonus turn.jpg");
        imgFinish    = loadImageSafe("finish.jpg");

        if (imgNodeBiasa == null) imgNodeBiasa = loadImageSafe("Node biasa.png");

        shipImages = new BufferedImage[4];
        String[] shipFiles = {"kapal 1.png", "kapal 2.png", "kapal 3.png", "kapal 4.png"};
        for (int i = 0; i < 4; i++) {
            shipImages[i] = loadImageSafe(shipFiles[i]);
        }
    }

    private BufferedImage loadImageSafe(String filename) {
        try {
            File f = new File(filename);
            if (!f.exists()) {
                if (filename.endsWith(".png")) f = new File(filename.replace(".png", ".jpg"));
                else if (filename.endsWith(".jpg")) f = new File(filename.replace(".jpg", ".png"));
            }

            if (f.exists()) {
                return ImageIO.read(f);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void showNotification(String message) {
        this.notificationMessage = message;
        notificationTimer.restart();
        repaint();
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, w, h, null);
        } else {
            g2d.setColor(new Color(44, 62, 80));
            g2d.fillRect(0, 0, w, h);
        }

        int minDimension = Math.min(w, h);
        int cellSize = (minDimension - 40) / 8;

        int startX = (w - (cellSize * 8)) / 2;
        int startY = (h - (cellSize * 8)) / 2;

        List<Node> nodes = board.getNodes();

        drawRandomLinks(g2d, cellSize, startX, startY);

        for (Node node : nodes) {
            Point p = getTopLeftCoordinates(node.getId(), cellSize, startX, startY);
            int x = p.x; int y = p.y;

            BufferedImage imgToDraw;

            if (node.getId() == 64) {
                imgToDraw = imgFinish;
            } else if (node.isStar()) {
                imgToDraw = imgBonusTurn;
            } else {
                imgToDraw = imgNodeBiasa;
            }

            if (imgToDraw != null) {
                g2d.drawImage(imgToDraw, x, y, cellSize, cellSize, null);
            } else {
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillOval(x+5, y+5, cellSize-10, cellSize-10);
            }

            String idStr = String.valueOf(node.getId());
            int fontSize = Math.max(16, cellSize / 4);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
            FontMetrics fm = g2d.getFontMetrics();

            int centerX = x + cellSize / 2;
            int centerYBoard = y + (int)(cellSize * 0.78);

            int textXId = centerX - fm.stringWidth(idStr) / 2;
            int textYId = centerYBoard + (fm.getAscent() - fm.getDescent()) / 2;

            g2d.setColor(ID_TEXT_COLOR);
            g2d.drawString(idStr, textXId, textYId);

            if (node.getBonusScore() > 0 || node.getId() == 1 || node.getId() == 64) {
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                String lbl = "";
                Color lblColor = Color.WHITE;

                if (node.getId() == 1) { lbl = "START"; lblColor = new Color(39, 174, 96); }
                else if (node.getId() == 64) { lbl = "FINISH"; lblColor = new Color(192, 57, 43); }
                else if (node.getBonusScore() > 0) {
                    lbl = "+" + node.getBonusScore();
                    lblColor = SCORE_TEXT_COLOR;
                }

                FontMetrics fmLbl = g2d.getFontMetrics();
                int lblX = centerX - fmLbl.stringWidth(lbl) / 2;

                int lblY;
                if (node.getId() == 1 || node.getId() == 64) {
                    lblY = y + cellSize + 12;
                } else {
                    lblY = y + (int)(cellSize * 0.35) + (fmLbl.getAscent() / 2);
                }

                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawString(lbl, lblX+1, lblY+1);
                g2d.drawString(lbl, lblX-1, lblY-1);
                g2d.drawString(lbl, lblX+1, lblY-1);
                g2d.drawString(lbl, lblX-1, lblY+1);

                g2d.setColor(lblColor);
                g2d.drawString(lbl, lblX, lblY);
            }
        }

        drawPlayers(g2d, cellSize, startX, startY);

        if (notificationMessage != null) {
            drawNotification(g2d, w, h);
        }
    }

    private void drawNotification(Graphics2D g2d, int w, int h) {
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int msgWidth = fm.stringWidth(notificationMessage);
        int boxWidth = msgWidth + 40;
        int boxHeight = 50;

        int x = w - boxWidth - 20;
        int y = h - boxHeight - 20;

        g2d.setColor(notificationColor);
        g2d.fillRoundRect(x, y, boxWidth, boxHeight, 15, 15);
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, boxWidth, boxHeight, 15, 15);
        g2d.setColor(Color.WHITE);
        g2d.drawString(notificationMessage, x + 20, y + 30);
    }

    private void drawRandomLinks(Graphics2D g2d, int cellSize, int startX, int startY) {
        List<int[]> links = board.getRandomLinks();
        g2d.setStroke(new BasicStroke(4));
        g2d.setColor(LINK_COLOR);
        int centerOffset = cellSize / 2;

        for (int[] link : links) {
            Point p1 = getTopLeftCoordinates(link[0], cellSize, startX, startY);
            Point p2 = getTopLeftCoordinates(link[1], cellSize, startX, startY);

            int x1 = p1.x + centerOffset; int y1 = p1.y + centerOffset - (cellSize/6);
            int x2 = p2.x + centerOffset; int y2 = p2.y + centerOffset - (cellSize/6);

            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(8));
            g2d.drawLine(x1, y1, x2, y2);

            g2d.setColor(LINK_COLOR);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawLine(x1, y1, x2, y2);

            int dotSize = 14;
            g2d.setColor(Color.BLACK);
            g2d.fillOval(x1 - dotSize/2 - 1, y1 - dotSize/2 - 1, dotSize+2, dotSize+2);
            g2d.fillOval(x2 - dotSize/2 - 1, y2 - dotSize/2 - 1, dotSize+2, dotSize+2);

            g2d.setColor(LINK_COLOR);
            g2d.fillOval(x1 - dotSize/2, y1 - dotSize/2, dotSize, dotSize);
            g2d.fillOval(x2 - dotSize/2, y2 - dotSize/2, dotSize, dotSize);
        }
    }

    private Point getTopLeftCoordinates(int id, int cellSize, int startX, int startY) {
        int index = id - 1;
        int row = index / 8;
        int col = index % 8;
        int visualRow = 7 - row;
        int visualCol = (row % 2 == 0) ? col : (7 - col);
        int x = startX + (visualCol * cellSize);
        int y = startY + (visualRow * cellSize);
        return new Point(x, y);
    }

    private void drawPlayers(Graphics2D g2d, int cellSize, int startX, int startY) {
        if (players.isEmpty()) return;

        Map<Integer, List<Player>> playersByNode = new HashMap<>();
        for (Player p : players) {
            int id = p.getPosition().getId();
            playersByNode.computeIfAbsent(id, k -> new ArrayList<>()).add(p);
        }

        int shipVerticalOffset = -cellSize / 4;

        for (Map.Entry<Integer, List<Player>> entry : playersByNode.entrySet()) {
            int nodeId = entry.getKey();
            List<Player> playersOnThisNode = entry.getValue();
            Point pCoord = getTopLeftCoordinates(nodeId, cellSize, startX, startY);

            if (playersOnThisNode.size() == 1) {
                Player p = playersOnThisNode.get(0);
                int bigShipSize = (int)(cellSize * 0.85);
                int offset = (cellSize - bigShipSize) / 2;
                drawSingleShip(g2d, p, pCoord.x + offset, pCoord.y + offset + shipVerticalOffset, bigShipSize);
            } else {
                int smallShipSize = (int)(cellSize * 0.55);
                for (int i = 0; i < playersOnThisNode.size(); i++) {
                    Player p = playersOnThisNode.get(i);
                    int shiftX = (i % 2) * (cellSize / 3);
                    int shiftY = (i / 2) * (cellSize / 4);
                    int baseX = pCoord.x + cellSize/4;
                    int baseY = pCoord.y + cellSize/4 + shipVerticalOffset;

                    drawSingleShip(g2d, p, baseX + shiftX, baseY + shiftY, smallShipSize);
                }
            }
        }
    }

    private void drawSingleShip(Graphics2D g2d, Player p, int x, int y, int size) {
        int playerIndex = 0;
        for(int i=0; i<players.size(); i++) {
            if(players.get(i) == p) { playerIndex = i; break; }
        }
        BufferedImage img = null;
        if (shipImages != null && shipImages.length > 0) {
            img = shipImages[playerIndex % shipImages.length];
        }

        if (img != null) {
            int id = p.getPosition().getId();
            int row = (id - 1) / 8;
            boolean flip = (row % 2 != 0);

            if (flip) {
                g2d.drawImage(img, x + size, y, -size, size, null);
            } else {
                g2d.drawImage(img, x, y, size, size, null);
            }
        } else {
            g2d.setColor(p.getColor());
            g2d.fillOval(x, y, size, size);
        }
    }
}