package image;

import colorConvertor.Pixel;
import uilti.Qmatrix;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


// Different way to deal with img and show the JPG
public class ProcessJPG {
    private static int[][] originR;
    private static int[][] originG;
    private static int[][] originB;
    private Pixel pixels;

    public static String getFileName() {
        return fileName;
    }

    private static String fileName;

    public static String getPathString() {
        return pathString;
    }

    private static String pathString;
    private int imageHeight;
    private int imageWidth;

    public void processJPG(String pathStr) throws IOException {
        try {
            pathString = pathStr;
//            Image image = Toolkit.getDefaultToolkit().createImage(pathStr);
            Path pathFile = Paths.get(pathStr);
            fileName = pathFile.getFileName().toString();
            BufferedImage bi=ImageIO.read(new File(pathStr));
            int[] pixel;
            imageHeight = bi.getHeight();
            imageWidth = bi.getWidth();
            originR = new int[imageHeight][imageWidth];
            originG = new int[imageHeight][imageWidth];
            originB = new int[imageHeight][imageWidth];
            for (int i = 0; i < bi.getHeight(); i++) {
                for (int j = 0; j < bi.getWidth(); j++) {
                    pixel = bi.getRaster().getPixel(j, i, new int[3]);
                    originR[i][j] = pixel[0];
                    originG[i][j] = pixel[1];
                    originB[i][j] = pixel[2];
                }
            }
            pixels = new Pixel(originR, originG, originB, imageHeight, imageWidth);
            showImage(originR, originG, originB,imageHeight,imageWidth);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
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
//        panel.add(jComboBoxRatio,BorderLayout.SOUTH);
        panel.add(rotateButton);

        frame.add(panel,BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
