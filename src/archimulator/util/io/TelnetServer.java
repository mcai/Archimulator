/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.util.io;

import archimulator.util.Reference;
import archimulator.util.io.appender.OutputAppender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TelnetServer implements OutputAppender {
    private ServerSocket serverSocket;
    private List<SocketThread> socketThreads;

    public TelnetServer() {
        this.socketThreads = new ArrayList<SocketThread>();
        this.start();
    }

    private void start() {
        Thread threadTelnetServer = new Thread() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(SERVER_PORT);

                    System.out.println("Archimulator's Telnet server is listening at port " + serverSocket.getLocalPort() + "\n");

                    for (; ; ) {
                        SocketThread socketThread = new SocketThread(serverSocket.accept());
                        socketThread.setDaemon(true);

                        socketThreads.add(socketThread);

                        socketThread.start();
                    }
                } catch (IOException e) {
                    //            System.out.println(e);
                }
            }
        };
        threadTelnetServer.setDaemon(true);
        threadTelnetServer.start();
    }

    private void welcome(SocketThread socketThread) {
        appendStdOutLine(socketThread, "Archimulator - A Flexible Multicore Architectural Simulator Written in Java.\n");
        appendStdOutLine(socketThread, "Version: 2.0.\n");
        appendStdOutLine(socketThread, "Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).\n");
    }

    public void stop() {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void appendStdOutLine(long currentCycle, String text) {
        for (SocketThread socketThread : this.socketThreads) {
            appendStdOutLine(socketThread, "[" + currentCycle + "] " + text);
        }
    }

    public void appendStdErrLine(long currentCycle, String text) {
        for (SocketThread socketThread : this.socketThreads) {
            appendStdErrLine(socketThread, "[" + currentCycle + "] " + text);
        }
    }

    private void appendStdOutLine(SocketThread socketThread, String text) {
        socketThread.out.print(text + "\r\n");
        socketThread.out.flush();
    }

    private void appendStdErrLine(SocketThread socketThread, String text) {
        socketThread.out.print(text + "\r\n");
        socketThread.out.flush();
    }

    private class SocketThread extends Thread {
        private Socket socket;
        private PrintWriter out;

        public SocketThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                this.out = new PrintWriter(this.socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

                welcome(this);

                final Reference<Boolean> running = new Reference<Boolean>(true);

                String line = "";

                for (; line == null || !line.equals("exit"); ) {
                    line = in.readLine();
                }

                running.set(false);

                this.out.close();
                this.socket.close();
                in.close();

                socketThreads.remove(this);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public static final int SERVER_PORT = 3900;
}