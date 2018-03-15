import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

/**
 * Represents the background layer which holds the black background and sector lines. This is redrawn when the user
 * toggles sector lines or changes the number of sector lines.
 */
public class BackgroundLayer extends JPanel {

    // Store a reference to the editor object to allow the background layer to get the number of sectors from it
    private Editor editor;

    // The graphics object used to draw the background and sector lines
    private Graphics2D g2;

    // The image canvas on which to draw the background
    private BufferedImage image;

    // Flag to control whether the lines separating the sectors are drawn
    private Boolean showSectorLines = true;

    /**
     * Constructor for the background layer.
     * @param editor to store a reference to so it's methods can be called.
     */
    BackgroundLayer(Editor editor) {
        this.editor = editor;
    }

    /**
     * Draws the background and sector lines onto the image.
     */
    void drawBackground() {
        // Fills the image with a black rectangle
        g2.setPaint(Color.black);
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (showSectorLines) {
            g2.setPaint(Color.white);

            // Create a line from the center to the top of the window
            Line2D sectorLine = new Line2D.Double(400, 400, 400, 0);

            // Loop from 0 to 360, using the number of sectors to calculate the step. Drawing copies of the line.
            for (double i = 0; i <= 360; i = i + ((double) 360 / editor.getNumberSectors())) {
                // Define a rotation of the calculated number of degrees about the origin of the line (center)
                AffineTransform rotate =
                        AffineTransform.getRotateInstance(
                                Math.toRadians(i), sectorLine.getX1(), sectorLine.getY1());

                // Draw the rotated line
                g2.draw(rotate.createTransformedShape(sectorLine));
            }
        }

        // Once the background and sector lines have been drawn call repaint on the panel to refresh it
        repaint();
    }

    /**
     * Invert the showSectorLines boolean flag.
     */
    void toggleShowSectorLines() {
        showSectorLines = !showSectorLines;
    }

    /**
     * Create a new image to draw onto or refreshes the displayed image.
     * @param g graphics object.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // If an image hasn't yet been created
        if (image == null) {
            // Create a new image to fill the panel
            image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

            // Store a reference to it's graphics object
            g2 = (Graphics2D) image.getGraphics();

            // Use antialiasing on the sector lines to smooth them (especially important for diagonal sector lines)
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawBackground();
        }

        // If an image has already been created simply redraw the image
        g.drawImage(image, 0, 0, null);
    }
}
