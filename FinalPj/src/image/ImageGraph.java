package image;

import colorConvertor.Pixel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import static java.lang.Math.min;

/**
 * A class display the image graph and including all the effect to draw the image and histogram
 * @YIXU YE
 */
//https://stackoverflow.com/questions/3325546/how-to-color-a-pixel
public class ImageGraph extends JPanel {
    // Read the bmp file by byte
    private BufferedImage canvas;
    private Color color = null;
    private int[] grayScale = null;
    private int height = 0;
    private int width = 0;
    private int[][]R;
    private int[][]G;
    private int[][]B;
    private int r;
    private int g;
    private int b;
    private final static int LOWERBOUND = 25;
    private final static int HIGHERBOUND = 235;
    private final static int MAX = 255;
    private final static int MIN = 0;
    // Constructor
    public ImageGraph(int[][]R, int[][]G, int[][]B, int height, int width) {
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        grayScale = new int[height*width];
        this.height = height;
        this.width  = width;
        this.R = R;
        this.G = G;
        this.B = B;
        originalDraw();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(canvas, 0, 0, null);
        g.dispose();
    }
    /**
     * Draw original image
     */
    public Pixel originalDraw(){
        int index = 0;
        for (int i = 0; i < canvas.getHeight(); i++) {
            for (int j = 0; j < canvas.getWidth(); j++) {
                color = new Color(R[i][j], G[i][j], B[i][j]);
                canvas.setRGB(j, i, color.getRGB());
                grayScale[index++] = (color.getRed()+color.getBlue()+color.getGreen())/3;
            }
        }
        repaint();
        return new Pixel(R,G,B, height, width);
    }
    /**
     * Draw grayScale image for original image
     */
    // https://www.johndcook.com/blog/2009/08/24/algorithms-convert-color-grayscale/
    public void grayScaleDraw(){
        int index = 0;
//        int[][] newR = new int[height][width];
//        int[][] newG = new int[height][width];
//        int[][] newB = new int[height][width];
        for (int i = 0; i < canvas.getHeight(); i++) {
            for (int j = 0; j < canvas.getWidth(); j++) {
                r = (int)(R[i][j] * 0.21);
                g = (int)(G[i][j] * 0.72);
                b = (int)(B[i][j] * 0.07);
//                System.out.println(r+g+b);
                color = new Color(r+b+g, r+b+g, r+b+g);
//                newR[i][j] =  r + g+ b;
//                newG[i][j] =  r + g+ b;
//                newB[i][j] =  r + g+ b;
                canvas.setRGB(j, i, color.getRGB());
                grayScale[index++] = (r+g+b)/3;
//                System.out.println(r+g+b + " " +newR[i][j]);
            }
        }
        repaint();
//        return new Pixel(newR,newG,newB, height, width);
    }
    /**
     * Draw daeker 50% image for original image
     */
    // https://stackoverflow.com/questions/33072365/how-to-darken-a-given-color-int
    public Pixel darkerDraw(){
        int index = 0;
        int[][] newR = new int[height][width];
        int[][] newG = new int[height][width];
        int[][] newB = new int[height][width];
        for (int i = 0; i < canvas.getHeight(); i++) {
            for (int j = 0; j < canvas.getWidth(); j++) {
                r = min((int) Math.round(R[i][j] * 0.5),255);
                g = min((int) Math.round(G[i][j] * 0.5),255);
                b = min((int) Math.round(B[i][j] * 0.5),255);
                color = new Color(r, g, b);
                newR[i][j] =  r;
                newG[i][j] =  g;
                newB[i][j] =  b;
                canvas.setRGB(j, i, color.getRGB());
                grayScale[index++] = (r+g+b)/3;
            }
        }
        repaint();
        return new Pixel(newR, newG, newB, height,width);
    }
    /**
     * Draw gamma correction image for original image
     */
    public Pixel gammaDraw() {
        double gamma = 2.2;
        double prime = 1 / gamma;
        int index = 0;
        int[][] newR = new int[height][width];
        int[][] newG = new int[height][width];
        int[][] newB = new int[height][width];
        for (int i = 0; i < canvas.getHeight(); i++) {
            for (int j = 0; j < canvas.getWidth(); j++) {
                r = (int) Math.round(255 * Math.pow(R[i][j] / 255.0, prime));
                g = (int) Math.round(255 * Math.pow(G[i][j] / 255.0, prime));
                b = (int) Math.round(255 * Math.pow(B[i][j] / 255.0, prime));
                newR[i][j] =  r;
                newG[i][j] =  g;
                newB[i][j] =  b;
                color = new Color(r, g, b);
                canvas.setRGB(j, i, color.getRGB());
                grayScale[index++] = (r+g+b)/3;
            }
        }
        repaint();
        return new Pixel(newR, newG, newB, height,width);
    }
    /**
     * Draw vivid image for original image
     */
    // Using gamma correction + linear scratch
    // https://www.dfstudios.co.uk/articles/programming/image-programming-algorithms/image-processing-algorithms-part-6-gamma-correction/
    public Pixel vividDraw() {
        double gamma = 1.27;
        double prime = 1 / gamma;
        int index = 0;
        int[][] newR = new int[height][width];
        int[][] newG = new int[height][width];
        int[][] newB = new int[height][width];
        for (int i = 0; i < canvas.getHeight(); i++) {
            for (int j = 0; j < canvas.getWidth(); j++) {
                r = ((int) Math.round(255 * Math.pow( stretchValue(R[i][j]) / 255.0, prime)));
                g = ((int) Math.round(255 * Math.pow(stretchValue(G[i][j]) / 255.0, prime)));
                b = ((int) Math.round(255 * Math.pow(stretchValue(B[i][j]) / 255.0, prime)));
                color = new Color(r, g, b);
                canvas.setRGB(j, i, color.getRGB());
                grayScale[index++] = (r+g+b)/3;
                newR[i][j] = r;
                newB[i][j] = b;
                newG[i][j] = g;
            }
        }
        repaint();
        return new Pixel(newR,newG, newB, height, width);
    }
    /**
     * Calculate the r,g,b stretchValue
     */
    private int stretchValue(int color) {
        int value = 0;
        // parameters for linear stretch
        if (LOWERBOUND <= color && color <= HIGHERBOUND){
            value = (int)(((double)(MAX - MIN)*1.0/(HIGHERBOUND - LOWERBOUND)) * (color - LOWERBOUND) + MIN);
        } else if (color < LOWERBOUND){
            value = MIN;
        } else{
            value = MAX;
        }
        return value;
    }
    /**
     * Paint the grayScale histogram to check the image quality
     */
    public void histogramDraw() {
        for (int i = 0; i < canvas.getHeight() ; i++) {
            for (int j = 0; j < canvas.getWidth() ; j++) {
//                Random random = new Random();
                color = Color.BLACK;
                canvas.setRGB(j, i, color.getRGB());
            }
        }
        Graphics2D g2d = (Graphics2D) canvas.getGraphics();
        int[] histogram = new int[256];
        // Calculate the frequency
        int max = Integer.MIN_VALUE;
        for (int pixel : grayScale) {
            histogram[pixel]++;
        }
        for (int pixel : grayScale) {
            max = Math.max(max, histogram[pixel]);
        }
        int hisHeight = 0;
        int interval = width / histogram.length;
        for (int i = 0; i < histogram.length; i++) {
            hisHeight = (int) (height * ((double) histogram[i] / max));
            g2d.draw(new Rectangle(i*interval, height - hisHeight, interval, hisHeight));
        }
        repaint();
    }
//    https://stackoverflow.com/questions/15547125/pixelate-image-in-code
    public Pixel pixelateDraw(){
        int amount = 2;
        int index = 0;
        int[][] newR = new int[height][width];
        int[][] newG = new int[height][width];
        int[][] newB = new int[height][width];
//        int averR = 0, averG = 0, averB =0;
        for(int i = 0; i < height;  i += amount) {
            for(int j = 0; j < width; j += amount) {
                r = 0;
                g = 0;
                b = 0;
                if (j + 1 >= width || i + 1 >= height) {
                    continue;
                }
                for(int x = i; x < amount + i; x++){
                    for(int y = j; y < amount + j; y++){
                        r += R[x][y];
                        g += G[x][y];
                        b += B[x][y];
                    }
                }

                r /= Math.pow(amount, 2);
                g /= Math.pow(amount, 2);
                b /= Math.pow(amount, 2);

                for(int x = i; x < amount + i; x++){
                    for(int y = j; y < amount + j; y++){
                        newR[i][j] = r;
                        newG[i][j] = g;
                        newB[i][j] = b;
                    }
                }
                grayScale[index++] = (r + g+ b)/3;
            }
        }

        for (int i = 0; i < canvas.getHeight() ; i++) {
            for (int j = 0; j < canvas.getWidth(); j++) {
                color = new Color(newR[i][j], newG[i][j], newB[i][j]);
                canvas.setRGB(j, i, color.getRGB());
            }
        }
        repaint();
        return new Pixel(newR,newG, newB, height, width);
    }

