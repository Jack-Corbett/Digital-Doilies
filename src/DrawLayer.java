import sun.security.provider.SHA;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.Stack;

/**
 * The transparent drawing layer that the user draws their pattern on to. Each time the user presses the mouse a point
 * is drawn. Each time they drag the mouse a line is drawn - this produces a much smoother image than using
 * points alone. Once the mouse is released this saves a sketch which holds the point and the lines the user has
 * drawn. This makes it easy to undo and redo drawn sketches.
 */
public class DrawLayer extends JPanel {

    // Stores a reference to the editor class which holds the JFrame container
    private Editor editor;

    // Stores the transparent image for the user to draw onto
    private BufferedImage image;

    // Reference the graphics object to ensure all methods can access it
    private Graphics2D g2;

    // Stores the current brush stroke style
    private int brushWidth = 3;
    private Color brushColour = Color.RED;

    // Flag to control if drawn points are reflected within a sector
    private boolean reflect = true;

    // Flag to control whether to draw a line or remove others
    private boolean erase = false;

    // Mouse coordinates
    private int currentX, currentY, oldX, oldY;

    // Hold brush strokes to enable undo/redo operations
    private Stack<Sketch> undoStack = new Stack<>();
    private Stack<Sketch> redoStack = new Stack<>();

    /* Object to store the lines drawn while the mouse is dragged along with: brush colour and width and the state of
       the erase and reflect flags. */
    private Sketch sketch;

