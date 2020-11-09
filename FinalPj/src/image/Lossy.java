package image;

import colorConvertor.Pixel;
import entropy.Huffman;
import uilti.Qmatrix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Lossy {
    // For make the zig zag order
    public final static int[][] zigzagOrder = {{0,0},{0,1},{1,0},{2,0},{1,1},{0,2},{0,3},{1,2},
                                                {2,1},{3,0},{4,0},{3,1},{2,2},{1,3},{0,4},{0,5},
                                                {1,4},{2,3},{3,2},{4,1},{5,0},{6,0},{5,1},{4,2},
                                                {3,3},{2,4},{1,5},{0,6},{0,7},{1,6},{2,5},{3,4},
                                                {4,3},{5,2},{6,1},{7,0},{7,1},{6,2},{5,3},{4,4},
                                                {3,5},{2,6},{1,7},{2,7},{3,6},{4,5},{5,4},{6,3},
                                                {7,2},{7,3},{6,4},{5,5},{4,6},{3,7},{4,7},{5,6},
                                                {6,5},{7,4},{7,5},{6,6},{5,7},{6,7},{7,6},{7,7}};

    final private static int BLOCK_SIZE = 8;
    // Get first raw YUV
    private static int[][] blockY;
    private static int[][] blockU;
    private static int[][] blockV;
    // Get decompressed YUV
    private static int[][] newY;
    private static int[][] newV;
    private static int[][] newU;
    // After inverse DCT and quantized YUV
    private static int[][] inTransferredY;
    private static int[][] inTransferredU;
    private static int[][] inTransferredV;
    private Pixel pixels;
    private int imageHeight;
    private int imageWidth;
    // After DCT YUV
    private int[][] dctY;
    private int[][] dctU;
    private int[][] dctV;
    private final double[][] CMatrix;
    private final double[][] CTMatrix;
    private static int[][] R;
    private static int[][] G;
    private static int[][] B;
    private final List<Integer> scannerY = new ArrayList<>();
    private final List<Integer> scannerU = new ArrayList<>();
    private final List<Integer> scannerV = new ArrayList<>();
    private final List<Integer> readY = new ArrayList<>();
    private final List<Integer> readU = new ArrayList<>();
    private final List<Integer> readV = new ArrayList<>();
    private static int outWidth;
    private static int outHeight;

    public void setQMatrix(int[][] QMatrix) {
        Lossy.QMatrix = QMatrix;
    }

    private static int[][] QMatrix =  Qmatrix.Q50Y;
    private static String outputBuff;
    private static String finalPathString;
    public static double compressedTime = 0;
    public static double deCompressedTime = 0;

    public Lossy(){
        CMatrix = new double[BLOCK_SIZE][BLOCK_SIZE];
        CTMatrix = new double[BLOCK_SIZE][BLOCK_SIZE];
    }
    public Lossy(Pixel pixels, int imageHeight, int imageWidth) {
        this.pixels = pixels;
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
        dctY = new int[imageHeight][imageWidth];
        dctU = new int[imageHeight][imageWidth];
        dctV = new int[imageHeight][imageWidth];
        R = new int[imageHeight][imageWidth];
        G = new int[imageHeight][imageWidth];
        B = new int[imageHeight][imageWidth];
        CMatrix = new double[BLOCK_SIZE][BLOCK_SIZE];
        CTMatrix = new double[BLOCK_SIZE][BLOCK_SIZE];
    }
    public void lossyCompress() throws IOException {
        long startTime = System.nanoTime();
        String fileName = ProcessBMP.getFileName();
        String path = ProcessBMP.getPathString().replaceAll(fileName, "");
        // Get YUV
        initBlockYUV();
        // DCT and quantized
        DCT();
        // Zig zag scan and run length
        zigzagScanner();
        // Huffman compress
        fileName = Huffman.Compress(outputBuff, fileName, path);
        File tempFile = new File(fileName);
        long endTime   = System.nanoTime();
        compressedTime = (endTime - startTime) / 1000000000.0;
        lossyDecompress(fileName,path);
    }

    public void lossyDecompress(String fileName, String filePath) throws IOException {
        long startTime = System.nanoTime();
        String rlFile = Huffman.Decompress(fileName, filePath);
        deCode(filePath + rlFile);
        finalPathString = filePath + fileName;
        inDCT();
        long endTime = System.nanoTime();
        deCompressedTime = (endTime - startTime)/ 1000000000.0;
        getRGBImg();
//        PSNR();
    }


    public void initBlockYUV() {
        blockY = pixels.getY();
        blockU = pixels.getU();
        blockV = pixels.getV();
    }
    private void getRGBImg() {
        ProcessBMP image = new ProcessBMP();
        image.showDecompressedImage(R,G,B,outHeight,outWidth, finalPathString);
    }

    // Use "/" separate the YUV and image height and width "," separate data
    private void deCode(String outputFile) throws IOException{
        outputBuff = Files.readString(Path.of(outputFile), StandardCharsets.US_ASCII);
        String[] tokens = outputBuff.split("/");
        outHeight = Integer.parseInt(tokens[0]);
        outWidth = Integer.parseInt(tokens[1]);
        for (int i = 0; i < tokens.length; i++){
            String[] items = tokens[i].split(",");
            if(i == 3){
                for (String item : items) {
                    String[] element = item.split(":");
                    int num = Integer.parseInt(element[0]);
                    int count = Integer.parseInt(element[1]);
                    for (int k = 0; k < count; k++) {
                        readY.add(num);
                    }
                }
            }else if(i == 4){
                for (String item : items) {
                    String[] element = item.split(":");
//                    System.out.println(element[0]);
                    int num = Integer.parseInt(element[0]);
                    int count = Integer.parseInt(element[1]);
                    for (int k = 0; k < count; k++) {
                        readU.add(num);
                    }
                }
            }else if(i == 5){
                for (String item : items) {
                    String[] element = item.split(":");
                    int num = Integer.parseInt(element[0]);
                    int count = Integer.parseInt(element[1]);
                    for (int k = 0; k < count; k++) {
                        readV.add(num);
                    }
                }
            }
        }
        newY = new int[outHeight][outWidth];
        newU = new int[outHeight][outWidth];
        newV = new int[outHeight][outWidth];
        inTransferredY = new int[outHeight][outWidth];
        inTransferredU = new int[outHeight][outWidth];
        inTransferredV = new int[outHeight][outWidth];

        int index = 0;
        for (int u = 0; u < outHeight; u+=8) {
            for (int v = 0; v < outWidth; v +=8) {
                for (int i = 0; i < 64; i++) {
                    newY[zigzagOrder[i][0]+u][zigzagOrder[i][1]+v] = readY.get(index);
                    newU[zigzagOrder[i][0]+u][zigzagOrder[i][1]+v] = readU.get(index);
                    newV[zigzagOrder[i][0]+u][zigzagOrder[i][1]+v] = readV.get(index);
                    index++;
                }
            }
        }
    }
    // Run length algorithm using zigzag scan
    private void zigzagScanner() throws IOException {
        for (int u = 0; u < imageHeight; u+=8) {
            for (int v = 0; v < imageWidth; v += 8) {
                for (int i = 0; i < 64; i++) {
                    scannerY.add(dctY[zigzagOrder[i][0]+u][zigzagOrder[i][1]+v]);
                    scannerU.add(dctU[zigzagOrder[i][0]+u][zigzagOrder[i][1]+v]);
                    scannerV.add(dctV[zigzagOrder[i][0]+u][zigzagOrder[i][1]+v]);
                }
            }
        }
        ArrayList<Integer> scanner = new ArrayList<>();
        scanner.addAll(scannerY);
        scanner.addAll(scannerU);
        scanner.addAll(scannerV);
        String outputFile = makeRunLengthOutputFile(scanner);
        outputBuff = Files.readString(Path.of(outputFile), StandardCharsets.US_ASCII);
    }
    // Output
    public String makeRunLengthOutputFile(List<Integer> scanner){
        String fileName = "output.rl";
        try {
            FileWriter myWriter = new FileWriter("output.rl");
            myWriter.write(imageHeight + "/");
            myWriter.write(imageWidth  + "/");
            int count = 0;
            int curr = scanner.get(0);
            for (int i = 0; i < scanner.size(); i++) {
                if(scanner.get(i) == curr){
                    count ++;
                }else {
                    myWriter.write( curr + ":"+ count +",");
                    curr = scanner.get(i);
                    count = 1;
                }
                if(i % (imageWidth*imageHeight) == 0){
                    myWriter.write("/");
                }
            }
            myWriter.write( curr +":"+ count + ",");
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return fileName;
    }
    // Inverse DCT
    private void inDCT() {
        initialCMatrix();
        getTransposeC();
        int [][]tempBlockY = new int[BLOCK_SIZE][BLOCK_SIZE];
        int [][]tempBlockU = new int[BLOCK_SIZE][BLOCK_SIZE];
        int [][]tempBlockV = new int[BLOCK_SIZE][BLOCK_SIZE];
        R = new int[outHeight][outWidth];
        G = new int[outHeight][outWidth];
        B = new int[outHeight][outWidth];

        for (int u = 0; u < outHeight; u+=8) {
            for (int v = 0; v < outWidth; v += 8) {
                for (int i = u; i < u + 8; i++){
                    for (int j = v; j < v + 8; j++ ){
                        // Central to -1 to 1 as 0
                        tempBlockY[i-u][j-v] = newY[i][j];
                        tempBlockU[i-u][j-v] = newU[i][j];
                        tempBlockV[i-u][j-v] = newV[i][j];
                    }
                }
                for (int i = u; i < u + 8; i++) {
                    for (int j = v; j < v + 8; j++) {
                        inTransferredY[i][j]=  (int)inTransform(tempBlockY, true)[i-u][j-v]  + 128;
                        inTransferredU[i][j] = (int)inTransform(tempBlockU, false)[i-u][j-v] + 128;
                        inTransferredV[i][j] = (int)inTransform(tempBlockV, false)[i-u][j-v] + 128;
                        YUV2RGB(inTransferredY[i][j],inTransferredU[i][j], inTransferredV[i][j], i, j);
                    }
                }
            }
        }
    }
    // 2D DCT
    public void DCT(){
        initialCMatrix();
        getTransposeC();
        double [][]tempBlockY = new double[BLOCK_SIZE][BLOCK_SIZE];
        double [][]tempBlockU = new double[BLOCK_SIZE][BLOCK_SIZE];
        double [][]tempBlockV = new double[BLOCK_SIZE][BLOCK_SIZE];

        for (int u = 0; u < imageHeight; u+=8) {
            for (int v = 0; v < imageWidth; v+=8) {
                for (int i = u; i < u + 8; i++){
                    for (int j = v; j < v + 8; j++ ){
                        // Central to -1 to 1 as 0
                        tempBlockY[i-u][j-v] = blockY[i][j]-128;
                        tempBlockU[i-u][j-v] = blockU[i][j]-128;
                        tempBlockV[i-u][j-v] = blockV[i][j]-128;
                    }
                }

                for (int i = u; i < u + 8; i++){
                    for (int j = v; j < v + 8; j++ ){
                        dctY[i][j] = Transform(tempBlockY, true)[i-u][j-v];
                        dctU[i][j] = Transform(tempBlockU, false)[i-u][j-v];
                        dctV[i][j] = Transform(tempBlockV,false)[i-u][j-v];
                    }
                }
            }
        }
    }
    private void initialCMatrix() {
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                if(i == 0){
                    CMatrix[i][j] = Math.sqrt(1.0/BLOCK_SIZE);
                }else {
                    CMatrix[i][j] = Math.sqrt(2.0/BLOCK_SIZE)
                                    *Math.cos( ((2*j+1)*i*Math.PI/(2.0*BLOCK_SIZE)) );
                }
            }
        }
    }
    // Get inverse 
    private void getTransposeC(){
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                CTMatrix[i][j] = CMatrix[j][i];
            }
        }
    }
    // Quantization
    private int[][] Transform(double[][] matrix, boolean isY) {
        int[][] result = new int[BLOCK_SIZE][BLOCK_SIZE];
        double[][] F = matrixMulti(CMatrix, matrix);
        F = matrixMulti(F, CTMatrix);
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                if(isY){
                    result[i][j] =(int) Math.round(F[i][j] / QMatrix[i][j]) ;
                }else {
                    result[i][j] =(int) Math.round(F[i][j] /Qmatrix.Q50UV[i][j]);
                }
            }
        }
        return result;
    }
    private double[][] matrixMulti(double[][] matrix1, double[][]matrix2){
        double[][] temp = new double[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                for(int k = 0; k < BLOCK_SIZE; k++) {
                    temp[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }
        return temp;
    }
    private double[][] inTransform(int[][] matrix, boolean isY) {
        int[][] F = new int[BLOCK_SIZE][BLOCK_SIZE];
        double[][] temp = new double[BLOCK_SIZE][BLOCK_SIZE];
        double[][] result =  new double[BLOCK_SIZE][BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                if(isY){
                    F[i][j] = Math.round(matrix[i][j] * QMatrix[i][j]) ;
                }else {
                    F[i][j] = Math.round(matrix[i][j] * Qmatrix.Q50UV[i][j]);
                }
            }
        }
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                for(int k = 0; k < BLOCK_SIZE; k++) {
                    temp[i][j] += CTMatrix[i][k] * F[k][j];
                }
            }
        }
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                for(int k = 0; k < BLOCK_SIZE; k++) {
                    result[i][j] += temp[i][k] * CMatrix[k][j];
                }
            }
        }
        return result;
    }
    // YUV to RGB (Maybe overflow)
    public static void YUV2RGB(int Y, int U, int V, int i, int j) {
        R[i][j] = (int) (Y  + 1.4 * (V - 128));
        G[i][j] = (int) (Y - 0.343 * (U - 128) - 0.711 * (V - 128));
        B[i][j] = (int) (Y + 1.765 * (U - 128));
        if (R[i][j] < 0) { R[i][j] = 0; }
        else if (R[i][j] > 255) { R[i][j] = 255; }
        if (G[i][j] < 0) { G[i][j] = 0; }
        else if (G[i][j] > 255) { G[i][j] = 255; }
        if (B[i][j]  < 0) { B[i][j]  = 0; }
        else if (B[i][j]  > 255) { B[i][j]  = 255; }
    }

//    public void PSNR() {
//        double mse = 0.0;
//        for (int i = 0; i < outHeight; i++) {
//            for (int j = 0; j < outWidth; j++) {
//                mse += Math.pow((R[i][j] - pixels.getR()[i][j]) , 2) + Math.pow((B[i][j] - pixels.getB()[i][j]), 2) + Math.pow((G[i][j] - pixels.getG()[i][j]), 2);
//            }
//        }
//        System.out.println(mse);
//        mse /= outHeight * outWidth * 3;
//        System.out.println(20 * Math.log(255 / Math.sqrt(mse))/ Math.log(10.0));
//    }
}
