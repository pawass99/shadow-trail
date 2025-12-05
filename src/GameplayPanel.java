import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;

public class GameplayPanel extends JPanel {
    private final GameLauncher launcher;
    private final LevelManager levelManager;
    private User currentUser;
    private LevelConfig levelConfig;
    private int currentRound = 1;
    private int totalRounds = 6;
    
    private Timer countdownTimer;
    private int remainingSeconds = 30;
    private static final int ROUND_TIME_LIMIT = 30;
    
    private long levelScore = 0; 
    private static final long WIN_POINTS = 100; 
    private static final long FAIL_PENALTY = 25; 
    private static final long MIN_SCORE_TO_UNLOCK = 300; 
    
    private int currentSteps = 0;
    private int totalHazardTouches = 0;
    private long roundStartTime = 0;

    private final JPanel hudPanel = new JPanel(new BorderLayout());
    private final JPanel hudContainer = new JPanel(new GridBagLayout());
    private final JLabel timerLabel = new JLabel("10");
    private final JLabel roundLabel = new JLabel("Round 1");
    private final JLabel scoreLabel = new JLabel("Score: 0");
    private final JButton pauseButton = new JButton(new ImageIcon("assets/pause.png"));
    private boolean paused = false;
    private boolean isShowingDialog = false;

    private JPanel boardWrapper;
    private JPanel gridPanel;
    private JButton[][] tileButtons;
    private ImageIcon defaultIcon;
    private ImageIcon pressedIcon;
    private ImageIcon hazardIcon;
    private ImageIcon pointDefaultIcon;
    private ImageIcon pointPressedIcon;

    private Image baseDefaultImage;
    private Image basePressedImage;
    private Image baseHazardImage;
    private Image basePointDefaultImage;
    private Image basePointPressedImage;

    private final List<Point> pathPoints = new ArrayList<>();
    private Set<Point> hazards = new HashSet<>();
    private List<Point> endpoints = new ArrayList<>();
    private boolean showHazards = false;
    private boolean showEndpoints = false;

    private Timer hazardPreviewTimer;
    private Timer endpointRevealTimer;

