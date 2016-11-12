/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 *
 * @author andrei
 */
public class FileRequest extends PeerRequest {
    public static final int CHUNK_SIZE = 20480;
    public String filename; // with full path
    public BufferedInputStream inputFile;
    public BufferedOutputStream outputFile;
    public byte[] buffer;
    public int sizeInBuffer;

    public FileRequest(String filename, String requesterUsername, String responderUsername) {
        super(requesterUsername, responderUsername);
        this.filename = filename;
    }

    public boolean openInputFile(String inputFilename) {
        try {
            inputFile = new BufferedInputStream(new FileInputStream(inputFilename));
        } catch (FileNotFoundException ex) {
            return false;
        }
        return true;
    }

    public boolean openOutputFile(String outputFilename) {
        try {
            outputFile = new BufferedOutputStream(new FileOutputStream(outputFilename));
        } catch (FileNotFoundException ex) {
            return false;
        }
        return true;
    }
}
