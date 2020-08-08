package wob.test;

import wob.test.db.Database;
import wob.test.ftp.UploadReport;
import wob.test.reporter.InvalidLogger;
import wob.test.reporter.ResultReporter;
import wob.test.task.CreateReport;
import wob.test.task.SyncApi;
import wob.test.validator.Validator;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;


public class WobTest {

    public static final String dbConfig = "database.cfg";
    public static final String ftpConfig = "ftp.cfg";

    public static Database database = new Database();
    public static SyncApi syncApi = new SyncApi();
    public static Validator validator = new Validator();
    public static InvalidLogger invalidLogger = new InvalidLogger();
    public static CreateReport createReport = new CreateReport();
    public static ResultReporter resultReport = new ResultReporter();
    public static UploadReport uploadReport = new UploadReport();

    public static void main(String[] args) throws IOException, SQLException, ParseException, InterruptedException {
        System.out.println("- Status -");
        syncApi.sync();
        createReport.reportResults();
        uploadReport.uploadReport();
    }
}
