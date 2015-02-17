package fr.moribus.imageonmap.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;


public class DownloadImageThread implements Callable<BufferedImage>
{
    private final URL imageURL;
    private BufferedImage imgSrc;

    public DownloadImageThread(URL imageURL)
    {
        this.imageURL = imageURL;
    }

    @Override
    public BufferedImage call() throws IOException
    {
        imgSrc = ImageIO.read(imageURL);
        
        if(imgSrc == null) throw new IOException("URL does not points to a valid image.");

        return imgSrc;
    }

}
