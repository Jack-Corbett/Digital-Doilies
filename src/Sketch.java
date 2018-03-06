import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;

/**
 * A brush stroke of multiple lines on the doily, triggered by a mouse drag event.
 * This is used when redrawing the doily.
 */
class Sketch {

    // Brush colour
    private Color colour;

    // Brush width
    private Integer width;

    // FLAGS
    // Reflect toggle
    private Boolean reflect;
    // Erase toggle
    private Boolean erase;

    // Stores all the lines that made up the sketch
    private ArrayList<Line2D> lines = new ArrayList<>();

    /**
     * Constructor to set member variables
     * @param colour save the brush colour
     * @param width save the brush width
     * @param reflect store whether to reflect points when redrawing sketch
     * @param erase store whether to erase other sketches when redrawing
     */
    Sketch(Color colour, Integer width, Boolean reflect, Boolean erase) {
        this.colour = colour;
        this.width = width;
        this.reflect = reflect;
        this.erase = erase;
    }

    /**
     * @param line is added to the sketches array list.
     */
    void addLine(Line2D line) {
        lines.add(line);
    }

    /**
     * @return the array list containing all the lines
     */
    ArrayList<Line2D> getLines() {
        return lines;
    }

    /**
     * @return the brush colour used for the sketch
     */
    Color getColour() { return colour; }

    /**
     * @return the brush width used for the sketch
     */
    Integer getWidth() {
        return width;
    }

    /**
     * @return whether to reflect the sketch in each sector
     */
    Boolean getReflect() {
        return reflect;
    }

    /**
     * @return whether to set the composite to clear to remove parts of other sketches
     */
    Boolean getErase() { return erase; }
}
