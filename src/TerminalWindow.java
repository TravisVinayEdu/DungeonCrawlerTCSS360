import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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
                myGame.runGame(this);
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
