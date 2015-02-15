package fr.moribus.imageonmap;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/* Class which represents a picture cut into several parts */
public class Poster
{

    private BufferedImage src;
    private BufferedImage[] ImgDecoupe;
    private HashMap<Integer, String> NumeroMap;
    private int nbPartie;
    private int nbColonne;

    public Poster(BufferedImage img)
    {
        src = img;
        NumeroMap = new HashMap<Integer, String>();
        DecoupeImg();
    }

    public BufferedImage[] getPoster()
    {
        return ImgDecoupe;
    }

    public int getNbColonne()
    {
        return nbColonne;
    }

    private void DecoupeImg()
    {
        int ligne, colonne;
        int x = 0, y = 0;
        int index = 0;
        int resteX = src.getWidth() % 128;
        int resteY = src.getHeight() % 128;
        if (src.getWidth() / 128 <= 0)
        {
            ligne = 1;
        }
        else if (src.getWidth() % 128 != 0)
        {
            ligne = src.getWidth() / 128 + 1;
        }
        else
        {
            ligne = src.getWidth() / 128;
        }

        if (src.getHeight() <= 0)
        {
            colonne = 1;
        }
        else if (src.getHeight() % 128 != 0)
        {
            colonne = src.getHeight() / 128 + 1;
        }
        else
        {
            colonne = src.getHeight() / 128;
        }

        nbColonne = colonne;
        nbPartie = ligne * colonne;
        ImgDecoupe = new BufferedImage[nbPartie];

        for (int lig = 0; lig < ligne; lig++)
        {
            y = 0;
            if (lig == ligne - 1 && resteX != 0)
            {
                for (int col = 0; col < colonne; col++)
                {
                    if (col == colonne - 1 && resteY != 0)
                    {
                        ImgDecoupe[index] = src.getSubimage(x, y, resteX, resteY);

                    }
                    else
                    {
                        ImgDecoupe[index] = src.getSubimage(x, y, resteX, 128);
                        y += 128;
                    }
                    NumeroMap.put(index, "column " + (lig + 1) + ", row " + (col + 1));
                    index++;

                }
            }
            else
            {
                for (int col = 0; col < colonne; col++)
                {
                    if (col == colonne - 1 && resteY != 0)
                    {
                        ImgDecoupe[index] = src.getSubimage(x, y, 128, resteY);
                    }
                    else
                    {
                        ImgDecoupe[index] = src.getSubimage(x, y, 128, 128);
                        y += 128;
                    }
                    NumeroMap.put(index, "column " + (lig + 1) + ", row " + (col + 1));
                    index++;

                }
                x += 128;
            }

        }

    }
}
