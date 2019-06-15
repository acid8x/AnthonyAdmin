package com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector;


import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.Connection;

public class UnsupportedMySQLServerException extends Exception
{
    public UnsupportedMySQLServerException(Connection connection)
    {
        super("MySQL Server version " + connection.getMajorVersion() + "."
                + connection.getMinorVersion() + "." + connection.getSubMinorVersion() + " is not currently supported");
    }

    public UnsupportedMySQLServerException(Connection connection, String message)
    {
        super(message + " MySQL Server Version " + connection.getMajorVersion() + "." + connection.getMinorVersion() + "." + connection.getSubMinorVersion());
    }
}
