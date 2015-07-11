package browser.xtreme.com.xtremefbrowser.util;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;

/**
 * Created by kishan on 28/04/15.
 * File zip and unzip implementation
 */
public class ZipUtils {
    private String filePath;

    //constrcutor
    public ZipUtils(String paramFilePath) {
        this.filePath = paramFilePath;
    }

    //compress method
    public void compress() throws ZipException {
        ZipFile zipFile = new ZipFile(filePath + ".zip");
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        if (new File(filePath).isDirectory()) {
            System.out.println("In here");
            zipFile.addFolder(new File(filePath), parameters);
            System.out.println("Also In here");
        } else {
            zipFile.addFile(new File(filePath), parameters);
        }
    }

    //decompress method
    public void decompress() throws ZipException {
        ZipFile zipFile = new ZipFile(filePath);
        zipFile.extractAll(getOutputPath());
    }

    //getting output path
    private String getOutputPath() {
        return filePath.substring(0, filePath.lastIndexOf("/") + 1);

    }

}
