package ru.romanbrazhnikov.agilescraper.resultsaver;

import io.reactivex.Completable;
import ru.romanbrazhnikov.commonparsers.ParseResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MySQLSaver implements ICommonSaver {

    private final String mTableName;
    private ArrayList<String> mFields;
    private final Connection mConnection;


    public MySQLSaver(String tableName, Connection connection) {
        mTableName = tableName;
        mConnection = connection;

    }


    @Override
    public void setFields(Set<String> fields) {
        mFields = new ArrayList<>(fields);
    }

    @Override
    public Completable save(ParseResult parseResult) {
        return Completable.create(emitter -> {
            if (mConnection == null) {
                emitter.onError(new NullPointerException("mConnection is null"));
            }
            if (mConnection.isClosed()) {
                emitter.onError(new Exception("mConnection is closed"));
            }
            try {
                mConnection.setAutoCommit(false);
            } catch (SQLException e) {
                e.printStackTrace();
                emitter.onError(e);
            }
            String sql_insert = generateSqlInsert();
            PreparedStatement insertStatement = mConnection.prepareStatement(sql_insert);
            List<Map<String, String>> resultToSave = parseResult.getResult();
            for (Map<String, String> currentRow : resultToSave) {
                // clearing
                for(int i = 1; i <= mFields.size(); i++){
                    insertStatement.setString(i, "");
                }
                // setting
                for (Map.Entry<String, String> curValue : currentRow.entrySet()) {
                    insertStatement.setString(
                            mFields.indexOf(curValue.getKey()) + 1,
                            curValue.getValue().replaceAll("\\s+", " "));
                }
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
            mConnection.commit();
            insertStatement.close();
        });
    }

    private String generateSqlInsert() {
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append("`").append(mTableName).append("`");
        builder.append(" (");
        for (int i = 0; i < mFields.size(); i++) {
            builder.append("`").append(mFields.get(i)).append("`").append(",");
        }
        builder.setLength(builder.length() - 1);
        builder.append(")VALUES(");
        for (int i = 0; i < mFields.size(); i++) {
            builder.append("?").append(",");
        }
        builder.setLength(builder.length() - 1);
        builder.append(")");

        return builder.toString();
    }
}
