import java.awt.*;
import javax.swing.*;

public class NewGamePanel extends JPanel {
    private static final int CARD_PADDING = 18;
    private static final int CARD_HORIZONTAL_PADDING = 24;
    private static final int TITLE_FONT_SIZE = 18;
    private static final int INPUT_FONT_SIZE = 16;
    private static final int COMPONENT_SPACING = 12;
    private static final int BUTTON_SPACING = 16;
    private static final int TOP_MARGIN = 250;
    private static final int HORIZONTAL_MARGIN = 40;

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

        JPanel card = createCardPanel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(TOP_MARGIN, HORIZONTAL_MARGIN, 0, HORIZONTAL_MARGIN);
        backgroundPanel.add(card, gbc);

        add(backgroundPanel, BorderLayout.CENTER);
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(
                CARD_PADDING, CARD_HORIZONTAL_PADDING, CARD_PADDING, CARD_HORIZONTAL_PADDING));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = createTitleLabel();
        JPanel inputStack = createInputField();
        startButton = createStartButton();
        CustomImageButton backButton = createBackButton();

        card.add(title);
        card.add(Box.createVerticalStrut(COMPONENT_SPACING));
        card.add(inputStack);
        card.add(Box.createVerticalStrut(COMPONENT_SPACING));
        card.add(startButton);
        card.add(Box.createVerticalStrut(BUTTON_SPACING));
        card.add(backButton);

        return card;
    }

    private JLabel createTitleLabel() {
        JLabel title = new JLabel("Input Username");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, (float) TITLE_FONT_SIZE));
        return title;
    }

    private JPanel createInputField() {
        nameField.setOpaque(false);
        nameField.setFont(new Font("Monospaced", Font.BOLD, INPUT_FONT_SIZE));
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
        inputStack.setAlignmentX(Component.CENTER_ALIGNMENT);

        return inputStack;
    }

    private JButton createStartButton() {
        CustomImageButton button = new CustomImageButton(
                "assets/cn.png", "assets/cnh.png", "assets/cnp.png");
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(e -> handleCreate());
        return button;
    }

    private CustomImageButton createBackButton() {
        CustomImageButton button = new CustomImageButton(
                "assets/back.png", "assets/backh.png", "assets/backp.png");
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(e -> launcher.showMainMenu());
        return button;
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
                    JOptionPane.showMessageDialog(NewGamePanel.this,
                            "Gagal membuat save: " + ex.getMessage());
                }
            }
        }.execute();
    }

}