package wob.test.db;

import org.json.JSONArray;
import wob.test.wobTest;

import java.io.FileReader;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class Database {
    private Connection connection;
    private static Map<String, PreparedQuery> preparedStatements = new HashMap<>();

    public Database() {
        preparedStatements.put("emptyListings", new PreparedQuery("TRUNCATE TABLE `listing`;"));
        preparedStatements.put("emptyLocations", new PreparedQuery("TRUNCATE TABLE `location`;"));
        preparedStatements.put("emptyListingStats", new PreparedQuery("TRUNCATE TABLE `listing_status`;"));
        preparedStatements.put("emptyMarketplaces", new PreparedQuery("TRUNCATE TABLE `marketplace`;"));

        preparedStatements.put("foreignKeyChecksOn", new PreparedQuery("SET FOREIGN_KEY_CHECKS = 1;"));
        preparedStatements.put("foreignKeyChecksOff", new PreparedQuery("SET FOREIGN_KEY_CHECKS = 0;"));

        preparedStatements.put("uploadLocation", new PreparedQuery("INSERT INTO `location` (`id`, `manager_name`, `phone`, `address_primary`, `address_secondary`, `country`, `town`, `postal_code`) VALUES (?, ?, ?, ?, ?, ?, ?, ?);"));
        preparedStatements.put("uploadListing", new PreparedQuery("INSERT INTO `listing` (`id`, `title`, `description`, `inventory_id`, `listing_price`, `currency`, `quantity`, `listing_status`, `marketplace`, `upload_time`, `owner_email`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"));
        preparedStatements.put("uploadListingStatus", new PreparedQuery("INSERT INTO `listing_status` (`id`, `status_name`) VALUES (?, ?);"));
        preparedStatements.put("uploadMarketplace", new PreparedQuery("INSERT INTO `marketplace` (`id`, `marketplace_name`) VALUES (?, ?);"));

        preparedStatements.put("getTotalListing", new PreparedQuery("SELECT count(id) AS 'total_listings' FROM `listing`;"));
        preparedStatements.put("getEbayReport", new PreparedQuery("SELECT count(m.id) AS 'total_ebay_listings', ROUND(sum(l.listing_price), 2) AS 'total_ebay_listing_price', ROUND(avg(l.listing_price), 2) AS 'avg_ebay_listing_price' FROM `listing` AS l INNER JOIN `marketplace` AS m ON l.marketplace = m.id WHERE m.marketplace_name = 'Ebay';"));
        preparedStatements.put("getAmazonReport", new PreparedQuery("SELECT count(m.id) AS 'total_amazon_listings', ROUND(sum(l.listing_price), 2) AS 'total_amazon_listing_price', ROUND(avg(l.listing_price), 2) AS 'avg_amazon_listing_price' FROM `listing` AS l INNER JOIN `marketplace` AS m ON l.marketplace = m.id WHERE m.marketplace_name = 'Amazon';"));
        preparedStatements.put("getBestLister", new PreparedQuery("SELECT `owner_email` AS best_lister, count(`owner_email`) AS listings FROM `listing` GROUP BY `owner_email` ORDER BY listings DESC LIMIT 1;"));

        preparedStatements.put("getTotalListingByMonth", new PreparedQuery("SELECT count(id) AS 'total_listings' FROM `listing` WHERE year(upload_time) = ? AND month(upload_time) = ?;"));
        preparedStatements.put("getEbayReportByMonth", new PreparedQuery("SELECT count(m.id) AS 'total_ebay_listings', ROUND(sum(l.listing_price), 2) AS 'total_ebay_listing_price', ROUND(avg(l.listing_price), 2) AS 'avg_ebay_listing_price' FROM `listing` AS l INNER JOIN `marketplace` AS m ON l.marketplace = m.id WHERE m.marketplace_name = 'Ebay' AND year(l.upload_time) = ? AND month(l.upload_time) = ?;"));
        preparedStatements.put("getAmazonReportByMonth", new PreparedQuery("SELECT count(m.id) AS 'total_amazon_listings', ROUND(sum(l.listing_price), 2) AS 'total_amazon_listing_price', ROUND(avg(l.listing_price), 2) AS 'avg_amazon_listing_price' FROM `listing` AS l INNER JOIN `marketplace` AS m ON l.marketplace = m.id WHERE m.marketplace_name = 'Amazon' AND year(l.upload_time) = ? AND month(l.upload_time) = ?;"));
        preparedStatements.put("getBestListerByMonth", new PreparedQuery("SELECT `owner_email` AS best_lister, count(`owner_email`) AS listings FROM `listing` WHERE year(upload_time) = ? AND month(upload_time) = ? GROUP BY `owner_email` ORDER BY listings DESC LIMIT 1;"));


        preparedStatements.put("getOldestListing", new PreparedQuery("SELECT upload_time AS oldest_listing FROM listing ORDER BY upload_time ASC LIMIT 1"));
        preparedStatements.put("getNewestListing", new PreparedQuery("SELECT upload_time AS oldest_listing FROM listing ORDER BY upload_time DESC LIMIT 1"));


        init();

        for (Map.Entry<String, PreparedQuery> tmpMap : preparedStatements.entrySet()) {
            PreparedQuery preparedQuery = preparedStatements.get(tmpMap.getKey());
            String queryString = preparedQuery.getPreparedQuery();
            try {
                preparedQuery.setPreparedStatement(connection.prepareStatement(queryString));
            } catch (SQLException e) {
                System.out.println("Error: " + e);
            }
        }
    }

    private void init() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Properties dbconfig = new Properties();
            dbconfig.load(new FileReader(wobTest.dbconfig));

            connection = DriverManager.getConnection(dbconfig.getProperty("url"), dbconfig);
        }catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    public void uploadListings(HashMap<String, JSONArray> data) throws SQLException, ParseException, InterruptedException {
        runQuery("foreignKeyChecksOff");
        runQuery("emptyListings");
        DateFormat tempFormat = new SimpleDateFormat("M/d/yyyy");
        for(int i = 0; i < data.get("valid").length(); i++) {
            System.out.printf("\r Uploading Listings: %.2f %% ( %d / %d )", (double) i / data.get("valid").length() * 100, i, data.get("valid").length());
            Thread.sleep(20);
            Date tempDate = tempFormat.parse(data.get("valid").getJSONObject(i).get("upload_time").toString());
            PreparedStatement preparedStatement = preparedStatements.get("uploadListing").getPreparedStatement();
            preparedStatement.setString(1, data.get("valid").getJSONObject(i).getString("id"));
            preparedStatement.setString(2, data.get("valid").getJSONObject(i).getString("title"));
            preparedStatement.setString(3, data.get("valid").getJSONObject(i).getString("description"));
            preparedStatement.setString(4, data.get("valid").getJSONObject(i).getString("location_id"));
            preparedStatement.setDouble(5, data.get("valid").getJSONObject(i).getDouble("listing_price"));
            preparedStatement.setString(6, data.get("valid").getJSONObject(i).getString("currency"));
            preparedStatement.setInt(7, data.get("valid").getJSONObject(i).getInt("quantity"));
            preparedStatement.setInt(8, data.get("valid").getJSONObject(i).getInt("listing_status"));
            preparedStatement.setInt(9, data.get("valid").getJSONObject(i).getInt("marketplace"));
            preparedStatement.setDate(10, new java.sql.Date((tempDate).getTime()));
            preparedStatement.setString(11, data.get("valid").getJSONObject(i).getString("owner_email_address"));
            runQuery("uploadListing");
        }
        runQuery("foreignKeyChecksOn");
    }

    public void uploadLocations(JSONArray data) throws SQLException, InterruptedException {
        runQuery("foreignKeyChecksOff");
        runQuery("emptyLocations");
        runQuery("foreignKeyChecksOn");
        for(int i = 0; i < data.length(); i++) {
            System.out.printf("\r Uploading Locations: %.2f %% ( %d / %d )", (double) i / data.length() * 100, i, data.length());
            Thread.sleep(20);
            PreparedStatement preparedStatement = preparedStatements.get("uploadLocation").getPreparedStatement();
            preparedStatement.setString(1, data.getJSONObject(i).getString("id"));
            preparedStatement.setString(2, data.getJSONObject(i).getString("manager_name"));
            preparedStatement.setString(3, data.getJSONObject(i).getString("phone"));
            preparedStatement.setString(4, data.getJSONObject(i).getString("address_primary"));
            preparedStatement.setString(5, data.getJSONObject(i).get("address_secondary").toString());
            preparedStatement.setString(6, data.getJSONObject(i).getString("country"));
            preparedStatement.setString(7, data.getJSONObject(i).getString("town"));
            preparedStatement.setString(8, data.getJSONObject(i).get("postal_code").toString());
            runQuery("uploadLocation");
        }
    }

    public void uploadListingStats(JSONArray data) throws SQLException, InterruptedException {
        runQuery("foreignKeyChecksOff");
        runQuery("emptyListingStats");
        runQuery("foreignKeyChecksOn");
        for(int i = 0; i < data.length(); i++) {
            System.out.printf("\r Uploading Listing Stats: %.2f %% ( %d / %d )", (double) i / data.length() * 100, i, data.length());
            Thread.sleep(20);
            PreparedStatement preparedStatement = preparedStatements.get("uploadListingStatus").getPreparedStatement();
            preparedStatement.setInt(1, data.getJSONObject(i).getInt("id"));
            preparedStatement.setString(2, data.getJSONObject(i).getString("status_name"));
            runQuery("uploadListingStatus");
        }
    }

    public void uploadMarketplaces(JSONArray data) throws SQLException, InterruptedException {
        runQuery("foreignKeyChecksOff");
        runQuery("emptyMarketplaces");
        runQuery("foreignKeyChecksOn");
        for(int i = 0; i < data.length(); i++) {
            System.out.printf("\r Uploading Marketplaces: %.2f %% ( %d / %d )", (double) i / data.length() * 100, i, data.length());
            Thread.sleep(20);
            PreparedStatement preparedStatement = preparedStatements.get("uploadMarketplace").getPreparedStatement();
            preparedStatement.setInt(1, data.getJSONObject(i).getInt("id"));
            preparedStatement.setString(2, data.getJSONObject(i).getString("marketplace_name"));
            runQuery("uploadMarketplace");
        }
    }

    @SuppressWarnings("rawtypes")
    public List<List> getTotalListings(int year, int month) throws SQLException {
        List<List> total_query_results;
        if(year == 0) {
            total_query_results = runQuery("getTotalListing");
        } else {
            PreparedStatement preparedStatement = preparedStatements.get("getTotalListingByMonth").getPreparedStatement();
            preparedStatement.setInt(1, year);
            preparedStatement.setInt(2, month);
            total_query_results = runQuery("getTotalListingByMonth");
        }
        return total_query_results;
    }

    @SuppressWarnings("rawtypes")
    public List<List> getTotalEbayListings(int year, int month) throws SQLException {
        List<List> ebay_query_results;
        if(year == 0) {
            ebay_query_results = runQuery("getEbayReport");
        } else {
            PreparedStatement preparedStatement = preparedStatements.get("getEbayReportByMonth").getPreparedStatement();
            preparedStatement.setInt(1, year);
            preparedStatement.setInt(2, month);
            ebay_query_results = runQuery("getEbayReportByMonth");
        }
        return ebay_query_results;
    }

    @SuppressWarnings("rawtypes")
    public List<List> getTotalAmazonListings(int year, int month) throws SQLException {
        List<List> amazon_query_results;
        if(year == 0) {
            amazon_query_results = runQuery("getAmazonReport");
        } else {
            PreparedStatement preparedStatement = preparedStatements.get("getAmazonReportByMonth").getPreparedStatement();
            preparedStatement.setInt(1, year);
            preparedStatement.setInt(2, month);
            amazon_query_results = runQuery("getAmazonReportByMonth");
        }
        return amazon_query_results;
    }

    @SuppressWarnings("rawtypes")
    public List<List> getBestLister(int year, int month) throws SQLException {
        List<List> best_query_results;
        if(year == 0) {
            best_query_results = runQuery("getBestLister");
        } else {
            PreparedStatement preparedStatement = preparedStatements.get("getBestListerByMonth").getPreparedStatement();
            preparedStatement.setInt(1, year);
            preparedStatement.setInt(2, month);
            best_query_results = runQuery("getBestListerByMonth");
        }
        return best_query_results;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<List> runQuery(String preparedQueryName) {
        PreparedQuery preparedQuery = preparedStatements.get(preparedQueryName);
        String queryString = preparedQuery.getPreparedQuery();

        init();
        List<List> backArray = null;

        try {
            ResultSet rs = null;

            if (queryString.toLowerCase().startsWith("update") || queryString.toLowerCase().startsWith("insert")
                    || queryString.toLowerCase().startsWith("delete") || queryString.toLowerCase().startsWith("truncate"))
                preparedQuery.getPreparedStatement().executeUpdate();
            else
                preparedQuery.getPreparedStatement().execute();
                rs = preparedQuery.getPreparedStatement().getResultSet();

            preparedQuery.getPreparedStatement().clearParameters();

            if (rs != null) {
                ResultSetMetaData metaData = rs.getMetaData();

                backArray = new ArrayList<>();
                for (int i = 0; i < metaData.getColumnCount(); i++) {
                    backArray.add(new ArrayList<ArrayList>());
                }

                while (rs.next()) {
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        backArray.get(i - 1).add(rs.getObject(i));
                    }
                }
                rs.close();
            }
        } catch (SQLException e) {
            System.out.println("Error: "+e);
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                System.out.println("Error:"+e);
            }
        }
        return backArray;
    }
}
