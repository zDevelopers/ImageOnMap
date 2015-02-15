package fr.moribus.imageonmap;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class TacheTraitementMap extends BukkitRunnable
{
    private Player joueur;
    private DownloadImageThread renduImg;
    private PlayerInventory inv;
    private ItemStack map;
    private ImageOnMap plugin;
    private boolean resized, renamed;
    private ExecutorService dlImg;
    private Future<BufferedImage> futurDlImg;
    private int compteurExec;

    protected TacheTraitementMap(URL url)
    {
        renduImg = new DownloadImageThread(url);
        dlImg = Executors.newSingleThreadExecutor();
        futurDlImg = dlImg.submit(renduImg);
        plugin = (ImageOnMap) Bukkit.getPluginManager().getPlugin("ImageOnMap");
    }

    public TacheTraitementMap(Player j, URL url, boolean rs, boolean rn)
    {
        this(url);
        joueur = j;
        inv = joueur.getInventory();
        resized = rs;
        renamed = rn;
    }

    @Override
    public void run()
    {
        compteurExec = 0;
        if (!futurDlImg.isDone())
        {
            compteurExec++;
            if (compteurExec > 20)
            {
                joueur.sendMessage("TIMEOUT: the render took too many time");
                futurDlImg.cancel(true);
                cancel();
            }
        }
        else
        {
            if (!futurDlImg.isCancelled())
            {
                cancel();
                int nbImage = 1;
                if (plugin.getConfig().getInt("Limit-map-by-server") != 0 && nbImage + ImgUtility.getNombreDeMaps(plugin) > plugin.getConfig().getInt("Limit-map-by-server"))
                {
                    joueur.sendMessage("ERROR: cannot render " + nbImage + " picture(s): the limit of maps per server would be exceeded.");
                    return;
                }
                if (!joueur.hasPermission("imageonmap.nolimit"))
                {
                    if (plugin.getConfig().getInt("Limit-map-by-player") != 0 && nbImage + ImgUtility.getNombreDeMapsParJoueur(plugin, joueur.getName()) > plugin.getConfig().getInt("Limit-map-by-player"))
                    {
                        joueur.sendMessage(ChatColor.RED + "ERROR: cannot render " + nbImage + " picture(s): the limit of maps allowed for you (per player) would be exceeded.");
                        return;
                    }
                }

                try
                {
                    BufferedImage dlimg = futurDlImg.get();
                    traiterMap(dlimg);
                    joueur.sendMessage("Image successfuly downloaded !");
                }
                catch (InterruptedException ex)
                {
                    joueur.sendMessage(ChatColor.RED + "Download task has been interrupted unexpectedly. Check server console for details.");
                    PluginLogger.LogError("Download task has been interrupted", ex);
                }
                catch (ExecutionException ex)
                {
                    joueur.sendMessage(ChatColor.RED + "Download failed : " + ex.getMessage());
                    joueur.sendMessage(ChatColor.RED + "Please check your URL");
                }
                catch(IOException ex)
                {
                    joueur.sendMessage(ChatColor.RED + "Failed to process the image. Check server console for details.");
                    PluginLogger.LogError("Image processing failed", ex);
                }
                
            }
            else
            {
                joueur.sendMessage(ChatColor.RED + "An error occured. See the console for details");
            }
        }
    }

    public abstract void traiterMap(BufferedImage img) throws IOException;

    protected Player getJoueur()
    {
        return joueur;
    }

    protected void setJoueur(Player joueur)
    {
        this.joueur = joueur;
    }

    protected DownloadImageThread getRenduImg()
    {
        return renduImg;
    }

    protected void setRenduImg(DownloadImageThread renduImg)
    {
        this.renduImg = renduImg;
    }

    protected PlayerInventory getInv()
    {
        return inv;
    }

    protected void setInv(PlayerInventory inv)
    {
        this.inv = inv;
    }

    protected ItemStack getMap()
    {
        return map;
    }

    protected void setMap(ItemStack map)
    {
        this.map = map;
    }

    protected ImageOnMap getPlugin()
    {
        return plugin;
    }

    protected void setPlugin(ImageOnMap plugin)
    {
        this.plugin = plugin;
    }

    protected boolean isResized()
    {
        return resized;
    }

    protected void setResized(boolean resized)
    {
        this.resized = resized;
    }

    protected boolean isRenamed()
    {
        return renamed;
    }

    protected void setRenamed(boolean renamed)
    {
        this.renamed = renamed;
    }

    protected int getCompteurExec()
    {
        return compteurExec;
    }

    protected void setCompteurExec(int compteurExec)
    {
        this.compteurExec = compteurExec;
    }

}
