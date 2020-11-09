package uilti;

//import entropyCoding.LZWDecode;
import image.Lossy;
import image.ProcessBMP;
import image.ProcessJPG;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A class interface for user choose the file as user like
 * @YIXU YE
 */
//https://blog.csdn.net/java_faep/article/details/53523401
public class DragFileBox extends JFrame {
    private JTextField field;
    private String pathFile;
    public String getPathFile() {
        return pathFile;
    }
    /**
     * Drag  FileBox and get String path for the file as user choose
     */
    public DragFileBox() {
        this.setTitle("Drag the file to Here ↓↓↓↓");
        this.setSize(400, 300);
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        field = new JTextField();
        field.setBounds(0, 0, 400, 300);
        field.setText("BMP,JPG to read/IMG to show");
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setFont(new Font("TimesRoman", Font.BOLD, 16));
        field.setTransferHandler(new TransferHandler() {
            @Override
            public boolean importData(JComponent comp, Transferable t) {
                try {
                    Object o = t.getTransferData(DataFlavor.javaFileListFlavor);
                    pathFile = o.toString();

                    if (pathFile.startsWith("[")) {
                        pathFile = pathFile.substring(1);
                    }
                    if (pathFile.endsWith("]")) {
                        pathFile = pathFile.substring(0, pathFile.length() - 1);
                    }
                    // Entrance for read file
                    if (pathFile.contains(".bmp")|| pathFile.contains(".BMP")){
                        ProcessBMP processImage = new ProcessBMP();
                        processImage.processBMP(pathFile);
                    } else if (pathFile.contains(".img")|| pathFile.contains(".IMG")){
                        Path p = Paths.get(pathFile);
                        String fileName = p.getFileName().toString();
                        String path = pathFile.replaceAll(fileName,"");
                        Lossy lossy = new Lossy();
                        lossy.lossyDecompress(fileName, path);
                    } else if(pathFile.contains(".jpg")|| pathFile.contains(".JPG")){
                         ProcessJPG processImage = new ProcessJPG();
                         processImage.processJPG(pathFile);
                    }else{
                        field.setText("Please drag a valid file format !!!");
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
            @Override
            public boolean canImport(JComponent comp, DataFlavor[] flavors) {
                for (int i = 0; i < flavors.length; i++) {
                    if (DataFlavor.javaFileListFlavor.equals(flavors[i])) {
                        return true;
                    }
                }
                return false;
            }
        });
        this.add(field);
        this.setVisible(true);
    }
} // DragFileBox.java
