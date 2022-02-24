package fr.moribus.imageonmap.economy;

import fr.moribus.imageonmap.PluginConfiguration;
import fr.zcraft.quartzlib.core.QuartzComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyComponent extends QuartzComponent {

    private static Economy economy = null;

    public static void init() {
        System.out.println("HELLO!");
        if (PluginConfiguration.ENABLE_ECONOMY.get() 
                && Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
            }
        }
    }

    public static Economy getEconomy() throws EconomyNotEnabledException {

        if (economy != null) {
            return economy;
        } else {
            throw new EconomyNotEnabledException();
        }

    }

}
