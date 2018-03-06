import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Holds the users saved drawings, allowing them to view and delete them
 */
class Gallery extends JPanel {
    // Store a reference to the main editor - this is used to get the main window as a parent for a dialogue box
    private Editor editor;

    // The layout to allow us to switch between the three pages
    private CardLayout cardLayout;

    // Array to store the labels that have the saved drawings as their icons
    private JLabel[] labels = new JLabel[12];

    // Array list to store all the buffered images taken from the draw layer when the user saves
    private ArrayList<BufferedImage> images = new ArrayList<>();

    /* Array to store the button used to delete each drawing - defined here to allow setting
       enabled status outside the constructor */
    private JButton[] delete = new JButton[12];

    // Represents the current page displayed so the status of the next and previous buttons can be updated
    private int currentPage = 1;

    /**
     * Instantiate all panels to hold the drawings, adding the delete buttons and page controls.
     * @param editor to allow access to the JFrame container
     */
    Gallery(Editor editor) {
        this.editor = editor;
        // An array of panels which will hold each drawing and its corresponding delete button
        JPanel[] panels = new JPanel[12];

        // Instantiate each page setting 4 images per page using a grid layout
        JPanel pageOne = new JPanel(new GridLayout(2, 2));
        JPanel pageTwo = new JPanel(new GridLayout(2, 2));
        JPanel pageThree = new JPanel(new GridLayout(2, 2));

        // Set the gallery's overall layout so we can add the image panels along with the controls
        this.setLayout(new BorderLayout());

        // Use a card layout to switch between pages
        cardLayout = new CardLayout();
        JPanel pages = new JPanel(cardLayout);

        this.add(pages, BorderLayout.CENTER);

        // Add the three pages to the card layout
        pages.add(pageOne);
        pages.add(pageTwo);
        pages.add(pageThree);

        // A panel to store the bottom control panel holding the next page and prev page buttons
        JPanel controls = new JPanel();

        // Instantiate the forwards and backwards navigation buttons
        JButton previousPage = new JButton("Prev Page");
        JButton nextPage = new JButton("Next Page");

        // Initially make the previous button disabled as we will start on the first page
        previousPage.setEnabled(false);

        // Add an action listener to move to the previous page and update the status of the buttons
        previousPage.addActionListener(e -> {
            // Decrement the page number and enable the next button
            prevPage();
            nextPage.setEnabled(true);
            // Check if we are on the first page, if so disable the previous button
            if (currentPage == 1) {
                previousPage.setEnabled(false);
            } else {
                previousPage.setEnabled(true);
            }
            // Display the previous card
            cardLayout.previous(pages);
        });

        // Add an action listener to move to the next page and update the status of the buttons
        nextPage.addActionListener(e -> {
            // Increment the page number and enable the previous button
            nextPage();
            previousPage.setEnabled(true);
            // Check if we are on the last page, if so disable the next button
            if (currentPage == 3) {
                nextPage.setEnabled(false);
            } else {
                nextPage.setEnabled(true);
            }
            // Display the next card
            cardLayout.next(pages);
        });

        controls.add(previousPage);
        controls.add(nextPage);

        // Add the control panel to the bottom of the gallery panel
        this.add(controls, BorderLayout.SOUTH);

        // Show the first page by default
        cardLayout.show(pages, "1");

        // Create the panels to hold the images
        for (int i = 0; i < 12; i++) {
            /* Set a border layout for each panel and make their backgrounds black to make it easier to see the
               drawings */
            panels[i] = new JPanel(new BorderLayout());
            panels[i].setBackground(Color.BLACK);

            // Instantiate each label, set the alignment to the center and add them to the panel
            labels[i] = new JLabel("", JLabel.CENTER);
            panels[i].add(labels[i], BorderLayout.CENTER);

            // Instantiate each delete button initially setting all disabled
            delete[i] = new JButton("Delete");
            delete[i].setEnabled(false);
            // Declare a final variable from the counter for use in the lambda expression
            final int position = i;
            // Remove the corresponding image and refresh the gallery
            delete[i].addActionListener(e -> {
                images.remove(position);
                refresh();
            });

            // Add the delete button to the panel
            panels[i].add(delete[i], BorderLayout.SOUTH);

            // Draw a border around each panel
            panels[i].setBorder(BorderFactory.createLineBorder(Color.WHITE));

            // Depending on the count add the panel to the correct page
            if (i < 4) {
                pageOne.add(panels[i]);
            } else if (i < 8) {
                pageTwo.add(panels[i]);
            } else {
                pageThree.add(panels[i]);
            }
        }
    }

    /**
     * Increment the current page count
     */
    private void nextPage() {
        currentPage++;
    }

    /**
     * Decrement the current page count
     */
    private void prevPage() {
        currentPage--;
    }

    /**
     * Saves the drawing to the images array list
     * @param source is the image containing the drawing to be saved to the gallery
     */
    void saveImage(BufferedImage source) {
        // Copy the image
        BufferedImage image = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics g = image.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();

        // Check if there are already 12 images stored
        if (images.size() < 12) {
            // Save the actual image so it can be rescaled
            images.add(image);
            // Move images to
            refresh();
        } else {
            // Display a warning dialogue to the user
            JOptionPane.showMessageDialog(editor.getWindow(),
                    "You can only save 12 drawings at a time, please delete a drawing from the gallery.",
                    "Cannot Save Drawing",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Adds the actual images to each panel by setting the icon of each label
     */
    private void refresh() {
        for (int i = 0; i < 12; i++) {
            // If there is an image at that position set the icon of the corresponding label to it
            if (i < images.size()) {
                // Subtract 10 from the height and width to make it easier to see the full drawing
                labels[i].setIcon(new ImageIcon(images.get(i).getScaledInstance(labels[i].getHeight() - 10,
                        labels[i].getHeight() - 10, Image.SCALE_SMOOTH)));
                delete[i].setEnabled(true);
            } else {
                // If there is no image in that position set the icon to null and disable the delete button
                labels[i].setIcon(null);
                delete[i].setEnabled(false);
            }
        }
    }
}