package wob.test.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import wob.test.wobTest;

import java.io.*;
import java.util.Properties;

public class UploadReport {
    private FTPClient client = new FTPClient();

    public UploadReport() {
        init();
    }

    private void init() {
        try {
            Properties ftpconfig = new Properties();
            ftpconfig.load(new FileReader(wobTest.ftpconfig));

            client.connect(ftpconfig.getProperty("url"));
            client.login(ftpconfig.getProperty("user"), ftpconfig.getProperty("password"));
            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadReport() {
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
