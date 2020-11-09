package image;

import colorConvertor.Pixel;
//import entropyCoding.LZWEncode;
import uilti.Qmatrix;
import uilti.ReadFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A class process the image for user and will send the data to show image graph
 * @YIXU YE
 */
public class ProcessBMP {
    // File Data
    private byte[] buff;
    private static String fileName;
    private static String pathString;
    // Header data
    private int imageHeight;
    private int imageWidth;
    private int fileSize;
    private int bitsPerPixel;
    private int imageSize;
    private int compression;
    // Raw data
    private static int[][] originR;
    private static int[][] originG;
    private static int[][] originB;
    private static int[][] Y;
    private static int[][] U;
    private static int[][] V;
    private Pixel pixels;
    public static String getPathString(){
        return pathString;
    }
    public static String getFileName(){
        return fileName;
    }
    public void processBMP(String pathStr) throws IOException {
        // Read in buffer
        buff = ReadFile.readByte(pathStr);
        pathString = pathStr;
        fileName = Paths.get(pathStr).getFileName().toString();
        extractBMPData();
        showImage(originR, originG, originB, imageHeight, imageWidth);
    }
    /**
     * Extract all data from the file including header information and color data
     */
    private void extractBMPData() {
        int index = 2;
        // Extract Header data
        fileSize =  ReadFile.byte2Int(buff, index,4);
        index += 4 + 2 * 2 + 4 * 2;
        // Extract info Header data
        imageWidth = ReadFile.byte2Int(buff, index,4);
        index += 4;
        imageHeight = ReadFile.byte2Int(buff, index,4);
        index += 4 + 2;
        bitsPerPixel = ReadFile.byte2Int(buff, index,2);
        index += 2;
        compression = ReadFile.byte2Int(buff, index,4);
        index += 4;
        imageSize = ReadFile.byte2Int(buff, index,4);
        index += 4 * 5;
        // Extract image RGB data
        originR = new int[imageHeight][imageWidth];
        originG = new int[imageHeight][imageWidth];
        originB = new int[imageHeight][imageWidth];
        Y = new int[imageHeight][imageWidth];
        U = new int[imageHeight][imageWidth];
        V = new int[imageHeight][imageWidth];
        // The width does not have 4 times
        // determine whether is required an extra 0 behind
        // https://research.cs.queensu.ca/home/blostein/image/Example/BMPFile.java
        int skip = 0;
        if(!(imageWidth*3 % 4==0)){
             skip = 4 - imageWidth*3 % 4;
        }
        // Read conversely
        for (int i = imageHeight -1; i >= 0; i--) {
            for (int j = 0; j < imageWidth; j++) {
                // You can not use the readByte due to just 1 byte cause buffer underflow
                originB[i][j] = readRGB(index);
                index ++;
                originG[i][j] = readRGB(index);
                index ++;
                originR[i][j] = readRGB(index);
                index ++;
                RGB2YUV(originR[i][j], originG[i][j], originB[i][j], i, j);
            }
            // Debug the automatically keep 4 byte
            index += skip;
        }
        pixels = new Pixel(originR, originG, originB, imageHeight, imageWidth);
        pixels.setYUV(Y,U,V);
    }

//    http://hushiyu1995.com/2018/01/21/Image-Compression-Alogrithm-and-RGB-to-YUV-Transfer/index.html
    private static void RGB2YUV(int R, int G, int B, int i, int j) {
        Y[i][j] = (int) (R*(0.299) + G*(0.5870) + B*(0.1140));
        U[i][j] = (int) (R*(-0.169) + G*(-0.331) + B*(0.500)) + 128;
        V[i][j] = (int) (R*(0.500 ) + G*(-0.419) + B*(-0.081)) + 128;
        if (Y[i][j] < 0) {
            Y[i][j] = 0;
        } else if (Y[i][j] > 255) {
            Y[i][j] = 255;
        }
        if (U[i][j] < 0) {
            U[i][j] = 0;
        }
        else if (U[i][j] > 255) {
            U[i][j] = 255;
        }
        if (V[i][j] < 0) {
            V[i][j] = 0;
        }
        else if (V[i][j] > 255) {
            V[i][j] = 255;
        }
    }

