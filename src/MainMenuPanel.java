import java.awt.*;
import javax.swing.*;

public class MainMenuPanel extends JPanel {
    private static final Dimension BUTTON_SIZE = new Dimension(240, 70);
    private static final int BUTTON_SPACING = 10;
    private static final int TOP_MARGIN = 300;

    public MainMenuPanel(GameLauncher launcher) {
        setLayout(new BorderLayout());

        BackgroundPanel backgroundPanel = new BackgroundPanel("bg.png");
        backgroundPanel.setLayout(new GridBagLayout());

        JPanel buttonColumn = createButtonPanel(launcher);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(TOP_MARGIN, 0, 0, 0);
        backgroundPanel.add(buttonColumn, gbc);

        add(backgroundPanel, BorderLayout.CENTER);
    }

    private JPanel createButtonPanel(GameLauncher launcher) {
        JPanel buttonColumn = new JPanel();
        buttonColumn.setOpaque(false);
        buttonColumn.setLayout(new BoxLayout(buttonColumn, BoxLayout.Y_AXIS));

        CustomImageButton newGameButton = createButton("ng");
        CustomImageButton continueButton = createButton("rs");
        CustomImageButton leaderboardButton = createButton("ld");
        CustomImageButton quitButton = createButton("quit");

        newGameButton.addActionListener(e -> launcher.showNewGame());
        continueButton.addActionListener(e -> launcher.showContinue());
        leaderboardButton.addActionListener(e -> launcher.showLeaderboard());
        quitButton.addActionListener(e -> System.exit(0));

        buttonColumn.add(newGameButton);
        buttonColumn.add(Box.createVerticalStrut(BUTTON_SPACING));
        buttonColumn.add(continueButton);
        buttonColumn.add(Box.createVerticalStrut(BUTTON_SPACING));
        buttonColumn.add(leaderboardButton);
        buttonColumn.add(Box.createVerticalStrut(BUTTON_SPACING));
        buttonColumn.add(quitButton);

        return buttonColumn;
    }

    private CustomImageButton createButton(String name) {
        CustomImageButton button = new CustomImageButton(
            "assets/" + name + ".png",
            "assets/" + name + "h.png",
            "assets/" + name + "p.png"
        );
        button.setPreferredSize(BUTTON_SIZE);
        return button;
    }
}