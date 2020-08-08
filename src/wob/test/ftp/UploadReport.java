package wob.test.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import wob.test.WobTest;

import java.io.*;
import java.util.Properties;

public class UploadReport {
    private final FTPClient client = new FTPClient();

    private void init() {
        try {
            Properties ftpConfig = new Properties();
            ftpConfig.load(new FileReader(WobTest.ftpConfig));

            client.connect(ftpConfig.getProperty("url"));
            client.login(ftpConfig.getProperty("user"), ftpConfig.getProperty("password"));
            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadReport() {
        init();
        try {
            System.out.print("\r Uploading Report File");
            File reportFile = new File("report.json");
            InputStream inputStream = new FileInputStream(reportFile);

            boolean uploaded = client.storeFile("report.json", inputStream);
            inputStream.close();
            if (uploaded) {
                System.out.print("\r Report File Uploaded");
            }
        } catch (IOException e) {
            System.out.println("Error: "+e);
        }
    }

}
