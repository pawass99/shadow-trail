import java.awt.*;
import java.util.List;
import java.util.Objects;
import javax.swing.*;

public class LevelBoardPanel extends JPanel {
    private static final int TITLE_FONT_SIZE = 20;
    private static final int PROGRESS_FONT_SIZE = 14;
    private static final Color PROGRESS_COLOR = new Color(0xFF6B35);
    private static final int SAVED_PROGRESS_BORDER_WIDTH = 3;
    private static final int GRID_SPACING = 20;
    private static final int TOP_PADDING = 24;
    private static final int TITLE_SPACING = 8;
    private static final int BOTTOM_BUTTON_SPACING = 16;
    private static final int BOTTOM_PADDING = 12;

    private final GameLauncher launcher;
    private final LevelManager levelManager;
    private User activeUser;
    private LevelConfig selectedLevel;
    private JPanel gridPanel;
    private JButton selectedButton;
    private JLabel progressLabel;

    public LevelBoardPanel(GameLauncher launcher, LevelManager levelManager) {
        this.launcher = launcher;
        this.levelManager = levelManager;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        BackgroundPanel backgroundPanel = new BackgroundPanel("levelboard_bg.png");
        backgroundPanel.setLayout(new BorderLayout());

        JPanel topPanel = createTopPanel();
        backgroundPanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerWrapper = createCenterWrapper();
        backgroundPanel.add(centerWrapper, BorderLayout.CENTER);

        JPanel bottomBar = createBottomBar();
        backgroundPanel.add(bottomBar, BorderLayout.SOUTH);

        add(backgroundPanel, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(TOP_PADDING, 0, TITLE_SPACING * 2, 0));

        JLabel title = new JLabel("LEVEL BOARD", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, (float) TITLE_FONT_SIZE));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        progressLabel = new JLabel("", SwingConstants.CENTER);
        progressLabel.setFont(progressLabel.getFont().deriveFont(Font.PLAIN, (float) PROGRESS_FONT_SIZE));
        progressLabel.setForeground(PROGRESS_COLOR);
        progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(title);
        topPanel.add(Box.createVerticalStrut(TITLE_SPACING));
        topPanel.add(progressLabel);

        return topPanel;
    }

    private JPanel createCenterWrapper() {
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        gridPanel = new JPanel(new GridLayout(1, 5, GRID_SPACING, GRID_SPACING));
        gridPanel.setOpaque(false);
        centerWrapper.add(gridPanel);

        return centerWrapper;
    }

    private JPanel createBottomBar() {
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, BOTTOM_BUTTON_SPACING, BOTTOM_PADDING));
        bottomBar.setOpaque(false);

        CustomImageButton backButton = new CustomImageButton(
            "assets/back.png", "assets/backh.png", "assets/backp.png");
        backButton.addActionListener(e -> launcher.showNewGame());
        bottomBar.add(backButton);

        return bottomBar;
    }

    public void setActiveUser(User activeUser) {
        this.activeUser = Objects.requireNonNull(activeUser, "User diperlukan untuk membuka level board");
        reloadLevels();
        updateProgressLabel();
    }

    private void updateProgressLabel() {
        if (activeUser != null) {
            String summary = launcher.getUserManager().getLevelProgressSummary(activeUser.getId());
            progressLabel.setText(summary);
        } else {
            progressLabel.setText("");
        }
    }

    private void reloadLevels() {
        gridPanel.removeAll();
        selectedLevel = null;
        selectedButton = null;

        if (activeUser != null) {
            List<LevelConfig> levels = levelManager.getAllLevels();
            int unlocked = Math.max(activeUser.getUnlockedLevel(), 1);

            for (LevelConfig config : levels) {
                if (config.getId() <= unlocked) {
                    JButton levelButton = buildLevelButton(config);
                    highlightSavedProgress(levelButton, config);
                    gridPanel.add(levelButton);

                    if (selectedLevel == null) {
                        selectedLevel = config;
                    }
                } else {
                    JButton locked = buildLockedButton();
                    gridPanel.add(locked);
                }
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void highlightSavedProgress(JButton levelButton, LevelConfig config) {
        int savedRound = launcher.getUserManager().getSavedRound(activeUser.getId(), config.getId());
        if (savedRound > 1) {
            levelButton.setBorder(BorderFactory.createLineBorder(
                PROGRESS_COLOR, SAVED_PROGRESS_BORDER_WIDTH));
        }
    }

    private JButton buildLevelButton(LevelConfig config) {
        String suffix = config.getId() == 1 ? "" : "-" + (config.getId() - 1);
        CustomImageButton button = new CustomImageButton(
            "assets/default" + suffix + ".png",
            "assets/hover" + suffix + ".png",
            "assets/pressed" + suffix + ".png"
        );
        button.addActionListener(e -> handlePlay(config));
        button.setBorder(null);
        button.setBorderPainted(false);
        return button;
    }

    private JButton buildLockedButton() {
        JButton button = new JButton(new ImageIcon("assets/level_locked.png"));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setEnabled(false);
        button.setDisabledIcon(button.getIcon());
        return button;
    }

    private void handlePlay(LevelConfig config) {
        selectedLevel = config;
        int roundCount = levelManager.getRoundCount(selectedLevel.getId());
        launcher.startGameplay(selectedLevel, roundCount);
    }
}