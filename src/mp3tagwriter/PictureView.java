package mp3tagwriter;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * This class is used to display the album art work of an mp3 song file.
 *
 * @author Aditya Nivarthi
 */
public class PictureView extends JComponent {

    private BufferedImage image;

    /**
     * Constructor for PictureView object. Sets the image file and the width and
     * height
     *
     * @param i The image to display as artwork
     */
    public PictureView(BufferedImage i) {
        this.image = i;
        this.setSize(i.getWidth(), i.getHeight());
    }

    /**
     * Paints the album artwork image given a Graphics object
     *
     * @param g The Graphics object to use for displaying the artwork
     */
    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }
}
