import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LeaderboardPanel extends JPanel {
    private static final int TITLE_FONT_SIZE = 26;
    private static final int HEADER_FONT_SIZE = 14;
    private static final int ENTRY_FONT_SIZE = 13;
    private static final Color TITLE_COLOR = new Color(0xFFD700);
    private static final Color HEADER_COLOR = new Color(0x1F6D8C);
    private static final Color GOLD_BG = new Color(255, 215, 0, 80);
    private static final Color SILVER_BG = new Color(192, 192, 192, 80);
    private static final Color BRONZE_BG = new Color(205, 127, 50, 80);
    private static final Color DEFAULT_BG = new Color(255, 255, 255, 40);
    private static final int TITLE_PADDING = 10;
    private static final int TITLE_BOTTOM_PADDING = 20;
    private static final int PANEL_PADDING = 24;
    private static final int PANEL_SIDE_PADDING = 32;
    private static final int ENTRY_HEIGHT = 40;
    private static final int ENTRY_SPACING = 5;
    private static final int HEADER_SPACING = 10;
    private static final int HEADER_HEIGHT = 40;

    private final GameLauncher launcher;
    private final UserManager userManager;
    private final JPanel listPanel = new JPanel();

    public LeaderboardPanel(GameLauncher launcher, UserManager userManager) {
        this.launcher = launcher;
        this.userManager = userManager;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        BackgroundPanel bg = new BackgroundPanel("gameplay_bg.png");
        bg.setLayout(new BorderLayout());
        bg.setBorder(BorderFactory.createEmptyBorder(
            PANEL_PADDING, PANEL_SIDE_PADDING, PANEL_SIDE_PADDING, PANEL_SIDE_PADDING));

        JLabel title = createTitleLabel();
        bg.add(title, BorderLayout.NORTH);

        JScrollPane scroll = createScrollPane();
        bg.add(scroll, BorderLayout.CENTER);

        JPanel buttons = createButtonPanel();
        bg.add(buttons, BorderLayout.SOUTH);

        add(bg, BorderLayout.CENTER);
    }

    private JLabel createTitleLabel() {
        JLabel title = new JLabel("🏆 LEADERBOARD 🏆", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, (float) TITLE_FONT_SIZE));
        title.setForeground(TITLE_COLOR);
        title.setBorder(BorderFactory.createEmptyBorder(
            TITLE_PADDING, 0, TITLE_BOTTOM_PADDING, 0));
        return title;
    }

    private JScrollPane createScrollPane() {
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    private JPanel createButtonPanel() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.setOpaque(false);

        CustomImageButton back = new CustomImageButton(
            "assets/back.png", "assets/backh.png", "assets/backp.png");
        back.addActionListener(e -> launcher.showMainMenu());
        buttons.add(back);

        return buttons;
    }

    public void refreshLeaderboard() {
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() {
                return userManager.getLeaderboard();
            }

            @Override
            protected void done() {
                listPanel.removeAll();
                try {
                    List<User> users = get();

                    if (users.isEmpty()) {
                        addEmptyMessage();
                    } else {
                        addLeaderboardEntries(users);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LeaderboardPanel.this,
                        "Gagal memuat leaderboard: " + ex.getMessage());
                }
                listPanel.revalidate();
                listPanel.repaint();
            }
        }.execute();
    }

    private void addEmptyMessage() {
        JLabel empty = new JLabel("Belum ada pemain di leaderboard");
        empty.setForeground(Color.GRAY);
        empty.setAlignmentX(Component.CENTER_ALIGNMENT);
        listPanel.add(empty);
    }

    private void addLeaderboardEntries(List<User> users) {
        JPanel header = createHeaderPanel();
        listPanel.add(header);
        listPanel.add(Box.createVerticalStrut(HEADER_SPACING));

        int rank = 1;
        for (User u : users) {
            JPanel entry = createLeaderboardEntry(rank, u);
            listPanel.add(entry);
            listPanel.add(Box.createVerticalStrut(ENTRY_SPACING));
            rank++;
        }
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new GridLayout(1, 4, HEADER_SPACING, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEADER_HEIGHT));
        header.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        Font headerFont = new Font("Arial", Font.BOLD, HEADER_FONT_SIZE);

        JLabel rankLabel = createLabel("Rank", headerFont, HEADER_COLOR);
        JLabel nameLabel = createLabel("Username", headerFont, HEADER_COLOR);
        JLabel scoreLabel = createLabel("Total Score", headerFont, HEADER_COLOR);
        JLabel levelLabel = createLabel("Level", headerFont, HEADER_COLOR);

        header.add(rankLabel);
        header.add(nameLabel);
        header.add(scoreLabel);
        header.add(levelLabel);

        return header;
    }

    private JPanel createLeaderboardEntry(int rank, User user) {
        JPanel entry = new JPanel(new GridLayout(1, 4, HEADER_SPACING, 0));
        entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, ENTRY_HEIGHT));
        entry.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        RankInfo rankInfo = getRankInfo(rank);
        entry.setOpaque(true);
        entry.setBackground(rankInfo.bgColor);

        Font entryFont = new Font("Arial", Font.PLAIN, ENTRY_FONT_SIZE);
        Color textColor = Color.BLACK;

        JLabel rankLabel = createLabel(rankInfo.text, entryFont, textColor);
        JLabel nameLabel = createLabel(user.getUsername(), entryFont, textColor);
        JLabel scoreLabel = createLabel(String.valueOf(user.getTotalScore()), entryFont, textColor);
        JLabel levelLabel = createLabel("Level " + user.getUnlockedLevel(), entryFont, textColor);

        entry.add(rankLabel);
        entry.add(nameLabel);
        entry.add(scoreLabel);
        entry.add(levelLabel);

        return entry;
    }

    private JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    private RankInfo getRankInfo(int rank) {
        switch (rank) {
            case 1:
                return new RankInfo("🥇 " + rank, GOLD_BG);
            case 2:
                return new RankInfo("🥈 " + rank, SILVER_BG);
            case 3:
                return new RankInfo("🥉 " + rank, BRONZE_BG);
            default:
                return new RankInfo(String.valueOf(rank), DEFAULT_BG);
        }
    }

    private static class RankInfo {
        final String text;
        final Color bgColor;

        RankInfo(String text, Color bgColor) {
            this.text = text;
            this.bgColor = bgColor;
        }
    }
}