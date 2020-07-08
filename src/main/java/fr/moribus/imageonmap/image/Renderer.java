/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2020)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2020)
 *
 * This software is a computer program whose purpose is to allow insertion of
 * custom images in a Minecraft world.
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package fr.moribus.imageonmap.image;

import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Renderer extends MapRenderer {
    private BufferedImage image;

    protected Renderer() {
        this(null);
    }

    protected Renderer(BufferedImage image) {
        this.image = image;
    }

    static public boolean isHandled(MapView map) {
        if (map == null) return false;
        for (MapRenderer renderer : map.getRenderers()) {
            if (renderer instanceof Renderer) return true;
        }
        return false;
    }

    static public void installRenderer(PosterImage image, int[] mapsIds) {
        for (int i = 0; i < mapsIds.length; i++) {
            installRenderer(image.getImageAt(i), mapsIds[i]);
        }
    }

    static public void installRenderer(BufferedImage image, int mapID) {
        MapView map = Bukkit.getMap(mapID);
        if (map == null) {
            PluginLogger.warning("Could not install renderer for map {0}: the Minecraft map does not exist", mapID);
        } else {
            installRenderer(map).setImage(image);
        }
    }

    static public Renderer installRenderer(MapView map) {
        Renderer renderer = new Renderer();
        removeRenderers(map);
        map.addRenderer(renderer);
        return renderer;
    }

    static public void removeRenderers(MapView map) {
        for (MapRenderer renderer : map.getRenderers()) {
            map.removeRenderer(renderer);
        }
    }




    private static double getDistance(Color c1, Color c2) {
        double rmean = (c1.getRed() + c2.getRed()) / 2.0;
        double r = c1.getRed() - c2.getRed();
        double g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        double weightR = 2 + rmean / 256.0;
        double weightG = 4.0;
        double weightB = 2 + (255 - rmean) / 256.0;
        return weightR * r * r + weightG * g * g + weightB * b * b;
    }

    private byte closestColor(int rgb) {
        int alpha = (rgb >>> 24) & 0xFF;
        int r = (rgb >>> 16) & 0xFF;
        int g = (rgb >>> 8) & 0xFF;
        int b = rgb & 0xFF;
        byte col = MapPalette.matchColor(new Color(r, g, b, alpha));

        return col;
    }

    protected class RGB{
        private int alpha,r,g,b;
        public RGB(int r, int g, int b,int alpha){

            this.r=r;
            this.g=g;
            this.b=b;
            this.alpha=alpha;
        }
        public RGB(int rgb){
            int alpha = (rgb >>> 24) & 0xFF;
            int r = (rgb >>> 16) & 0xFF;
            int g = (rgb >>> 8) & 0xFF;
            int b = rgb & 0xFF;

            this.r=r;
            this.g=g;
            this.b=b;
            this.alpha=alpha;

        }

        public RGB(int r, int g, int b){

            this.r=r;
            this.g=g;
            this.b=b;
            this.alpha=255;
        }
        public void applyFactor(double factor){
            r*=factor;
            g*=factor;
            b*=factor;
        }
    }
    private void dithering(MapCanvas canvas) {

        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                int rgb = image.getRGB(x, y);
                Color old_pix = new Color(rgb);
                Color new_pix = MapPalette.getColor(MapPalette.matchColor(old_pix));//Couleur la plus proche

                int alpha = (rgb >>> 24) & 0xFF;
                if (alpha < 128) {
                    canvas.setPixel(x, y, (byte) 0);
                    continue;
                }
                canvas.setPixel(x, y, MapPalette.matchColor(new_pix));//On applique la nouvelle couleur

                int diff_red=old_pix.getRed()-new_pix.getRed();
                int diff_green=old_pix.getGreen()-new_pix.getGreen();
                int diff_blue=old_pix.getBlue()-new_pix.getBlue();


                //Distribution de l'erreur
                int X = 0, Y = 0, r, g, b;
                int value = 0;//120+4+4+7*4+24+1+16;//208+10*4+4;
                if (x + 1 >= 128 | y + 1 >= 128 | x - 1 < 0) {
                    //PluginLogger.info("On va trop loin");
                    canvas.setPixel(x, y, (byte) 20);//MapPalette.matchColor(new_pix));
                    continue;
                }
                double factor;
                try {
                    X = x + 1;
                    Y = y;
                    factor=7.0/16;

                    rgb=(MapPalette.getColor(canvas.getPixel(X, Y) < 0 ? (byte) (canvas.getPixel(x, y) + value) : canvas.getPixel(x, y)).getRGB());
                    //rgb = (int) ((MapPalette.getColor(canvas.getPixel(X, Y) < 0 ? (byte) (canvas.getPixel(x, y) + value) : canvas.getPixel(x, y)).getRGB()) + (error * 7.0 / 16));
                    alpha = (rgb >>> 24) & 0xFF;
                    r = (rgb >>> 16) & 0xFF;
                    g = (rgb >>> 8) & 0xFF;
                    b = rgb & 0xFF;
                    r+=diff_red*factor;
                    g+=diff_green*factor;
                    b+=diff_blue*factor;

                    if (alpha < 128)
                        canvas.setPixel(x, y, (byte) 0);
                    else
                        canvas.setPixel(x, y, MapPalette.matchColor(r, g, b));//Pb possible avec alpha

                    X = x - 1;
                    Y = y + 1;
                    factor=3.0/16;
                    rgb=(MapPalette.getColor(canvas.getPixel(X, Y) < 0 ? (byte) (canvas.getPixel(x, y) + value) : canvas.getPixel(x, y)).getRGB());
                    //rgb = (int) ((MapPalette.getColor(canvas.getPixel(X, Y) < 0 ? (byte) (canvas.getPixel(x, y) + value) : canvas.getPixel(x, y)).getRGB()) + (error * 3.0 / 16));
                    alpha = (rgb >>> 24) & 0xFF;
                    r = (rgb >>> 16) & 0xFF;
                    g = (rgb >>> 8) & 0xFF;
                    b = rgb & 0xFF;
                    r+=diff_red*factor;
                    g+=diff_green*factor;
                    b+=diff_blue*factor;
                    if (alpha < 128)
                        canvas.setPixel(x, y, (byte) 0);
                    else
                        canvas.setPixel(x, y, MapPalette.matchColor(r, g, b));//Pb possible avec alpha

                    X = x;
                    Y = y + 1;
                    factor=5.0/16;
                    rgb=(MapPalette.getColor(canvas.getPixel(X, Y) < 0 ? (byte) (canvas.getPixel(x, y) + value) : canvas.getPixel(x, y)).getRGB());
                    //rgb = (int) ((MapPalette.getColor(canvas.getPixel(X, Y) < 0 ? (byte) (canvas.getPixel(x, y) + value) : canvas.getPixel(x, y)).getRGB()) + (error * 5.0 / 16));
                    alpha = (rgb >>> 24) & 0xFF;
                    r = (rgb >>> 16) & 0xFF;
                    g = (rgb >>> 8) & 0xFF;
                    b = rgb & 0xFF;
                    r+=diff_red*factor;
                    g+=diff_green*factor;
                    b+=diff_blue*factor;
                    if (alpha < 128)
                        canvas.setPixel(x, y, (byte) 0);
                    else
                        canvas.setPixel(x, y, MapPalette.matchColor(r, g, b));//Pb possible avec alpha

                    X = x + 1;
                    Y = y + 1;
                    factor=1.0/16;
                    rgb=(MapPalette.getColor(canvas.getPixel(X, Y) < 0 ? (byte) (canvas.getPixel(x, y) + value) : canvas.getPixel(x, y)).getRGB());
                    //rgb = (int) ((MapPalette.getColor(canvas.getPixel(X, Y) < 0 ? (byte) (canvas.getPixel(x, y) + value) : canvas.getPixel(x, y)).getRGB()) + (error * 1.0 / 16));
                    alpha = (rgb >>> 24) & 0xFF;
                    r = (rgb >>> 16) & 0xFF;
                    g = (rgb >>> 8) & 0xFF;
                    b = rgb & 0xFF;
                    int rold = r;
                    int gold = g;
                    int bold = b;
                    r+=diff_red*factor;
                    g+=diff_green*factor;
                    b+=diff_blue*factor;
                    if (alpha < 128)
                        canvas.setPixel(x, y, (byte) 0);
                    else
                        canvas.setPixel(x, y, MapPalette.matchColor(r, g, b));//Pb possible avec alpha
                   // PluginLogger.info("r "+r+" g "+g+" b "+b);
                    //PluginLogger.info("rold "+rold+" gold "+gold+" bold "+bold);
                } catch (final Exception e) {
                    PluginLogger.info("Exception get color ");
                    throw e;
                }
                //canvas.setPixel(x-1,y+1,canvas.getPixel(x-1,y+1)+(error*7.0/16));
                //canvas.setPixel(x,y+1,canvas.getPixel(x,y+1)+(error*7.0/16));
                //canvas.setPixel(x+1,y+1,canvas.getPixel(x+1,y+1)+(error*7.0/16));
            }
        }
    }

    @Override
    public void render(MapView v, MapCanvas canvas, Player p) {


        //Render only once to avoid overloading the server
        if (image == null) return;
        boolean dither = true;
        // canvas.drawImage(0, 0, image);
        if (dither) {
            dithering(canvas);
            return;
        }
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                int rgb = image.getRGB(x, y);
                byte col = closestColor(rgb);

                canvas.setPixel(x, y, col);
            }
        }

       /* for(int x=0;x<128;x++){
            for(int y=0;y<128;y++){



                        int rgb=image.getRGB(x,y);
                        int r = (rgb >>> 16) & 0xFF;
                        int g = (rgb >>>  8) & 0xFF;
                        int b = rgb & 0xFF;
                        //PluginLogger.info("canvas pixel "+x+"  "+y+" r "+r+" g "+g+" b "+b);
                        PluginLogger.info(""+canvas.getBasePixel(x,y));
                    canvas.setPixel(x,y,(byte)208);

            }*/


        image = null;

    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
}
