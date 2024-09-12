package com.oopModel.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static com.db.DBDetails.*;

public class DBaseConnection {
    private final String DBASE_ADDRESS;
    private final String DBASE_USERID;
    private final String DBASE_PWORD;

    public DBaseConnection() {
        DBASE_ADDRESS = getDbaseAddress();
        DBASE_USERID = getDbaseUserid();
        DBASE_PWORD = getDbasePword();
    }

    //This code connects to the database and returns statement, which can be used to make queries to update or retrieve data.
    public Statement connectToDBase() {
        Statement statement = null;
        try {
            Connection databaseConnection = DriverManager.getConnection(DBASE_ADDRESS, DBASE_USERID, DBASE_PWORD);
            statement = databaseConnection.createStatement();
        } catch (SQLException e) {e.printStackTrace();}
        return statement;
    }

    //This code calls connectToDbase() to get statement, which is then used to update the database with the SQL string query.
    public void queryDbase(String query) {
        try {
            Statement statement = connectToDBase();
            System.out.println(query);
            statement.executeUpdate(query);
        } catch (SQLException e) {e.printStackTrace();}
    }

    //This procedure formats the contents of columns,
    //data, table and id into an UPDATE query, which it then passes to queryDBase() to execute the query.
    public void updateRecord(String table, String[] columns, String[] data, boolean[] typeIsString, String id) {
        String query = "UPDATE "+table+" SET ";
        boolean first = true;
        for (int index=0; index<columns.length; index++) {
            String columnName = columns[index];
            String dataItem = data[index];
            if (typeIsString[index]) {
                dataItem = "'"+dataItem+"'";
            }
            if (first) {
                query += columnName + " = " + dataItem;
                first = false;
            } else {
                query += ", " + columnName + " = " + dataItem;
            }
        }
        query += " WHERE ";
        switch (table) {
            case "patient":
                query += "idpatient";
                break;
            case "adminaccount":
                query += "id_admin";
                break;
            case "location":
                query += "idlocation";
                break;
            case "route":
                query+= "idroute";
                break;
            case "nurse_account":
                query += "nurse_id";
                break;
            default:
                query += "idnurse";
        }
        query += " = '" + id + "'";
        queryDbase(query);
    }

    //The procedure below formats the data in table and values into an INSERT query, which it then passes to queryDb().
    public void insertRecord(String table, String[] values, boolean[] typeIsString) {
        String query = "INSERT INTO " +table+ " VALUES (";
        boolean first = true;
        for (int index = 0; index<values.length; index++) {
            String value = values[index];
            if (typeIsString[index]) {
                value = "'"+value+"'";
            }
            if (first) {
                query += value;
                first = false;
            } else {
                query += ", "+value;
            }
        }
        query += ")";
        queryDbase(query);
    }

    //The procedure below performs the same function as insertRecord(), but instead formats the data in its
    //parameters into an INSERT query which only inserts data into selected columns in a table in the database,
    //for example, in nurse where fields route and orderedshifts can be null.
    public void insertNotAllColumns(String table, String[] columns, String[]values, boolean[]typeIsString) {
        String query = "INSERT INTO " +table+ " (" + columns[0];
        for (int columnIndex=1;columnIndex<columns.length;columnIndex++) {
            query += ", " + columns[columnIndex];
        }
        query += ") VALUES (";
        boolean first = true;
        for (int index = 0; index<values.length; index++) {
            String value = values[index];
            if (typeIsString[index]) {
                value = "'"+value+"'";
            }
            if (first) {
                query += value;
                first = false;
            } else {
                query += ", "+value;
            }
        }
        query += ")";
        queryDbase(query);
    }
    //This method formats a DELETE query to delete the record with primary key id in table,
    //passing the query as a String to queryDb() to execute.
    public void deleteRecord(String table, String idFieldName, String id) {
        String query = "DELETE FROM " + table + " WHERE " + idFieldName + " = '" + id + "'";
        queryDbase(query);
    }
}
