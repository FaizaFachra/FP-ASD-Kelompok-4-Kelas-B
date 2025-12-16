import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BackgroundPanel extends JPanel {
    private BufferedImage backgroundImage;

    public BackgroundPanel(String imagePath) {
        try {
            File f = new File(imagePath);
            if(!f.exists()) {
                if(imagePath.endsWith(".jpg")) f = new File(imagePath.replace(".jpg", ".png"));
                else if(imagePath.endsWith(".png")) f = new File(imagePath.replace(".png", ".jpg"));
            }

            if (f.exists()) {
                backgroundImage = ImageIO.read(f);
            } else {
                System.err.println("Background image not found: " + imagePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, new Color(44, 62, 80), 0, getHeight(), new Color(20, 30, 40));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}