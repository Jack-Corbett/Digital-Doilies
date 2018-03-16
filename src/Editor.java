import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;

/**
 * The main class for holding the menus, the drawing area for the doily and the gallery.
 */
class Editor {
    // The window that holds the GUI for the application
    private JFrame window;

    // The front layer on which the user draws their design
    private DrawLayer drawLayer;

    // The background layer which holds the black backdrop and sector lines
    private BackgroundLayer backgroundLayer;

    // The gallery pane where the saved images are displayed
    private Gallery gallery;

    // Stores the number of sectors the draw area is split into - 12 is the default
    private Integer numberSectors = 12;

    // The editors layout which holds the canvas layered pane (combination of the draw and background layers)
    // and the gallery panel
    private CardLayout cardLayout;
    private JPanel cards;

    // Store the menu bars so their settings are maintained
    private JMenuBar canvasMenuBar;
    private JMenuBar galleryMenuBar;

    /**
     * Constructor for the main editor which sets up it's properties, adds the drawing and background panels as well
     * as the gallery.
     */
    Editor() {
        // Instantiate the JFrame to hold the GUI and ensure the program terminates when the window is closed
        window = new JFrame("Digital Doilies");
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // A layered panel to hold the draw layer and background layer
        JLayeredPane canvas = new JLayeredPane();

        drawLayer = new DrawLayer(this);
        backgroundLayer = new BackgroundLayer(this);

        gallery = new Gallery(this);

        // Make the draw layer transparent so the background can be seen through it
        drawLayer.setOpaque(false);

        // Set the bounds of each layer to make sure they are displayed
        drawLayer.setBounds(0, 0, 800, 800);
        backgroundLayer.setBounds(0, 0, 800, 800);

        // Add the draw layer to the canvas to be displayed in front of the background
        canvas.add(drawLayer, JLayeredPane.PALETTE_LAYER);
        canvas.add(backgroundLayer, JLayeredPane.DEFAULT_LAYER);

        // Create a panel using the card layout to hold the canvas and gallery, making it easy to switch between them.
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        cards.add(canvas, "Canvas");
        cards.add(gallery, "Gallery");

        // Show the canvas by default for the user to draw
        cardLayout.show(cards, "Canvas");
        window.setContentPane(cards);

        // Set the menu bar by calling the canvasMenu method
        canvasMenuBar = canvasMenu();
        galleryMenuBar = galleryMenu();
        window.setJMenuBar(canvasMenuBar);

        window.setSize(800, 844);
        window.setResizable(false);
        window.setVisible(true);
    }

    /**
     * @return the number of sectors the drawing should be repeated in, this is used to calculate the degree of
     * rotation in the draw layer and the angles between the sector lines in the background layer
     */
    Integer getNumberSectors() {
        return numberSectors;
    }

    /**
     * @return the JFrame holding the UI to use as a parent for dialogue boxes
     */
    JFrame getWindow() {
        return window;
    }

    /**
     * Creates the menu bar displayed at the top of the application editor holding the tools and options, as well as
     * holding listeners to trigger actions.
     *
     * @return a JMenuBar holding all the tools for changing canvas settings.
     */
    private JMenuBar canvasMenu() {
        JMenuBar menuBar = new JMenuBar();

        // FILE Menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        // Pass the current draw layer image to the gallery to be copied and stored
        JMenuItem saveToGallery = new JMenuItem("Save to Gallery");
        saveToGallery.addActionListener(e -> gallery.saveImage(drawLayer.getImage()));

        // Display the gallery panel by switching cards and set the gallery menu bar
        JMenuItem viewGallery = new JMenuItem("View Gallery");
        viewGallery.addActionListener(e -> {
            cardLayout.show(cards, "Gallery");
            window.setJMenuBar(galleryMenuBar);
        });

        fileMenu.add(saveToGallery);
        fileMenu.add(viewGallery);

        // EDIT Menu
        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);

        // Call the undo method on the draw layer to remove the last sketch
        JMenuItem undo = new JMenuItem("Undo");
        undo.addActionListener(e -> drawLayer.undo());

        // Call the redo method on the draw layer to redo the previously undone sketch
        JMenuItem redo = new JMenuItem("Redo");
        redo.addActionListener(e -> drawLayer.redo());

