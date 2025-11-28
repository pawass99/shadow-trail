import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {
    public MainMenuPanel(GameLauncher launcher) {
        setLayout(new BorderLayout());

        BackgroundPanel backgroundPanel = new BackgroundPanel("bg.png");
        backgroundPanel.setLayout(new GridBagLayout());

        JPanel buttonColumn = new JPanel();
        buttonColumn.setOpaque(false);
        buttonColumn.setLayout(new BoxLayout(buttonColumn, BoxLayout.Y_AXIS));

        Dimension uniform = new Dimension(240, 70);
        CustomImageButton newGameButton = new CustomImageButton("assets/ng.png", "assets/ngh.png", "assets/ngp.png");
        CustomImageButton continueButton = new CustomImageButton("assets/rs.png", "assets/rsh.png", "assets/rsp.png");
        CustomImageButton quitButton = new CustomImageButton("assets/quit.png", "assets/quith.png", "assets/quitp.png");

        newGameButton.setPreferredSize(uniform);
        continueButton.setPreferredSize(uniform);
        quitButton.setPreferredSize(uniform);

        newGameButton.addActionListener(e -> launcher.showNewGame());
        continueButton.addActionListener(e -> launcher.showContinue());
        quitButton.addActionListener(e -> System.exit(0));

        buttonColumn.add(newGameButton);
        buttonColumn.add(Box.createVerticalStrut(10));
        buttonColumn.add(continueButton);
        buttonColumn.add(Box.createVerticalStrut(10));
        buttonColumn.add(quitButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(300, 0, 0, 0);
        backgroundPanel.add(buttonColumn, gbc);
        add(backgroundPanel, BorderLayout.CENTER);
    }
}