    public GameplayPanel(GameLauncher launcher, LevelManager levelManager) {
        this.launcher = launcher;
        this.levelManager = levelManager;
        setLayout(new BorderLayout());

        loadBaseImages();

        BackgroundPanel backgroundPanel = new BackgroundPanel("gameplay_bg.png");
        backgroundPanel.setLayout(new BorderLayout());

        hudPanel.setOpaque(false);
        hudContainer.setOpaque(false);

        timerLabel.setForeground(new Color(0xFF0000));
        timerLabel.setFont(timerLabel.getFont().deriveFont(Font.BOLD, 24f));
        roundLabel.setForeground(new Color(0x1F6D8C));
        roundLabel.setFont(roundLabel.getFont().deriveFont(Font.BOLD, 18f));
        scoreLabel.setForeground(new Color(0x1F6D8C));
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 18f));

        pauseButton.setBorderPainted(false);
        pauseButton.setContentAreaFilled(false);
        pauseButton.setFocusPainted(false);
        pauseButton.setOpaque(false);
        pauseButton.addActionListener(e -> showPauseOverlay());

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        leftBox.setOpaque(false);
        leftBox.add(new JLabel("Time: "));
        leftBox.add(timerLabel);

        JPanel centerBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        centerBox.setOpaque(false);
        centerBox.add(roundLabel);

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        rightBox.setOpaque(false);
        rightBox.add(scoreLabel);

        hudPanel.add(leftBox, BorderLayout.WEST);
        hudPanel.add(centerBox, BorderLayout.CENTER);
        hudPanel.add(rightBox, BorderLayout.EAST);

        hudPanel.setPreferredSize(new Dimension(620, 40));

        GridBagConstraints gbcHud = new GridBagConstraints();
        gbcHud.gridx = 0;
        gbcHud.gridy = 0;
        gbcHud.insets = new Insets(0, 100, 0, 0);
        hudContainer.add(hudPanel, gbcHud);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(hudContainer, BorderLayout.CENTER);
        topBar.add(pauseButton, BorderLayout.EAST);

        backgroundPanel.add(topBar, BorderLayout.NORTH);

        boardWrapper = new JPanel(new GridBagLayout());
        boardWrapper.setOpaque(false);
        backgroundPanel.add(boardWrapper, BorderLayout.CENTER);

        add(backgroundPanel, BorderLayout.CENTER);
    }

    public void loadLevel(LevelConfig config, int roundCount, User user) {
        this.levelConfig = config;
        this.totalRounds = roundCount;
        this.currentUser = user;
        this.totalHazardTouches = 0;
        this.paused = false;
        this.isShowingDialog = false;
        
        int maxLevelInDB = levelManager.getMaxLevelInDatabase();
        int currentUnlockedLevel = Math.min(user.getUnlockedLevel(), maxLevelInDB);
        
        if (user.getUnlockedLevel() > maxLevelInDB) {
            System.out.println("WARNING - User unlocked level (" + user.getUnlockedLevel() + 
                            ") exceeds max level in DB (" + maxLevelInDB + "). Fixing...");
            launcher.getUserManager().updateUnlockedLevel(user.getId(), maxLevelInDB);
            currentUnlockedLevel = maxLevelInDB;
            
            User updatedUser = launcher.getUserManager().getUserById(user.getId());
            if (updatedUser != null) {
                this.currentUser = updatedUser;
            }
        }
        
        boolean isHighestLevel = (config.getId() == currentUnlockedLevel);
        
        System.out.println("DEBUG - Current Level: " + config.getId());
        System.out.println("DEBUG - Max Level in DB: " + maxLevelInDB);
        System.out.println("DEBUG - Unlocked Level (corrected): " + currentUnlockedLevel);
        System.out.println("DEBUG - Is Highest Level: " + isHighestLevel);
        
        int savedRound = launcher.getUserManager().getSavedRound(user.getId(), config.getId());
        long tempScore = launcher.getUserManager().getTempScore(user.getId(), config.getId());
        
        if (savedRound > 1) {
            int choice = JOptionPane.showConfirmDialog(this,
                "Kamu memiliki progress tersimpan di Level " + config.getId() + 
                ", Round " + savedRound + ".\n" +
                "Current Score: " + tempScore + "\n" +
                "Lanjutkan dari round tersebut?",
                "Resume Progress",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (choice == JOptionPane.YES_OPTION) {
                this.currentRound = savedRound;
                this.levelScore = tempScore;
            } else {
                this.currentRound = 1;
                this.levelScore = 0;
                launcher.getUserManager().resetLevelProgress(user.getId(), config.getId());
                launcher.getUserManager().updateTempScore(user.getId(), config.getId(), 0);
            }
        } else {
            this.currentRound = 1;
            this.levelScore = 0;
        }
        
        roundLabel.setText("Round " + currentRound);
        scoreLabel.setText("Score: " + levelScore);
        buildGrid();
        prepareRound();
    }

   private void handleLevelComplete() {
        boolean canUnlock = levelScore >= MIN_SCORE_TO_UNLOCK;
        
        int maxLevelInDB = levelManager.getMaxLevelInDatabase();
        int currentUnlockedLevel = Math.min(currentUser.getUnlockedLevel(), maxLevelInDB);
        
        int nextLevelId = currentUnlockedLevel + 1;
        boolean hasNextLevel = (nextLevelId <= maxLevelInDB);
        
        if (hasNextLevel) {
            LevelConfig nextLevelCheck = levelManager.loadLevel(nextLevelId);
            hasNextLevel = (nextLevelCheck != null && nextLevelCheck.getId() == nextLevelId);
        }
        
        boolean isHighestLevel = (levelConfig.getId() == currentUnlockedLevel);
        
        boolean isLastLevelInDB = (levelConfig.getId() == maxLevelInDB);
        
        System.out.println("DEBUG - Current Level: " + levelConfig.getId());
        System.out.println("DEBUG - Max Level in DB: " + maxLevelInDB);
        System.out.println("DEBUG - Unlocked Level (corrected): " + currentUnlockedLevel);
        System.out.println("DEBUG - Is Highest Level: " + isHighestLevel);
        System.out.println("DEBUG - Is Last Level in DB: " + isLastLevelInDB);
        
        if (currentUser != null) {
            if (isLastLevelInDB) {
                launcher.getUserManager().saveLevelScoreRepeatable(
                    currentUser.getId(), 
                    levelConfig.getId(), 
                    levelScore
                );
                
                System.out.println("DEBUG - Repeatable Score! Added +" + levelScore + 
                                " to total (Level " + levelConfig.getId() + ")");
            } 
            else if (isHighestLevel) {
                launcher.getUserManager().saveLevelScore(
                    currentUser.getId(), 
                    levelConfig.getId(), 
                    levelScore
                );
                
                System.out.println("DEBUG - Normal Score! Replaced score with " + levelScore + 
                                " (Level " + levelConfig.getId() + ")");
            }
            
            launcher.getUserManager().resetLevelProgress(
                currentUser.getId(),
                levelConfig.getId()
            );
            
            User updatedUser = launcher.getUserManager().getUserById(currentUser.getId());
            if (updatedUser != null) {
                currentUser = updatedUser;
            }
        
            if (canUnlock && isHighestLevel && hasNextLevel) {
                launcher.getUserManager().updateUnlockedLevel(currentUser.getId(), nextLevelId);
                
                updatedUser = launcher.getUserManager().getUserById(currentUser.getId());
                if (updatedUser != null) {
                    currentUser = updatedUser;
                }
                
                JOptionPane.showMessageDialog(this, 
                    "🎉 LEVEL CLEAR! 🎉\n\n" +
                    "Level Score: " + levelScore + "\n" +
                    "Total Score: " + updatedUser.getTotalScore() + "\n" +
                    "Level berikutnya terbuka!",
                    "Level Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            } 
            else if (canUnlock && isHighestLevel && !hasNextLevel && isLastLevelInDB) {
                JOptionPane.showMessageDialog(this, 
                    "🎉 FINAL LEVEL CLEAR! 🎉\n\n" +
                    "Level Score: " + levelScore + "\n" +
                    "Total Score: " + updatedUser.getTotalScore() + "\n\n" +
                    "⭐ LEVEL TERAKHIR - REPEATABLE! ⭐\n" +
                    "Kamu bisa main level ini berulang kali!\n" +
                    "Score akan TERUS BERTAMBAH setiap permainan!\n\n" +
                    "✨ Main lagi untuk farming score!",
                    "All Levels Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            else if (!canUnlock) {
                String message = "❌ Level Tidak Selesai!\n\n" +
                    "Your Score: " + levelScore + "\n" +
                    "Minimal Score: " + MIN_SCORE_TO_UNLOCK + "\n" +
                    "Kurang: " + (MIN_SCORE_TO_UNLOCK - levelScore) + " points\n\n";
                
                if (isHighestLevel || isLastLevelInDB) {
                    if (isLastLevelInDB) {
                        message += "⚠️ Score sudah tersimpan di leaderboard.\n" +
                                "🔄 Ini level terakhir - main lagi untuk farming score!";
                    } else {
                        message += "⚠️ Score sudah tersimpan di leaderboard.\n" +
                                "Coba lagi untuk unlock level berikutnya!";
                    }
                } else {
                    message += "Level ini bukan level tertinggimu.\nScore tidak dihitung di leaderboard.";
                }
                
                JOptionPane.showMessageDialog(this, message, "Level Failed", JOptionPane.WARNING_MESSAGE);
            } 
            else if (isLastLevelInDB) {
                JOptionPane.showMessageDialog(this,
                    "✅ Level Terakhir Clear! 🔄\n\n" +
                    "Score Gained: +" + levelScore + "\n" +
                    "Total Score: " + updatedUser.getTotalScore() + "\n\n" +
                    "⭐ REPEATABLE MODE AKTIF! ⭐\n" +
                    "Score DITAMBAHKAN ke total (bukan di-replace)!\n" +
                    "Main lagi untuk terus farming score!",
                    "Last Level Complete (Farming Mode)",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            else if (isHighestLevel) {
                JOptionPane.showMessageDialog(this,
                    "✅ Level Clear! (Updated)\n\n" +
                    "Level Score: " + levelScore + "\n" +
                    "Total Score: " + updatedUser.getTotalScore() + "\n\n" +
                    "✨ Score updated di leaderboard!",
                    "Level Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            } 
            else {
                JOptionPane.showMessageDialog(this,
                    "✅ Level Clear!\n\n" +
                    "Level Score: " + levelScore + "\n" +
                    "Total Score: " + updatedUser.getTotalScore() + "\n\n" +
                    "⚠️ Score tidak ditambahkan (bukan level tertinggi)",
                    "Level Complete (No Score Update)",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
        
        launcher.showLevelBoardCurrent();
    }


    private void buildGrid() {
        boardWrapper.removeAll();
        int rows = levelConfig.getGridRows();
        int cols = levelConfig.getGridCols();
        gridPanel = new JPanel(new GridLayout(rows, cols, 2, 2));
        gridPanel.setOpaque(false);
        gridPanel.setPreferredSize(new Dimension(620, 620));

        int gap = 2;
        int gridSize = gridPanel.getPreferredSize().width;
        int cellWidth = Math.max(1, (gridSize - (cols - 1) * gap) / cols);
        int cellHeight = Math.max(1, (gridSize - (rows - 1) * gap) / rows);

        defaultIcon = scaleIcon(baseDefaultImage, cellWidth, cellHeight);
        pressedIcon = scaleIcon(basePressedImage, cellWidth, cellHeight);
        hazardIcon = scaleIcon(baseHazardImage, cellWidth, cellHeight);
        pointDefaultIcon = scaleIcon(basePointDefaultImage, cellWidth, cellHeight);
        pointPressedIcon = scaleIcon(basePointPressedImage, cellWidth, cellHeight);

        tileButtons = new JButton[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JButton btn = createTileButton(r, c);
                tileButtons[r][c] = btn;
                gridPanel.add(btn);
            }
        }

        boardWrapper.add(gridPanel, new GridBagConstraints());
        boardWrapper.revalidate();
        boardWrapper.repaint();
    }

    private void loadBaseImages() {
        baseDefaultImage = loadImage("tile_default.png");
        basePressedImage = loadImage("tile_pressed.png");
        baseHazardImage = loadImage("tile_hazard.png");
        basePointDefaultImage = loadImage("tilepoint_default.png");
        basePointPressedImage = loadImage("tilepoint_pressed.png");
    }

    private Image loadImage(String fileName) {
        File iconFile = new File("assets", fileName);
        if (!iconFile.exists()) {
            System.err.println("Icon not found: " + iconFile.getAbsolutePath());
            return null;
        }
        return new ImageIcon(iconFile.getAbsolutePath()).getImage();
    }

    private ImageIcon scaleIcon(Image base, int width, int height) {
        if (base == null || width <= 0 || height <= 0) {
            return new ImageIcon();
        }
        return new ImageIcon(base.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    private JButton createTileButton(int row, int col) {
        JButton button = new JButton(defaultIcon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        
        button.addActionListener(tileClickHandler(row, col));
        return button;
    }

    private ActionListener tileClickHandler(int row, int col) {
        return e -> handleTileClick(row, col);
    }

    private void prepareRound() {
        stopAllTimers();
        
        int maxAttempts = 100;
        boolean validSetup = false;
        
        for (int attempt = 0; attempt < maxAttempts && !validSetup; attempt++) {
            hazards = levelManager.generateDangerousTiles(levelConfig);
            endpoints = levelManager.generateTwoBluePoints(levelConfig, hazards);
            
            if (hasValidPath(endpoints.get(0), endpoints.get(1), hazards)) {
                validSetup = true;
            }
        }
        
        if (!validSetup) {
            hazards.clear();
            endpoints = levelManager.generateTwoBluePoints(levelConfig, hazards);
        }
        
        pathPoints.clear();
        currentSteps = 0;
        roundStartTime = System.currentTimeMillis();
        
        setTilesEnabled(false);
        
        showHazards = true;
        showEndpoints = false;
        refreshTiles();

        hazardPreviewTimer = new Timer(1200, e -> {
            showHazards = false;
            ((Timer) e.getSource()).stop();
            startEndpointReveal();
            refreshTiles();
        });
        hazardPreviewTimer.setRepeats(false);
        hazardPreviewTimer.start();
    }
    
    private void stopAllTimers() {
        if (hazardPreviewTimer != null && hazardPreviewTimer.isRunning()) {
            hazardPreviewTimer.stop();
        }
        if (endpointRevealTimer != null && endpointRevealTimer.isRunning()) {
            endpointRevealTimer.stop();
        }
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
    }
    
    private void setTilesEnabled(boolean enabled) {
        if (tileButtons == null) return;
        int rows = levelConfig.getGridRows();
        int cols = levelConfig.getGridCols();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tileButtons[r][c].setEnabled(enabled);
            }
        }
    }
    
    private boolean hasValidPath(Point start, Point end, Set<Point> obstacles) {
        if (start.equals(end)) return true;
        
        int rows = levelConfig.getGridRows();
        int cols = levelConfig.getGridCols();
        
        Queue<Point> queue = new LinkedList<>();
        Set<Point> visited = new HashSet<>();
        
        queue.offer(start);
        visited.add(start);
        
        int[][] directions = {{0,1}, {1,0}, {0,-1}, {-1,0}};
        
        while (!queue.isEmpty()) {
            Point current = queue.poll();
            
            for (int[] dir : directions) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];
                Point next = new Point(newX, newY);
                
                if (newX < 0 || newX >= cols || newY < 0 || newY >= rows) continue;
                if (visited.contains(next) || obstacles.contains(next)) continue;
                if (next.equals(end)) return true;
                
                visited.add(next);
                queue.offer(next);
            }
        }
        
        return false;
    }

    private void startEndpointReveal() {
        if (endpointRevealTimer != null && endpointRevealTimer.isRunning()) {
            endpointRevealTimer.stop();
        }
        endpointRevealTimer = new Timer(200, e -> {
            showEndpoints = true;
            ((Timer) e.getSource()).stop();
            
            setTilesEnabled(true);
            
            startCountdown();
            refreshTiles();
        });
        endpointRevealTimer.setRepeats(false);
        endpointRevealTimer.start();
    }
    
    private void startCountdown() {
        remainingSeconds = ROUND_TIME_LIMIT;
        timerLabel.setText(String.valueOf(remainingSeconds));
        timerLabel.setForeground(new Color(0xFF0000));
        
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        
        countdownTimer = new Timer(1000, e -> {
            remainingSeconds--;
            timerLabel.setText(String.valueOf(remainingSeconds));
            
            if (remainingSeconds <= 3) {
                timerLabel.setForeground(new Color(0xFF0000));
            } else if (remainingSeconds <= 5) {
                timerLabel.setForeground(Color.ORANGE);
            } else {
                timerLabel.setForeground(new Color(0xFF0000));
            }
            
            if (remainingSeconds <= 0) {
                countdownTimer.stop();
                handleTimeOut();
            }
        });
        countdownTimer.start();
    }
    
    private void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
    }
    
    private void handleTimeOut() {
        setTilesEnabled(false);
        
        levelScore -= FAIL_PENALTY;
        scoreLabel.setText("Score: " + levelScore);
        
        JOptionPane.showMessageDialog(this, 
            "⏰ Waktu habis! -" + FAIL_PENALTY + " points\n" +
            "Current Score: " + levelScore + "\n" +
            "Ulangi round ini.",
            "Time Out",
            JOptionPane.WARNING_MESSAGE);
        
        prepareRound();
    }

private void refreshTiles() {
    if (tileButtons == null || levelConfig == null) {
        return;
    }
    int rows = levelConfig.getGridRows();
    int cols = levelConfig.getGridCols();
    for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {
            Point p = new Point(c, r);
            JButton btn = tileButtons[r][c];

            if (showHazards && hazards.contains(p)) {
                btn.setIcon(hazardIcon);
            } else if (showEndpoints && endpoints.contains(p)) {
                boolean pressedEndpoint = pathPoints.contains(p);
                btn.setIcon(pressedEndpoint ? pointPressedIcon : pointDefaultIcon);
            } else if (pathPoints.contains(p)) {
                btn.setIcon(pressedIcon);
            } else {
                btn.setIcon(defaultIcon);
            }
            
            btn.setDisabledIcon(btn.getIcon());
        }
    }
    
    gridPanel.revalidate();
    gridPanel.repaint();
}

    private void pauseGame() {
        if (!paused && countdownTimer != null) {
            countdownTimer.stop();
            paused = true;
            
            if (currentUser != null && levelConfig != null) {
                launcher.getUserManager().saveCurrentProgress(
                    currentUser.getId(), 
                    levelConfig.getId(), 
                    currentRound
                );
            }
        }
    }

    private void resumeGame() {
        if (paused && remainingSeconds > 0) {
            paused = false;
            if (countdownTimer != null) {
                countdownTimer.start();
            }
        }
    }

    public void handleTileClick(int row, int col) {
        if (levelConfig == null) {
            System.out.println("DEBUG: levelConfig is null");
            return;
        }
        if (!showEndpoints) {
            System.out.println("DEBUG: Endpoints not shown yet");
            return;
        }
        if (paused) {
            System.out.println("DEBUG: Game is paused");
            return;
        }
        if (isShowingDialog) {
            System.out.println("DEBUG: Dialog is showing");
            return;
        }
        
        Point gridPoint = new Point(col, row);
        if (hazards.contains(gridPoint)) {
            isShowingDialog = true;
            setTilesEnabled(false);
            totalHazardTouches++;
            levelScore -= FAIL_PENALTY;
            scoreLabel.setText("Score: " + levelScore);
            
            stopCountdown();
            JOptionPane.showMessageDialog(this, 
                "💥 Kena hazard! -" + FAIL_PENALTY + " points\n" +
                "Current Score: " + levelScore + "\n" +
                "Ulangi round ini.",
                "Hazard Hit",
                JOptionPane.WARNING_MESSAGE);
            
            isShowingDialog = false;
            prepareRound();
            return;
        }
        
        if (pathPoints.contains(gridPoint)) {
            return;
        }
        
        if (!pathPoints.isEmpty()) {
            Point lastPoint = pathPoints.get(pathPoints.size() - 1);
            if (!isAdjacent(lastPoint, gridPoint)) {
                return;
            }
        } else {
            if (!endpoints.contains(gridPoint)) {
                return;
            }
        }
        
        pathPoints.add(gridPoint);
        currentSteps++;
        refreshTiles();

        if (checkRoundComplete()) {
            stopCountdown();
            completeCurrentRound();
        }
    }
    
    private boolean isAdjacent(Point p1, Point p2) {
        int dx = Math.abs(p1.x - p2.x);
        int dy = Math.abs(p1.y - p2.y);
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    public boolean checkConnected() {
        if (endpoints == null || endpoints.size() < 2) {
            return false;
        }
        return pathPoints.containsAll(endpoints);
    }

    public boolean checkPathContinuous() {
        if (pathPoints.size() < 2) return true;
        
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            if (!isAdjacent(pathPoints.get(i), pathPoints.get(i + 1))) {
                return false;
            }
        }
        return true;
    }

    public boolean checkRoundComplete() {
        return checkConnected() && checkPathContinuous();
    }

    private void completeCurrentRound() {
        long roundElapsedMs = System.currentTimeMillis() - roundStartTime;
        levelScore += WIN_POINTS;
        scoreLabel.setText("Score: " + levelScore);
        
        if (currentUser != null) {
            launcher.getUserManager().recordRoundResult(
                currentUser.getId(),
                levelConfig.getId(),
                currentRound,
                roundElapsedMs,
                WIN_POINTS, 
                currentSteps,
                totalHazardTouches
            );
            
            launcher.getUserManager().updateTempScore(
                currentUser.getId(),
                levelConfig.getId(),
                levelScore
            );
            
            launcher.getUserManager().saveCurrentProgress(
                currentUser.getId(),
                levelConfig.getId(),
                currentRound + 1
            );
        }
       
        if (currentRound >= totalRounds) {
            stopCountdown();
            handleLevelComplete();
            return;
        }
       
        currentRound++;
        roundLabel.setText("Round " + currentRound);
        prepareRound();
    }
    

    public void cleanup() {
        stopAllTimers();
        setTilesEnabled(false);
    }

    private void showPauseOverlay() {
        pauseGame();

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Paused", true);
        dialog.setUndecorated(true);

        Image rawImg = new ImageIcon("assets/pause_panel.png").getImage();
        JPanel overlay = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(rawImg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        overlay.setOpaque(false);

        CustomImageButton resumeBtn = new CustomImageButton("assets/rs.png", "assets/rsh.png", "assets/rsp.png");
        resumeBtn.addActionListener(e -> {
            dialog.dispose();
            resumeGame();
        });

        CustomImageButton menuBtn = new CustomImageButton("assets/mainmenu.png", "assets/mainmenuh.png",
                "assets/mainmenup.png");
        menuBtn.addActionListener(e -> {
            cleanup();
            dialog.dispose();
            launcher.showMainMenu();
        });

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.add(resumeBtn);
        buttons.add(Box.createVerticalStrut(12));
        buttons.add(menuBtn);

        overlay.add(buttons, new GridBagConstraints());

        dialog.setContentPane(overlay);
        if (owner != null) {
            dialog.setSize(owner.getSize());
            dialog.setLocationRelativeTo(owner);
        } else {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            dialog.setSize(screen);
            dialog.setLocationRelativeTo(null);
        }
        dialog.setVisible(true);
    }
}
