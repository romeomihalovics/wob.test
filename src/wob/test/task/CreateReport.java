package wob.test.task;

import org.json.JSONArray;
import org.json.JSONObject;
import wob.test.WobTest;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class CreateReport {

    public void reportResults() throws SQLException, IOException {
        System.out.print("\r Parsing Report JSON");

        JSONObject finalResult = new JSONObject();
        JSONArray monthlyResult = new JSONArray();

        List<Object> total_query_results = WobTest.database.getTotalListings(0,0);
        List<Object> ebay_query_results = WobTest.database.getTotalEbayListings(0,0);
        List<Object> amazon_query_results = WobTest.database.getTotalAmazonListings(0,0);
        List<Object> best_query_results = WobTest.database.getBestLister(0,0);

        Date oldest_listing = (Date) WobTest.database.runQuery("getOldestListing").get(0);
        Date newest_listing = (Date) WobTest.database.runQuery("getNewestListing").get(0);

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        Calendar firstListing = Calendar.getInstance();
        Calendar lastListing = Calendar.getInstance();

        finalResult.put("total_listings", total_query_results.get(0));

        finalResult.put("total_ebay_listings", ebay_query_results.get(0));
        finalResult.put("total_ebay_listing_price", ebay_query_results.get(1));
        finalResult.put("avg_ebay_listing_price", ebay_query_results.get(2));

        finalResult.put("total_amazon_listings", amazon_query_results.get(0));
        finalResult.put("total_amazon_listing_price", amazon_query_results.get(1));
        finalResult.put("avg_amazon_listing_price", amazon_query_results.get(2));

        finalResult.put("best_lister", best_query_results.get(0) + " ("+best_query_results.get(1)+" listing)");

        try {
            firstListing.setTime(formatter.parse(oldest_listing.toString()));
            lastListing.setTime(formatter.parse(newest_listing.toString()));
        } catch (ParseException e) {
            System.out.println("Error: "+e);
        }

        int year;
        int month;

        JSONObject tmpObject;

        List<Object> tmp_total_query_results;
        List<Object> tmp_ebay_query_results;
        List<Object> tmp_amazon_query_results;
        List<Object> tmp_best_query_results;

        while(firstListing.before(lastListing)) {
            year = firstListing.get(Calendar.YEAR);
            month = firstListing.get(Calendar.MONTH);

            tmp_total_query_results = WobTest.database.getTotalListings(year, month);
            tmp_ebay_query_results = WobTest.database.getTotalEbayListings(year, month);
            tmp_amazon_query_results = WobTest.database.getTotalAmazonListings(year, month);
            tmp_best_query_results = WobTest.database.getBestLister(year, month);

            tmpObject = new JSONObject();

            tmpObject.put("year_month", year+"/"+month);

            tmpObject.put("total_listings", tmp_total_query_results.get(0));

            tmpObject.put("total_ebay_listings", tmp_ebay_query_results.get(0));
            tmpObject.put("total_ebay_listing_price", tmp_ebay_query_results.get(1));
            tmpObject.put("avg_ebay_listing_price", tmp_ebay_query_results.get(2));

            tmpObject.put("total_amazon_listings", tmp_amazon_query_results.get(0));
            tmpObject.put("total_amazon_listing_price", tmp_amazon_query_results.get(1));
            tmpObject.put("avg_amazon_listing_price", tmp_amazon_query_results.get(2));

            if (tmpObject.getInt("total_listings") > 0) {
                tmpObject.put("best_lister", tmp_best_query_results.get(0) + " (" + tmp_best_query_results.get(1) + " listing)");
            } else {
                tmpObject.put("best_lister", "unknown (0 listing)");
            }

            monthlyResult.put(tmpObject);

            firstListing.add(Calendar.MONTH, 1);
        }
        finalResult.put("monthly_results", monthlyResult);
        WobTest.resultReport.resultReporter(finalResult);
    }
}
