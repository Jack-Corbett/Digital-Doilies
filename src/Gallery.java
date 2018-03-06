import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 */
class Gallery extends JPanel {

    private Editor editor;

    private CardLayout cardLayout;

    private JLabel[] labels = new JLabel[12];
    private ArrayList<BufferedImage> images = new ArrayList<>();
    private JButton[] delete = new JButton[12];

    private int currentPage = 1;

    Gallery(Editor editor) {
        this.editor = editor;
        JPanel[] panels = new JPanel[12];

        // Instantiate each page setting 4 images per page using a grid layout
        JPanel pageOne = new JPanel(new GridLayout(2, 2));
        JPanel pageTwo = new JPanel(new GridLayout(2, 2));
        JPanel pageThree = new JPanel(new GridLayout(2, 2));

        // Set the gallery layout so we can add the image panels along with the controls
        this.setLayout(new BorderLayout());

        // Use a card layout to switch between pages
        cardLayout = new CardLayout();
        JPanel pages = new JPanel(cardLayout);

        this.add(pages, BorderLayout.CENTER);

        // Add the three pages to the card layout
        pages.add(pageOne, "1");
        pages.add(pageTwo, "2");
        pages.add(pageThree, "3");

        // The bottom control panel holding the next page and prev page buttons
        JPanel controls = new JPanel();

        JButton previousPage = new JButton("Prev Page");
        JButton nextPage = new JButton("Next Page");

        previousPage.setEnabled(false);

        previousPage.addActionListener(e -> {
            prevPage();
            nextPage.setEnabled(true);
            if (currentPage == 1) {
                previousPage.setEnabled(false);
            } else {
                previousPage.setEnabled(true);
            }
            cardLayout.previous(pages);
        });

        nextPage.addActionListener(e -> {
            nextPage();
            previousPage.setEnabled(true);
            if (currentPage == 3) {
                nextPage.setEnabled(false);
            } else {
                nextPage.setEnabled(true);
            }
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
            panels[i] = new JPanel(new BorderLayout());
            panels[i].setBackground(Color.BLACK);

            labels[i] = new JLabel("", JLabel.CENTER);

            panels[i].add(labels[i], BorderLayout.CENTER);

            delete[i] = new JButton("Delete");
            delete[i].setEnabled(false);
            final int position = i;
            delete[i].addActionListener(e -> {
                images.remove(position);
                refresh();
            });
            panels[i].add(delete[i], BorderLayout.SOUTH);

            panels[i].setBorder(BorderFactory.createLineBorder(Color.WHITE));

            if (i < 4) {
                pageOne.add(panels[i]);
            } else if (i < 8) {
                pageTwo.add(panels[i]);
            } else {
                pageThree.add(panels[i]);
            }
        }
    }

    private void nextPage() {
        currentPage++;
    }

    private void prevPage() {
        currentPage--;
    }

    void saveImage(BufferedImage source) {
        // Copy the image
        BufferedImage image = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics g = image.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();

        // Check less than 12 as if it is 12 already we don't have any more spaces to store it
        if (images.size() < 12) {
            // Save the actual image so it can be rescaled
            images.add(image);

            refresh();
        } else {
            JOptionPane.showMessageDialog(editor.getWindow(),
                    "You can only save 12 drawings at a time, please delete a drawing from the gallery.",
                    "Cannot Save Drawing",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // Adds the actual images to each slot
    private void refresh() {
        for (int i = 0; i < 12; i++) {
            if (i < images.size()) {
                labels[i].setIcon(new ImageIcon(images.get(i).getScaledInstance(labels[i].getHeight() - 10,
                        labels[i].getHeight() - 10, Image.SCALE_SMOOTH)));
                delete[i].setEnabled(true);
            } else {
                labels[i].setIcon(null);
                delete[i].setEnabled(false);
            }
        }
    }
}