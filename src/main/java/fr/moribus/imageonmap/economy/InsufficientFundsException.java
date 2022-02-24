package fr.moribus.imageonmap.economy;

import org.bukkit.OfflinePlayer;

public class InsufficientFundsException extends Exception {

    private final OfflinePlayer player;
    private final double cost;

    public InsufficientFundsException(OfflinePlayer player, double cost) {
        super("Insufficient funds");
        this.player = player;
        this.cost = cost;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public double getCost() {
        return cost;
    } 

}
