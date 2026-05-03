import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class TerminalWindow extends JFrame implements Appendable {
    private static final Color BACKGROUND = new Color(43, 43, 43);
    private static final Color EDITOR_BACKGROUND = new Color(30, 31, 34);
    private static final Color FOREGROUND = new Color(188, 190, 196);
    private static final Color PROMPT = new Color(106, 171, 115);
    private static final Color INPUT_BACKGROUND = new Color(39, 40, 44);
    private static final Color BORDER = new Color(70, 73, 78);
    private static final Color CARET = new Color(169, 183, 198);
    private static final Color DOOM_ACCENT = new Color(189, 147, 249);
    private static final Font TERMINAL_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 15);

    private final DungeonCrawler myGame;
    private final JTextArea myOutput;
    private final JTextField myInput;

    public TerminalWindow(final DungeonCrawler theGame) {
        super("Dungeon Crawler Terminal");
        myGame = theGame;
        myOutput = buildOutputArea();
        myInput = buildInputField();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 460));
        setLocationByPlatform(true);
        setContentPane(buildContentPane());
        bindInput();
        printIntro();
    }

    public void open() {
        pack();
        setVisible(true);
        myInput.requestFocusInWindow();
    }

    @Override
    public TerminalWindow append(final CharSequence theText) {
        appendText(String.valueOf(theText));
        return this;
    }

    @Override
    public TerminalWindow append(final CharSequence theText,
                                 final int theStart,
                                 final int theEnd) {
        appendText(String.valueOf(theText).substring(theStart, theEnd));
        return this;
    }

    @Override
    public TerminalWindow append(final char theCharacter) throws IOException {
        appendText(String.valueOf(theCharacter));
        return this;
    }

    private JPanel buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JScrollPane scrollPane = new JScrollPane(myOutput);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(EDITOR_BACKGROUND);

        panel.add(buildHeader(), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(myInput, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setBackground(BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 2));

        JLabel title = new JLabel("Dungeon Crawler");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(DOOM_ACCENT);
        title.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));

        JLabel authors = new JLabel("Pavlo Puzik    Travis Vinay    Andrew DeFord");
        authors.setHorizontalAlignment(SwingConstants.CENTER);
        authors.setForeground(FOREGROUND);
        authors.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        header.add(title, BorderLayout.NORTH);
        header.add(authors, BorderLayout.SOUTH);
        return header;
    }

    private JTextArea buildOutputArea() {
        JTextArea output = new JTextArea(22, 76);
        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        output.setBackground(EDITOR_BACKGROUND);
        output.setForeground(FOREGROUND);
        output.setCaretColor(CARET);
        output.setFont(TERMINAL_FONT);
        output.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return output;
    }

    private JTextField buildInputField() {
        JTextField input = new JTextField();
        input.setBackground(INPUT_BACKGROUND);
        input.setForeground(FOREGROUND);
        input.setCaretColor(CARET);
        input.setFont(TERMINAL_FONT);
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        return input;
    }

    private void bindInput() {
        myInput.addActionListener(this::handleCommand);
    }

    private void handleCommand(final ActionEvent theEvent) {
        String command = myInput.getText().trim();
        myInput.setText("");

        if (command.isEmpty()) {
            return;
        }

        println("> " + command);
        runCommand(command.toLowerCase());
    }

    private void runCommand(final String theCommand) {
        switch (theCommand) {
            case "1":
            case "start":
            case "launch":
            case "play":
                showCharacterCreation();
                Dungeon theDungeon = new Dungeon(10, 10);
                break;
            case "2":
            case "load":
            case "load game":
                myGame.loadGame("save.dat");
                println("Load game selected.");
                break;
            case "3":
            case "help":
                println("Commands: 1/start, 2/load, 3/help, 4/clear, 5/exit");
                break;
            case "4":
            case "clear":
                myOutput.setText("");
                printIntro();
                break;
            case "5":
            case "exit":
            case "quit":
                dispose();
                System.exit(0);
                break;
            default:
                println("Unknown command. Type 'help' for commands.");
                break;
        }
    }

    private void printIntro() {
        println("Dungeon Crawler Terminal");
        println("Choose a command by typing its number or name:");
        println("1. start - Launch the game");
        println("2. load  - Load a saved game");
        println("3. help  - Show commands");
        println("4. clear - Clear the terminal");
        println("5. exit  - Close the game");
        println("");
        printCentered("#########################");
        printCentered("# S #     #       #     #");
        printCentered("# ### ### # ##### # ### #");
        printCentered("#   # #   #     #   #   #");
        printCentered("### # # ##### # ##### ###");
        printCentered("#   # #     # #     #   #");
        printCentered("# ### ##### # ##### ### #");
        printCentered("#     #     #     #   E #");
        printCentered("#########################");
        printCentered("S = Start      E = Exit");
        println("");
    }

    private void showCharacterCreation() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JTextField nameField = buildInputField();
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(255, 121, 198));
        errorLabel.setFont(TERMINAL_FONT);

        JPanel namePanel = new JPanel(new BorderLayout(0, 6));
        namePanel.setBackground(BACKGROUND);
        namePanel.add(buildCharacterLabel("Hero Name"), BorderLayout.NORTH);
        namePanel.add(nameField, BorderLayout.CENTER);
        namePanel.add(errorLabel, BorderLayout.SOUTH);

        ButtonGroup classGroup = new ButtonGroup();
        JPanel classPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        classPanel.setBackground(BACKGROUND);

        JToggleButton warrior = buildClassChoice("Warrior", new Warrior("Preview"));
        JToggleButton thief = buildClassChoice("Thief", new Thief("Preview"));
        JToggleButton priestess = buildClassChoice("Priestess", new Priestess("Preview"));
        warrior.setSelected(true);
        warrior.addActionListener(event -> updateClassCards(warrior, thief, priestess));
        thief.addActionListener(event -> updateClassCards(warrior, thief, priestess));
        priestess.addActionListener(event -> updateClassCards(warrior, thief, priestess));
        updateClassCards(warrior, thief, priestess);

        classGroup.add(warrior);
        classGroup.add(thief);
        classGroup.add(priestess);
        classPanel.add(warrior);
        classPanel.add(thief);
        classPanel.add(priestess);

        JPanel controls = new JPanel(new GridLayout(1, 2, 10, 0));
        controls.setBackground(BACKGROUND);
        JButton backButton = buildButton("Back");
        JButton beginButton = buildButton("Begin Adventure");
        controls.add(backButton);
        controls.add(beginButton);

        backButton.addActionListener(event -> showTerminal());
        beginButton.addActionListener(event -> {
            String heroName = nameField.getText().trim();
            if (heroName.isEmpty()) {
                errorLabel.setText("Enter a hero name before beginning.");
                nameField.requestFocusInWindow();
                return;
            }

            Hero hero = myGame.createHero(getSelectedClassName(classGroup), heroName);
            showTerminal();
            myGame.runGame(this, hero);
        });
        nameField.addActionListener(beginButton.getActionListeners()[0]);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setBackground(BACKGROUND);
        bottomPanel.add(namePanel, BorderLayout.CENTER);
        bottomPanel.add(controls, BorderLayout.SOUTH);

        panel.add(buildCreationHeader(), BorderLayout.NORTH);
        panel.add(classPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(panel);
        revalidate();
        repaint();
        pack();
        nameField.requestFocusInWindow();
    }

    private JLabel buildCreationHeader() {
        JLabel title = new JLabel("Hero Creation");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(DOOM_ACCENT);
        title.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
        return title;
    }

    private JToggleButton buildClassChoice(final String theClassName, final Hero thePreview) {
        JToggleButton button = new JToggleButton(buildStatsText(theClassName, thePreview));
        button.setActionCommand(theClassName);
        button.setBackground(EDITOR_BACKGROUND);
        button.setForeground(FOREGROUND);
        button.setFont(TERMINAL_FONT);
        button.setVerticalAlignment(SwingConstants.TOP);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        button.setFocusPainted(false);
        return button;
    }

    private void updateClassCards(final JToggleButton... theButtons) {
        for (JToggleButton button : theButtons) {
            if (button.isSelected()) {
                button.setBackground(DOOM_ACCENT);
                button.setForeground(EDITOR_BACKGROUND);
            } else {
                button.setBackground(EDITOR_BACKGROUND);
                button.setForeground(FOREGROUND);
            }
        }
    }

    private String buildStatsText(final String theClassName, final Hero thePreview) {
        return "<html><body style='width: 150px'>"
                + "<b>" + theClassName + "</b><br><br>"
                + "HP: " + thePreview.getMaxHitPoints() + "<br>"
                + "Damage: " + thePreview.getMinDamage() + "-"
                + thePreview.getMaxDamage() + "<br>"
                + "Speed: " + thePreview.getAttackSpeed() + "<br>"
                + "Hit: " + percent(thePreview.getHitChance()) + "<br>"
                + "Block: " + percent(thePreview.getChanceToBlock())
                + "</body></html>";
    }

    private JLabel buildCharacterLabel(final String theText) {
        JLabel label = new JLabel(theText);
        label.setForeground(DOOM_ACCENT);
        label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        return label;
    }

    private JButton buildButton(final String theText) {
        JButton button = new JButton(theText);
        button.setBackground(INPUT_BACKGROUND);
        button.setForeground(FOREGROUND);
        button.setFont(TERMINAL_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        return button;
    }

    private String getSelectedClassName(final ButtonGroup theClassGroup) {
        return theClassGroup.getSelection().getActionCommand();
    }

    private void showTerminal() {
        setContentPane(buildContentPane());
        revalidate();
        repaint();
        pack();
        myInput.requestFocusInWindow();
    }

    private static String percent(final double theChance) {
        return Math.round(theChance * 100) + "%";
    }

    private void println(final String theText) {
        appendText(theText + System.lineSeparator());
    }

    private void printCentered(final String theText) {
        final int terminalColumns = 76;
        int leftPadding = Math.max(0, (terminalColumns - theText.length()) / 2);
        println(" ".repeat(leftPadding) + theText);
    }

    private void appendText(final String theText) {
        if (SwingUtilities.isEventDispatchThread()) {
            myOutput.append(theText);
            myOutput.setCaretPosition(myOutput.getDocument().getLength());
        } else {
            SwingUtilities.invokeLater(() -> appendText(theText));
        }
    }
}
