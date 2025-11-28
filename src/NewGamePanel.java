import javax.swing.*;
import java.awt.*;

import javax.swing.SwingWorker;

public class NewGamePanel extends JPanel {
    private final GameLauncher launcher;
    private final UserManager userManager;
    private final JTextField nameField = new JTextField();
    private JButton startButton;

    public NewGamePanel(GameLauncher launcher, UserManager userManager) {
        this.launcher = launcher;
        this.userManager = userManager;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        BackgroundPanel backgroundPanel = new BackgroundPanel("newgame_bg.png");
        backgroundPanel.setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Input Username");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        nameField.setOpaque(false);
        nameField.setFont(new Font("Monospaced", Font.BOLD, 16));
        nameField.setHorizontalAlignment(SwingConstants.CENTER);
        nameField.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        nameField.addActionListener(e -> handleCreate());

        ImageIcon inputBg = new ImageIcon("assets/text_input.png");
        JLabel inputBackground = new JLabel(inputBg);
        inputBackground.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel inputStack = new JPanel();
        inputStack.setOpaque(false);
        inputStack.setLayout(new OverlayLayout(inputStack));
        inputStack.setMaximumSize(new Dimension(inputBg.getIconWidth(), inputBg.getIconHeight()));
        inputStack.setPreferredSize(new Dimension(inputBg.getIconWidth(), inputBg.getIconHeight()));
        inputStack.add(inputBackground);
        inputStack.add(nameField);

        startButton = new CustomImageButton("assets/cn.png", "assets/cnh.png", "assets/cnp.png");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(e -> handleCreate());

        CustomImageButton backButton = new CustomImageButton("assets/back.png", "assets/backh.png", "assets/backp.png");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> launcher.showMainMenu());

        card.add(title);
        card.add(Box.createVerticalStrut(12));
        inputStack.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(inputStack);
        card.add(Box.createVerticalStrut(12));
        card.add(startButton);
        card.add(Box.createVerticalStrut(16));
        card.add(backButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(250, 40, 0, 40); // horizontal padding for card
        backgroundPanel.add(card, gbc);
        add(backgroundPanel, BorderLayout.CENTER);
    }

    private void handleCreate() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama tidak boleh kosong");
            return;
        }
        startButton.setEnabled(false);
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() {
                return userManager.createUser(name);
            }

            @Override
            protected void done() {
                startButton.setEnabled(true);
                try {
                    User newUser = get();
                    if (newUser != null) {
                        nameField.setText("");
                        launcher.showLevelBoard(newUser);
                    } else {
                        String err = userManager.getLastError();
                        JOptionPane.showMessageDialog(NewGamePanel.this,
                                "Gagal membuat save: " + (err != null ? err : "Unknown error"));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(NewGamePanel.this, "Gagal membuat save: " + ex.getMessage());
                }
            }
        }.execute();
    }

    public void refreshUsers() {

    }
}
