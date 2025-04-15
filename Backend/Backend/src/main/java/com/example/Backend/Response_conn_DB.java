package com.example.Backend;

public class Response_conn_DB {
    private static String hostNo;
    private static String serverNo;
    private static String databaseName;
    private static String userName;
    private static String password;


    public static String getHostNo() {
        return Response_conn_DB.hostNo;
    }

    public static void setHostNo(String hostNo) {
        Response_conn_DB.hostNo = hostNo;
    }

    public static String getServerNo() {
        return Response_conn_DB.serverNo;
    }

    public static void setServerNo(String serverNo) {
        Response_conn_DB.serverNo = serverNo;
    }

    public static String getDatabaseName() {
        return Response_conn_DB.databaseName;
    }

    public static void setDatabaseName(String databaseName) {
        Response_conn_DB.databaseName = databaseName;
    }

    public static  String getUserName() {
        return Response_conn_DB.userName;
    }

    public static void setUserName(String userName) {
        Response_conn_DB.userName = userName;
    }

    public static String getPassword() {
        return Response_conn_DB.password;
    }

    public static void setPassword(String password) {
        Response_conn_DB.password = password;
    }
}
