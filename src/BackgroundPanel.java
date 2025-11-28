import javax.swing.*;
import java.awt.*;

public class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        backgroundImage = new ImageIcon("assets/" + imagePath).getImage();
        setLayout(null); // supaya bebas menaruh button di atasnya
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int panelWidth = getWidth();
        int imgWidth = backgroundImage.getWidth(null);
        int imgHeight = backgroundImage.getHeight(null);

        // Scale proporsional berdasarkan lebar window
        double scale = (double) panelWidth / imgWidth;
        int scaledWidth = panelWidth;
        int scaledHeight = (int) (imgHeight * scale);

        g.drawImage(backgroundImage, 0, 0, scaledWidth, scaledHeight, null);
    }
}