    public Pixel blurDraw(){
        int amount = 2;
        int index = 0;
        int[][] newR = new int[height][width];
        int[][] newG = new int[height][width];
        int[][] newB = new int[height][width];
        for(int i = 1; i < height;  i += amount) {
            for(int j = 1; j < width; j += amount) {
                if (i < 1 || j < 1 || j + 1 >= width || i + 1 >= height){
                    newR[i-1][j-1] = r;
                    newG[i-1][j-1] = g;
                    newB[i-1][j-1] = b;
                    continue;
                }
                r = 0;
                g = 0;
                b = 0;
                for(int x = -1; x <= 1; x++){
                    for(int y = -1; y <= 1; y++){
                        r += R[x+i][y+j];
                        g += G[x+i][y+j];
                        b += B[x+i][y+j];
                    }
                }
                r /= 9;
                g /= 9;
                b /= 9;
                for(int x = -1; x <= 1; x++){
                    for(int y = -1; y <= 1; y++){
                        newR[x+i][y+j] = r;
                        newG[x+i][y+j] = g;
                        newB[x+i][y+j] = b;
                    }
                }
                grayScale[index++] = (r + g+ b)/3;
            }
        }
        for (int i = 0; i < canvas.getHeight() ; i++) {
            for (int j = 0; j < canvas.getWidth(); j++) {
                color = new Color(newR[i][j], newG[i][j], newB[i][j]);
                canvas.setRGB(j, i, color.getRGB());
            }
        }
        repaint();
        return new Pixel(newR,newG, newB, height, width);
    }

