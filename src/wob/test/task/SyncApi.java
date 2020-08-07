package wob.test.task;

import org.json.JSONArray;
import org.json.JSONObject;
import wob.test.parser.Parser;
import wob.test.wobTest;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

public class SyncApi {

    public void sync() throws IOException, SQLException, ParseException {

        HashMap<String, ArrayList<JSONObject>> listingValidated = new HashMap<>();

        JSONArray listingRaw = Parser.readJsonFromUrl("https://my.api.mockaroo.com/listing?key=63304c70");
        JSONArray locations = Parser.readJsonFromUrl("https://my.api.mockaroo.com/location?key=63304c70");
        JSONArray listingStats = Parser.readJsonFromUrl("https://my.api.mockaroo.com/listingStatus?key=63304c70");
        JSONArray marketplaces = Parser.readJsonFromUrl("https://my.api.mockaroo.com/marketplace?key=63304c70");

        listingValidated = wobTest.validator.validateListings(listingRaw);

        wobTest.database.uploadLocations(locations);
        wobTest.database.uploadListingStats(listingStats);
        wobTest.database.uploadMarketplaces(marketplaces);
        wobTest.database.uploadListings(listingValidated);
    }
}
