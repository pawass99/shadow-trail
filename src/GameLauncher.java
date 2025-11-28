import javax.swing.*;
import java.awt.*;

public class GameLauncher {
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel rootPanel;
    private User currentUser;

    private final UserManager userManager;
    private final LevelManager levelManager;

    private MainMenuPanel mainMenuPanel;
    private NewGamePanel newGamePanel;
    private ContinuePanel continuePanel;
    private LevelBoardPanel levelBoardPanel;
    private GameplayPanel gameplayPanel;

    public GameLauncher() {
        DatabaseManager databaseManager = new DatabaseManager(); // stubbed for now
        this.userManager = new UserManager(databaseManager);
        this.levelManager = new LevelManager(databaseManager);
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

        rootPanel.add(mainMenuPanel, "mainMenu");
        rootPanel.add(newGamePanel, "newGame");
        rootPanel.add(continuePanel, "continue");
        rootPanel.add(levelBoardPanel, "levelBoard");
        rootPanel.add(gameplayPanel, "gameplay");
    }

    public void showMainMenu() {
        cardLayout.show(rootPanel, "mainMenu");
    }

    public void showNewGame() {
        newGamePanel.refreshUsers();
        cardLayout.show(rootPanel, "newGame");
    }

    public void showContinue() {
        continuePanel.refreshUsers();
        cardLayout.show(rootPanel, "continue");
    }

    public void showLevelBoard(User user) {
        this.currentUser = user;
        levelBoardPanel.setActiveUser(user);
        cardLayout.show(rootPanel, "levelBoard");
    }

    public void showLevelBoardCurrent() {
        if (currentUser != null) {
            showLevelBoard(currentUser);
        } else {
            showMainMenu();
        }
    }

    public void startGameplay(LevelConfig config, int roundCount) {
        gameplayPanel.loadLevel(config, roundCount);
        cardLayout.show(rootPanel, "gameplay");
    }
}
