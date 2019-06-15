package com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;

public class Statement
{
    Connection mysqlConn;
    int affectedRows = 0;

    public Statement(Connection mysqlConn)
    {
        this.mysqlConn = mysqlConn;
    }

    /**
     * Get the number of rows that were affected by the last query
     * @return
     */
    public int getAffectedRows()
    {
        return this.affectedRows;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void execute(String query, final IConnectionInterface iConnectionInterface)
    {
        try {
            this.mysqlConn.resetPacketSequenceNumber(true);
            COM_Query comQuery = new COM_Query(this.mysqlConn, COM_Query.COM_QUERY, query);
            byte[] data = comQuery.getPacketData().toByteArray();

            SocketSender socketSender = new SocketSender(this.mysqlConn, new IIntConnectionInterface()
            {
                @Override
                public void socketDataSent()
                {
                    try {
                        //Check the packet response
                        if (Helpers.getMySQLPacketType(Statement.this.mysqlConn.getMysqlIO().getSocketByteArray()) == Helpers.MYSQL_PACKET_TYPE.MYSQL_ERROR_PACKET) {
                            try {
                                MySQLErrorPacket mySQLErrorPacket = new MySQLErrorPacket(Statement.this.mysqlConn);
                                throw new MySQLException(mySQLErrorPacket.getErrorMsg(), mySQLErrorPacket.getErrorCode(), mySQLErrorPacket.getSqlState());
                            }
                            catch (final InvalidSQLPacketException e) {
                                if (mysqlConn.getReturnCallbackToMainThread())
                                {
                                    mysqlConn.getActivity().runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            iConnectionInterface.handleInvalidSQLPacketException(e);
                                        }
                                    });
                                }
                                else
                                {
                                    iConnectionInterface.handleInvalidSQLPacketException(e);
                                }
                            }
                            catch (final MySQLException ex) {
                                if (mysqlConn.getReturnCallbackToMainThread())
                                {
                                    mysqlConn.getActivity().runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            iConnectionInterface.handleMySQLException(ex);
                                        }
                                    });
                                }
                                else
                                {
                                    iConnectionInterface.handleMySQLException(ex);
                                }
                            }
                        }
                        else {

                            if (Helpers.getMySQLPacketType(Statement.this.mysqlConn.getMysqlIO().getSocketByteArray()) == Helpers.MYSQL_PACKET_TYPE.MYSQL_OK_PACKET) {
                                try
                                {
                                    MySQLOKPacket mySQLOKPacket = new MySQLOKPacket(Statement.this.mysqlConn);
                                    affectedRows = mySQLOKPacket.getAffectedRows();
                                    Statement.this.mysqlConn.setLastInsertID(mySQLOKPacket.getLastInsertID());
                                    if (mysqlConn.getReturnCallbackToMainThread())
                                    {
                                        mysqlConn.getActivity().runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                iConnectionInterface.actionCompleted();
                                            }
                                        });
                                    }
                                    else
                                    {
                                        iConnectionInterface.actionCompleted();
                                    }
                                }
                                catch (final InvalidSQLPacketException e) {
                                    if (mysqlConn.getReturnCallbackToMainThread())
                                    {
                                        mysqlConn.getActivity().runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                iConnectionInterface.handleInvalidSQLPacketException(e);
                                            }
                                        });
                                    }
                                    else
                                    {
                                        iConnectionInterface.handleInvalidSQLPacketException(e);
                                    }
                                }
                            }
                        }
                    }
                    catch (final IOException ex) {
                        if (mysqlConn.getReturnCallbackToMainThread())
                        {
                            mysqlConn.getActivity().runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    iConnectionInterface.handleIOException(ex);
                                }
                            });
                        }
                        else
                        {
                            iConnectionInterface.handleIOException(ex);
                        }
                    }
                }

                @Override
                public void handleException(final MySQLConnException ex)
                {
                    if (mysqlConn.getReturnCallbackToMainThread())
                    {
                        mysqlConn.getActivity().runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                iConnectionInterface.handleMySQLConnException(ex);
                            }
                        });
                    }
                    else
                    {
                        iConnectionInterface.handleMySQLConnException(ex);
                    }
                }
            });

            socketSender.execute(data);
        }
        catch  (IOException ex)
        {
            iConnectionInterface.handleIOException(ex);
        }
    }

    public void executeQuery(String query, final IResultInterface iResultInterface)
    {
        try
        {
            this.mysqlConn.resetPacketSequenceNumber(true);

            COM_Query comQuery = new COM_Query(this.mysqlConn, COM_Query.COM_QUERY, query);
            byte[] data = comQuery.getPacketData().toByteArray();
            MySQLIO.breakSocketGetData = false;
            SocketSender socketSender = new SocketSender(this.mysqlConn, new IIntConnectionInterface()
            {
                @Override
                public void socketDataSent()
                {
                    Log.d("Statement", "Interface for SocketDataSent called");
                    try {
                        if (Helpers.getMySQLPacketType(Statement.this.mysqlConn.getMysqlIO().getSocketByteArray()) == Helpers.MYSQL_PACKET_TYPE.MYSQL_ERROR_PACKET) {
                            try {
                                MySQLErrorPacket mySQLErrorPacket = new MySQLErrorPacket(Statement.this.mysqlConn);
                                throw new MySQLException(mySQLErrorPacket.getErrorMsg(), mySQLErrorPacket.getErrorCode(), mySQLErrorPacket.getSqlState());
                            }
                            catch (final MySQLException ex)
                            {
                                if (mysqlConn.getReturnCallbackToMainThread())
                                {
                                    mysqlConn.getActivity().runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            iResultInterface.handleMySQLException(ex);
                                        }
                                    });
                                }
                                else
                                {
                                    iResultInterface.handleMySQLException(ex);
                                }
                            }
                            catch (final InvalidSQLPacketException e) {
                                if (mysqlConn.getReturnCallbackToMainThread())
                                {
                                    mysqlConn.getActivity().runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            iResultInterface.handleInvalidSQLPacketException(e);
                                        }
                                    });
                                }
                                else
                                {
                                    iResultInterface.handleInvalidSQLPacketException(e);
                                }
                            }
                        }
                        else {
                            final COM_QueryResponse comQueryResponse = new COM_QueryResponse(Statement.this.mysqlConn);
                            if (mysqlConn.getReturnCallbackToMainThread())
                                mysqlConn.getActivity().runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        iResultInterface.executionComplete(new ResultSet(comQueryResponse.getColumnDefinitions(), comQueryResponse.getRows()));
                                    }
                                });
                            else
                            {
                                iResultInterface.executionComplete(new ResultSet(comQueryResponse.getColumnDefinitions(), comQueryResponse.getRows()));
                            }
                        }
                    }
                    catch (final IOException ex)
                    {
                        if (mysqlConn.getReturnCallbackToMainThread())
                        {
                            mysqlConn.getActivity().runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    iResultInterface.handleIOException(ex);
                                }
                            });
                        }
                        else
                        {
                            iResultInterface.handleIOException(ex);
                        }

                    }
                }

                @Override
                public void handleException(final MySQLConnException ex)
                {
                    if (mysqlConn.getReturnCallbackToMainThread())
                    {
                        mysqlConn.getActivity().runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                iResultInterface.handleMySQLConnException(ex);
                            }
                        });
                    }
                    else
                    {
                        iResultInterface.handleMySQLConnException(ex);
                    }

                }
            });
            socketSender.execute(data);
        }
        catch (IOException ex)
        {
            iResultInterface.handleException(ex);
        }
    }
}
