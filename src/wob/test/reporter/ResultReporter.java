package wob.test.reporter;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ResultReporter {
    public void resultReporter(JSONObject data) throws IOException {
        System.out.print("\r Saving Report JSON");
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("report.json", false)));
        writer.println(data.toString());
        writer.close();
    }
}
