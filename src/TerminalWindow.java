import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ActionMap;
import javax.swing.AbstractAction;

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
    private Hero myHero;
    private Dungeon myDungeon;
    private JTextArea myMapDisplay;
    private JTextArea myRoomDisplay;
    private JTextArea myHeroDisplay;
    private JLabel myStatusLabel;
    private JButton myNorthButton;
    private JButton myEastButton;
    private JButton mySouthButton;
    private JButton myWestButton;

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
            showGame(hero);
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

    private void showGame(final Hero theHero) {
        myHero = theHero;
        myDungeon = myGame.createDungeon();

        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        panel.add(buildGameHeader(theHero), BorderLayout.NORTH);
        panel.add(buildMapPanel(), BorderLayout.CENTER);
        panel.add(buildSidePanel(), BorderLayout.WEST);
        panel.add(buildMovementPanel(), BorderLayout.SOUTH);
        bindMovementKeys(panel);

        setTitle("Dungeon Crawler");
        setContentPane(panel);
        resolveCurrentRoom("You enter the dungeon.");
        updateGameView();
        revalidate();
        repaint();
        pack();
    }

    private JPanel buildGameHeader(final Hero theHero) {
        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setBackground(BACKGROUND);

        JLabel title = new JLabel("Dungeon");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(DOOM_ACCENT);
        title.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));

        JLabel subtitle = new JLabel(theHero.getName() + " the "
                + theHero.getClass().getSimpleName());
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setForeground(FOREGROUND);
        subtitle.setFont(TERMINAL_FONT);

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildMapPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BACKGROUND);

        JLabel label = buildCharacterLabel("Map");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        myMapDisplay = buildOutputArea();
        myMapDisplay.setRows(22);
        myMapDisplay.setColumns(54);
        myMapDisplay.setLineWrap(false);
        myMapDisplay.setWrapStyleWord(false);

        panel.add(label, BorderLayout.NORTH);
        panel.add(myMapDisplay, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSidePanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 14));
        panel.setBackground(BACKGROUND);
        panel.setPreferredSize(new Dimension(250, 420));

        panel.add(buildHeroPanel());
        panel.add(buildRoomPanel());
        return panel;
    }

    private JPanel buildHeroPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BACKGROUND);

        JLabel label = buildCharacterLabel("Hero");
        myHeroDisplay = buildOutputArea();
        myHeroDisplay.setRows(9);
        myHeroDisplay.setColumns(22);

        panel.add(label, BorderLayout.NORTH);
        panel.add(myHeroDisplay, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BACKGROUND);

        JLabel label = buildCharacterLabel("Current Room");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        myRoomDisplay = buildOutputArea();
        myRoomDisplay.setRows(7);
        myRoomDisplay.setColumns(22);
        myRoomDisplay.setLineWrap(false);
        myRoomDisplay.setWrapStyleWord(false);

        panel.add(label, BorderLayout.NORTH);
        panel.add(myRoomDisplay, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMovementPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BACKGROUND);

        myStatusLabel = new JLabel(" ");
        myStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        myStatusLabel.setForeground(FOREGROUND);
        myStatusLabel.setFont(TERMINAL_FONT);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        controls.setBackground(BACKGROUND);
        myNorthButton = buildMoveButton("North", Direction.NORTH);
        myEastButton = buildMoveButton("East", Direction.EAST);
        mySouthButton = buildMoveButton("South", Direction.SOUTH);
        myWestButton = buildMoveButton("West", Direction.WEST);
        controls.add(myNorthButton);
        controls.add(myEastButton);
        controls.add(mySouthButton);
        controls.add(myWestButton);

        panel.add(myStatusLabel, BorderLayout.NORTH);
        panel.add(controls, BorderLayout.CENTER);
        return panel;
    }

    private JButton buildMoveButton(final String theText, final Direction theDirection) {
        JButton button = buildButton(theText);
        button.addActionListener(event -> moveHero(theDirection));
        return button;
    }

    private void bindMovementKeys(final JPanel thePanel) {
        InputMap inputMap = thePanel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = thePanel.getActionMap();

        bindMovementKey(inputMap, actionMap, "UP", Direction.NORTH);
        bindMovementKey(inputMap, actionMap, "RIGHT", Direction.EAST);
        bindMovementKey(inputMap, actionMap, "DOWN", Direction.SOUTH);
        bindMovementKey(inputMap, actionMap, "LEFT", Direction.WEST);
    }

    private void bindMovementKey(final InputMap theInputMap,
                                 final ActionMap theActionMap,
                                 final String theKey,
                                 final Direction theDirection) {
        String actionName = "move" + theDirection.name();
        theInputMap.put(KeyStroke.getKeyStroke(theKey), actionName);
        theActionMap.put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                moveHero(theDirection);
            }
        });
    }

    private void moveHero(final Direction theDirection) {
        if (myHero == null || myDungeon == null || myHero.isFainted()) {
            return;
        }

        if (!myDungeon.moveHero(theDirection)) {
            setStatus("There is no door that way.");
            updateGameView();
            return;
        }

        resolveCurrentRoom("Moved " + theDirection.name().toLowerCase() + ".");
        updateGameView();
    }

    private void resolveCurrentRoom(final String theBaseMessage) {
        Room room = myDungeon.getCurrentRoom();
        String message = theBaseMessage;

        if (room.hasHealingPotion()) {
            myHero.addHealingPotion();
            room.removePotion(new HealingPotion());
            message += " Picked up a healing potion.";
        }
        if (room.hasVisionPotion()) {
            myHero.addVisionPotion();
            room.removePotion(new VisionPotion());
            message += " Picked up a vision potion.";
        }
        Pillar pillar = room.removePillar();
        if (pillar != null) {
            myHero.addPillar(pillar);
            message += " Found " + formatPillar(pillar) + ".";
        }
        int pitDamage = room.fallInPit();
        if (pitDamage > 0) {
            myHero.takeDamage(pitDamage);
            message += " Fell in a pit for " + pitDamage + " damage.";
        }
        if (room.isExit()) {
            if (myHero.hasAllPillars()) {
                message += " You escaped with all four pillars.";
            } else {
                message += " The exit is here, but you still need every pillar.";
            }
        }
        if (myHero.isFainted()) {
            message += " You have fallen.";
        }

        setStatus(message);
    }

    private void updateGameView() {
        Room room = myDungeon.getCurrentRoom();
        myMapDisplay.setText(buildMapText());
        myRoomDisplay.setText(centerRoomText(room.toString()));
        myHeroDisplay.setText(buildHeroStatsText());

        boolean gameOver = myHero.isFainted()
                || (room.isExit() && myHero.hasAllPillars());
        myNorthButton.setEnabled(!gameOver && room.workingDoor(Direction.NORTH));
        myEastButton.setEnabled(!gameOver && room.workingDoor(Direction.EAST));
        mySouthButton.setEnabled(!gameOver && room.workingDoor(Direction.SOUTH));
        myWestButton.setEnabled(!gameOver && room.workingDoor(Direction.WEST));
    }

    private void setStatus(final String theMessage) {
        if (myStatusLabel != null) {
            myStatusLabel.setText(theMessage);
        }
    }

    private String buildHeroStatsText() {
        return "Class: " + myHero.getClass().getSimpleName()
                + System.lineSeparator() + myHero.toString();
    }

    private String centerRoomText(final String theRoomText) {
        final int displayRows = 7;
        final int displayColumns = 22;
        String[] lines = theRoomText.split(System.lineSeparator());
        String text = "";

        int topPadding = Math.max(0, (displayRows - lines.length) / 2);
        for (int i = 0; i < topPadding; i++) {
            text += System.lineSeparator();
        }
        for (String line : lines) {
            int leftPadding = Math.max(0, (displayColumns - line.length()) / 2);
            text += " ".repeat(leftPadding) + line + System.lineSeparator();
        }
        return text;
    }

    private String buildMapText() {
        String text = "";
        for (int row = 0; row < myDungeon.getHeight(); row++) {
            for (int col = 0; col < myDungeon.getWidth(); col++) {
                if (myDungeon.isDiscovered(row, col)) {
                    text += "+";
                    text += topMapWall(row, col);
                } else {
                    text += "    ";
                }
            }
            text += trailingMapCorner(row) + System.lineSeparator();

            for (int col = 0; col < myDungeon.getWidth(); col++) {
                Room room = myDungeon.getRoom(row, col);
                if (myDungeon.isDiscovered(row, col)) {
                    text += leftMapWall(room, row, col);
                    text += roomMapSymbol(room, row, col);
                } else {
                    text += "    ";
                }
            }
            text += trailingMapWall(row) + System.lineSeparator();
        }
        for (int col = 0; col < myDungeon.getWidth(); col++) {
            if (myDungeon.isDiscovered(myDungeon.getHeight() - 1, col)) {
                text += "+---";
            } else {
                text += "    ";
            }
        }
        return text + trailingBottomCorner();
    }

    private String trailingMapCorner(final int theRow) {
        if (myDungeon.isDiscovered(theRow, myDungeon.getWidth() - 1)) {
            return "+";
        }
        return "";
    }

    private String trailingMapWall(final int theRow) {
        if (myDungeon.isDiscovered(theRow, myDungeon.getWidth() - 1)) {
            return "|";
        }
        return "";
    }

    private String trailingBottomCorner() {
        if (myDungeon.isDiscovered(myDungeon.getHeight() - 1,
                myDungeon.getWidth() - 1)) {
            return "+";
        }
        return "";
    }

    private String topMapWall(final int theRow, final int theCol) {
        return myDungeon.getRoom(theRow, theCol).workingDoor(Direction.NORTH)
                ? "   " : "---";
    }

    private String leftMapWall(final Room theRoom,
                               final int theRow,
                               final int theCol) {
        return theRoom.workingDoor(Direction.WEST) ? " " : "|";
    }

    private String roomMapSymbol(final Room theRoom,
                                 final int theRow,
                                 final int theCol) {
        if (theRow == myDungeon.getHeroRow() && theCol == myDungeon.getHeroCol()) {
            return " @ ";
        }
        if (theRoom.isEntrance()) {
            return " S ";
        }
        if (theRoom.isExit()) {
            return " X ";
        }
        if (theRoom.getPillar() != null) {
            return " " + pillarLetter(theRoom.getPillar()) + " ";
        }
        if (theRoom.hasPit()) {
            return " P ";
        }
        if (theRoom.hasHealingPotion()) {
            return " H ";
        }
        if (theRoom.hasVisionPotion()) {
            return " V ";
        }
        if (theRoom.getMonster() != null) {
            return " M ";
        }
        return "   ";
    }

    private String formatPillar(final Pillar thePillar) {
        String name = thePillar.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String pillarLetter(final Pillar thePillar) {
        switch (thePillar) {
            case ABSTRACTION:
                return "A";
            case ENCAPSULATION:
                return "E";
            case INHERITANCE:
                return "I";
            case POLYMORPHISM:
                return "O";
            default:
                return "?";
        }
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
