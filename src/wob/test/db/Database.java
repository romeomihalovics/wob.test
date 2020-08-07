package wob.test.db;

import org.json.JSONArray;
import org.json.JSONObject;
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
        preparedStatements.put("emptyMarketplace", new PreparedQuery("TRUNCATE TABLE `marketplace`;"));

        preparedStatements.put("foreignKeyChecksOn", new PreparedQuery("SET FOREIGN_KEY_CHECKS = 1;"));
        preparedStatements.put("foreignKeyChecksOff", new PreparedQuery("SET FOREIGN_KEY_CHECKS = 0;"));

        preparedStatements.put("uploadLocation", new PreparedQuery("INSERT INTO `location` (`id`, `manager_name`, `phone`, `address_primary`, `address_secondary`, `country`, `town`, `postal_code`) VALUES (?, ?, ?, ?, ?, ?, ?, ?);"));
        preparedStatements.put("uploadListing", new PreparedQuery("INSERT INTO `listing` (`id`, `title`, `description`, `inventory_id`, `listing_price`, `currency`, `quantity`, `listing_status`, `marketplace`, `upload_time`, `owner_email`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"));
        preparedStatements.put("uploadListingStatus", new PreparedQuery("INSERT INTO `listing_status` (`id`, `status_name`) VALUES (?, ?);"));
        preparedStatements.put("uploadMarketplace", new PreparedQuery("INSERT INTO `marketplace` (`id`, `marketplace_name`) VALUES (?, ?);"));

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

    public void uploadListings(HashMap<String, ArrayList<JSONObject>> data) throws SQLException, ParseException {
        runQuery("foreignKeyChecksOff");
        runQuery("emptyListings");
        runQuery("foreignKeyChecksOn");
        DateFormat tempFormat = new SimpleDateFormat("M/d/yyyy");
        for(JSONObject obj : data.get("valid")) {
            Date tempDate = tempFormat.parse(obj.get("upload_time").toString());
            PreparedStatement preparedStatement = preparedStatements.get("uploadListing").getPreparedStatement();
            preparedStatement.setString(1, obj.getString("id"));
            preparedStatement.setString(2, obj.getString("title"));
            preparedStatement.setString(3, obj.getString("description"));
            preparedStatement.setString(4, obj.getString("location_id"));
            preparedStatement.setDouble(5, obj.getDouble("listing_price"));
            preparedStatement.setString(6, obj.getString("currency"));
            preparedStatement.setInt(7, obj.getInt("quantity"));
            preparedStatement.setInt(8, obj.getInt("listing_status"));
            preparedStatement.setInt(9, obj.getInt("marketplace"));
            preparedStatement.setDate(10, new java.sql.Date((tempDate).getTime()));
            preparedStatement.setString(11, obj.getString("owner_email_address"));
            runQuery("uploadListing");
        }
    }

    public void uploadLocations(JSONArray data) throws SQLException {
        runQuery("foreignKeyChecksOff");
        runQuery("emptyLocations");
        runQuery("foreignKeyChecksOn");
        for(int i = 0; i < data.length(); i++) {
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

    public void uploadListingStats(JSONArray data) throws SQLException {
        runQuery("foreignKeyChecksOff");
        runQuery("emptyListingStats");
        runQuery("foreignKeyChecksOn");
        for(int i = 0; i < data.length(); i++) {
            PreparedStatement preparedStatement = preparedStatements.get("uploadListingStatus").getPreparedStatement();
            preparedStatement.setInt(1, data.getJSONObject(i).getInt("id"));
            preparedStatement.setString(2, data.getJSONObject(i).getString("status_name"));
            runQuery("uploadListingStatus");
        }
    }

    public void uploadMarketplaces(JSONArray data) throws SQLException {
        runQuery("foreignKeyChecksOff");
        runQuery("emptyMarketplaces");
        runQuery("foreignKeyChecksOn");
        for(int i = 0; i < data.length(); i++) {
            PreparedStatement preparedStatement = preparedStatements.get("uploadMarketplace").getPreparedStatement();
            preparedStatement.setInt(1, data.getJSONObject(i).getInt("id"));
            preparedStatement.setString(2, data.getJSONObject(i).getString("marketplace_name"));
            runQuery("uploadMarketplace");
        }
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
                rs = preparedQuery.getPreparedStatement().executeQuery();

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
