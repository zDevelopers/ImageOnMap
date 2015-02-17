package fr.moribus.imageonmap.image;

import java.awt.image.BufferedImage;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class Renderer extends MapRenderer
{
    private boolean isRendered;
    private final BufferedImage image;
    public Renderer(BufferedImage image)
    {
        isRendered = false;
        this.image = image;
    }

    @Override
    public void render(MapView v, final MapCanvas canvas, Player p)
    {
        //Render only once to avoid overloading the server
        if (!isRendered)
        {
            canvas.drawImage(0, 0, image);
            isRendered = true;
        }
    }
}