    public void rotate(Pixel pixel) {
        int index = 0;
        int[][] newR = new int[width][height];
        int[][] newG = new int[width][height];
        int[][] newB = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                newR[width - i - 1][height - j - 1] = pixel.getR()[j][i];
                newG[width - i - 1][height - j - 1] = pixel.getG()[j][i];
                newB[width - i - 1][height - j - 1] = pixel.getB()[j][i];
                grayScale[index++] = (r + g+ b)/3;
                Color color = new Color(newR[width - i - 1][height - j - 1], newG[width - i - 1][height - j - 1]
                        ,newB[width - i - 1][height - j - 1]
                );
                canvas.setRGB(width - i - 1, height - j - 1, color.getRGB());
            }
        }
        repaint();
    }
    public void normalDraw(Pixel pixel) {
        int index = 0;
        for (int i = 0; i < canvas.getHeight(); i++) {
            for (int j = 0; j < canvas.getWidth(); j++) {
                color = new Color(pixel.getR()[i][j], pixel.getG()[i][j], pixel.getB()[i][j]);
                canvas.setRGB(j, i, color.getRGB());
                grayScale[index++] = (color.getRed()+color.getBlue()+color.getGreen())/3;
            }
        }
        repaint();
    }
    public void halfSize(Pixel pixel) {
        int index = 0;
        int[][] newR = new int[height/2][width/2];
        int[][] newG = new int[height/2][width/2];
        int[][] newB = new int[height/2][width/2];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width ; j++) {
                canvas.setRGB(j, i , UIManager.getColor ( "Panel.background" ).getRGB());
            }
        }
        repaint();

        for (int i = 0; i < height/2; i++) {
            for (int j = 0; j < width/2; j++) {
                newR[i][j] =  pixel.getR()[i*2][j*2];
                newG[i][j] =  pixel.getG()[i*2][j*2];
                newB[i][j] =  pixel.getB()[i*2][j*2];
                grayScale[index++] = (newR[i][j] + newG[i][j] + newB[i][j] )/3;
                Color color = new Color(newR[i][j], newG[i][j], newB[i][j]);
                canvas.setRGB(j, i , color.getRGB());
            }
        }
        repaint();
    }
} // ImageGraph.java
