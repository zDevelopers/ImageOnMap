package fr.moribus.ImageOnMap;

import java.awt.image.BufferedImage;
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

	protected TacheTraitementMap(String u)
 	{
		renduImg = new DownloadImageThread(u);
 		dlImg = Executors.newSingleThreadExecutor();
 		futurDlImg = dlImg.submit(renduImg);
 		plugin = (ImageOnMap) Bukkit.getPluginManager().getPlugin("ImageOnMap");
 	}
	
 	public TacheTraitementMap(Player j, String u, boolean rs, boolean rn)
 	{
 		this(u);
 		joueur = j;
 		inv = joueur.getInventory();
 		resized = rs;
 		renamed = rn;
 	}
 	
	@Override
	public void run() 
	{
		compteurExec = 0;
		if(!futurDlImg.isDone())
		{
			compteurExec++;
			if(compteurExec > 20)
			{
				joueur.sendMessage("TIMEOUT: the render took too many time");
				futurDlImg.cancel(true);
				cancel();
			}
		}
		else
		{
			if(!futurDlImg.isCancelled())
			{
				cancel();
				int nbImage = 1;
				if (plugin.getConfig().getInt("Limit-map-by-server") != 0 && nbImage + ImgUtility.getNombreDeMaps(plugin) > plugin.getConfig().getInt("Limit-map-by-server"))
				{
					joueur.sendMessage("ERROR: cannot render "+ nbImage +" picture(s): the limit of maps per server would be exceeded.");
					return;
				}
				if(joueur.hasPermission("imageonmap.nolimit"))
				{
					
				}
				else
				{
					if (plugin.getConfig().getInt("Limit-map-by-player") != 0 && nbImage + ImgUtility.getNombreDeMapsParJoueur(plugin, joueur.getName()) > plugin.getConfig().getInt("Limit-map-by-player"))
					{
						joueur.sendMessage(ChatColor.RED +"ERROR: cannot render "+ nbImage +" picture(s): the limit of maps allowed for you (per player) would be exceeded.");
						return;
					}
				}
				
				joueur.sendMessage("Bingo ! Image téléchargée.");
				try
				{
					traiterMap(futurDlImg.get());
				}
				catch (InterruptedException e)
				{
					joueur.sendMessage(ChatColor.RED+ "ERROR: download task has been interrupted. Make sure your URL is valid.");
				}
				catch (ExecutionException e)
				{
					joueur.sendMessage(ChatColor.RED+ "Your image can't be downloaded. Please check your URL");
				}
			}
			else
			{
				joueur.sendMessage(ChatColor.RED+ "An error occured. See the console for details");
			}
				
			
			/*MapView carte;
			
			ArrayList<ItemStack> restant = new ArrayList<ItemStack>();
			short[] ids = new short[nbImage];
			for (int i = 0; i < nbImage; i++)
			{
				if(nbImage == 1 && joueur.getItemInHand().getType() == Material.MAP)
					carte = Bukkit.getMap(joueur.getItemInHand().getDurability());
				else
					carte = Bukkit.createMap(joueur.getWorld());
				MapCreateThread.SupprRendu(carte);
				carte.addRenderer(new Rendu(renduImg.getImg()[i]));
				map = new ItemStack(Material.MAP, 1, carte.getId());
				if(nbImage > 1)
				{
					ids[i] = carte.getId();
					if(renamed == true)
					{
						ItemMeta meta = map.getItemMeta();
						meta.setDisplayName("Map (" +renduImg.getNumeroMap().get(i) +")");
						map.setItemMeta(meta);
					}
					
				}
				
				if(nbImage == 1 && joueur.getItemInHand().getType() == Material.MAP)
					joueur.setItemInHand(map);
				else
					ImgUtility.AddMap(map, inv, restant);
				
				//Svg de la map
				SavedMap svg = new SavedMap(plugin, joueur.getName(), carte.getId(), renduImg.getImg()[i], joueur.getWorld().getName());
				svg.SaveMap();
				joueur.sendMap(carte);
			}
			SavedPoster poster;
			if(nbImage > 1)
			{
				poster = new SavedPoster(plugin, ids, joueur.getName());
				poster.Save();
				joueur.sendMessage("Poster ( Id: "+ poster.getId()+ " ) finished");
			}
			else
				joueur.sendMessage("Render finished");
			if(!restant.isEmpty())
				joueur.sendMessage(restant.size()+ " maps can't be place in your inventory. Please make free space in your inventory and run "+ ChatColor.GOLD+  "/maptool getrest");
			plugin.setRemainingMaps(joueur.getName(), restant);*/
		}
	}
	
	public abstract void traiterMap(BufferedImage img);
	
	protected Player getJoueur() {
		return joueur;
	}

	protected void setJoueur(Player joueur) {
		this.joueur = joueur;
	}

	protected DownloadImageThread getRenduImg() {
		return renduImg;
	}

	protected void setRenduImg(DownloadImageThread renduImg) {
		this.renduImg = renduImg;
	}

	protected PlayerInventory getInv() {
		return inv;
	}

	protected void setInv(PlayerInventory inv) {
		this.inv = inv;
	}

	protected ItemStack getMap() {
		return map;
	}

	protected void setMap(ItemStack map) {
		this.map = map;
	}

	protected ImageOnMap getPlugin() {
		return plugin;
	}

	protected void setPlugin(ImageOnMap plugin) {
		this.plugin = plugin;
	}

	protected boolean isResized() {
		return resized;
	}

	protected void setResized(boolean resized) {
		this.resized = resized;
	}

	protected boolean isRenamed() {
		return renamed;
	}

	protected void setRenamed(boolean renamed) {
		this.renamed = renamed;
	}

	protected int getCompteurExec() {
		return compteurExec;
	}

	protected void setCompteurExec(int compteurExec) {
		this.compteurExec = compteurExec;
	}

}