    /**
     * Constructor to instantiate the drawing layer and attach listeners to act on users mouse input.
     */
    DrawLayer(Editor editor) {
        // Store a reference to the editor
        this.editor = editor;

        // Handles mouse pressed and released events
        addMouseListener(new MouseAdapter() {
            private boolean drawPoint = true;

            /**
             * Store the current coordinates, start a new sketch and clear the redo stack
             * @param e mouse event
             */
            @Override
            public void mousePressed(MouseEvent e) {
                // Fetch the mouse coordinates
                oldX = e.getX();
                oldY = e.getY();
                // Create a new sketch passing it the current settings
                sketch = new Sketch(brushColour, brushWidth, reflect, erase);
                // Once a new sketch has started clear the redo stack to avoid concurrency issues
                redoStack.clear();

                // If the graphics context isn't empty and this is the first event for the current sketch
                if (g2 != null && drawPoint) {
                    // Create a new point at the current mouse position
                    Ellipse2D point = new Ellipse2D.Double(oldX, oldY, brushWidth-0.9, brushWidth-0.9);
                    // Add the point to the sketch object
                    sketch.setStartPoint(point);
                    // Draw the point (respecting reflection and number of sectors)
                    drawShape(point);
                    // Set the flag so another point will not be drawn until the mouse has been released
                    drawPoint = false;
                }
            }

            /**
             * Push the current sketch to the undo stack as the mouse has been released and set the
             * drawPoint flag to true.
             * @param e mouse event
             */
            @Override
            public void mouseReleased(MouseEvent e) {
                undoStack.push(sketch);
                drawPoint = true;
            }
        });

        // Handles mouse dragged events to draw a line using the new and old coordinates
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // Get the new coordinates for the new mouse position
                currentX = e.getX();
                currentY = e.getY();

                // If the graphics context is not empty draw the line
                if (g2 != null) {
                    // Create a line from the point the user clicked to the new position
                    Line2D line = new Line2D.Double(oldX, oldY, currentX, currentY);

                    // Add the drawn line to the sketch array list
                    sketch.addLine(line);

                    // Draw the lines on the image
                    drawShape(line);

                    /* Update the old coordinates to the new - this allows smooth line drawing by having multiple lines
                    make up a sketch. */
                    oldX = currentX;
                    oldY = currentY;
                }
            }
        });
    }

    /**
     * Invert the reflection flag
     */
    void toggleReflection() { reflect = !reflect; }

    /**
     * Invert the erase flag
     */
    void toggleErase() { erase = !erase; }

    /**
     * @param colour to set the brush colour
     */
    void setBrushColour(Color colour) {
        brushColour = colour;
    }

    /**
     * @return the current brush colour
     */
    Color getBrushColour() {
        return brushColour;
    }

    /**
     * @param width to set the brush width
     */
    void setBrushWidth(Integer width) {
        brushWidth = width;
    }

    /**
     * @return the current brush width
     */
    int getBrushWidth() {
        return brushWidth;
    }

    /**
     * @return the drawn image which is used to save to the gallery
     */
    BufferedImage getImage() { return image; }

    /**
     * Draws a line, rotating it through every sector and reflecting it in each sector depending on the flags.
     * It also sets the brush colour, size and switches between clearing or drawing depending on the erase flag.
     * @param shape The shape object to be drawn.
     */
    private void drawShape(Shape shape) {
        /* Initialise reflectedLine to null to avoid compiler errors if reflect isn't true as it is used
           in separate if statements. */
        Shape reflectedShape = null;

        if (reflect) {
            /* Use a transformation to move the line to the y axis, flip it horizontally and then move it back to it's
            original position. */
            AffineTransform reflectLine = new AffineTransform();
            reflectLine.translate(400, 0);
            reflectLine.scale(-1, 1);
            reflectLine.translate(-400, 0);

            // Apply the transformation to our line and store it
            reflectedShape = reflectLine.createTransformedShape(shape);
        }

        /* Loop through each sector and draw the line(s) by counting up by the angle between each sector.
           Use doubles to avoid rounding errors for odd numbers of sectors. */
        for (double i = 0; i <= 360; i = i + ((double) 360 / editor.getNumberSectors())) {

            // Use a transformation to rotate the line through each sector about the center point
            AffineTransform rotate =
                    AffineTransform.getRotateInstance(
                            Math.toRadians(i), 400, 400);

            // Set the brush width
            g2.setStroke(new BasicStroke(brushWidth));

            /* If the erase flag is true set the composite to clear the drawing
               If not set the brush colour and set the composite to draw. */
            if (erase) {
                g2.setComposite(AlphaComposite.Clear);
            } else {
                g2.setComposite(AlphaComposite.Src);
                g2.setPaint(brushColour);
            }

            // Draw the original line rotated through each sector
            g2.draw(rotate.createTransformedShape(shape));

            // Draw the reflected line mirrored in each sector if reflection is toggled
            if (reflect) {
                g2.draw(rotate.createTransformedShape(reflectedShape));
            }
        }

        // Refresh the draw area
        repaint();
    }

    /**
     * Pushes the top of the undo stack to the redo stack and redraws all previous sketches.
     */
    void undo() {
        redoStack.push(undoStack.pop());
        redraw();
    }

    /**
     * @return true if there are any sketches in the undo stack.
     */
    Boolean canUndo() {
        return undoStack.size() > 0;
    }

    /**
     * Pushes the top of the redo stack to the undo stack and redraws all previous sketches. It is not strictly
     * necessary to redraw all sketches when redoing but this leads to much neater code to reuse the redraw method.
     */
    void redo() {
        undoStack.push(redoStack.pop());
        redraw();
    }

    /**
     * @return true if there are any sketches in the redo stack.
     */
    Boolean canRedo() {
        return redoStack.size() > 0;
    }

    /**
     * When an undo or redo action has been triggered this saves the state of all settings,
     * redraws all sketches in the undo stack and then sets the old brush settings back.
     */
    void redraw() {
        // Save the current brush colour so this can be set back after the redraw
        // We use getRGB as we want to ensure our saved colour holds the value not reference
        Color saveColour = new Color(brushColour.getRGB());
        // Use primitives to ensure the value is stored
        int saveBrushWidth = brushWidth;
        boolean saveReflect = reflect;
        boolean saveErase = erase;

        // Clear the background by filling it with a clear rectangle
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Loop while there are still sketches to be redrawn
        for (Sketch sketch : undoStack) {
            // Set all brush settings for that sketch
            brushColour = sketch.getColour();
            brushWidth = sketch.getWidth();
            reflect = sketch.getReflect();
            erase = sketch.getErase();
            Ellipse2D startPoint = sketch.getStartPoint();

            drawShape(startPoint);

            // Loop through each line in the sketch and draw them on the image
            for (Line2D line : sketch.getLines()) {
                drawShape(line);
            }
        }

        // Set all the brush settings back to their original values
        brushColour = saveColour;
        brushWidth = saveBrushWidth;
        reflect = saveReflect;
        erase = saveErase;

        // Refresh the image
        repaint();
    }

    /**
     * Fill the layer in with alpha chanel (clear) and empty all stacks
     */
    void clear() {
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, getWidth(), getHeight());
        undoStack.clear();
        redoStack.clear();
        repaint();
    }

    /**
     * If an image hasn't been created, make a new one and get it's graphics object.
     * Refresh the image.
     * @param g graphics object
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Create the image
        if (image == null) {
            image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

            g2 = (Graphics2D) image.getGraphics();

            // Use antialiasing on the drawn image to smooth it
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        g.drawImage(image, 0, 0, null);
    }
}