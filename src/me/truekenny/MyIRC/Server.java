package me.truekenny.MyIRC;

import java.net.*;   
import java.io.*;   
import java.util.*;   
   
public class Server implements Runnable {
	/**
	 * Порт сервера для IRC клиентов
	 */
    private int port = 6564;   
   
    /**
     * Набор подключений
     */
    private Hashtable<String, ClientConnection> idcon = new Hashtable<String, ClientConnection>();   
   
    /**
     * Идентификатор следующего подключения
     */
    private int id = 0;   
   
    /**
     * Перенос строки, разделяет строки при отправке клиенту
     */
    static final String CRLF = "\r\n";   
   
    /**
     * Добавляет нового клиента
     * @param s
     */
	public synchronized void addConnection(Socket s) {   
	    @SuppressWarnings("unused")
		ClientConnection con = new ClientConnection(this, s, id);   
        id++;   
    }   
   
	/**
	 * Рассылает информацию о новом клиенте
	 * @param the_id
	 * @param con
	 */
    public synchronized void set(String the_id, ClientConnection con) {   
        idcon.remove(the_id);   
        con.setBusy(false);   
        Enumeration<String> e = idcon.keys();   
        while (e.hasMoreElements()) {   
            String id = e.nextElement();   
            ClientConnection other = idcon.get(id);   
            if (!other.isBusy())   
                con.write("add " + other + CRLF);   
        }   
        idcon.put(the_id, con);   
        broadcast(the_id, "add " + con);   
    }   
   
    /**
     * Отправляет сообщение клиенту
     * @param dest
     * @param body
     */
    public synchronized void sendto(String dest, String body) {   
        ClientConnection con = idcon.get(dest);   
        if (con != null) {   
            con.write(body + CRLF);   
        }   
    }   
   
    /**
     * Широковещательная отправка сообщения
     * @param exclude Исключенный клиент
     * @param body Сообщение
     */
    private synchronized void broadcast(String exclude, String body) {   
        Enumeration<String> e = idcon.keys();   
        while (e.hasMoreElements()) {   
            String id = e.nextElement();   
            if (!exclude.equals(id)) {   
                ClientConnection con = idcon.get(id);   
                con.write(body + CRLF);   
            }   
        }   
    }   
   
    /**
     * 
     * @param the_id
     */
    public synchronized void delete(String the_id) {   
        broadcast(the_id, "delete " + the_id);   
    }   
   
    /**
     * Выполняет удаление клиента
     * @param c
     */
    public synchronized void kill(ClientConnection c) {   
        if (idcon.remove(c.getId()) == c) {   
            delete(c.getId());   
        }   
    }   
   
    /**
     * Выполняемый в потоке цикл ожидания подключений 
     */
    public void run() {   
        try {   
            @SuppressWarnings("resource")
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
   
    /**
     * Статичный метод для запуска нового сервера
     * @param args
     */
    public static void main(String args[]) {   
        new Thread(new Server()).start();   
        try {   
            Thread.currentThread().join();   
        } catch (InterruptedException e) {   
        }   
    }   
}   