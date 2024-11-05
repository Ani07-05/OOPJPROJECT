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
    // NoteManager instance to handle all note-related operations (add, remove, load, and save notes).
    private NoteManager noteManager = new NoteManager();
    
    // Model to hold and manage the list of note titles, displayed in noteList.
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    
    // JList component to display the titles of notes. The user can select a note from this list.
    private JList<String> noteList = new JList<>(listModel);
    
    // Text area to display the content of the currently selected note.
    // This area becomes editable when the user selects a note from the list.
    private JTextArea noteContentArea = new JTextArea();
    
    // Button to save any changes made to the current note's content.
    // It is initially hidden and only becomes visible when a note is selected for editing.
    private JButton saveButton = new JButton("Save Changes");
    
    // Reference to the currently selected note. Used for tracking the note that is being edited.
    private Note currentNote = null;
    
    // Panel displayed initially when the application starts. It contains the application title and a "Create New Note" button.
    private JPanel welcomePanel;
    
    // Main panel for editing notes, containing the note list on the left, note content in the center, and save button at the bottom.
    // This panel is displayed after the user creates or selects a note.
    private JPanel notePanel;
    
    // Toolbar panel containing buttons for creating and removing notes.
    // The toolbar is hidden initially and only shown after the welcome screen is dismissed.
    private JPanel toolbarPanel;

    public MainWindow() {
        // Constructor for MainWindow, sets up the initial interface and displays the welcome screen.
        setTitle("VakyaVault");  
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        noteManager.loadNotes(); // Load previously saved notes using NoteManager.

        // Apply dark theme to the application components for a consistent appearance.
        applyDarkTheme();

        // Initialize and configure the toolbarPanel.
        // This panel contains buttons for "Create New Note" and "Remove Selected Note".
        toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        toolbarPanel.setBackground(Color.DARK_GRAY);
        toolbarPanel.setVisible(false);  // Initially hidden until a note is created or selected.

        // Button to create a new note. Added to the toolbar and also triggers addTextNote() when clicked.
        JButton addTextNoteButton = new JButton("Create New Note");
        
        // Button to remove the currently selected note from the list. Added to the toolbar and triggers removeSelectedNote() on click.
        JButton removeNoteButton = new JButton("Remove Selected Note");

        // Add action listeners to the buttons to handle the note creation and removal functionalities.
        addTextNoteButton.addActionListener(e -> addTextNote());
        removeNoteButton.addActionListener(e -> removeSelectedNote());

        // Add buttons to the toolbar panel.
        toolbarPanel.add(addTextNoteButton);
        toolbarPanel.add(removeNoteButton);

        // Initialize the welcomePanel for the initial screen shown at startup.
        welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(Color.BLACK);

        // Label displaying the application title in the welcome screen.
        JLabel titleLabel = new JLabel("VakyaVault", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Button in the welcome screen to create a new note.
        // Triggers addTextNote() when clicked and is centered on the screen.
        JButton welcomeAddNoteButton = new JButton("Create New Note");
        welcomeAddNoteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeAddNoteButton.addActionListener(e -> addTextNote());

        // Add title and button to the welcomePanel with spacing for centered alignment.
        welcomePanel.add(Box.createVerticalGlue());
        welcomePanel.add(titleLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomePanel.add(welcomeAddNoteButton);
        welcomePanel.add(Box.createVerticalGlue());

        // Initialize the main notePanel, which is displayed after the welcome screen is dismissed.
        notePanel = new JPanel(new BorderLayout());
        notePanel.setBackground(Color.BLACK);

        // Configure the noteContentArea (display area for note content).
        // Initially set to non-editable until a note is selected.
        noteContentArea.setEditable(false);
        noteContentArea.setLineWrap(true);
        noteContentArea.setWrapStyleWord(true);
        noteContentArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Add listener to noteList for displaying selected note content in noteContentArea.
        noteList.addListSelectionListener(e -> displaySelectedNote());
        
        // Configure saveButton to save changes to the current note when clicked.
        // Initially hidden until a note is selected for editing.
        saveButton.addActionListener(e -> saveChanges());
        saveButton.setVisible(false);

        // Arrange components in notePanel.
        // The noteList is displayed on the left, noteContentArea in the center, and saveButton at the bottom.
        notePanel.add(new JScrollPane(noteList), BorderLayout.WEST);
        notePanel.add(new JScrollPane(noteContentArea), BorderLayout.CENTER);
        notePanel.add(saveButton, BorderLayout.SOUTH);

        // Set the initial view to display the welcomePanel in the center of the window.
        setLayout(new BorderLayout());
        add(welcomePanel, BorderLayout.CENTER);
    }

    /**
     * Method to create a new text note. Opens a dialog to enter title and content.
     * If the user confirms, it adds the note to NoteManager, updates the list, and switches to the note editor view.
     */
    private void addTextNote() {
        // ...
    }

    /**
     * Method to remove the selected note from the note list.
     * Finds the selected note, removes it from NoteManager, updates the list, and clears the content area.
     */
    private void removeSelectedNote() {
        // ...
    }

    /**
     * Method to display the content of the selected note in the text area.
     * Sets the text area to editable and shows the save button.
     */
    private void displaySelectedNote() {
        // ...
    }

    /**
     * Method to save changes made to the content of the currently selected note.
     * Updates the note content in NoteManager and reloads notes from storage.
     */
    private void saveChanges() {
        // ...
    }

    /**
     * Method to update the note list displayed in noteList with titles of all notes.
     */
    private void updateNoteList() {
        // ...
    }

    /**
     * Method to apply a dark theme to the main window and its components.
     * Sets background and foreground colors to match the dark theme.
     */
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

    /**
     * Method to switch from the welcome screen to the main note editor view.
     * Displays the toolbar and notePanel, and hides the welcomePanel.
     */
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
