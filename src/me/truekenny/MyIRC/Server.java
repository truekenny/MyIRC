package me.truekenny.MyIRC;

import java.net.*;   
import java.io.*;   
import java.util.*;   
   
public class Server implements Runnable {   
    private int port = 6564;   
   
    private Hashtable idcon = new Hashtable();   
   
    private int id = 0;   
   
    static final String CRLF = "\r\n";   
   
    public synchronized void addConnection(Socket s) {   
        ClientConnection con = new ClientConnection(this, s, id);   
        id++;   
    }   
   
    public synchronized void set(String the_id, ClientConnection con) {   
        idcon.remove(the_id);   
        con.setBusy(false);   
        Enumeration e = idcon.keys();   
        while (e.hasMoreElements()) {   
            String id = (String) e.nextElement();   
            ClientConnection other = (ClientConnection) idcon.get(id);   
            if (!other.isBusy())   
                con.write("add " + other + CRLF);   
        }   
        idcon.put(the_id, con);   
        broadcast(the_id, "add " + con);   
    }   
   
    public synchronized void sendto(String dest, String body) {   
        ClientConnection con = (ClientConnection) idcon.get(dest);   
        if (con != null) {   
            con.write(body + CRLF);   
        }   
    }   
   
    private synchronized void broadcast(String exclude, String body) {   
        Enumeration e = idcon.keys();   
        while (e.hasMoreElements()) {   
            String id = (String) e.nextElement();   
            if (!exclude.equals(id)) {   
                ClientConnection con = (ClientConnection) idcon.get(id);   
                con.write(body + CRLF);   
            }   
        }   
    }   
   
    public synchronized void delete(String the_id) {   
        broadcast(the_id, "delete " + the_id);   
    }   
   
    public synchronized void kill(ClientConnection c) {   
        if (idcon.remove(c.getId()) == c) {   
            delete(c.getId());   
        }   
    }   
   
    public void run() {   
        try {   
            ServerSocket acceptSocket = new ServerSocket(port);   
            System.out.println("Server listening on port " + port);   
            while (true) {   
                Socket s = acceptSocket.accept();   
                addConnection(s);   
            }   
        } catch (IOException e) {   
            System.out.println("accept loop IOException: " + e);   
        }   
    }   
   
    public static void main(String args[]) {   
        new Thread(new Server()).start();   
        try {   
            Thread.currentThread().join();   
        } catch (InterruptedException e) {   
        }   
    }   
}   