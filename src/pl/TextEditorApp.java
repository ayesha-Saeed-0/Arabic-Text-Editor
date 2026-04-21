package pl;

import bll.DocumentFacadeService;
import bll.DuplicateFileException;
import bll.LemmaBO;

import dal.LemmaDAO;
import dto.SearchResult;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class TextEditorApp extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static DocumentFacadeService documentFacadeService;
	private JTextField searchField;
	private JTextArea searchResultsArea;
	private JProgressBar progressBar;
	public static boolean checkboxMode = false;
	private JTextArea textArea;
	// Assuming you need this for something in the app

	public TextEditorApp(DocumentFacadeService documentFacadeService) {
		TextEditorApp.documentFacadeService = documentFacadeService;

		// Set the main frame properties
		setTitle("Text Editor App");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create the main panel to hold all other panels and set its background color
		// to white
		JPanel mainPanel = new JPanel(new GridBagLayout());
		setExtendedState(JFrame.MAXIMIZED_BOTH); // Set to full screen
		setUndecorated(false);
		mainPanel.setBackground(new Color(245, 245, 220)); // Set main panel background color to white
		JLabel Name = new JLabel("تطبيق محرر النصوص");
		Name.setFont(new Font("Calibri Light", Font.BOLD, 24));

		JPanel Namepanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		Namepanel.setBackground(new Color(245, 245, 220));
		Namepanel.add(Name);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		// Create a panel for the search section with minimal spacing and set its
		// background color
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		searchPanel.setBackground(new Color(245, 245, 220)); // Set search panel background color to white
		JLabel searchLabel = new JLabel("Search: ");
		searchField = new JTextField(15);
		searchField.setBackground(new Color(245, 245, 220));
		JButton searchButton = new JButton("Search");
		searchButton.setBackground(new Color(210, 180, 140));
		searchPanel.add(searchLabel);
		searchPanel.add(searchField);
		searchPanel.add(searchButton);

		// Create a panel for the other buttons (Import, View, Create) with minimal
		// spacing and set its background color
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		buttonPanel.setBackground(new Color(245, 245, 220));// Set button panel background color to white
		JButton importButton = new JButton("Import File");
		JButton viewButton = new JButton("View File");
		JButton createButton = new JButton("Create File");

		importButton.setBackground(new Color(210, 180, 140));
		viewButton.setBackground(new Color(210, 180, 140));
		createButton.setBackground(new Color(210, 180, 140));

		JButton performAnalysis = new JButton("Perform Analysis");
		performAnalysis.setBackground(new Color(210, 180, 140));
		JButton StatisticalAnalysis = new JButton("Statistical Measure");
		StatisticalAnalysis.setBackground(new Color(210, 180, 140));

		buttonPanel.add(importButton);
		buttonPanel.add(viewButton);
		buttonPanel.add(createButton);
		buttonPanel.add(performAnalysis);
		buttonPanel.add(StatisticalAnalysis);

		// Create a text area for displaying search results
		searchResultsArea = new JTextArea(15, 50);
		searchResultsArea.setEditable(false);
		JScrollPane resultsScrollPane = new JScrollPane(searchResultsArea);
		resultsScrollPane.setBackground(new Color(245, 245, 220));
		// Add action listeners for the buttons

		importButton.addActionListener(e -> {
			setVisible(false);
			FileImporterGUI();
		});
		viewButton.addActionListener(e -> {
			setVisible(false);
			ViewTable();
		});
		createButton.addActionListener(e -> {
			setVisible(false);
			createGUI();
		});
		StatisticalAnalysis.addActionListener(e -> {
			setVisible(false);
			StatisticalAnalysis();

		});

		performAnalysis.addActionListener(e -> {
			setVisible(false);
			performLemmatizationAndPOSTagging();
		});

		searchButton.addActionListener(e -> {
			String searchWord = searchField.getText().trim();
			if (!searchWord.isEmpty()) {
				performSearch(searchWord);
			} else {
				JOptionPane.showMessageDialog(null, "Please enter a word to search.");
			}
		});

		gbc.gridx = 3; // Move to the right of Namepanel
		gbc.gridy = 0; // Same row as Namepanel
		gbc.anchor = GridBagConstraints.EAST; // Align to the right
		mainPanel.add(searchPanel, gbc);
		gbc.gridx = 0; // Move to the right of Namepanel
		gbc.gridy = 0; // Same row as Namepanel
		gbc.anchor = GridBagConstraints.CENTER; // Align to the right
		mainPanel.add(Namepanel, gbc);

		// Position the buttonPanel below the Namepanel
		gbc.gridx = 1; // Center it below Namepanel
		gbc.gridy = 1; // Move to the next row
		gbc.gridwidth = 2; // Span across both columns
		gbc.anchor = GridBagConstraints.EAST; // Center align
		mainPanel.add(buttonPanel, gbc);

		// Position the resultsScrollPane at the bottom, spanning the entire width
		gbc.gridx = 0;
		gbc.gridy = 2; // Move to the next row
		gbc.gridwidth = 50; // Span across both columns
		gbc.fill = GridBagConstraints.BOTH; // Allow it to expand in both directions
		gbc.weighty = 1.0; // Give vertical space for stretching
		mainPanel.add(resultsScrollPane, gbc);

		// Add the mainPanel to the frame
		add(mainPanel, BorderLayout.CENTER);

		setSize(860, 500); // Adjust window size as needed
		setVisible(true);
	}

	private void FileImporterGUI() { // Create a new dialog for importing files
		JDialog importDialog = new JDialog(this, "Import Files", true);
		importDialog.setLayout(new BorderLayout());
		importDialog.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		importDialog.setUndecorated(true);
		importDialog.setLocationRelativeTo(this); // Center the dialog
		JButton backButton = new JButton("←");
		backButton.setBackground(new Color(210, 180, 140));
		JPanel topPanel = new JPanel();
		JPanel secPanel = new JPanel();

		topPanel.setBackground(new Color(245, 245, 220));
		topPanel.add(backButton, BorderLayout.WEST);
		JButton importFilesButton = new JButton("Import Files");
		secPanel.setBackground(new Color(50, 25, 0));
		secPanel.add(importFilesButton);

		importFilesButton.setBackground(new Color(210, 180, 140));
		JTextArea displayArea = new JTextArea(10, 30);
		displayArea.setEditable(false);
		displayArea.setBackground(new Color(245, 245, 220));
		JScrollPane scrollPane = new JScrollPane(displayArea);

		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				importDialog.dispose(); // Close the current frame
				setVisible(true);
			}
		});
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setBackground(new Color(210, 180, 140));
		JLabel statusLabel = new JLabel("Select .txt files to import.");
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setForeground(new Color(50, 25, 0));
		bottomPanel.add(statusLabel, BorderLayout.NORTH);
		bottomPanel.add(progressBar, BorderLayout.SOUTH);
		importDialog.add(secPanel, BorderLayout.NORTH);
		importDialog.add(topPanel, BorderLayout.WEST);

		importDialog.add(scrollPane, BorderLayout.CENTER);
		importDialog.add(bottomPanel, BorderLayout.SOUTH);

		setSize(860, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		// Initialize text area and add it to the frame
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane1 = new JScrollPane(textArea);
		add(scrollPane1, BorderLayout.CENTER);

//	    // Request document and page ID, then fetch content
//	    requestDocumentAndPageId();
//	    fetchAndDisplayContent();

		importFilesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setMultiSelectionEnabled(true); // Allow multiple selection
				fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
				fileChooser.setAcceptAllFileFilterUsed(false); // Disable "All Files" option

				int result = fileChooser.showOpenDialog(importDialog);

				if (result == JFileChooser.APPROVE_OPTION) {
					File[] selectedFiles = fileChooser.getSelectedFiles();
					if (selectedFiles.length == 0) {
						statusLabel.setText("No files selected.");
						return;
					}

					List<String> validFilePaths = new ArrayList<>();
					List<String> invalidFiles = new ArrayList<>();

					for (File file : selectedFiles) {
						if (file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
							validFilePaths.add(file.getAbsolutePath());
						} else {
							invalidFiles.add(file.getName());
						}
					}

					if (!invalidFiles.isEmpty()) {
						String invalidList = String.join(", ", invalidFiles);
						JOptionPane.showMessageDialog(importDialog,
								"The following files are not .txt and will be skipped:\n" + invalidList,
								"Invalid File Types", JOptionPane.WARNING_MESSAGE);
					}

					if (validFilePaths.isEmpty()) {
						statusLabel.setText("No valid .txt files selected.");
						return;
					}

					statusLabel.setText("Importing " + validFilePaths.size() + " .txt files...");
					progressBar.setValue(0);
					progressBar.setMaximum(validFilePaths.size());
					displayArea.setText("");
					SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
						private List<String> duplicateFiles = new ArrayList<>();

						@Override
						protected Void doInBackground() throws Exception {
							int count = 0;
							for (String filePath : validFilePaths) {
								try {
									documentFacadeService.importFile(filePath);
									count++;
									publish("Imported: " + new File(filePath).getName());
								} catch (DuplicateFileException dupEx) {
									duplicateFiles.add(new File(filePath).getName());
									publish("Duplicate: " + new File(filePath).getName());
								} catch (Exception ex) {
									publish("Error importing '" + new File(filePath).getName() + "': "
											+ ex.getMessage());
								}
								setProgress((int) ((count / (double) validFilePaths.size()) * 100));
							}
							return null;
						}

						@Override
						protected void process(List<String> chunks) {
							for (String message : chunks) {
								displayArea.append(message + "\n");
							}
							progressBar.setValue(getProgress());
							statusLabel.setText("Importing files...");
						}

						@Override
						protected void done() {
							try {
								get();
								statusLabel.setText(
										"Bulk import completed. Imported " + progressBar.getValue() + " files.");
								progressBar.setValue(validFilePaths.size());
							} catch (Exception e) {
								statusLabel.setText("An error occurred during import.");
							}

							if (!duplicateFiles.isEmpty()) {
								String duplicates = String.join(", ", duplicateFiles);
								JOptionPane.showMessageDialog(importDialog,
										"Duplicate files detected and not imported:\n" + duplicates, "Duplicate Files",
										JOptionPane.ERROR_MESSAGE);
							} else {
								JOptionPane.showMessageDialog(importDialog, "Bulk import completed successfully.",
										"Import Status", JOptionPane.INFORMATION_MESSAGE);
							}
						}
					};

					worker.execute();

				} else {
					statusLabel.setText("File import canceled.");
				}
			}
		});

		importDialog.setVisible(true);
	}

	private void ViewTable() {
		JFrame frame = new JFrame("Document");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Changed to DISPOSE_ON_CLOSE
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Set to full screen
		frame.setUndecorated(false);
		frame.setLayout(new BorderLayout());
		ImageIcon refreshIcon = new ImageIcon("C:\\Users\\Khushbakht\\Downloads\\refresh.png");
		Image scaledImage = refreshIcon.getImage().getScaledInstance(30, 10, Image.SCALE_SMOOTH);
		JButton refreshButton = new JButton(new ImageIcon(scaledImage));
		frame.setBackground(new Color(245, 245, 220));
		JPanel topPanel = new JPanel(new BorderLayout());
		JButton backButton = new JButton("←");
		backButton.setBackground(new Color(210, 180, 140));
		topPanel.add(backButton, BorderLayout.WEST);
		topPanel.setBackground(new Color(50, 25, 0)); // Optional: Background color for the top panel
		frame.add(topPanel, BorderLayout.NORTH);

		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose(); // Close the current frame
				setVisible(true);
			}
		});

		refreshButton.setPreferredSize(new Dimension(0, 15));
		frame.add(refreshButton, BorderLayout.SOUTH);

		DefaultTableModel model = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 0;
			}

			@Override
			public Class<?> getColumnClass(int column) {
				return (column == 0) ? Boolean.class : String.class;
			}
		};
		model.setColumnIdentifiers(new Object[] { "Select", "Title", "Creation Date", "Updation Date" });

		JTable table = new JTable(model);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setBackground(new Color(245, 245, 220));
		JTableHeader header = table.getTableHeader();
		header.setBackground(new Color(210, 180, 140)); // Light brown background
		header.setForeground(Color.BLACK); // Optional: Set text color for contrast
		header.setFont(header.getFont().deriveFont(Font.BOLD));
		TableColumn checkboxColumn = table.getColumnModel().getColumn(0);
		checkboxColumn.setMinWidth(0);
		checkboxColumn.setMaxWidth(0);
		checkboxColumn.setPreferredWidth(0);

		TableColumn creationDateColumn = table.getColumnModel().getColumn(2);
		creationDateColumn.setMinWidth(100);
		creationDateColumn.setMaxWidth(120);
		creationDateColumn.setPreferredWidth(110);
		TableColumn updationDateColumn = table.getColumnModel().getColumn(3);
		updationDateColumn.setMinWidth(100);
		updationDateColumn.setMaxWidth(120);
		updationDateColumn.setPreferredWidth(110);

		frame.add(scrollPane, BorderLayout.CENTER);

		List<String> documentDetails = documentFacadeService.getFormattedDocuments();

		for (String documentDetail : documentDetails) {
			Vector<Object> row = new Vector<>();
			row.add(false);

			String[] parts = documentDetail.split(", ");

			for (String part : parts) {
				String[] keyValue = part.split(": ");
				if (keyValue.length == 2) {
					row.add(keyValue[1]);
				}
			}

			model.addRow(row);
		}

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int selectedRow = table.rowAtPoint(e.getPoint());
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && selectedRow != -1) {
					String title = (String) table.getValueAt(selectedRow, 1);
					try {
						openDocumentContent(frame, title, documentFacadeService);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					showPopupMenu(e, table, model, checkboxColumn, documentFacadeService);
				}
			}
		});

		refreshButton.addActionListener(e -> {
			checkboxColumn.setMinWidth(0);
			checkboxColumn.setMaxWidth(0);
		});

		frame.setVisible(true);
	}

	public static void openDocumentContent(JFrame mainFrame, String title, DocumentFacadeService documentBO)
			throws SQLException {
		List<String> documentContentList = documentBO.getDocumentContentByTitle(title);

		if (documentContentList.isEmpty()) {
			System.out.println("No content found for the document with title: " + title);
			return;
		}

		final int[] currentPage = { 0 };
		final String[] originalText = { documentContentList.get(currentPage[0]) }; // Track original text
		JButton undoButton = new JButton("Undo"); // Undo button
		undoButton.setBackground(new Color(210, 180, 140));
		JFrame contentFrame = createContentFrame(title);
		contentFrame.setBackground(new Color(245, 245, 220));
		contentFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Set to full screen
		contentFrame.setUndecorated(false);

		JTextArea textArea = new JTextArea(documentContentList.get(currentPage[0])) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				// Define left and right margin positions
				int leftMargin = 50;
				int rightMargin = getWidth() - 50;

				// Set color and draw the margin lines
				g.setColor(Color.LIGHT_GRAY);
				g.drawLine(leftMargin, 0, leftMargin, getHeight()); // Left margin line
				g.drawLine(rightMargin, 0, rightMargin, getHeight()); // Right margin line
			}
		};

		// Enable word wrap in JTextArea
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setFont(new Font("Serif", Font.PLAIN, 16));
		textArea.setMargin(new Insets(0, 50, 0, 50)); // Add padding inside JTextArea
		textArea.setBackground(new Color(245, 245, 220));

		contentFrame.add(new JScrollPane(textArea), BorderLayout.CENTER);

		JLabel wordCountLabel = new JLabel("Word Count: " + countWords(documentContentList.get(currentPage[0])));
		JLabel pageNumberLabel = new JLabel("Page: " + (currentPage[0] + 1) + " of " + documentContentList.size());
		wordCountLabel.setForeground(Color.LIGHT_GRAY);
		pageNumberLabel.setForeground(Color.LIGHT_GRAY);
		JPanel buttonPanel = createButtonPanel(textArea, documentContentList, currentPage, wordCountLabel,
				pageNumberLabel, title, documentBO, mainFrame, contentFrame);

		// Add Undo button to button panel
		undoButton.setEnabled(false); // Initially disabled
		buttonPanel.add(undoButton);
		buttonPanel.setBackground(new Color(50, 25, 0));
		JPanel fontControlPanel = createFontControlPanel(textArea, documentContentList, currentPage, wordCountLabel,
				pageNumberLabel, title, documentBO, mainFrame, contentFrame);
		fontControlPanel.setBackground(new Color(50, 25, 0));
		contentFrame.add(fontControlPanel, BorderLayout.NORTH);
		contentFrame.add(buttonPanel, BorderLayout.SOUTH);

		// Document listener to update word count dynamically
		textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			@Override
			public void insertUpdate(javax.swing.event.DocumentEvent e) {
				updateWordCount();
				checkUndoState(); // Check if we can enable undo
			}

			@Override
			public void removeUpdate(javax.swing.event.DocumentEvent e) {
				updateWordCount();
				checkUndoState(); // Check if we can enable undo
			}

			@Override
			public void changedUpdate(javax.swing.event.DocumentEvent e) {
				updateWordCount();
				checkUndoState(); // Check if we can enable undo
			}

			private void updateWordCount() {
				String text = textArea.getText().trim();
				int wordCount = text.isEmpty() ? 0 : text.split("\\s+").length;
				wordCountLabel.setText("Word Count: " + wordCount);
			}

			private void checkUndoState() {
				undoButton.setEnabled(!textArea.getText().equals(originalText[0]));
			}
		});

		undoButton.addActionListener(e -> {
			// Restore the original content
			textArea.setText(originalText[0]);
			originalText[0] = textArea.getText(); // Update original text to current
			undoButton.setEnabled(false); // Disable undo after action
		});

		// Right-click context menu for transliteration (same as before)
		JPopupMenu contextMenu = new JPopupMenu();
		JMenuItem transliterateMenuItem = new JMenuItem("Transliterate");

		transliterateMenuItem.addActionListener(e -> {
			String selectedText = textArea.getSelectedText();
			if (selectedText != null && !selectedText.isEmpty()) {
				// Transliterate the selected content
				String transliteratedText = documentBO.transliterateContent(selectedText, title);
				// Store the original content for undo
				originalText[0] = textArea.getText();
				// Display the transliterated content in the JTextArea
				textArea.replaceSelection(transliteratedText);
				
			} else {
				JOptionPane.showMessageDialog(contentFrame, "No text selected. Please select content to transliterate.",
						"Warning", JOptionPane.WARNING_MESSAGE);
			}
		});

		textArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		contextMenu.add(transliterateMenuItem);

		textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					contextMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					contextMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		mainFrame.setVisible(false);
		contentFrame.setVisible(true);
	}

	private static JPanel createFontControlPanel(JTextArea textArea, List<String> documentContentList,
			int[] currentPage, JLabel wordCountLabel, JLabel pageNumberLabel, String title,
			DocumentFacadeService documentBO, JFrame mainFrame, JFrame contentFrame) {
		JPanel fontControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Align to top-left

		// Set the preferred size with a reduced width, e.g., width 300 pixels and
		// height 50 pixels
		fontControlPanel.setPreferredSize(new Dimension(300, 50));

		// Menu button added to top-left
		JButton mainMenuButton = new JButton("Menu");
		mainMenuButton.setBackground(new Color(210, 180, 140));
		mainMenuButton.addActionListener(e -> createPopupMenu(mainMenuButton, textArea, documentContentList,
				currentPage, wordCountLabel, pageNumberLabel, title, documentBO, mainFrame, contentFrame));

		fontControlPanel.add(mainMenuButton);

		// Font size control
		String[] fontSizes = { "12", "14", "16", "18", "20", "22", "24", "26", "28", "30" };
		JComboBox<String> fontSizeComboBox = new JComboBox<>(fontSizes);
		fontSizeComboBox.setSelectedItem("18");
		fontSizeComboBox.setBackground(new Color(210, 180, 140));
		fontSizeComboBox.addActionListener(e -> {
			int fontSize = Integer.parseInt((String) fontSizeComboBox.getSelectedItem());
			textArea.setFont(textArea.getFont().deriveFont((float) fontSize));
		});
		fontControlPanel.add(fontSizeComboBox);

		// Font style control
		String[] fontStyles = { "Serif", "SansSerif", "Monospaced", "Arial", "Tahoma" };
		JComboBox<String> fontStyleComboBox = new JComboBox<>(fontStyles);

		fontStyleComboBox.setBackground(new Color(210, 180, 140));
		fontStyleComboBox.setSelectedItem("Serif");
		fontStyleComboBox.addActionListener(e -> {
			String selectedStyle = (String) fontStyleComboBox.getSelectedItem();
			textArea.setFont(new Font(selectedStyle, Font.PLAIN, textArea.getFont().getSize()));
		});
		fontControlPanel.add(fontStyleComboBox);

		return fontControlPanel;
	}

	// Method to create the content frame
	private static JFrame createContentFrame(String title) {
		JFrame contentFrame = new JFrame("Document: " + title);
		contentFrame.setSize(1400, 700);
		contentFrame.setLayout(new BorderLayout());
		return contentFrame;
	}

	// Method to create the button panel with navigation, menu, and other controls
	private static JPanel createButtonPanel(JTextArea textArea, List<String> documentContentList, int[] currentPage,
			JLabel wordCountLabel, JLabel pageNumberLabel, String title, DocumentFacadeService documentBO,
			JFrame mainFrame, JFrame contentFrame) {
		JPanel buttonPanel = new JPanel(new FlowLayout());

		buttonPanel.add(pageNumberLabel);
		buttonPanel.add(wordCountLabel);

		// Previous Button
		JButton prevButton = new JButton("Previous");
		prevButton.setBackground(new Color(210, 180, 140));
		prevButton.addActionListener(
				e -> navigatePage(-1, textArea, documentContentList, currentPage, wordCountLabel, pageNumberLabel));
		buttonPanel.add(prevButton);

		// Next Button
		JButton nextButton = new JButton("Next");
		nextButton.setBackground(new Color(210, 180, 140));
		nextButton.addActionListener(
				e -> navigatePage(1, textArea, documentContentList, currentPage, wordCountLabel, pageNumberLabel));
		buttonPanel.add(nextButton);

		return buttonPanel;
	}

	// Method to navigate pages
	private static void navigatePage(int direction, JTextArea textArea, List<String> documentContentList,
			int[] currentPage, JLabel wordCountLabel, JLabel pageNumberLabel) {
		int newPage = currentPage[0] + direction;
		if (newPage >= 0 && newPage < documentContentList.size()) {
			currentPage[0] = newPage;
			textArea.setText(documentContentList.get(currentPage[0]));
			wordCountLabel.setText("Word Count: " + countWords(documentContentList.get(currentPage[0])));
			pageNumberLabel.setText("Page: " + (currentPage[0] + 1) + " of " + documentContentList.size());
		}
	}

	// Method to create the popup menu for the main menu button
	private static void createPopupMenu(JButton mainMenuButton, JTextArea textArea, List<String> documentContentList,
			int[] currentPage, JLabel wordCountLabel, JLabel pageNumberLabel, String title,
			DocumentFacadeService documentBO, JFrame mainFrame, JFrame contentFrame) {
		JPopupMenu menu = new JPopupMenu();
		menu.setBackground(new Color(210, 180, 140));
		JMenuItem saveFileItem = new JMenuItem("Save File");
		saveFileItem.setBackground(new Color(210, 180, 140));
		saveFileItem.addActionListener(ae -> saveFile(textArea, documentContentList, currentPage, title, documentBO));
		menu.add(saveFileItem);

		JMenuItem addPageItem = new JMenuItem("Add Page");
		addPageItem.setBackground(new Color(210, 180, 140));
		addPageItem.addActionListener(
				ae -> addPage(textArea, documentContentList, currentPage, wordCountLabel, pageNumberLabel));
		menu.add(addPageItem);

		JMenuItem removePageItem = new JMenuItem("Remove Page");
		removePageItem.setBackground(new Color(210, 180, 140));
		removePageItem.addActionListener(
				ae -> removePage(textArea, documentContentList, currentPage, wordCountLabel, pageNumberLabel));
		menu.add(removePageItem);

		JMenuItem deleteFileItem = new JMenuItem("Delete File");
		deleteFileItem.setBackground(new Color(210, 180, 140));
		deleteFileItem.addActionListener(ae -> deleteFile(title, documentBO, mainFrame, contentFrame));
		menu.add(deleteFileItem);

		JMenuItem openOtherFileItem = new JMenuItem("Open Other File");
		openOtherFileItem.setBackground(new Color(210, 180, 140));
		openOtherFileItem.addActionListener(ae -> {
			contentFrame.dispose();
			mainFrame.setVisible(true);
		});
		JMenuItem analysis = new JMenuItem("Analysis");
		analysis.setBackground(new Color(210, 180, 140));
		analysis.addActionListener(ae -> displayAnalysis(title, documentContentList));

		JMenuItem export = new JMenuItem("Export");
		export.setBackground(new Color(210, 180, 140));
		export.addActionListener(ae -> exportFile(title, documentContentList));

		menu.add(analysis);
		menu.add(openOtherFileItem);
		menu.add(export);

		menu.show(mainMenuButton, mainMenuButton.getWidth() / 2, mainMenuButton.getHeight() / 2);
	}

	private static void exportFile(String title, List<String> documentContentList) {

		// Remove the file extension from the title, if any
		int lastDotIndex = title.lastIndexOf('.');
		if (lastDotIndex > 0) {
			title = title.substring(0, lastDotIndex);
		}

		if (!documentContentList.isEmpty()) {

			// Convert the list of strings into a single string
			String documentContent = String.join("\n", documentContentList);

			// Assuming documentFacadeService has a method to convert content to Markdown
			String markdownContent = documentFacadeService.convertToMarkdown(documentContent);

			if (markdownContent != null && !markdownContent.isEmpty()) {
				try (FileWriter writer = new FileWriter(new File(title + ".md"))) {
					writer.write(markdownContent);
					JOptionPane.showMessageDialog(null, "File exported successfully with Markdown formatting!",
							"Success", JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Error writing the file: " + e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(null, "Error: Markdown content is empty.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(null, "Error: Document content is empty.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void displayAnalysis(String title, List<String> documentContentList) {
		SwingWorker<Map<String, WordAnalysis>, Void> worker = new SwingWorker<>() {
			@Override
			protected Map<String, WordAnalysis> doInBackground() throws Exception {

				Map<String, WordAnalysis> analysisMap = new HashMap<>();
				for (String line : documentContentList) {
					String[] words = line.split("\\s+");
					for (String word : words) {
						if (!word.isEmpty()) {
							String normalizedWord = documentFacadeService.findStem(word);
							String lemma = documentFacadeService.getLemma(word);
							String stem = documentFacadeService.findStem(word);
							double tfidf = documentFacadeService.calculateTFIDF(normalizedWord, title);
							String posTaggingResponse = documentFacadeService.tagText(word);
							Object[][] posTaggingData = parsePOSResponse(posTaggingResponse);
							String posTag = (String) posTaggingData[0][1];

							analysisMap.put(word, new WordAnalysis(lemma, stem, posTag, tfidf));
						}
					}
				}
				return analysisMap;
			}

			@Override
			protected void done() {
				try {
					Map<String, WordAnalysis> analysisMap = get();
					showAnalysisWindow(analysisMap);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Error during analysis: " + e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	private static void showAnalysisWindow(Map<String, WordAnalysis> analysisMap) {
		JFrame frame = new JFrame("Analysis View");

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(600, 400);

		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen
		frame.setUndecorated(false);
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		topPanel.setBackground(new Color(50, 25, 0));
		JButton backButton = new JButton("←");
		backButton.setBackground(new Color(210, 180, 140)); // Light brown
		topPanel.add(backButton);

		String[] columnNames = { "Word", "Lemma", "Stem", "Post Tag", "TF-IDF" };
		Object[][] data = new Object[analysisMap.size()][5];

		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				frame.dispose();
				// setVisible(true);
			}
		});

		int rowIndex = 0;
		for (Map.Entry<String, WordAnalysis> entry : analysisMap.entrySet()) {
			String word = entry.getKey();
			WordAnalysis analysis = entry.getValue();

			data[rowIndex][0] = word;
			data[rowIndex][1] = analysis.getLemma();
			data[rowIndex][2] = analysis.getStem();
			data[rowIndex][3] = analysis.getPostTag();
			data[rowIndex][4] = analysis.getTfidf();
			rowIndex++;
		}

		JTable table = new JTable(data, columnNames);
		table.setBackground(new Color(245, 245, 220));
		JTableHeader header = table.getTableHeader();
		header.setBackground(new Color(210, 180, 140)); // Light brown background
		header.setForeground(Color.BLACK); // Optional: Set text color for contrast
		header.setFont(header.getFont().deriveFont(Font.BOLD));

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBackground(new Color(245, 245, 220));
		frame.add(topPanel, BorderLayout.NORTH);
		frame.add(scrollPane, BorderLayout.CENTER);
		frame.setVisible(true);
	}

	// Save file method
	private static void saveFile(JTextArea textArea, List<String> documentContentList, int[] currentPage, String title,
			DocumentFacadeService documentBO) {

		documentContentList.set(currentPage[0], textArea.getText());
		String content = String.join(" ", documentContentList);

		SwingWorker<Void, Void> saveWorker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws Exception {

				documentBO.updateDocument(title, content);
				return null;
			}

			@Override
			protected void done() {
				try {

					get();

					JOptionPane.showMessageDialog(null, "File saved successfully.", "Confirmation",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {

					ex.printStackTrace();
					JOptionPane.showMessageDialog(null, "An error occurred while saving the file: " + ex.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		// Execute the SwingWorker
		saveWorker.execute();
	}

	// Add page method
	private static void addPage(JTextArea textArea, List<String> documentContentList, int[] currentPage,
			JLabel wordCountLabel, JLabel pageNumberLabel) {
		documentContentList.add("");
		currentPage[0] = documentContentList.size() - 1;
		textArea.setText("");
		wordCountLabel.setText("Word Count: 0");
		pageNumberLabel.setText("Page: " + (currentPage[0] + 1) + " of " + documentContentList.size());
	}

	// Remove page method
	private static void removePage(JTextArea textArea, List<String> documentContentList, int[] currentPage,
			JLabel wordCountLabel, JLabel pageNumberLabel) {
		if (documentContentList.size() > 1) {
			documentContentList.remove(currentPage[0]);
			currentPage[0] = Math.max(0, currentPage[0] - 1);
			textArea.setText(documentContentList.get(currentPage[0]));
			wordCountLabel.setText("Word Count: " + countWords(documentContentList.get(currentPage[0])));
			pageNumberLabel.setText("Page: " + (currentPage[0] + 1) + " of " + documentContentList.size());
		} else {
			System.out.println("Cannot remove the only page.");
		}
	}

	// Delete file method
	private static void deleteFile(String title, DocumentFacadeService documentBO, JFrame mainFrame,
			JFrame contentFrame) {
		int confirm = JOptionPane.showConfirmDialog(contentFrame, "Are you sure you want to delete this document?",
				"Confirm Delete", JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			try {
				documentBO.deleteDocument(title);
				contentFrame.dispose();
				mainFrame.setVisible(true);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private static int countWords(String content) {
		return content.trim().isEmpty() ? 0 : content.trim().split("\\s+").length;
	}

	private static void showPopupMenu(MouseEvent e, JTable table, DefaultTableModel model, TableColumn checkboxColumn,
			DocumentFacadeService documentBO) {
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setBackground(new Color(50, 25, 0));
		JMenuItem selectItem = new JMenuItem("Select");
		selectItem.setBackground(new Color(210, 180, 140));
		JMenuItem deleteItem = new JMenuItem("Delete");
		deleteItem.setBackground(new Color(210, 180, 140));

		selectItem.addActionListener(event -> {
			if (!checkboxMode) {
				checkboxColumn.setMinWidth(50);
				checkboxColumn.setMaxWidth(50);
				checkboxColumn.setPreferredWidth(50);
				checkboxMode = true;
				JOptionPane.showMessageDialog(null, "You can now select rows using checkboxes.");
			}
		});

		deleteItem.addActionListener(event -> {
			if (checkboxMode) {
				int confirmation = JOptionPane.showConfirmDialog(null,
						"Are you sure you want to delete the selected documents?", "Delete Confirmation",
						JOptionPane.YES_NO_OPTION);
				if (confirmation == JOptionPane.YES_OPTION) {
					try {
						deleteSelectedDocuments(table, model, documentBO);
					} catch (HeadlessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			} else {
				JOptionPane.showMessageDialog(null, "No rows selected for deletion!", "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		});

		popupMenu.add(selectItem);
		popupMenu.add(deleteItem);
		popupMenu.show(table, e.getX(), e.getY());
	}

	private static void deleteSelectedDocuments(JTable table, DefaultTableModel model, DocumentFacadeService documentBO)
			throws HeadlessException, SQLException {
		for (int i = table.getRowCount() - 1; i >= 0; i--) {
			Boolean isSelected = (Boolean) table.getValueAt(i, 0);
			if (isSelected) {
				String title = (String) table.getValueAt(i, 1);
				if (documentBO.deleteDocument(title)) {
					model.removeRow(i);
				} else {
					JOptionPane.showMessageDialog(null, "Failed to delete the document.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private void createGUI() {
		JFrame createFrame = new JFrame("Create Document");
		createFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Set to full screen
		createFrame.setUndecorated(false);
		createFrame.setLayout(new BorderLayout());
		createFrame.setBackground(new Color(245, 245, 220));
		JPanel topPanel = new JPanel(new BorderLayout());
		JButton backButton = new JButton("←");
		backButton.setBackground(new Color(210, 180, 140));
		topPanel.add(backButton, BorderLayout.WEST);
		topPanel.setBackground(new Color(50, 25, 0)); // Optional: Background color for the top panel
		createFrame.add(topPanel, BorderLayout.NORTH);
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createFrame.dispose(); // Close the current frame
				setVisible(true);
			}
		});
		JTextArea textArea = new JTextArea() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				// Define margin positions
				int leftMargin = 50;
				int rightMargin = getWidth() - 50;

				// Set margin color and style
				g.setColor(Color.LIGHT_GRAY);
				g.drawLine(leftMargin, 0, leftMargin, getHeight()); // Left margin line
				g.drawLine(rightMargin, 0, rightMargin, getHeight()); // Right margin line
			}
		};

		// Enable line wrapping
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		textArea.setFont(new Font("Serif", Font.PLAIN, 16));
		textArea.setMargin(new Insets(0, 50, 0, 50)); // Add padding inside JTextArea
		textArea.setBackground(new Color(245, 245, 220));
		// textArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		JScrollPane scrollPane = new JScrollPane(textArea);
		createFrame.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setBackground(new Color(50, 25, 0));
		JButton saveButton = new JButton("Save");
		JButton cancelButton = new JButton("Cancel");
		saveButton.setBackground(new Color(210, 180, 140));
		cancelButton.setBackground(new Color(210, 180, 140));
		JLabel wordCountLabel = new JLabel("Word Count: 0");
		wordCountLabel.setForeground(Color.WHITE);
		buttonPanel.add(wordCountLabel);
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);
		createFrame.add(buttonPanel, BorderLayout.SOUTH);

		textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			@Override
			public void insertUpdate(javax.swing.event.DocumentEvent e) {
				updateWordCount();
			}

			@Override
			public void removeUpdate(javax.swing.event.DocumentEvent e) {
				updateWordCount();
			}

			@Override
			public void changedUpdate(javax.swing.event.DocumentEvent e) {
				updateWordCount();
			}

			private void updateWordCount() {
				String text = textArea.getText().trim();
				int wordCount = text.isEmpty() ? 0 : text.split("\\s+").length;
				wordCountLabel.setText("Word Count: " + wordCount);
			}
		});

		saveButton.addActionListener(e -> {
			String content = textArea.getText();
			String title = JOptionPane.showInputDialog(createFrame, "Enter Document Title:", "Document Title",
					JOptionPane.PLAIN_MESSAGE);
			if (title != null && !title.trim().isEmpty()) {
				try {
					title = title + ".txt";
					documentFacadeService.createAndSaveDocument(title, content);
					JOptionPane.showMessageDialog(createFrame, "Document created successfully!");
					createFrame.dispose();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(createFrame, "Error creating document: " + ex.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(createFrame, "Document title cannot be empty.", "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		});

		cancelButton.addActionListener(e -> createFrame.dispose());

		createFrame.setVisible(true);
	}

	private void displaySearchResults(List<SearchResult> results, String word) {

		JFrame resultsFrame = new JFrame("Search Results");
		resultsFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Set to full screen
		resultsFrame.setUndecorated(false);
		resultsFrame.setLayout(new BorderLayout()); // Specify layout
		JPanel topPanel = new JPanel(new BorderLayout());
		JButton backButton = new JButton("←");
		backButton.setBackground(new Color(210, 180, 140));
		topPanel.add(backButton, BorderLayout.WEST);
		topPanel.setBackground(new Color(50, 25, 0)); // Optional: Background color for the top panel

		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resultsFrame.dispose(); // Close the current frame
				setVisible(true);
			}
		});

		JLabel wordd = new JLabel("Searched Word: " + word);
		resultsFrame.add(wordd, BorderLayout.NORTH); // Add to specific region

		String[] columnNames = { "Title", "Page Number", "Word Before", "Word After" };
		DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

		JTable resultsTable = new JTable(tableModel);
		resultsTable.setBackground(new Color(245, 245, 220));
		JTableHeader header = resultsTable.getTableHeader();
		header.setBackground(new Color(50, 25, 0)); // Dark brown color
		header.setForeground(Color.WHITE); // Set text color for contrast
		header.setFont(header.getFont().deriveFont(Font.BOLD));
		resultsFrame.add(new JScrollPane(resultsTable), BorderLayout.CENTER); // Add JScrollPane

		// Clear previous results
		tableModel.setRowCount(0);

		// Add rows for each search result
		for (SearchResult result : results) {
			Object[] row = { result.getTitle(), result.getPageNumber(), result.getWordBefore(), result.getWordAfter() };
			tableModel.addRow(row); // Add a new row to the table model
		}

		resultsFrame.add(topPanel, BorderLayout.NORTH);
		resultsFrame.setVisible(true); // Show the results window
	}

	private static boolean isPopupEnabled = true; // Variable to track the state of the popup

	// Method to toggle the popup menu
	public static void togglePopupMenu(JButton toggleButton) {
		toggleButton.addActionListener(e -> {
			isPopupEnabled = !isPopupEnabled; // Toggle the state
			String message = isPopupEnabled ? "Popup menu enabled." : "Popup menu disabled.";
			JOptionPane.showMessageDialog(null, message);
		});
	}

	public void transliterateSelectedContent(String title) {
		String selectedText = textArea.getSelectedText();

		if (selectedText != null && !selectedText.isEmpty()) {
			// Transliterate the selected content
			String transliteratedText = documentFacadeService.transliterateContent(selectedText, title);

			// Display the transliterated content in a dialog
			JOptionPane.showMessageDialog(this, "Transliterated Content: " + transliteratedText, "Result",
					JOptionPane.INFORMATION_MESSAGE);

			// Replace selected text with transliterated text in JTextArea
			textArea.replaceSelection(transliteratedText);
		} else {
			JOptionPane.showMessageDialog(this, "No text selected. Please select content to transliterate.", "Warning",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	private void performSearch(String searchWord) {
		List<SearchResult> results = documentFacadeService.search(searchWord);
		displaySearchResults(results, searchWord); // Call the new method to display results
	}

//	private static void displayResultInNewWindow(Object[][] data) {
//		JFrame resultFrame = new JFrame("POS Tagging Results");
//		resultFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		resultFrame.setSize(600, 400);
//
//		String[] columnNames = { "Word", "POS Tag", "Tag Description" };
//		JTable resultTable = new JTable(data, columnNames);
//		JScrollPane scrollPane = new JScrollPane(resultTable);
//
//		resultFrame.add(scrollPane);
//		resultFrame.setVisible(true);
//	}

	private void performLemmatizationAndPOSTagging() {
		// Create JFrame
		JFrame frame = new JFrame("Arabic Word Analyzer");

		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen
		frame.setUndecorated(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		// Create top panel with a back button and input controls
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel tPanel = new JPanel(new FlowLayout());
		topPanel.setBackground(new Color(50, 25, 0));
		JButton backButton = new JButton("←");
		backButton.setBackground(new Color(210, 180, 140)); // Light brown
		topPanel.add(backButton);
		JLabel inputLabel = new JLabel("Enter Arabic Word:");
		inputLabel.setForeground(Color.WHITE);
		tPanel.add(inputLabel, BorderLayout.CENTER);

		JTextField inputField = new JTextField(20); // Wider input field
		tPanel.add(inputField, BorderLayout.CENTER);

		JButton analyzeButton = new JButton("Analyze");
		analyzeButton.setBackground(new Color(210, 180, 140));
		tPanel.add(analyzeButton, BorderLayout.CENTER);
		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BorderLayout()); // Nested layout for better control
		containerPanel.add(topPanel, BorderLayout.NORTH);
		containerPanel.add(tPanel, BorderLayout.SOUTH);
		frame.add(containerPanel, BorderLayout.NORTH);
		// Table for Results
		String[] columnNames = { "Word", "Root", "Stem", "Lemma", "POS Tag", "Tag Description" };
		DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
		JTable resultTable = new JTable(tableModel);
		tPanel.setBackground(new Color(50, 25, 0));
		resultTable.setBackground(new Color(245, 245, 220));
		JTableHeader header = resultTable.getTableHeader();
		header.setBackground(new Color(210, 180, 140)); // Light brown background
		header.setForeground(Color.BLACK); // Optional: Set text color for contrast
		header.setFont(header.getFont().deriveFont(Font.BOLD));

		// Adjust table size and center it within the frame
		JScrollPane tableScrollPane = new JScrollPane(resultTable);
		int tableWidth = 1400;
		int tableHeight = 600;
		tableScrollPane.setBackground(new Color(245, 245, 220));
		tableScrollPane.setPreferredSize(new Dimension(tableWidth, tableHeight));

		JPanel centerPanel = new JPanel(new GridBagLayout());
		centerPanel.setBackground(new Color(245, 245, 220)); // Optional background color
		centerPanel.add(tableScrollPane);

		frame.add(centerPanel, BorderLayout.CENTER);

		analyzeButton.addActionListener(e -> {
			String inputWord = inputField.getText().trim();

			if (inputWord.isEmpty()) {
				JOptionPane.showMessageDialog(frame, "Please enter a word.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			try {
				LemmaBO lemmaBO = new LemmaBO();
				String root = lemmaBO.findStem(inputWord); // Find root using LemmaBO
				String stem = lemmaBO.findStem(inputWord); // Find stem
				LemmaDAO lemmaDAO = new LemmaDAO();
				String lemma = lemmaDAO.fetchLemma(inputWord);

				String posTaggingResponse = documentFacadeService.tagText(inputWord);
				Object[][] posTaggingData = parsePOSResponse(posTaggingResponse);

				// Populate the table with combined results
				if (posTaggingData.length > 0) {
					for (Object[] posData : posTaggingData) {
						tableModel.addRow(new Object[] { posData[0], // Word
								root, // Root
								stem, // Stem
								lemma, // Lemma
								posData[1], // POS Tag
								posData[2] // Tag Description
						});
					}
				} else {
					tableModel.addRow(new Object[] { inputWord, root, stem, lemma, "N/A", "N/A" });
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(frame, "An error occurred: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				frame.dispose();
				setVisible(true);

				String inputWord = inputField.getText().trim();

				if (inputWord.isEmpty()) {
					JOptionPane.showMessageDialog(frame, "Please enter a word.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				try {

					String root = documentFacadeService.findStem(inputWord);
					String stem = documentFacadeService.findStem(inputWord);

					String lemma = documentFacadeService.getLemma(inputWord);

					// Add the results to the table
					tableModel.addRow(new Object[] { inputWord, root, stem, lemma });
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, "An error occurred: " + ex.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}

			}
		});

		frame.pack();
		frame.setVisible(true);
	}

	private static Object[][] parsePOSResponse(String response) {
		try {
			String[] xmlStrings = response.split("\n");

			List<Object[]> dataList = new ArrayList<>();

			for (String xmlString : xmlStrings) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				InputSource inputSource = new InputSource(new StringReader(xmlString));
				Document document = builder.parse(inputSource);

				NodeList infoList = document.getElementsByTagName("Info");
				for (int i = 0; i < infoList.getLength(); i++) {
					Node infoNode = infoList.item(i);
					if (infoNode.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) infoNode;
						String word = element.getElementsByTagName("Word").item(0).getTextContent();
						String serieTagAng = element.getElementsByTagName("SerieTagAng").item(0).getTextContent();
						String serieTagAra = element.getElementsByTagName("SerieTagAra").item(0).getTextContent();

						dataList.add(new Object[] { word, serieTagAng, serieTagAra });
					}
				}
			}

			return dataList.toArray(new Object[0][0]);
		} catch (Exception e) {
			e.printStackTrace();
			return new Object[0][0];
		}
	}

	void StatisticalAnalysis() {
		LemmaDAO lemmaDAO = new LemmaDAO();

		JFrame frame = new JFrame("Arabic Word Analyzer");
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		JButton backButton = new JButton("←");
		backButton.setBackground(new Color(210, 180, 140));
		JPanel secPanel = new JPanel();

		secPanel.setBackground(new Color(50, 25, 0));
		secPanel.add(backButton, BorderLayout.WEST);
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				setVisible(true);
			}
		});
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 0;
		JLabel top = new JLabel("File Name:");

		top.setForeground(Color.WHITE);
		topPanel.add(top, gbc);
		topPanel.setBackground(new Color(50, 25, 0)); // Beige color

		gbc.gridx = 1;
		JTextField fileNameField = new JTextField(20);
		topPanel.add(fileNameField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		JLabel select = new JLabel("Select Analysis Type:");
		select.setForeground(Color.WHITE);
		topPanel.add(select, gbc);

		gbc.gridx = 1;
		String[] analysisTypes = { "TF-IDF", "PMI", "PKL" };
		JComboBox<String> analysisTypeComboBox = new JComboBox<>(analysisTypes);
		analysisTypeComboBox.setBackground(new Color(245, 245, 220));
		topPanel.add(analysisTypeComboBox, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		JLabel word11 = new JLabel("Enter Word 1:");
		word11.setForeground(Color.WHITE);
		topPanel.add(word11, gbc);

		gbc.gridx = 1;
		JTextField word1Field = new JTextField(20);
		topPanel.add(word1Field, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		JLabel word22 = new JLabel("Enter Word 2:");
		word22.setForeground(Color.WHITE);
		topPanel.add(word22, gbc);

		gbc.gridx = 1;
		JTextField word2Field = new JTextField(20);
		word2Field.setEnabled(false); // Initially disabled
		topPanel.add(word2Field, gbc);

		gbc.gridx = 2;
		gbc.gridy = 3;
		JButton analyzeButton = new JButton("Perform Analysis");
		analyzeButton.setBackground(new Color(210, 180, 140));
		topPanel.add(analyzeButton, gbc);
		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BorderLayout()); // Nested layout for better control
		containerPanel.add(secPanel, BorderLayout.WEST);
		containerPanel.add(topPanel, BorderLayout.SOUTH);
		frame.add(containerPanel, BorderLayout.NORTH);

		analysisTypeComboBox.addActionListener(e -> {
			String selectedType = (String) analysisTypeComboBox.getSelectedItem();
			if ("PMI".equals(selectedType) || "PKL".equals(selectedType)) {
				word2Field.setEnabled(true);
			} else {
				word2Field.setEnabled(false);
				word2Field.setText("");
			}
		});

		JPanel centerPanel = new JPanel(new BorderLayout());
		String[] columnNames = { "Original Word", "Result" };
		DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
		JTable resultTable = new JTable(tableModel);
		JTableHeader header = resultTable.getTableHeader();
		header.setBackground(new Color(210, 180, 140));
		header.setForeground(Color.BLACK);
		header.setFont(header.getFont().deriveFont(Font.BOLD));

		JScrollPane tableScrollPane = new JScrollPane(resultTable);
		tableScrollPane.setBackground(new Color(245, 245, 220));
		centerPanel.add(tableScrollPane, BorderLayout.CENTER);
		frame.add(centerPanel, BorderLayout.CENTER);

		analyzeButton.addActionListener(e -> {
			String normalizedWord1;
			String normalizedWord2;
			String word1 = word1Field.getText().trim();
			String word2 = word2Field.getText().trim();
			String analysisType = (String) analysisTypeComboBox.getSelectedItem();
			String fileName = fileNameField.getText().trim();

			// List<String> documentContentList =
			// documentFacadeService.getDocumentContentByTitle(fileName);

			if (fileName.isEmpty()) {
				JOptionPane.showMessageDialog(frame, "Please select or enter a file name.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (word1.isEmpty()) {
				JOptionPane.showMessageDialog(frame, "Please enter Word 1.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			} else {
				normalizedWord1 = documentFacadeService.findStem(word2);
			}

			if (("PMI".equals(analysisType) || "PKL".equals(analysisType)) && word2.isEmpty()) {
				JOptionPane.showMessageDialog(frame, "Please enter Word 2 for analysis.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if ("PMI".equals(analysisType)) {
				normalizedWord2 = documentFacadeService.findStem(word2);
			} else if ("PKL".equals(analysisType)) {
				normalizedWord2 = documentFacadeService.findStem(word2);
			} else {
				normalizedWord2 = null;
			}

			String result;
			if ("PMI".equals(analysisType)) {
				double pmi = documentFacadeService.calculatePMI(normalizedWord1, normalizedWord2, fileName);
				result = "PMI: " + pmi;
			} else if ("PKL".equals(analysisType)) {
				result = "PKL: " + documentFacadeService.calculatePKL(normalizedWord1, normalizedWord2, fileName);
			} else {
				double tfidf = documentFacadeService.calculateTFIDF(normalizedWord1, fileName);
				result = "TF-IDF: " + tfidf;
			}

			String combinedWord = word1 + (word2.isEmpty() ? "" : ", " + word2);
			tableModel.addRow(new Object[] { combinedWord, result });
		});

		frame.setVisible(true);
	}

}
