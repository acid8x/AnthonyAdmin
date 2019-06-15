package com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector;

public interface IIntConnectionInterface
{
    void socketDataSent();
    void handleException(MySQLConnException ex);
}
