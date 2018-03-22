package fr.moribus.imageonmap.image;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Various image-related utilities
 */
public class ImageUtils {

    public enum ScalingType {
        NONE,
        CONTAINED,
        COVERED,
        STRETCHED,
        ;

        public BufferedImage resize(BufferedImage source, int destinationW, int destinationH) {
            switch(this) {
                case CONTAINED: return ImageUtils.resize(source, destinationW, destinationH, false);
                case COVERED: return ImageUtils.resize(source, destinationW, destinationH, true);
                case STRETCHED: return resizeStretched(source, destinationW, destinationH);
                default: return source;
            }
        }
    }

    /**
     * Generates a resized buffer of the given source
     * @param source
     * @param destinationW
     * @param destinationH
     * @return
     */
    static private BufferedImage resize(BufferedImage source, int destinationW, int destinationH, boolean covered)
    {
        float ratioW = (float)destinationW / (float)source.getWidth();
        float ratioH = (float)destinationH / (float)source.getHeight();
        int finalW, finalH;
        int x, y;

        if(covered ? ratioW > ratioH : ratioW < ratioH)
        {
            finalW = destinationW;
            finalH = (int)(source.getHeight() * ratioW);
        }
        else
        {
            finalW = (int)(source.getWidth() * ratioH);
            finalH = destinationH;
        }

        x = (destinationW - finalW) / 2;
        y = (destinationH - finalH) / 2;

        return drawImage(source,
                destinationW, destinationH,
                x, y, finalW, finalH);
    }

    /**
     *
     * @param source
     * @param destinationW
     * @param destinationH
     * @return
     */
    static private BufferedImage resizeStretched(BufferedImage source, int destinationW, int destinationH) {
        return drawImage(source,
                destinationW, destinationH,
                0, 0, destinationW, destinationH);
    }

    /**
     * Draws the source image on a new buffer, and returns it.
     * The source buffer can be drawn at any size and position in the new buffer.
     * @param source The source buffer to draw
     * @param bufferW The width of the new buffer
     * @param bufferH The height of the new buffer
     * @param posX The X position of the source buffer
     * @param posY The Y position of the source buffer
     * @param sourceW The width of the source buffer
     * @param sourceH The height of the source buffer
     * @return The new buffer, with the source buffer drawn on it
     */
    static private BufferedImage drawImage(BufferedImage source,
                                          int bufferW, int bufferH,
                                          int posX, int posY,
                                          int sourceW, int sourceH) {
        BufferedImage newImage = new BufferedImage(bufferW, bufferH, BufferedImage.TYPE_INT_ARGB);

        Graphics graphics = newImage.getGraphics();
        graphics.drawImage(source, posX, posY, sourceW, sourceH, null);
        graphics.dispose();
        return newImage;
    }
}
