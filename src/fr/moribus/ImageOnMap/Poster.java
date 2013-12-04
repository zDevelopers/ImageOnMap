package fr.moribus.ImageOnMap;

import java.awt.image.BufferedImage;

/* Class which represents a picture cut into several parts */
public class Poster 
{
	BufferedImage src;
	BufferedImage ImgDecoupe[];
	int nbPartie;
	
	Poster(BufferedImage img)
	{
		src = img;
		DecoupeImg();
	}
	
	public BufferedImage[] getPoster()
	{
		return ImgDecoupe;
	}
	
	private void DecoupeImg()
	{
			int ligne, colonne;
			int x = 0, y = 0;
			int index = 0;
			int resteX = src.getWidth() % 128;
			int resteY = src.getHeight() % 128;
			if(src.getWidth() / 128 <= 0)
				ligne = 1;
			else if(src.getWidth() % 128 != 0)
				ligne = src.getWidth() / 128 + 1;
			else
				ligne = src.getWidth() / 128;
			
			if(src.getHeight() <= 0)
				colonne = 1;
			else if(src.getHeight() % 128 != 0)
				colonne = src.getHeight() / 128 + 1;
			else
				colonne = src.getHeight() / 128;
			
			nbPartie = ligne * colonne;
			ImgDecoupe = new BufferedImage[nbPartie];
			
			for(int lig = 0; lig < ligne; lig++)
			{
				y = 0;
				if(lig == ligne - 1 && resteX != 0 )
				{
					for(int col = 0; col < colonne; col++)
					{
						if(col == colonne - 1  && resteY != 0)
						{
							ImgDecoupe[index] = src.getSubimage(x, y, resteX, resteY);
							index++;
						}
						else
						{
							ImgDecoupe[index] = src.getSubimage(x, y, resteX, 128);
							index++;
							y += 128;
						}
						
					}
				}
				else
				{
					for(int col = 0; col < colonne; col++)
					{
						if(col == colonne - 1 && resteY != 0)
						{
							ImgDecoupe[index] = src.getSubimage(x, y, 128, resteY);
							index++;
						}
						else
						{
							ImgDecoupe[index] = src.getSubimage(x, y, 128, 128);
							index++;
							y += 128;
						}
						
					}
					x += 128;
				}
				
			}
			
	}
}
