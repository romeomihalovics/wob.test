package wob.test.reporter;

import org.json.JSONArray;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class InvalidLogger {
    @SuppressWarnings("BusyWait")
    public void logInvalid(JSONArray data) throws IOException, InterruptedException {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("importLog.csv", false)));
        writer.println("ListingId;MarketplaceName;InvalidField");
        for(int i = 0; i < data.length(); i++) {
            System.out.printf("\r Creating invalidLog file: %.2f %% ( %d / %d )", (double) i / data.length() * 100, i, data.length());
            Thread.sleep(20);
            writer.println(data.getJSONObject(i).getString("id")+";"+data.getJSONObject(i).getInt("marketplace")+";"+data.getJSONObject(i).getString("invalidFields"));
        }
        writer.close();
    }
}
