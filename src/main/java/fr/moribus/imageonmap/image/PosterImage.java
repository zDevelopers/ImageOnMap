package fr.moribus.imageonmap.image;

import fr.moribus.imageonmap.map.ImageMap;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * This class represents an image split into pieces
 */
public class PosterImage
{
    private BufferedImage[] cutImages;
    private int lines;
    private int columns;
    private int cutImagesCount;
    private int remainderX, remainderY;
    
    /**
     * Creates and splits a new Poster from an entire image
     * @param originalImage the original image
     */
    public PosterImage(BufferedImage originalImage)
    {
        splitImages(originalImage);
    }
    
    private void splitImages(BufferedImage originalImage)
    {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        columns = (int) Math.ceil(originalWidth / ImageMap.WIDTH);
        lines = (int) Math.ceil(originalHeight / ImageMap.HEIGHT);
        
        remainderX = originalWidth % ImageMap.WIDTH;
        remainderY = originalHeight % ImageMap.HEIGHT;
        
        if(remainderX > 0) columns++;
        if(remainderY > 0) lines++;
        
        cutImagesCount = columns * lines;
        cutImages = new BufferedImage[cutImagesCount];
        
        int imageX;
        int imageY = (remainderY - ImageMap.HEIGHT) / 2;
        for(int i = 0; i < lines; i++)
        {
            imageX = (remainderX - ImageMap.WIDTH) / 2;
            for(int j = 0; j < columns; j++)
            {
                cutImages[i * columns + j] = makeSubImage(originalImage, imageX, imageY);
                imageX += ImageMap.WIDTH;
            }
            imageY += ImageMap.HEIGHT;
        }
    }
    
    /**
     * Generates the subimage that intersects with the given map rectangle.
     * @param x X coordinate of top-left point of the map.
     * @param y Y coordinate of top-left point of the map.
     * @return the requested subimage.
     */
    private BufferedImage makeSubImage(BufferedImage originalImage, int x, int y)
    {
        BufferedImage newImage = new BufferedImage(ImageMap.WIDTH, ImageMap.HEIGHT, BufferedImage.TYPE_INT_ARGB);
        
        Graphics graphics = newImage.getGraphics();
        
        graphics.drawImage(originalImage, -x, -y, null);
        graphics.dispose();
        return newImage;
    }
    
    
    
    private int boundValue(int min, int value, int max)
    {
        return Math.max(Math.min(value, max), min);
    }
    
    /**
     * 
     * @return the split images
     */
    public BufferedImage[] getImages()
    {
        return cutImages;
    }
    
    public BufferedImage getImageAt(int i)
    {
        return cutImages[i];
    }
    
    public BufferedImage getImageAt(int x, int y)
    {
        return cutImages[x * columns + y];
    }
    
    /**
     * 
     * @return the number of lines of the poster
     */
    public int getLines()
    {
        return lines;
    }
    
    /**
     * 
     * @return the number of columns of the poster
     */
    public int getColumns()
    {
        return columns;
    }
    
    /**
     * 
     * @return the number of split images
     */
    public int getImagesCount()
    {
        return cutImagesCount;
    }

    public int getRemainderX()
    {
        return remainderX;
    }

    public void setRemainderX(int remainderX)
    {
        this.remainderX = remainderX;
    }

    public int getRemainderY()
    {
        return remainderY;
    }

    public void setRemainderY(int remainderY)
    {
        this.remainderY = remainderY;
    }
}
