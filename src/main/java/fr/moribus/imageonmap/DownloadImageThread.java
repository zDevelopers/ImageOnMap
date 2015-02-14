package fr.moribus.imageonmap;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;


public class DownloadImageThread implements Callable<BufferedImage>
{
    private final String stringUrl;
    private BufferedImage imgSrc;

    DownloadImageThread(String u)
    {
        stringUrl = u;
    }

    @Override
    public BufferedImage call() throws IOException
    {
        URL url = new URL(stringUrl);

        imgSrc = ImageIO.read(url);
        
        if(imgSrc == null) throw new IOException("URL does not points to a valid image.");

        return imgSrc;
    }

}
