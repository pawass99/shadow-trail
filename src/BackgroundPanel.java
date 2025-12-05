import java.awt.*;
import javax.swing.*;

public class BackgroundPanel extends JPanel {
    private final Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        this.backgroundImage = new ImageIcon("assets/" + imagePath).getImage();
        setLayout(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int panelWidth = getWidth();
        int imgWidth = backgroundImage.getWidth(null);
        int imgHeight = backgroundImage.getHeight(null);

        double scale = (double) panelWidth / imgWidth;
        int scaledWidth = panelWidth;
        int scaledHeight = (int) (imgHeight * scale);

        g.drawImage(backgroundImage, 0, 0, scaledWidth, scaledHeight, null);
    }
}