package wob.test.reporter;

import org.json.JSONArray;

import java.io.*;

import static jdk.nashorn.internal.objects.NativeMath.round;

public class InvalidLogger {
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
