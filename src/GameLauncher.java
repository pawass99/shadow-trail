import java.awt.*;
import javax.swing.*;

public class GameLauncher {
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel rootPanel;
    private User currentUser;

    private final UserManager userManager;
    private final LevelManager levelManager;
    private final AudioManager audioManager;

    private MainMenuPanel mainMenuPanel;
    private NewGamePanel newGamePanel;
    private ContinuePanel continuePanel;
    private LevelBoardPanel levelBoardPanel;
    private GameplayPanel gameplayPanel;
    private LeaderboardPanel leaderboardPanel;

    public GameLauncher() {
        DatabaseManager databaseManager = new DatabaseManager();
        this.userManager = new UserManager(databaseManager);
        this.levelManager = new LevelManager(databaseManager);
        this.audioManager = new AudioManager();
        applyGlobalFont();
    }

    private void applyGlobalFont() {
        Font itim = new Font("Itim", Font.PLAIN, 14);
        UIManager.put("Label.font", itim);
        UIManager.put("Button.font", itim);
        UIManager.put("TextField.font", itim);
        UIManager.put("List.font", itim);
        UIManager.put("ComboBox.font", itim);
        UIManager.put("OptionPane.messageFont", itim);
        UIManager.put("OptionPane.buttonFont", itim);
    }

    public void initWindow() {
        frame = new JFrame("ShadowTrail");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 720);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);
        frame.setContentPane(rootPanel);

        buildPanels();

        frame.setVisible(true);
        showMainMenu();
    }

    private void buildPanels() {
        mainMenuPanel = new MainMenuPanel(this);
        newGamePanel = new NewGamePanel(this, userManager);
        continuePanel = new ContinuePanel(this, userManager);
        levelBoardPanel = new LevelBoardPanel(this, levelManager);
        gameplayPanel = new GameplayPanel(this, levelManager);
        leaderboardPanel = new LeaderboardPanel(this, userManager);

        rootPanel.add(mainMenuPanel, "mainMenu");
        rootPanel.add(newGamePanel, "newGame");
        rootPanel.add(continuePanel, "continue");
        rootPanel.add(levelBoardPanel, "levelBoard");
        rootPanel.add(gameplayPanel, "gameplay");
        rootPanel.add(leaderboardPanel, "leaderboard");
    }

    public void showMainMenu() {
        cardLayout.show(rootPanel, "mainMenu");
        audioManager.playMenuMusic();
    }

    public void showNewGame() {
        newGamePanel.refreshUsers();
        cardLayout.show(rootPanel, "newGame");
        audioManager.playMenuMusic();
    }

    public void showContinue() {
        continuePanel.refreshUsers();
        cardLayout.show(rootPanel, "continue");
        audioManager.playMenuMusic();
    }

    public void showLevelBoard(User user) {
        this.currentUser = user;
        levelBoardPanel.setActiveUser(user);
        cardLayout.show(rootPanel, "levelBoard");
        audioManager.playMenuMusic();
    }

    public void showLevelBoardCurrent() {
        if (currentUser != null) {
            User updatedUser = userManager.getUserById(currentUser.getId());
            if (updatedUser != null) {
                currentUser = updatedUser;
                showLevelBoard(currentUser);
            } else {
                JOptionPane.showMessageDialog(frame, 
                    "Save data tidak ditemukan. Kembali ke menu utama.");
                currentUser = null;
                showMainMenu();
            }
        } else {
            showMainMenu();
        }
    }

    public void showLeaderboard() {
        leaderboardPanel.refreshLeaderboard();
        cardLayout.show(rootPanel, "leaderboard");
        audioManager.playMenuMusic();
    }

    public void startGameplay(LevelConfig config, int roundCount) {
        audioManager.stopMusic();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(frame, "Error: No user selected!");
            showMainMenu();
            return;
        }
        gameplayPanel.loadLevel(config, roundCount, currentUser);
        cardLayout.show(rootPanel, "gameplay");
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void shutdown() {
        if (gameplayPanel != null) {
            gameplayPanel.cleanup();
        }
        audioManager.shutdown();
        frame.dispose();
    }
}
