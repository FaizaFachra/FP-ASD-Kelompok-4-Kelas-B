import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class DicePanel extends JPanel {
    private int value = 1;
    private Timer rollTimer;
    private int animationSteps = 0;
    private int finalValue = 1;
    private Runnable onAnimationEnd;
    private Random random;

    public DicePanel() {
        setPreferredSize(new Dimension(80, 80));
        setMinimumSize(new Dimension(80, 80));
        setMaximumSize(new Dimension(80, 80));

        setBackground(Color.WHITE);
        setOpaque(false);
        random = new Random();

        rollTimer = new Timer(80, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                value = random.nextInt(6) + 1;
                repaint();
                animationSteps++;
                if (animationSteps > 15) {
                    stopRoll();
                }
            }
        });
    }

    public void roll(int targetValue, Runnable callback) {
        this.finalValue = targetValue;
        this.onAnimationEnd = callback;
        this.animationSteps = 0;
        rollTimer.start();
    }

    private void stopRoll() {
        rollTimer.stop();
        value = finalValue;
        repaint();
        if (onAnimationEnd != null) {
            onAnimationEnd.run();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        int diceSize = 60;
        int x = (w - diceSize) / 2;
        int y = (h - diceSize) / 2;

        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(x, y, diceSize, diceSize, 15, 15);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, diceSize, diceSize, 15, 15);

        g2d.setColor(new Color(44, 62, 80));
        int dotSize = diceSize / 5;

        int center = x + (diceSize - dotSize) / 2;
        int left = x + (diceSize / 4 - dotSize / 2);
        int right = x + (diceSize * 3 / 4 - dotSize / 2);
        int top = y + (diceSize / 4 - dotSize / 2);
        int bottom = y + (diceSize * 3 / 4 - dotSize / 2);

        switch (value) {
            case 1: drawDot(g2d, center, center, dotSize); break;
            case 2: drawDot(g2d, left, top, dotSize); drawDot(g2d, right, bottom, dotSize); break;
            case 3: drawDot(g2d, left, top, dotSize); drawDot(g2d, center, center, dotSize); drawDot(g2d, right, bottom, dotSize); break;
            case 4: drawDot(g2d, left, top, dotSize); drawDot(g2d, right, top, dotSize); drawDot(g2d, left, bottom, dotSize); drawDot(g2d, right, bottom, dotSize); break;
            case 5: drawDot(g2d, left, top, dotSize); drawDot(g2d, right, top, dotSize); drawDot(g2d, center, center, dotSize); drawDot(g2d, left, bottom, dotSize); drawDot(g2d, right, bottom, dotSize); break;
            case 6: drawDot(g2d, left, top, dotSize); drawDot(g2d, right, top, dotSize); drawDot(g2d, left, center, dotSize); drawDot(g2d, right, center, dotSize); drawDot(g2d, left, bottom, dotSize); drawDot(g2d, right, bottom, dotSize); break;
        }
    }

    private void drawDot(Graphics2D g, int x, int y, int size) {
        g.fillOval(x, y, size, size);
    }
}