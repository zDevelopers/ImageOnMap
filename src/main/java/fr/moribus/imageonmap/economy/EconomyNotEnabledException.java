package fr.moribus.imageonmap.economy;

public class EconomyNotEnabledException extends Exception {
    
    public EconomyNotEnabledException() {
        super("The economy is disabled");
    }
    
}
