package com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector;

import java.io.IOException;

public interface IConnectionInterface
{
    void actionCompleted();
    void handleInvalidSQLPacketException(InvalidSQLPacketException ex);
    void handleMySQLException(MySQLException ex);
    void handleIOException(IOException ex);
    void handleMySQLConnException(MySQLConnException ex);
    void handleException(Exception exception);
}
