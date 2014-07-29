package me.truekenny.MyIRC;

import org.bukkit.ChatColor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class Server implements Runnable {
    /**
     * Порт сервера для IRC клиентов
     */
    private int port = 6667;

    /**
     * Набор подключений
     */
    private Hashtable<String, ClientConnection> idcon = new Hashtable<String, ClientConnection>();

    /**
     * Идентификатор следующего подключения
     */
    private int id = 0;

    /**
     * Объект для логирования сообщений плагина
     */
    Logger log = Logger.getLogger("Minecraft");

    /**
     * Экземпляр главного класса плягина
     */
    public static MyIRC myirc;

    /**
     * Имя канала для общения
     */
    public static final String channel = "#minecraft";

    /**
     * Добавляет нового клиента
     *
     * @param s
     */
    public synchronized void addConnection(Socket s) {
        @SuppressWarnings("unused")
        ClientConnection con = new ClientConnection(this, s, id);
        id++;
    }

    /**
     * Рассылает информацию о новом клиенте
     *
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
                con.write("add " + other);
        }
        idcon.put(the_id, con);

        // broadcast(the_id, "add " + con); – Перенесено в join
    }

    /**
     * Отправляет сообщение клиенту
     *
     * @param dest
     * @param body
     */
    public synchronized void sendto(String dest, String body) {
        ClientConnection con = idcon.get(dest);
        if (con != null) {
            con.write(body);
        }
    }

    /**
     * Широковещательная отправка сообщения
     *
     * @param exclude Исключенный клиент
     * @param body    Сообщение
     */
    public synchronized void broadcast(String exclude, String body) {
        Enumeration<String> e = idcon.keys();
        while (e.hasMoreElements()) {
            String id = e.nextElement();
            if (!exclude.equals(id)) {
                ClientConnection con = idcon.get(id);
                con.write(body);
            }
        }
    }

    /**
     * Рассылкает сообщение, что пользователь вышел
     *
     * @param id
     * @param fullNick
     */
    public synchronized void part(String id, String fullNick) {
        broadcast(id, ":" + fullNick + " PART " + channel);
    }

    /**
     * Рассылает сообщение, что пользователь подключился
     *
     * @param id
     * @param fullNick
     */
    public synchronized void join(String id, String fullNick) {
        broadcast(id, ":" + fullNick + " JOIN :" + channel);
    }

    /**
     * Смена режима игрока ingame
     *
     * @param id
     * @param nick
     */
    public synchronized void mode(String id, String nick) {
        broadcast(id, "MODE " + channel + " +v " + nick);
    }

    /**
     * Отправить сообщение другим игрокам
     *
     * @param id
     * @param fullNick
     * @param msg
     */
    public synchronized void privmsg(String id, String fullNick, String msg) {
        broadcast(id, ":" + fullNick + " PRIVMSG " + channel + " :" + msg);

        // Сообщение отправлено
        if (id.equals("-1") == false) {
            StringTokenizer st = new StringTokenizer(fullNick);
            String nick = st.nextToken("!");

            myirc.getServer().broadcastMessage(ChatColor.DARK_RED + "[irc] " + ChatColor.RESET + "<" + nick + "> " + msg);
        }
    }

    /**
     * Выполняет удаление клиента
     *
     * @param c
     */
    public synchronized void kill(ClientConnection c) {
        if (idcon.remove(c.getId()) == c) {
            part(c.getId(), c.getFullName());
        }
    }

    /**
     * Закрывает всех клиентов
     */
    private void killAll() {
        Enumeration<String> e = idcon.keys();
        while (e.hasMoreElements()) {
            String id = e.nextElement();
            ClientConnection con = idcon.get(id);
            con.close();
        }
    }

    /**
     * Позволяет перезагрузить IRC сервер при необходимости
     */
    private ServerSocket _acceptSocket;

    /**
     * Выполняемый в потоке цикл ожидания подключений
     */
    public void run() {
        try {
            ServerSocket acceptSocket = new ServerSocket(port);
            _acceptSocket = acceptSocket;
            log.info("Server listening on port " + port);
            while (true) {
                Socket s = acceptSocket.accept();
                addConnection(s);
            }
        } catch (IOException e) {
            log.info("accept loop IOException: " + e);
        }
    }

    /**
     * Статичный метод для запуска нового сервера
     */
    public static Server Activate(MyIRC irc) {
        myirc = irc;

        Server server = new Server();
        new Thread(server).start();

        return server;
        // Ожидать отключения сервера
        /*
            try {
	            Thread.currentThread().join();   
	        } catch (InterruptedException e) {   
	        } 
        */
    }

    public void Deactivate() {
        try {
            killAll();
            _acceptSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Возвращает список пользователей IRC
     *
     * @return
     */
    public ArrayList<String> userList() {
        ArrayList<String> userList = new ArrayList<String>();
        Enumeration<String> e = idcon.keys();
        while (e.hasMoreElements()) {
            String id = e.nextElement();
            ClientConnection con = idcon.get(id);

            String nick = con.getNick();

            if (nick == null) continue;

            userList.add(nick);
        }

        return userList;
    }

    /**
     * Кикнуть пользователя
     *
     * @param kicked
     */
    public void kick(String kicked) {
        kicked = kicked.toLowerCase();

        Enumeration<String> e = idcon.keys();
        while (e.hasMoreElements()) {
            String id = e.nextElement();
            ClientConnection con = idcon.get(id);

            String nick = con.getNick();

            if (nick == null) continue;

            nick = nick.toLowerCase();

            if (nick.equals(kicked)) {
                con.close();
                break;
            }
        }
    }
}