    /**
     * Display the image and histogram where it shows the effects as user like
     */
//    https://stackoverflow.com/questions/15173325/execute-an-action-when-an-item-on-the-combobox-is-selected
    public void showImage(int[][]R, int [][]G, int [][]B, int imageHeight, int imageWidth) {
        JFrame frame = new JFrame(fileName);
        ImageGraph imageGraph = new ImageGraph(R, G, B, imageHeight, imageWidth);
        frame.add(imageGraph, BorderLayout.CENTER);
        frame.setSize(imageWidth +200, imageHeight +100);
        JPanel panel = new JPanel();

        String effect[]={"Original","GrayScale","Darker","Vivid","Gamma", "Blur", "Pixelate"};
        JComboBox jComboBoxEffect=new JComboBox(effect);
        jComboBoxEffect.setFont(new Font ("TimesRoman", Font.BOLD, 13));
        jComboBoxEffect.setBounds(50, 50,90,20);

        String ratio[]={"10% saved","50% saved","90% saved"};
        JComboBox jComboBoxRatio=new JComboBox(ratio);
        jComboBoxRatio.setFont(new Font ("TimesRoman", Font.BOLD, 13));
        jComboBoxRatio.setBounds(50, 50,90,20);

        JButton histogramButton = new JButton("Histogram");
        histogramButton.setFont(new Font ("TimesRoman", Font.BOLD, 13));

        JButton halfSizeButton = new JButton("Half ");
        histogramButton.setFont(new Font ("TimesRoman", Font.BOLD, 13));

        JButton rotateButton = new JButton("Rotate");
        rotateButton.setFont(new Font ("TimesRoman", Font.BOLD, 13));

        // Button Event
        histogramButton.addActionListener(new ActionListener() {
            boolean isHistogram = true;
            @Override
            public void actionPerformed(ActionEvent e) {
                if(isHistogram){
                    imageGraph.histogramDraw();
                    isHistogram = false;
                }else {
                    imageGraph.normalDraw(pixels);
                    isHistogram = true;
                }
            }
        });
        rotateButton.addActionListener(new ActionListener() {
            boolean isRotate = true;
            @Override
            public void actionPerformed(ActionEvent e) {
                if(isRotate){
                    imageGraph.rotate(pixels);
                    isRotate = false;
                }else {
                    imageGraph.normalDraw(pixels);
                    isRotate = true;
                }
            }
        });
        halfSizeButton.addActionListener(new ActionListener() {
            boolean isHalf = true;
            @Override
            public void actionPerformed(ActionEvent e) {
                imageGraph.halfSize(pixels);
                if(isHalf){
                    imageGraph.halfSize(pixels);
                    isHalf = false;
                }else {
                    imageGraph.normalDraw(pixels);
                    isHalf = true;
                }
            }
        });
        jComboBoxEffect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JComboBox comboBox = (JComboBox) event.getSource();
                Object selected = comboBox.getSelectedItem();
                if(selected.toString().equals(effect[0])){
                    pixels = imageGraph.originalDraw();
                } else if(selected.toString().equals(effect[1])) {
                    imageGraph.grayScaleDraw();
                } else if(selected.toString().equals(effect[2])){
                    pixels = imageGraph.darkerDraw();
                } else if(selected.toString().equals(effect[3])){
                    pixels = imageGraph.vividDraw();
                } else if(selected.toString().equals(effect[4])){
                    pixels = imageGraph.gammaDraw();
                } else if(selected.toString().equals(effect[5])){
                    pixels = imageGraph.blurDraw();
                }else if(selected.toString().equals(effect[6])){
                    pixels = imageGraph.pixelateDraw();
                }

            }
        });

        jComboBoxRatio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JComboBox comboBox = (JComboBox) event.getSource();
                Object selected = comboBox.getSelectedItem();
                try {
                    Lossy lossy = new Lossy(pixels, imageHeight, imageWidth);
                    if(selected.toString().equals(ratio[0])){
                        lossy.setQMatrix(Qmatrix.Q10Y);
                    } else if(selected.toString().equals(ratio[1])) {
                        lossy.setQMatrix(Qmatrix.Q50Y);
                    } else if(selected.toString().equals(ratio[2])) {
                        lossy.setQMatrix(Qmatrix.Q90Y);
                    }
                    lossy.lossyCompress();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        panel.add(halfSizeButton);
        panel.add(jComboBoxEffect,BorderLayout.SOUTH);
        panel.add(histogramButton);
        panel.add(jComboBoxRatio,BorderLayout.SOUTH);
        panel.add(rotateButton);

        frame.add(panel,BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void showDecompressedImage(int[][]R, int [][]G, int [][]B, int imageHeight, int imageWidth, String pathStr) {
        Path pathFile = Paths.get(pathStr);
        String fileName = pathFile.getFileName().toString();

        JFrame frame = new JFrame(fileName);
        frame.setSize(imageWidth +500, imageHeight +100);
        ImageGraph imageGraph = new ImageGraph(R, G, B, imageHeight, imageWidth);
        frame.add(imageGraph, BorderLayout.CENTER);
        File file = new File(pathStr);

        JPanel panel = new JPanel();
        // Labels
        JLabel labelCompressionRatio = new JLabel();
        labelCompressionRatio.setSize(400,100);
        labelCompressionRatio.setFont(new Font ("TimesRoman", Font.BOLD, 16));
        if( pathString != null) {
            File oldFile = new File(pathString);
            labelCompressionRatio.setText("Compression Ratio: "+ oldFile.length() / (file.length()*1.0));
        }else {
            labelCompressionRatio.setText("IMG size: "+ file.length());
        }

        JLabel labelDeCompressedTime = new JLabel();
        labelDeCompressedTime.setSize(400,100);
        labelDeCompressedTime.setFont(new Font ("TimesRoman", Font.BOLD, 16));
        labelDeCompressedTime.setText("Decompress Time: " + Lossy.deCompressedTime + "s");

        JLabel labelCompressedTime = new JLabel();
        labelCompressedTime.setSize(400,100);
        labelCompressedTime.setFont(new Font ("TimesRoman", Font.BOLD, 16));
        labelCompressedTime.setText("Compress Time: "+ Lossy.compressedTime + "s");

        panel.add(labelCompressedTime);
        panel.add(labelDeCompressedTime);
        panel.add(labelCompressionRatio);

        frame.add(panel,BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        Lossy.compressedTime = 0;
        Lossy.deCompressedTime = 0;
    }
    private int readRGB(int index){
        if(index < buff.length)
            return (buff[index] & 0xff);
        else
            return 0;
    }
    private void getDataInfo() {
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j < imageWidth; j++) {
                System.out.println("R " + originR[i][j] + "G " + originG[i][j] + "B " + originB[i][j]);
            }
        }
    }
    private void buttonTrigger(){
        ImageGraph imageGraph = new ImageGraph(originR, originG, originB, imageHeight, imageWidth);
        JPanel panel = new JPanel();
        JButton originalButton = new JButton("Original");
        JButton grayscaleButton = new JButton("GrayScale");
        JButton darkerButton = new JButton("50% Darker");
        JButton vividButton = new JButton("Vivid");
        originalButton.setFont(new Font ("TimesRoman", Font.BOLD, 16));
        grayscaleButton.setFont(new Font ("TimesRoman", Font.BOLD, 16));
        darkerButton.setFont(new Font ("TimesRoman", Font.BOLD, 16));
        vividButton.setFont(new Font ("TimesRoman", Font.BOLD, 16));
        // Button Event
        originalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imageGraph.originalDraw();
            }
        });
        grayscaleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imageGraph.grayScaleDraw();
            }
        });
        darkerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imageGraph.darkerDraw();
            }
        });
        vividButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imageGraph.vividDraw();
            }
        });

        panel.add(originalButton);
        panel.add(grayscaleButton);
        panel.add(darkerButton);
        panel.add(vividButton);

        JFrame frame = new JFrame(fileName);
        frame.add(imageGraph, BorderLayout.CENTER);
        frame.setSize(imageWidth +100, imageHeight +100);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    public void getHeaderInfo(){
        System.out.println("fileSize: "+fileSize);
        System.out.println("width: "+ imageWidth);
        System.out.println("height: "+ imageHeight);
        System.out.println("bitsPerPixel: "+bitsPerPixel);
        System.out.println("compression: "+compression);
        System.out.println("imageSize: "+imageSize);
    }

}// ProcessImage.java
