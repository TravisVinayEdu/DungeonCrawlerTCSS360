package dungeoncrawler.view;

import dungeoncrawler.combat.Battle;
import dungeoncrawler.controller.DungeonCrawler;
import dungeoncrawler.model.Direction;
import dungeoncrawler.model.Dungeon;
import dungeoncrawler.model.HealingPotion;
import dungeoncrawler.model.Pillar;
import dungeoncrawler.model.Room;
import dungeoncrawler.model.VisionPotion;
import dungeoncrawler.model.characters.Hero;
import dungeoncrawler.model.characters.Monster;
import dungeoncrawler.model.characters.Priestess;
import dungeoncrawler.model.characters.Thief;
import dungeoncrawler.model.characters.Warrior;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JComponent;
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
    private static final Color VALID_MOVE_BORDER = new Color(80, 200, 120);
    private static final Color INVALID_MOVE_BORDER = new Color(255, 85, 85);
    private static final Color CARET = new Color(169, 183, 198);
    private static final Color DOOM_ACCENT = new Color(189, 147, 249);

    private final DungeonCrawler myGame;
    private final WindowScaler myWindowScaler;
    private final ScalableFontManager myFontManager;
    private final DungeonMapRenderer myMapRenderer;
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
    private Battle myBattle;
    private JTextArea myBattleHeroDisplay;
    private JTextArea myBattleMonsterDisplay;
    private JTextArea myBattleLogDisplay;
    private JButton myBattleAttackButton;
    private JButton myBattleSpecialButton;
    private JButton myBattleHealButton;
    private JButton myBattleVisionButton;
    private JButton myBattleRunButton;

    public TerminalWindow(final DungeonCrawler theGame) {
        super("Dungeon Crawler Terminal");
        myGame = theGame;
        myWindowScaler = new WindowScaler();
        myFontManager = new ScalableFontManager(this, myWindowScaler);
        myMapRenderer = new DungeonMapRenderer();
        myOutput = buildOutputArea();
        myInput = buildInputField();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(myWindowScaler.scaledMinimumSize());
        setLocationByPlatform(true);
        setContentPane(buildContentPane());
        bindInput();
        bindFontScaling();
        bindWindowZoom();
        printIntro();
    }

    public void open() {
        pack();
        setSize(myWindowScaler.scaledStartingSize());
        setLocationRelativeTo(null);
        updateScaledFonts();
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

        panel.add(buildHeader(), BorderLayout.NORTH);
        panel.add(wrapTextArea(myOutput), BorderLayout.CENTER);
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
        setScalableFont(title, Font.BOLD, 30);

        JLabel authors = new JLabel("Pavlo Puzik    Travis Vinay    Andrew DeFord");
        authors.setHorizontalAlignment(SwingConstants.CENTER);
        authors.setForeground(FOREGROUND);
        setScalableFont(authors, Font.PLAIN, 13);

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
        setScalableFont(output, Font.PLAIN, 15);
        output.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return output;
    }

    private JTextField buildInputField() {
        JTextField input = new JTextField();
        input.setBackground(INPUT_BACKGROUND);
        input.setForeground(FOREGROUND);
        input.setCaretColor(CARET);
        setScalableFont(input, Font.PLAIN, 15);
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        return input;
    }

    private void bindInput() {
        myInput.addActionListener(this::handleCommand);
    }

    private void bindFontScaling() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent theEvent) {
                updateScaledFonts();
            }
        });
    }

    private void bindWindowZoom() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        bindZoomKey(inputMap, actionMap, KeyEvent.VK_EQUALS,
                "zoomInEquals", () -> {
                    myWindowScaler.increaseUserScale();
                    refreshContent();
                });
        bindZoomKey(inputMap, actionMap, KeyEvent.VK_PLUS,
                "zoomInPlus", () -> {
                    myWindowScaler.increaseUserScale();
                    refreshContent();
                });
        bindZoomKey(inputMap, actionMap, KeyEvent.VK_ADD,
                "zoomInNumpad", () -> {
                    myWindowScaler.increaseUserScale();
                    refreshContent();
                });
        bindZoomKey(inputMap, actionMap, KeyEvent.VK_MINUS,
                "zoomOutMinus", () -> {
                    myWindowScaler.decreaseUserScale();
                    refreshContent();
                });
        bindZoomKey(inputMap, actionMap, KeyEvent.VK_SUBTRACT,
                "zoomOutNumpad", () -> {
                    myWindowScaler.decreaseUserScale();
                    refreshContent();
                });
        bindZoomKey(inputMap, actionMap, KeyEvent.VK_0,
                "zoomReset", () -> {
                    myWindowScaler.resetUserScale();
                    refreshContent();
                });
    }

    private void bindZoomKey(final InputMap theInputMap,
                             final ActionMap theActionMap,
                             final int theKeyCode,
                             final String theActionName,
                             final Runnable theAction) {
        theInputMap.put(KeyStroke.getKeyStroke(theKeyCode,
                InputEvent.CTRL_DOWN_MASK), theActionName);
        theActionMap.put(theActionName, new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                theAction.run();
            }
        });
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
        for (String line : StartupPreview.lines()) {
            printCentered(line);
        }
        println("");
    }

    private void showCharacterCreation() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JTextField nameField = buildInputField();
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(255, 121, 198));
        setScalableFont(errorLabel, Font.PLAIN, 15);

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
        refreshContent();
        nameField.requestFocusInWindow();
    }

    private JLabel buildCreationHeader() {
        JLabel title = new JLabel("Hero Creation");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(DOOM_ACCENT);
        setScalableFont(title, Font.BOLD, 30);
        return title;
    }

    private JToggleButton buildClassChoice(final String theClassName, final Hero thePreview) {
        JToggleButton button = new JToggleButton(buildStatsText(theClassName, thePreview));
        button.setActionCommand(theClassName);
        button.setBackground(EDITOR_BACKGROUND);
        button.setForeground(FOREGROUND);
        setScalableFont(button, Font.PLAIN, 15);
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
        return "<html><body style='width: " + myWindowScaler.scaledLength(150) + "px'>"
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
        setScalableFont(label, Font.BOLD, 16);
        return label;
    }

    private JButton buildButton(final String theText) {
        JButton button = new JButton(theText);
        button.setBackground(INPUT_BACKGROUND);
        button.setForeground(FOREGROUND);
        setScalableFont(button, Font.PLAIN, 15);
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
        refreshContent();
        myInput.requestFocusInWindow();
    }

    private void showGame(final Hero theHero) {
        myHero = theHero;
        myDungeon = myGame.createDungeon();
        showDungeonView();
        resolveCurrentRoom("You enter the dungeon.");
        updateGameView();
    }

    private void showDungeonView() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        panel.add(buildGameHeader(myHero), BorderLayout.NORTH);
        panel.add(buildMapPanel(), BorderLayout.CENTER);
        panel.add(buildSidePanel(), BorderLayout.WEST);
        panel.add(buildMovementPanel(), BorderLayout.SOUTH);
        bindMovementKeys(panel);

        setTitle("Dungeon Crawler");
        setContentPane(panel);
        updateGameView();
        refreshContent();
    }

    private JPanel buildGameHeader(final Hero theHero) {
        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setBackground(BACKGROUND);

        JLabel title = new JLabel("Dungeon");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(DOOM_ACCENT);
        setScalableFont(title, Font.BOLD, 30);

        JLabel subtitle = new JLabel(theHero.getName() + " the "
                + theHero.getClass().getSimpleName());
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setForeground(FOREGROUND);
        setScalableFont(subtitle, Font.PLAIN, 15);

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
        myMapDisplay.setColumns(44);
        myMapDisplay.setLineWrap(false);
        myMapDisplay.setWrapStyleWord(false);

        panel.add(label, BorderLayout.NORTH);
        panel.add(wrapTextArea(myMapDisplay), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSidePanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 14));
        panel.setBackground(BACKGROUND);
        panel.setPreferredSize(myWindowScaler.scaledDimension(260, 420));
        panel.setMinimumSize(myWindowScaler.scaledDimension(210, 260));

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
        panel.add(wrapTextArea(myHeroDisplay), BorderLayout.CENTER);
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
        panel.add(wrapTextArea(myRoomDisplay), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMovementPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BACKGROUND);

        myStatusLabel = new JLabel(" ");
        myStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        myStatusLabel.setForeground(FOREGROUND);
        setScalableFont(myStatusLabel, Font.PLAIN, 15);

        JPanel controls = new JPanel(new GridLayout(1, 4, 10, 0));
        controls.setBackground(BACKGROUND);
        myNorthButton = buildMoveButton("[W] North", Direction.NORTH);
        myEastButton = buildMoveButton("[D] East", Direction.EAST);
        mySouthButton = buildMoveButton("[S] South", Direction.SOUTH);
        myWestButton = buildMoveButton("[A] West", Direction.WEST);
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
        button.setBorder(buildMoveButtonBorder(BORDER));
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
        bindMovementKey(inputMap, actionMap, "pressed W", Direction.NORTH);
        bindMovementKey(inputMap, actionMap, "pressed D", Direction.EAST);
        bindMovementKey(inputMap, actionMap, "pressed S", Direction.SOUTH);
        bindMovementKey(inputMap, actionMap, "pressed A", Direction.WEST);
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

        final boolean moved = myDungeon.moveHero(theDirection);
        if (!moved) {
            setStatus("There is no door that way.");
            updateGameView();
            updateMoveFeedback(theDirection, false);
            return;
        }

        resolveCurrentRoom("Moved " + theDirection.name().toLowerCase() + ".");
        updateGameView();
        updateMoveFeedback(theDirection, true);
        enterBattleIfMonsterPresent();
    }

    private void enterBattleIfMonsterPresent() {
        if (myHero == null || myDungeon == null || myHero.isFainted()) {
            return;
        }

        Monster monster = myDungeon.getCurrentRoom().getMonster();
        if (monster != null && !monster.isFainted()) {
            showBattleScreen(monster);
        }
    }

    private void updateMoveFeedback(final Direction theDirection,
                                    final boolean theMoveWasValid) {
        resetMoveButtonBorders();
        JButton button = getMoveButton(theDirection);
        if (button != null) {
            button.setBorder(buildMoveButtonBorder(
                    theMoveWasValid ? VALID_MOVE_BORDER : INVALID_MOVE_BORDER));
            button.repaint();
        }
    }

    private void resetMoveButtonBorders() {
        resetMoveButtonBorder(myNorthButton);
        resetMoveButtonBorder(myEastButton);
        resetMoveButtonBorder(mySouthButton);
        resetMoveButtonBorder(myWestButton);
    }

    private void resetMoveButtonBorder(final JButton theButton) {
        if (theButton != null) {
            theButton.setBorder(buildMoveButtonBorder(BORDER));
        }
    }

    private JButton getMoveButton(final Direction theDirection) {
        switch (theDirection) {
            case NORTH:
                return myNorthButton;
            case EAST:
                return myEastButton;
            case SOUTH:
                return mySouthButton;
            case WEST:
                return myWestButton;
            default:
                return null;
        }
    }

    private javax.swing.border.Border buildMoveButtonBorder(final Color theBorderColor) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theBorderColor, 2),
                BorderFactory.createEmptyBorder(9, 11, 9, 11));
    }

    private void showBattleScreen(final Monster theMonster) {
        myBattle = new Battle(myHero, theMonster);

        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        panel.add(buildBattleHeader(), BorderLayout.NORTH);
        panel.add(buildBattleLogPanel(), BorderLayout.CENTER);
        panel.add(buildBattleHeroPanel(), BorderLayout.WEST);
        panel.add(buildBattleMonsterPanel(), BorderLayout.EAST);
        panel.add(buildBattleActionsPanel(), BorderLayout.SOUTH);

        setTitle("Dungeon Crawler - Battle");
        setContentPane(panel);
        appendBattleLog("A " + myBattle.getMonster().getName() + " blocks your path.");
        updateBattleView();
        refreshContent();
    }

    private JPanel buildBattleHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setBackground(BACKGROUND);

        JLabel title = new JLabel("Battle");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(DOOM_ACCENT);
        setScalableFont(title, Font.BOLD, 30);

        JLabel subtitle = new JLabel(myHero.getName() + " vs. "
                + myBattle.getMonster().getName());
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setForeground(FOREGROUND);
        setScalableFont(subtitle, Font.PLAIN, 15);

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildBattleHeroPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BACKGROUND);
        panel.setPreferredSize(myWindowScaler.scaledDimension(250, 360));
        panel.setMinimumSize(myWindowScaler.scaledDimension(200, 240));

        JLabel label = buildCharacterLabel("Hero Status");
        myBattleHeroDisplay = buildOutputArea();
        myBattleHeroDisplay.setRows(14);
        myBattleHeroDisplay.setColumns(22);

        panel.add(label, BorderLayout.NORTH);
        panel.add(wrapTextArea(myBattleHeroDisplay), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBattleMonsterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BACKGROUND);
        panel.setPreferredSize(myWindowScaler.scaledDimension(250, 360));
        panel.setMinimumSize(myWindowScaler.scaledDimension(200, 240));

        JLabel label = buildCharacterLabel("Monster Status");
        myBattleMonsterDisplay = buildOutputArea();
        myBattleMonsterDisplay.setRows(14);
        myBattleMonsterDisplay.setColumns(22);

        panel.add(label, BorderLayout.NORTH);
        panel.add(wrapTextArea(myBattleMonsterDisplay), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBattleLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BACKGROUND);

        JLabel label = buildCharacterLabel("Battle Actions");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        myBattleLogDisplay = buildOutputArea();
        myBattleLogDisplay.setRows(18);
        myBattleLogDisplay.setColumns(44);

        panel.add(label, BorderLayout.NORTH);
        panel.add(wrapTextArea(myBattleLogDisplay), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBattleActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 10, 0));
        panel.setBackground(BACKGROUND);

        myBattleAttackButton = buildButton("Attack");
        myBattleSpecialButton = buildButton("Special Skill");
        myBattleHealButton = buildButton("Healing Potion");
        myBattleVisionButton = buildButton("Vision Potion");
        myBattleRunButton = buildButton("Run");

        myBattleAttackButton.addActionListener(
                event -> handleBattleResult(myBattle.attack()));
        myBattleSpecialButton.addActionListener(
                event -> handleBattleResult(myBattle.specialSkill()));
        myBattleHealButton.addActionListener(
                event -> handleBattleResult(myBattle.useHealingPotion()));
        myBattleVisionButton.addActionListener(
                event -> handleBattleResult(myBattle.useVisionPotion()));
        myBattleRunButton.addActionListener(event -> leaveBattleScreen());

        panel.add(myBattleAttackButton);
        panel.add(myBattleSpecialButton);
        panel.add(myBattleHealButton);
        panel.add(myBattleVisionButton);
        panel.add(myBattleRunButton);
        return panel;
    }

    private JScrollPane wrapTextArea(final JTextArea theTextArea) {
        JScrollPane scrollPane = new JScrollPane(theTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(EDITOR_BACKGROUND);
        scrollPane.setMinimumSize(new Dimension(0, 0));
        theTextArea.setMinimumSize(new Dimension(0, 0));
        return scrollPane;
    }

    private void refreshContent() {
        updateScaledFonts();
        revalidate();
        repaint();
    }

    private void setScalableFont(final JComponent theComponent,
                                 final int theStyle,
                                 final int theBaseSize) {
        myFontManager.setScalableFont(theComponent, theStyle, theBaseSize);
    }

    private void updateScaledFonts() {
        myFontManager.updateScaledFonts(getContentPane());
    }

    private void handleBattleResult(final Battle.BattleResult theResult) {
        appendBattleMessages(theResult);
        if (theResult.isMonsterDefeated()
                && myDungeon.getCurrentRoom().getMonster() == myBattle.getMonster()) {
            myDungeon.getCurrentRoom().removeMonster();
        }
        updateBattleView();
    }

    private void leaveBattleScreen() {
        String message = "Returned to the dungeon.";
        if (myBattle != null && myBattle.isActive()) {
            Battle.BattleResult result = myBattle.run();
            appendBattleMessages(result);
            message = lastBattleMessage(result, message);
        }
        myBattle = null;
        showDungeonView();
        setStatus(message);
    }

    private void updateBattleView() {
        if (myBattleHeroDisplay != null) {
            myBattleHeroDisplay.setText(buildBattleHeroText());
        }
        if (myBattleMonsterDisplay != null && myBattle != null) {
            myBattleMonsterDisplay.setText(buildBattleMonsterText());
        }

        boolean active = myBattle != null && myBattle.isActive();
        setBattleButtonEnabled(myBattleAttackButton, active);
        setBattleButtonEnabled(myBattleSpecialButton, active);
        setBattleButtonEnabled(myBattleHealButton,
                active && myBattle.canUseHealingPotion());
        setBattleButtonEnabled(myBattleVisionButton,
                active && myBattle.canUseVisionPotion());
        if (myBattleRunButton != null) {
            myBattleRunButton.setText(active ? "Run" : "Return");
            myBattleRunButton.setEnabled(myBattle != null);
        }
    }

    private void setBattleButtonEnabled(final JButton theButton,
                                        final boolean theEnabled) {
        if (theButton != null) {
            theButton.setEnabled(theEnabled);
        }
    }

    private String buildBattleHeroText() {
        return "Class: " + myHero.getClass().getSimpleName()
                + System.lineSeparator() + "Name: " + myHero.getName()
                + System.lineSeparator() + "HP: " + myHero.getHitPoints()
                + "/" + myHero.getMaxHitPoints()
                + System.lineSeparator() + "Damage: " + myHero.getMinDamage()
                + "-" + myHero.getMaxDamage()
                + System.lineSeparator() + "Speed: " + myHero.getAttackSpeed()
                + System.lineSeparator() + "Hit Chance: " + percent(myHero.getHitChance())
                + System.lineSeparator() + "Block Chance: " + percent(myHero.getChanceToBlock())
                + System.lineSeparator() + "Healing Potions: " + myHero.getHealingPotions()
                + System.lineSeparator() + "Vision Potions: " + myHero.getVisionPotions()
                + System.lineSeparator() + "Pillars: " + myHero.getPillars();
    }

    private String buildBattleMonsterText() {
        Monster monster = myBattle.getMonster();
        return "Type: " + monster.getClass().getSimpleName()
                + System.lineSeparator() + "Name: " + monster.getName()
                + System.lineSeparator() + "HP: " + monster.getHitPoints()
                + "/" + monster.getMaxHitPoints()
                + System.lineSeparator() + "Damage: " + monster.getMinDamage()
                + "-" + monster.getMaxDamage()
                + System.lineSeparator() + "Speed: " + monster.getAttackSpeed()
                + System.lineSeparator() + "Hit Chance: " + percent(monster.getHitChance())
                + System.lineSeparator() + "Heal Chance: "
                + percent(monster.getChanceToHeal())
                + System.lineSeparator() + "Heal Range: "
                + monster.getMinHeal() + "-" + monster.getMaxHeal();
    }

    private void appendBattleMessages(final Battle.BattleResult theResult) {
        for (String message : theResult.getMessages()) {
            appendBattleLog(message);
        }
    }

    private String lastBattleMessage(final Battle.BattleResult theResult,
                                     final String theFallback) {
        if (theResult.getMessages().isEmpty()) {
            return theFallback;
        }
        return theResult.getMessages().get(theResult.getMessages().size() - 1);
    }

    private void appendBattleLog(final String theText) {
        if (myBattleLogDisplay != null) {
            myBattleLogDisplay.append(theText + System.lineSeparator());
            myBattleLogDisplay.setCaretPosition(
                    myBattleLogDisplay.getDocument().getLength());
        }
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
            message += " Stepped in lava for " + pitDamage + " damage.";
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
        return myMapRenderer.render(myDungeon);
    }

    private String formatPillar(final Pillar thePillar) {
        String name = thePillar.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
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
