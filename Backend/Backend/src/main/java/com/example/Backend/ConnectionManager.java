package com.example.Backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionManager {
    public static Connection getConn() throws SQLException {
        String host = Response_conn_DB.getHostNo(); // e.g., "localhost"
        String port = Response_conn_DB.getServerNo(); // e.g., "8123"
        String db = Response_conn_DB.getDatabaseName(); // e.g., "default"
        String user = Response_conn_DB.getUserName(); // e.g., "default"
        String password = Response_conn_DB.getPassword(); // JWT from UI
        String url = String.format("jdbc:clickhouse://%s:%s/%s", host, port, db);

        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password",password);
        props.setProperty("ssl", "false");

        return DriverManager.getConnection(url, props);
    }
}
