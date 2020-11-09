package uilti;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A class Reading different data as we need
 * @YIXU YE
 */
public class ReadFile {
    /**
     * Reading data byte by byte from pathString
     */
    public static byte[] readByte(String pathString){
        byte[] buff = null;
        try {
            buff = Files.readAllBytes(Paths.get(pathString));
        } catch (IOException e) {
            System.out.println("You can not  read the wav data in buffer");
            e.printStackTrace();
        }
        return buff;
    }
    /**
     * Reading buffer data and from index to indext + length
     * return the data based on LITTLE_ENDIAN order
     */
    // All the data we take is in LITTLE_ENDIAN
    // Byte transfer to Int
    // https://stackoverflow.com/questions/14827398/converting-byte-array-values-in-little-endian-order-to-short-values
    public static int byte2Int(byte[] buff,int index, int length){
        byte[] tempByte = new byte[length];
        int result = 0;
        for (int i = 0; i < length; i++) {
            tempByte[i] = buff[index + i];
        }
        ByteBuffer bb = ByteBuffer.wrap(tempByte).order(ByteOrder.LITTLE_ENDIAN);
        while( bb.hasRemaining()) {
            if(length == 2){
                result = bb.getShort();
            }else {
                result = bb.getInt();
            }
        }
//        System.out.println(result);
        return result;
    }


//    public static byte[] ImageToByte(Image img)
//    {
//        ImageConverter converter = new ImageConverter();
//        return (byte[])converter.ConvertTo(img, typeof(byte[]));
//    }

    //    https://stackoverflow.com/questions/40255039/how-to-choose-file-in-java
    //    https://stackoverflow.com/questions/35676485/jfilechooser-add-different-file-types-in-filter
//    public static String readPath(){
//        JFileChooser chooser = new JFileChooser();
//        chooser.addChoosableFileFilter(new FileNameExtensionFilter("WAV Files", ".wav"));
//        chooser.addChoosableFileFilter(new FileNameExtensionFilter("BMP Files", ".bmp"));;
//        int returnVal = chooser.showOpenDialog(null);
//        if (returnVal == JFileChooser.APPROVE_OPTION) {
//            System.out.println("You choose to open this file: " +
//                            chooser.getSelectedFile().getPath());
//        }
//        return chooser.getSelectedFile().getPath();
//    }
} // ReadFile.java
