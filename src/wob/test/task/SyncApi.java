package wob.test.task;

import org.json.JSONArray;
import wob.test.parser.Parser;
import wob.test.WobTest;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;

public class SyncApi {
    /**
     * Had to make a copy of the api data set cuz i ran out of quota lmao
     *   System.out.print("\r Parsing Data from API");
     *   JSONArray listingRaw = Parser.readJsonFromUrl("https://my.api.mockaroo.com/listing?key=913ff970");
     *   JSONArray locations = Parser.readJsonFromUrl("https://my.api.mockaroo.com/location?key=913ff970");
     *   JSONArray listingStats = Parser.readJsonFromUrl("https://my.api.mockaroo.com/listingStatus?key=913ff970");
     *   JSONArray marketplaces = Parser.readJsonFromUrl("https://my.api.mockaroo.com/marketplace?key=913ff970");
     */
    public void sync() throws IOException, SQLException, ParseException, InterruptedException {

        HashMap<String, JSONArray> listingValidated;


        JSONArray listingRaw = Parser.readJsonFromUrl("https://my.api.mockaroo.com/listing?key=63304c70");
        JSONArray locations = Parser.readJsonFromUrl("https://my.api.mockaroo.com/location?key=63304c70");
        JSONArray listingStats = Parser.readJsonFromUrl("https://my.api.mockaroo.com/listingStatus?key=63304c70");
        JSONArray marketplaces = Parser.readJsonFromUrl("https://my.api.mockaroo.com/marketplace?key=63304c70");

        listingValidated = WobTest.validator.validateListings(listingRaw);

        WobTest.invalidLogger.logInvalid(listingValidated.get("invalid"));

        WobTest.database.uploadLocations(locations);
        WobTest.database.uploadListingStats(listingStats);
        WobTest.database.uploadMarketplaces(marketplaces);
        WobTest.database.uploadListings(listingValidated);
    }
}
