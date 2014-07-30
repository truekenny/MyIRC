package me.truekenny.MyIRC;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
     * IRC хост
     */
    private static String host;

    /**
     * Game хост
     */
    private static String gameHost;

    /**
     * Порт сервера для IRC клиентов
     */
    private static int port;

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
    public static MyIRC plugin;

    /**
     * Имя канала для общения
     */
    public static String channel;

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

            plugin.getServer().broadcastMessage(ChatColor.DARK_RED + "[irc] " + ChatColor.RESET + "<" + nick + "> " + msg);
        }
    }

    /**
     * Реализация команды WHO
     *
     * @param c
     * @param nick
     */
    public synchronized void who(ClientConnection c, String nick) {
        Enumeration<String> e = idcon.keys();
        while (e.hasMoreElements()) {
            String id = e.nextElement();
            ClientConnection con = idcon.get(id);
            if (nick.toLowerCase().equals(con.getNick().toLowerCase()) || nick.toLowerCase().equals(channel.toLowerCase())) {
                c.write(":" + host + " 352 " + c.getNick() + " " + channel + " " + con.getId() + " " + con.getHost() + " irc.server " + con.getNick() + " H :0 NOREALNAME");
            }
        }

        for (Player player : plugin.getOnlinePlayers()) {
            if (nick.toLowerCase().equals(player.getName().toLowerCase()) || nick.toLowerCase().equals(channel.toLowerCase())) {
                c.write(":" + host + " 352 " + c.getNick() + " " + channel + " ingame " + plugin.host(player.getAddress().getHostName()) + " game.server " + player.getName() + " H+ :0 NOREALNAME");
            }
        }

        c.write(":" + host + " 315 " + c.getNick() + " " + channel + " :End of /WHO list.");
    }

    /**
     * Реализация команды WHOIS
     *
     * @param c
     * @param nick
     */
    public synchronized void whois(ClientConnection c, String nick) {
        Enumeration<String> e = idcon.keys();
        while (e.hasMoreElements()) {
            String id = e.nextElement();
            ClientConnection con = idcon.get(id);
            if (nick.toLowerCase().equals(con.getNick().toLowerCase())) {
                c.write(":" + host + " 311 " + c.getNick() + " " + con.getNick() + " " + con.getId() + " " + con.getHost() + " * :NOREALNAME");
                c.write(":" + host + " 319 " + c.getNick() + " " + con.getNick() + " :" + channel);
                c.write(":" + host + " 312 " + c.getNick() + " " + con.getNick() + " " + host + " :NOSERVERDESCRIPTION");
                c.write(":" + host + " 317 " + c.getNick() + " " + con.getNick() + " 0 1234567890 :seconds idle, signon time");
                c.write(":" + host + " 703 " + c.getNick() + " " + con.getNick() + " UTF-8 :translation scheme");
                c.write(":" + host + " 318 " + c.getNick() + " " + con.getNick() + " :End of /WHOIS list.");
            }
        }

        for (Player player : plugin.getOnlinePlayers()) {
            if (nick.toLowerCase().equals(player.getName().toLowerCase()) || nick.toLowerCase().equals(channel.toLowerCase())) {
                c.write(":" + host + " 311 " + c.getNick() + " " + player.getName() + " ingame " + plugin.host(player.getAddress().getHostName()) + " * :NOREALNAME");
                c.write(":" + host + " 319 " + c.getNick() + " " + player.getName() + " :+" + channel);
                c.write(":" + host + " 312 " + c.getNick() + " " + player.getName() + " " + gameHost + " :NOSERVERDESCRIPTION");
                c.write(":" + host + " 317 " + c.getNick() + " " + player.getName() + " 0 1234567890 :seconds idle, signon time");
                c.write(":" + host + " 703 " + c.getNick() + " " + player.getName() + " UTF-8 :translation scheme");
                c.write(":" + host + " 318 " + c.getNick() + " " + player.getName() + " :End of /WHOIS list.");
            }
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
        plugin = irc;

        channel = plugin.config.getString("irc.channel");
        gameHost = plugin.config.getString("irc.gameHost");
        host = plugin.config.getString("irc.host");
        port = plugin.config.getInt("irc.port");

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