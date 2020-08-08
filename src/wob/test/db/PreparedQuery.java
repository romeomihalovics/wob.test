package wob.test.db;

import java.sql.PreparedStatement;

public class PreparedQuery {
    private final String preparedQuery;
    private PreparedStatement preparedStatement;

    public PreparedQuery(String preparedQuery) {
        this.preparedQuery = preparedQuery;
        preparedStatement = null;
    }

    public String getPreparedQuery() {
        return preparedQuery;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }
}
