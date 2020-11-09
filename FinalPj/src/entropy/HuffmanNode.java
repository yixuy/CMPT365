package entropy;

import java.io.Serializable;
//https://github.com/sherifabdlnaby/Lossless-Text-Compression-App/blob/master/src/HuffNode.java
class HuffmanNode implements Serializable {
    public char Char;
    public String Code;
    public Integer Freq;
    public HuffmanNode RightNode;
    public HuffmanNode LeftNode;
    public Integer getFreq() {
        return Freq;
    }
    public HuffmanNode(Integer freq) {
        Freq = freq;
    }
    public HuffmanNode(char aChar, Integer freq) {
        Char = aChar;
        Freq = freq;
    }
}
