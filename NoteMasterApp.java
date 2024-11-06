import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// --- Main Application Entry ---

// Main class defining the entry point of the application is written here. It calls a function to create the main window of the application.
public class NoteMasterApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        });
    }
}

// --- FileHandler Class (Encapsulation) ---
// This class handles the way notes are stored and retrieved from a file. It encapsulates the file handling details, saving and loading notes.
class FileHandler {
    // Encapsulates file handling details, saving and loading notes
    private static final String NOTES_FILE = "notes.ser";  // Notes are stored in the format of a serialized object. A serialized object is a way to convert an object into a sequence of bytes that can be saved to a file or sent over a network.

    public void saveNotes(List<Note> notes) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(NOTES_FILE))) {  // ObjectOutputStream is used to write objects to a file in a serialized format. 
            oos.writeObject(notes);
        } catch (IOException e) {
        }
    }

    // This method loads notes from a particular file using the ObjectInput Stream. It reads the serialized object from the file and converts it back into a list of notes. FileInput Stream is used to read the file.
    public List<Note> loadNotes() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(NOTES_FILE))) {
            return (List<Note>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}

// --- NoteManager Class (Encapsulation) ---
// This class manages the notes. It encapsulates the logic for adding, removing, and retrieving notes. It also uses the FileHandler class to save and load notes.
class NoteManager {
    // Encapsulates note management logic, hides details from MainWindow
    private List<Note> notes = new ArrayList<>();
    private FileHandler fileHandler = new FileHandler();

    public void addNote(Note note) {
        notes.add(note);
        fileHandler.saveNotes(notes);
    }

    public void removeNoteById(String noteId) {
        notes.removeIf(note -> note.getId().equals(noteId)); // each note has a unique id, it is used to identify the note to be removed
        fileHandler.saveNotes(notes);
    }

    public List<Note> getAllNotes() {
        return notes;
    }

    public void loadNotes() {
        notes = fileHandler.loadNotes();
    }
}

// --- Abstract Note Class (Abstraction) ---

// --- Abstract Note Class (Abstraction) ---
abstract class Note implements Serializable {
    
    private String id = UUID.randomUUID().toString(); // Unique identifier for each note, generated automatically
    private String title; // Title of the note
    
    public String getId() {
        return id;
    }

  
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Abstract method to display note content, must be implemented by subclasses
    public abstract String display();
}

// --- TextNote Class (Inheritance & Polymorphism) ---
class TextNote extends Note {
    // Concrete subclass of Note representing a simple text note
    
    private String content; // Content of the text note

    // Constructor to initialize title and content of the text note
    public TextNote(String title, String content) {
        setTitle(title);      // Setting title using superclass's setTitle method
        this.content = content; // Initializing the content specific to TextNote
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // Overridden display method to show the text note's details
    @Override
    public String display() {
        // Polymorphism: Overriding the abstract display method from Note
        // Returns a formatted string containing the title and content of the note
        return "Text Note - Title: " + getTitle() + "\nContent: " + content;
    }
}

// --- GUI Components ---
class MainWindow extends JFrame {
    private NoteManager noteManager = new NoteManager();
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> noteList = new JList<>(listModel);
    private JTextArea noteContentArea = new JTextArea();
    private JButton saveButton = new JButton("Save Changes");
    private Note currentNote = null;
    private JPanel welcomePanel; 
    private JPanel notePanel;
    private JPanel toolbarPanel;

    public MainWindow() {
        setTitle("VakyaVault");  
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        noteManager.loadNotes(); 

        // Apply Dark Theme
        applyDarkTheme();

        // Create Toolbar Panel
        toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        toolbarPanel.setBackground(Color.DARK_GRAY);
        toolbarPanel.setVisible(false);  // Hidden initially

        JButton addTextNoteButton = new JButton("Create New Note");
        JButton removeNoteButton = new JButton("Remove Selected Note");

        addTextNoteButton.addActionListener(e -> addTextNote());
        removeNoteButton.addActionListener(e -> removeSelectedNote());

        toolbarPanel.add(addTextNoteButton);
        toolbarPanel.add(removeNoteButton);

        // Welcome Panel (shown initially)
        welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(Color.BLACK);

        JLabel titleLabel = new JLabel("VakyaVAULT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton welcomeAddNoteButton = new JButton("Create New Note");
        welcomeAddNoteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeAddNoteButton.addActionListener(e -> addTextNote());

        welcomePanel.add(Box.createVerticalGlue());
        welcomePanel.add(titleLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomePanel.add(welcomeAddNoteButton);
        welcomePanel.add(Box.createVerticalGlue());

        // Note Editing Panel (Hidden initially)
        notePanel = new JPanel(new BorderLayout());
        notePanel.setBackground(Color.BLACK);

        noteContentArea.setEditable(false);
        noteContentArea.setLineWrap(true);
        noteContentArea.setWrapStyleWord(true);
        noteContentArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        noteList.addListSelectionListener(e -> displaySelectedNote());
        saveButton.addActionListener(e -> saveChanges());
        saveButton.setVisible(false);

        notePanel.add(new JScrollPane(noteList), BorderLayout.WEST);
        notePanel.add(new JScrollPane(noteContentArea), BorderLayout.CENTER);
        notePanel.add(saveButton, BorderLayout.SOUTH);

        // Set initial view to welcomePanel
        setLayout(new BorderLayout());
        add(welcomePanel, BorderLayout.CENTER);
    }

    private void addTextNote() {
        // Prompt for a new note
        JTextField titleField = new JTextField(20);
        JTextArea contentArea = new JTextArea(10, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        titleField.setBackground(Color.DARK_GRAY);
        titleField.setForeground(Color.WHITE);
        contentArea.setBackground(Color.DARK_GRAY);
        contentArea.setForeground(Color.WHITE);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("Enter Note Title:"), BorderLayout.NORTH);
        inputPanel.add(titleField, BorderLayout.CENTER);
        inputPanel.add(new JScrollPane(contentArea), BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "New Text Note", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText();
            String content = contentArea.getText();
            if (!title.isEmpty() && !content.isEmpty()) {
                Note note = new TextNote(title, content);
                noteManager.addNote(note);
                updateNoteList();
                showNotePanel(); // Switch to the note editing panel
            }
        }
    }

    private void removeSelectedNote() {
        int selectedIndex = noteList.getSelectedIndex();
        if (selectedIndex != -1) {
            String selectedNoteTitle = noteList.getSelectedValue();
            Note note = noteManager.getAllNotes().stream()
                    .filter(n -> n.getTitle().equals(selectedNoteTitle))
                    .findFirst()
                    .orElse(null);
            if (note != null) {
                noteManager.removeNoteById(note.getId());
                updateNoteList();
                noteContentArea.setText("");
                saveButton.setVisible(false);
            }
        }
    }

    private void displaySelectedNote() {
        String selectedTitle = noteList.getSelectedValue();
        currentNote = noteManager.getAllNotes().stream()
                .filter(n -> n.getTitle().equals(selectedTitle))
                .findFirst()
                .orElse(null);

        if (currentNote != null) {
            noteContentArea.setText(((TextNote) currentNote).getContent());
            noteContentArea.setEditable(true);
            saveButton.setVisible(true);
            showNotePanel(); // Switch to the note editing panel
        }
    }

    private void saveChanges() {
        if (currentNote != null && currentNote instanceof TextNote) {
            ((TextNote) currentNote).setContent(noteContentArea.getText());
            noteManager.addNote(currentNote);
            noteManager.loadNotes();
            JOptionPane.showMessageDialog(this, "Changes saved!");
        }
    }

    private void updateNoteList() {
        listModel.clear();
        for (Note note : noteManager.getAllNotes()) {
            listModel.addElement(note.getTitle());
        }
    }

    private void applyDarkTheme() {
        getContentPane().setBackground(Color.BLACK);
        noteContentArea.setBackground(Color.BLACK);
        noteContentArea.setForeground(Color.WHITE);
        noteList.setBackground(Color.BLACK);
        noteList.setForeground(Color.WHITE);
        noteContentArea.setCaretColor(Color.WHITE);
        noteList.setSelectionBackground(Color.GRAY);
        noteList.setSelectionForeground(Color.WHITE);
        saveButton.setBackground(Color.GRAY);
        saveButton.setForeground(Color.WHITE);
    }

    private void showNotePanel() {
        // Remove the welcomePanel, display the notePanel, and show the toolbar
        remove(welcomePanel);
        add(toolbarPanel, BorderLayout.NORTH);  // Add toolbar at the top
        add(notePanel, BorderLayout.CENTER);
        toolbarPanel.setVisible(true);  // Show toolbar
        revalidate();
        repaint();
    }
}
