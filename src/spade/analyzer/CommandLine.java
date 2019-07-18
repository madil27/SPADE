/*
 --------------------------------------------------------------------------------
 SPADE - Support for Provenance Auditing in Distributed Environments.
 Copyright (C) 2018 SRI International

 This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.
 --------------------------------------------------------------------------------
 */
package spade.analyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import spade.core.AbstractAnalyzer;
import spade.core.AbstractQuery;
import spade.core.AbstractStorage;
import spade.core.Kernel;
import spade.query.postgresql.QuickGrailExecutor;
import spade.storage.PostgresExecutor;

import static spade.core.AbstractQuery.getCurrentStorage;

public class CommandLine extends AbstractAnalyzer
{
    private QuickGrailExecutor executor;
    private static Logger logger = Logger.getLogger(CommandLine.class.getName());

    public CommandLine()
    {
        QUERY_PORT = "commandline_query_port";
    }

    private class SocketListener implements Runnable
    {
        private ServerSocket serverSocket;

        public SocketListener(ServerSocket serverSocket)
        {
            this.serverSocket = serverSocket;
        }

        @Override
        public void run()
        {
            try
            {
                while(!Kernel.isShutdown() && !SHUTDOWN)
                {
                    Socket querySocket = serverSocket.accept();
                    QueryConnection thisConnection = new QueryConnection(querySocket);
                    Thread connectionThread = new Thread(thisConnection);
                    connectionThread.start();
                }
            }
            catch(SocketException ex)
            {
                logger.log(Level.INFO, "Stopping socket listener");
            }
            catch(Exception ex)
            {
                logger.log(Level.SEVERE, null, ex);
            }
            finally
            {
                try
                {
                    serverSocket.close();
                }
                catch(Exception ex)
                {
                    logger.log(Level.SEVERE, "Unable to close server socket", ex);
                }
            }
        }
    }

    @Override
    public boolean initialize()
    {
        ServerSocket serverSocket = AbstractAnalyzer.getServerSocket(QUERY_PORT);
        if(serverSocket == null)
        {
            logger.log(Level.SEVERE, "Server Socket not initialized");
            return false;
        }
        PostgresExecutor ps = new PostgresExecutor(null);
        executor = new QuickGrailExecutor(ps);
        new Thread(new SocketListener(serverSocket), "SocketListener-Thread").start();
        return true;
    }

    private class QueryConnection extends AbstractAnalyzer.QueryConnection
    {
        public QueryConnection(Socket socket)
        {
            super(socket);
        }

        @Override
        public void run()
        {
            try
            {
                InputStream inStream = querySocket.getInputStream();
                OutputStream outStream = querySocket.getOutputStream();
                BufferedReader queryInputStream = new BufferedReader(new InputStreamReader(inStream));
                ObjectOutputStream responseOutputStream = new ObjectOutputStream(outStream);

                boolean quitting = false;
                while(!quitting && !SHUTDOWN)
                {
                    quitting = processRequest(queryInputStream, responseOutputStream);
                }

                queryInputStream.close();
                responseOutputStream.close();
                inStream.close();
                outStream.close();
            }
            catch(Exception ex)
            {
                logger.log(Level.SEVERE, null, ex);
            }
            finally
            {
                try
                {
                    querySocket.close();
                }
                catch(Exception ex)
                {
                    logger.log(Level.SEVERE, "Unable to close query socket", ex);
                }
            }
        }

        private boolean processRequest(BufferedReader inputStream,
                                       ObjectOutputStream outputStream) throws IOException
        {
            String query = inputStream.readLine();
            if(query != null && query.trim().toLowerCase().startsWith("set"))
            {
                // set storage for querying
                String output = parseSetStorage(query);
                logger.log(Level.INFO, output);
                outputStream.writeObject(output);
                return false;
            }
            if(getCurrentStorage() == null)
            {
                String msg = "No storage set for querying. " +
                        "Use command: 'set storage <storage_name>'";
                outputStream.writeObject(msg);
                logger.log(Level.SEVERE, msg);
                return false;
            }
            if(query != null && query.toLowerCase().startsWith("export"))
            {
                query = query.substring(6);
            }
            if(query == null || query.trim().equalsIgnoreCase("exit"))
            {
                return true;
            }

            outputStream.writeObject(executor.execute(query));
            return false;
        }

        @Override
        protected boolean parseQuery(String line)
        {
            return true;
        }
    }

    private static String parseSetStorage(String line)
    {
        String output = null;
        try
        {
            String[] tokens = line.split("\\s+");
            String setCommand = tokens[0].toLowerCase().trim();
            String storageCommand = tokens[1].toLowerCase().trim();
            String storageName = tokens[2].toLowerCase().trim();
            if(setCommand.equals("set") && storageCommand.equals("storage"))
            {
                AbstractStorage storage = Kernel.getStorage(storageName);
                if(storage != null)
                {
                    AbstractQuery.setCurrentStorage(storage);
                    output = "Storage '" + storageName + "' successfully set for querying.";
                }
                else
                {
                    output = "Storage '" + tokens[2] + "' not found";
                }
            }
        }
        catch(Exception ex)
        {
            output = "Error setting storage!";
            logger.log(Level.SEVERE, output, ex);
        }

        return output;
    }
}