        /* Every time the edit menu is clicked use the canUndo and canRedo methods to set the enabled
           status of the undo and redo buttons. */
        editMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                if (drawLayer.canUndo()) {
                    undo.setEnabled(true);
                } else {
                    undo.setEnabled(false);
                }

                if (drawLayer.canRedo()) {
                    redo.setEnabled(true);
                } else {
                    redo.setEnabled(false);
                }
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });

        // Call the clear method on the draw layer to remove all drawn lines
        JMenuItem clear = new JMenuItem("Clear Drawing");
        clear.addActionListener(e -> drawLayer.clear());

        editMenu.add(undo);
        editMenu.add(redo);
        editMenu.addSeparator();
        editMenu.add(clear);

        // BRUSH Menu
        JMenu brushMenu = new JMenu("Brush");
        menuBar.add(brushMenu);

        /* Colour menu that displays Java's colour chooser in a dialogue window.
           If the colour is changed use the setBrushColour method on the draw layer. */
        JMenuItem colourMenu = new JMenuItem("Colour");
        colourMenu.addActionListener(e -> {
            Color newColour =
                    JColorChooser.showDialog(drawLayer, "Choose Brush Color", drawLayer.getBrushColour());
            if (newColour != null) {
                drawLayer.setBrushColour(newColour);
            }
        });

        brushMenu.add(colourMenu);


        /* Size menu that displays a dialogue window with a spinner for the user to cycle through brush sizes.
           If OK is selected use the setBrushWidth method on the draw layer to change it. */
        JMenuItem sizeMenu = new JMenuItem("Size");
        sizeMenu.addActionListener(e -> {
            JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(drawLayer.getBrushWidth(),
                    1, 15, 1));

            int option = JOptionPane.showOptionDialog(drawLayer, sizeSpinner, "Choose Brush Width",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

            if (option == JOptionPane.OK_OPTION) {
                drawLayer.setBrushWidth((Integer) sizeSpinner.getValue());
            }
        });

        brushMenu.add(sizeMenu);
        brushMenu.addSeparator();

        // Toggle erasing by calling the toggleErase method when ticked
        JCheckBoxMenuItem eraser = new JCheckBoxMenuItem("Eraser");
        eraser.addItemListener(e -> drawLayer.toggleErase());
        brushMenu.add(eraser);

        // CANVAS Menu
        JMenu canvasMenu = new JMenu("Canvas");
        menuBar.add(canvasMenu);

        // Spinner to change the number of sectors for the doily. The default is 12 with a max of 36 and minimum of 2.
        JLabel sectorLabel = new JLabel("Number of Sectors:");
        JSpinner sectors = new JSpinner(new SpinnerNumberModel(12, 2, 40, 1));
        // When the value is changed redraw the background and draw layer.
        sectors.addChangeListener(e -> {
            numberSectors = ((Integer) sectors.getValue());
            backgroundLayer.drawBackground();
            drawLayer.redraw();
        });

        canvasMenu.add(sectorLabel);
        canvasMenu.add(sectors);

        canvasMenu.addSeparator();

        // When toggled set the flag in the background layer using toggleShowSectorLines and then redraw the background
        JCheckBoxMenuItem sectorLines = new JCheckBoxMenuItem("Show Sector Lines");
        // Selected by default
        sectorLines.setSelected(true);
        sectorLines.addItemListener(e -> {
            backgroundLayer.toggleShowSectorLines();
            backgroundLayer.drawBackground();
        });

        // When toggled call toggleReflection to set the reflection state in the draw layer
        JCheckBoxMenuItem reflection = new JCheckBoxMenuItem("Toggle Reflection");
        // Selected by default
        reflection.setState(true);
        reflection.addItemListener(e -> drawLayer.toggleReflection());

        canvasMenu.add(sectorLines);
        canvasMenu.add(reflection);

        return menuBar;
    }

    /**
     * Creates the menu bar for the gallery pane.
     *
     * @return a JMenuBar holding a single button to return to the canvas
     */
    private JMenuBar galleryMenu() {
        JMenuBar menuBar = new JMenuBar();

        /* Set the windows menu bar back to the canvas menu bar to show the full list of options and show
           the canvas panel from the card layout. */
        JButton back = new JButton("Return to Canvas");
        back.addActionListener(e -> {
            window.setJMenuBar(canvasMenuBar);
            cardLayout.show(cards, "Canvas");
        });

        menuBar.add(back);

        return menuBar;
    }
}