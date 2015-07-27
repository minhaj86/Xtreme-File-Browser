package com.mjapps.mjfilebrowse.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Class for basic file manipulations
 */
public class BasicFileUtil {
    private String inputFilePath;
    private String outputFilePath;

    //constructor
    public BasicFileUtil(String paramInputFilePath, String paramOutputFilePath) {
        this.inputFilePath = paramInputFilePath;
        this.outputFilePath = paramOutputFilePath;
    }

    //constructor
    public BasicFileUtil(String paramInputFilePath) {
        this.inputFilePath = paramInputFilePath;
    }

    //moves file
    public void moveFile() throws IOException {
        File input = FileUtils.getFile(inputFilePath);
        File output = FileUtils.getFile(outputFilePath);
        FileUtils.moveToDirectory(input, output, true);

    }

    //deletes file
    public void deleteFile() throws IOException {
        if (FileUtils.getFile(inputFilePath).isDirectory()) {
            FileUtils.deleteDirectory(FileUtils.getFile(inputFilePath));
        } else {
            FileUtils.getFile(inputFilePath).delete();
        }

    }

    //creates directory
    public boolean createDirectory() throws IOException {
        File dir = FileUtils.getFile(inputFilePath);
        return dir.mkdirs();
    }

    public boolean createNewFile() throws IOException {
        File file = FileUtils.getFile(inputFilePath);
        System.out.println(inputFilePath);
        return file.createNewFile();
    }

    //creates file
    private File getCreatedOutputFile() throws IOException {
        String filename = inputFilePath.substring(inputFilePath.lastIndexOf("/") + 1);
        String filePath = outputFilePath + filename;
        File file = FileUtils.getFile(filePath);
        if (new File(inputFilePath).isDirectory()) {
            file.mkdirs();
        } else {
            file.createNewFile();
        }
        return file;
    }

    //copies file
    public void copyFile() throws IOException {
        File input = new File(inputFilePath);
        if (new File(inputFilePath).isDirectory()) {
            FileUtils.copyDirectory(input, getCreatedOutputFile());


        } else {
            FileUtils.copyFile(input, getCreatedOutputFile());

        }
    }

    private String getFilePath() {
        String filePath = inputFilePath.
                substring(0, inputFilePath.lastIndexOf(File.separator));
        return filePath;
    }

    public void rename(String newName) {
        File file = new File(inputFilePath);
        String filePathRenamed = getFilePath() + File.separator + newName;
        File renamedFile = new File(filePathRenamed);
        file.renameTo(renamedFile);
    }
}
