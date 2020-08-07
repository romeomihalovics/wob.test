package wob.test;

import wob.test.db.Database;
import wob.test.parser.Parser;
import wob.test.task.SyncApi;
import wob.test.validator.Validator;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;


public class wobTest {

    public static final String dbconfig = "database.cfg";

    public static Database database = new Database();
    public static SyncApi syncApi = new SyncApi();
    public static Validator validator = new Validator();

    public static void main(String[] args) throws IOException, SQLException, ParseException {
        syncApi.sync();
    }
}
