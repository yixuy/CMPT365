package colorConvertor;

import java.awt.*;

public class Pixel {
    private static int [][]R;
    private static int [][]G;
    private static int [][]B;
    private static int [][] Y;
    private static int [][] U;
    private static int [][] V;
    private static int imageHeight;
    private static int imageWidth;

    public Pixel(int[][] r, int[][] g, int[][] b, int height, int width) {
        R = r;
        G = g;
        B = b;
        imageHeight = height;
        imageWidth = width;
        Y = new int[imageHeight][imageWidth];
        U = new int[imageHeight][imageWidth];
        V = new int[imageHeight][imageWidth];
        RGB2YUV();
    }
    public void setYUV(int[][] y, int[][] u, int[][] v){
        Y = y;
        U = u;
        V = v;
    }
    public int[][] getR() {
        return R;
    }

    public void setR(int[][] r) {
        R = r;
    }

    public int[][] getG() {
        return G;
    }

    public void setG(int[][] g) {
        G = g;
    }

    public int[][] getB() {
        return B;
    }

    public void setB(int[][] b) {
        B = b;
    }
    public void setBValue(int value, int i, int j) {
        B[i][j] = value;
    }
    public void setGValue(int value, int i, int j) {
        G[i][j] = value;
    }
    public void setRValue(int value, int i, int j) {
        R[i][j] = value;
    }
    public int[][] getY() {
        return Y;
    }

    public void setY(int[][] y) {
        Y = y;
    }

    public int[][] getU() {
        return U;
    }

    public void setU(int[][] u) {
        U = u;
    }

    public int[][] getV() {
        return V;
    }

    public void setV(int[][] v) {
        V = v;
    }
    //    http://hushiyu1995.com/2018/01/21/Image-Compression-Alogrithm-and-RGB-to-YUV-Transfer/index.html

    public static void RGB2YUV() {
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j < imageWidth; j++) {
                Y[i][j] = (int) (R[i][j] * (0.299) + G[i][j] * (0.5870) + B[i][j] * (0.1140));
                U[i][j] = (int) (R[i][j] * (-0.169) + G[i][j] * (-0.331) + B[i][j] * (0.500)) + 128;
                V[i][j] = (int) (R[i][j] * (0.500) + G[i][j] * (-0.419) + B[i][j] * (-0.081)) + 128;
                if (Y[i][j] < 0) {
                    Y[i][j] = 0;
                } else if (Y[i][j] > 255) {
                    Y[i][j] = 255;
                }
                if (U[i][j] < 0) {
                    U[i][j] = 0;
                } else if (U[i][j] > 255) {
                    U[i][j] = 255;
                }
                if (V[i][j] < 0) {
                    V[i][j] = 0;
                } else if (V[i][j] > 255) {
                    V[i][j] = 255;
                }
            }
        }
    }
    public void YUV2RGB() {
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j < imageWidth; j++) {
                R[i][j] = (int) ((Y[i][j] + 0.000 * U[i][j] + 1.140 * V[i][j]) * 255);
                G[i][j] = (int) ((Y[i][j] - 0.396 * U[i][j] - 0.581 * V[i][j]) * 255);
                B[i][j] = (int) ((Y[i][j] + 2.029 * U[i][j] + 0.000 * V[i][j]) * 255);
            }
        }
    }

    public static int[][] getRGBColor(int imageHeight, int imageWidth) {
        Color color;
        int [][]colorValue = new int [imageHeight][imageWidth];
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j < imageWidth; j++) {
                color = new Color(R[i][j], G[i][j], B[i][j]);
                colorValue[i][j] = color.getRGB();
            }
        }
        return colorValue;
    }
}
