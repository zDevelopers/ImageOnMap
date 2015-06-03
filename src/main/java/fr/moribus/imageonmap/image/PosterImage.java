/*
 * Copyright (C) 2013 Moribus
 * Copyright (C) 2015 ProkopyL <prokopylmc@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.moribus.imageonmap.image;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * This class represents an image split into pieces
 */
public class PosterImage
{
    static private final int WIDTH = 128;
    static private final int HEIGHT = 128;
    
    private BufferedImage originalImage;
    private BufferedImage[] cutImages;
    private int lines;
    private int columns;
    private int cutImagesCount;
    private int remainderX, remainderY;
    
    /**
     * Creates a new Poster from an entire image
     * @param originalImage the original image
     */
    public PosterImage(BufferedImage originalImage)
    {
        this.originalImage = originalImage;
        calculateDimensions();
    }
    
    private void calculateDimensions()
    {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        columns = (int) Math.ceil(originalWidth / WIDTH);
        lines = (int) Math.ceil(originalHeight / HEIGHT);
        
        remainderX = originalWidth % WIDTH;
        remainderY = originalHeight % HEIGHT;
        
        if(remainderX > 0) columns++;
        if(remainderY > 0) lines++;
        
        cutImagesCount = columns * lines;
    }
    
    public void splitImages()
    {
        cutImages = new BufferedImage[cutImagesCount];
        
        int imageX;
        int imageY = (remainderY - HEIGHT) / 2;
        for(int i = 0; i < lines; i++)
        {
            imageX = (remainderX - WIDTH) / 2;
            for(int j = 0; j < columns; j++)
            {
                cutImages[i * columns + j] = makeSubImage(originalImage, imageX, imageY);
                imageX += WIDTH;
            }
            imageY += HEIGHT;
        }
        
        originalImage = null;
    }
    
    /**
     * Generates the subimage that intersects with the given map rectangle.
     * @param x X coordinate of top-left point of the map.
     * @param y Y coordinate of top-left point of the map.
     * @return the requested subimage.
     */
    private BufferedImage makeSubImage(BufferedImage originalImage, int x, int y)
    {
        BufferedImage newImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        
        Graphics graphics = newImage.getGraphics();
        
        graphics.drawImage(originalImage, -x, -y, null);
        graphics.dispose();
        return newImage;
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
    
    public int getColumnAt(int i)
    {
        return i % columns;
    }
    
    public int getLineAt(int i)
    {
        return i / columns;
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
