import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CustomImageButton extends JButton {
    private Icon defaultIcon;
    private Icon hoverIcon;
    private Icon pressedIcon;

    public CustomImageButton(String normal, String hover, String pressed) {
        defaultIcon = new ImageIcon(normal);
        hoverIcon = new ImageIcon(hover);
        pressedIcon = new ImageIcon(pressed);

        setIcon(defaultIcon);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);

        // Hover handling
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setIcon(hoverIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setIcon(defaultIcon);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setIcon(pressedIcon);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setIcon(hoverIcon);
            }
        });
    }
}
