import java.awt.*;
import javax.swing.*;

public class ContinuePanel extends JPanel {
    private final GameLauncher launcher;
    private final UserManager userManager;
    private final JPanel listPanel = new JPanel();

    public ContinuePanel(GameLauncher launcher, UserManager userManager) {
        this.launcher = launcher;
        this.userManager = userManager;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        BackgroundPanel bg = new BackgroundPanel("gameplay_bg.png");
        bg.setLayout(new BorderLayout());
        bg.setBorder(BorderFactory.createEmptyBorder(24, 32, 32, 32));

        JLabel title = new JLabel("Continue Save", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        bg.add(title, BorderLayout.NORTH);

        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        bg.add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        JButton back = new JButton("Back");
        back.addActionListener(e -> launcher.showMainMenu());
        buttons.add(back);
        bg.add(buttons, BorderLayout.SOUTH);

        add(bg, BorderLayout.CENTER);
    }

    public void refreshUsers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refreshUsers'");
    }

}
