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

public class IRCServer implements Runnable {

    /**
     * IRC хост
     */
    public static String host;

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
    private Hashtable<String, IRCClient> clients = new Hashtable<String, IRCClient>();

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
    public static MyIRC myIRC;

    /**
     * Имя канала для общения
     */
    public static String channel;

    /**
     * Время создание канала
     */
    public long createTime = System.currentTimeMillis() / 1000L;

    /**
     * Создатель канала channel
     */
    public String creator = myIRC.config.getString("irc.creator");

    /**
     * Топик канала
     */
    public String topic = myIRC.config.getString("irc.topic");

    /**
     * Сообщение kick
     */
    public String kickMessage = myIRC.config.getString("messages.irc.kickOnSameNick");

    /**
     * Добавляет нового клиента
     *
     * @param socket Подключение клиента
     */
    public synchronized void addConnection(Socket socket) {
        @SuppressWarnings("unused")
        IRCClient con = new IRCClient(this, socket, id);
        id++;
    }

    /**
     * Рассылает информацию о новом клиенте
     *
     * @param id     Идентификатор клиента
     * @param client Клиент
     */
    public synchronized void set(String id, IRCClient client) {
        clients.remove(id);
        client.setBusy(false);
        Enumeration<String> e = clients.keys();
        while (e.hasMoreElements()) {
            String _id = e.nextElement();
            IRCClient other = clients.get(_id);
            if (!other.isBusy())
                client.write("add " + other);
        }
        clients.put(id, client);

        // broadcast(the_id, "add " + con); – Перенесено в join
    }

    /**
     * Отправляет сообщение клиенту
     *
     * @param id      Идентификатор клиента
     * @param message Сообщение
     */
    public synchronized void sendTo(String id, String message) {
        IRCClient client = clients.get(id);
        if (client != null) {
            client.write(message);
        }
    }

    /**
     * Широковещательная отправка сообщения
     *
     * @param exclude Исключенный клиент
     * @param body    Сообщение
     */
    public synchronized void broadcast(String exclude, String body) {
        Enumeration<String> e = clients.keys();
        while (e.hasMoreElements()) {
            String id = e.nextElement();
            if (!exclude.equals(id)) {
                IRCClient con = clients.get(id);
                con.write(body);
            }
        }
    }

    /**
     * Рассылкает сообщение, что пользователь вышел
     *
     * @param id       Исключенный клиент
     * @param fullNick Полное имя покидающего клиента
     * @param reason   Сообщение выхода
     */
    public synchronized void part(String id, String fullNick, String reason) {
        broadcast(id, ":" + fullNick + " PART " + channel + " :" + reason);
    }

    /**
     * Рассылает сообщение, что пользователь подключился
     *
     * @param id       Исключенный клиент
     * @param fullNick Полное имя входящего клиента
     */
    public synchronized void join(String id, String fullNick) {
        broadcast(id, ":" + fullNick + " JOIN :" + channel);
    }

    /**
     * Смена режима игрока ingame
     *
     * @param id   Исключенный клиент
     * @param nick Имя игрока
     */
    public synchronized void mode(String id, String nick) {
        broadcast(id, "MODE " + channel + " +v " + nick);
    }

    /**
     * Отправить сообщение другим игрокам
     *
     * @param id       Исключенный клиент
     * @param fullNick Полное имя источника
     * @param message  Сообщение
     */
    public synchronized void privmsg(String id, String fullNick, String message) {
        broadcast(id, ":" + fullNick + " PRIVMSG " + channel + " :" + message);

        // Сообщение отправлено
        if (!id.equals("-1")) {
            StringTokenizer st = new StringTokenizer(fullNick);
            String nick = st.nextToken("!");

            myIRC.getServer().broadcastMessage(ChatColor.DARK_RED + "[irc] " + ChatColor.RESET + "<" + nick + "> " + message);
        }
    }

    /**
     * Реализация команды WHO
     *
     * @param client Клиент, потребовавший who
     * @param nick   Источник информации who
     */
    public synchronized void who(IRCClient client, String nick) {
        Enumeration<String> e = clients.keys();
        while (e.hasMoreElements()) {
            String id = e.nextElement();
            IRCClient con = clients.get(id);
            if (nick.toLowerCase().equals(con.getNick().toLowerCase()) || nick.toLowerCase().equals(channel.toLowerCase())) {
                client.write(":" + host + " 352 " + client.getNick() + " " + channel + " " + con.getId() + " " + con.getHost() + " irc.server " + con.getNick() + " H :0 NOREALNAME");
            }
        }

        for (Player player : MyIRC.getOnlinePlayers()) {
            if (nick.toLowerCase().equals(player.getName().toLowerCase()) || nick.toLowerCase().equals(channel.toLowerCase())) {
                client.write(":" + host + " 352 " + client.getNick() + " " + channel + " ingame " + myIRC.host(player.getAddress().getHostName()) + " game.server " + player.getName() + " H+ :0 NOREALNAME");
            }
        }

        client.write(":" + host + " 315 " + client.getNick() + " " + channel + " :End of /WHO list.");
    }

    /**
     * Реализация команды WHOIS
     *
     * @param client Клиент, потребовавший whois
     * @param nick   Источник информации whois
     */
    public synchronized void whois(IRCClient client, String nick) {
        Enumeration<String> e = clients.keys();
        while (e.hasMoreElements()) {
            String id = e.nextElement();
            IRCClient con = clients.get(id);
            if (nick.toLowerCase().equals(con.getNick().toLowerCase())) {
                client.write(":" + host + " 311 " + client.getNick() + " " + con.getNick() + " " + con.getId() + " " + con.getHost() + " * :NOREALNAME");
                client.write(":" + host + " 319 " + client.getNick() + " " + con.getNick() + " :" + channel);
                client.write(":" + host + " 312 " + client.getNick() + " " + con.getNick() + " " + host + " :NOSERVERDESCRIPTION");
                client.write(":" + host + " 317 " + client.getNick() + " " + con.getNick() + " 0 1234567890 :seconds idle, signon time");
                client.write(":" + host + " 703 " + client.getNick() + " " + con.getNick() + " UTF-8 :translation scheme");
                client.write(":" + host + " 318 " + client.getNick() + " " + con.getNick() + " :End of /WHOIS list.");
            }
        }

        for (Player player : MyIRC.getOnlinePlayers()) {
            if (nick.toLowerCase().equals(player.getName().toLowerCase()) || nick.toLowerCase().equals(channel.toLowerCase())) {
                client.write(":" + host + " 311 " + client.getNick() + " " + player.getName() + " ingame " + myIRC.host(player.getAddress().getHostName()) + " * :NOREALNAME");
                client.write(":" + host + " 319 " + client.getNick() + " " + player.getName() + " :+" + channel);
                client.write(":" + host + " 312 " + client.getNick() + " " + player.getName() + " " + gameHost + " :NOSERVERDESCRIPTION");
                client.write(":" + host + " 317 " + client.getNick() + " " + player.getName() + " 0 1234567890 :seconds idle, signon time");
                client.write(":" + host + " 703 " + client.getNick() + " " + player.getName() + " UTF-8 :translation scheme");
                client.write(":" + host + " 318 " + client.getNick() + " " + player.getName() + " :End of /WHOIS list.");
            }
        }
    }

    /**
     * Выполняет удаление клиента
     *
     * @param client Клиент
     * @param reason Сообщение выхода
     */
    public synchronized void kill(IRCClient client, String reason) {
        if (clients.remove(client.getId()) == client) {
            part(client.getId(), client.getFullName(), reason);
        }
    }

    /**
     * Закрывает всех клиентов
     */
    private void killAll() {
        Enumeration<String> e = clients.keys();
        while (e.hasMoreElements()) {
            String id = e.nextElement();
            IRCClient con = clients.get(id);
            con.close("All disconnect");
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
     *
     * @param irc Экземпляр плагина
     */
    public static IRCServer Activate(MyIRC irc) {
        myIRC = irc;

        channel = myIRC.config.getString("irc.channel");
        gameHost = myIRC.config.getString("irc.gameHost");
        host = myIRC.config.getString("irc.host");
        port = myIRC.config.getInt("irc.port");

        IRCServer server = new IRCServer();
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
            e.printStackTrace();
        }
    }

    /**
     * Возвращает список пользователей IRC
     *
     * @return Список имён игроков
     */
    public ArrayList<String> userList() {
        ArrayList<String> userList = new ArrayList<String>();
        Enumeration<String> e = clients.keys();
        while (e.hasMoreElements()) {
            String id = e.nextElement();
            IRCClient client = clients.get(id);

            String nick = client.getNick();

            if (nick == null) continue;

            userList.add(nick);
        }

        return userList;
    }

    /**
     * Кикнуть пользователя
     *
     * @param kicked Имя игрока для исключения
     */
    public void kick(String kicked) {
        kicked = kicked.toLowerCase();

        Enumeration<String> e = clients.keys();
        while (e.hasMoreElements()) {
            String id = e.nextElement();
            IRCClient client = clients.get(id);

            String nick = client.getNick();

            if (nick == null) continue;

            nick = nick.toLowerCase();

            if (nick.equals(kicked)) {
                // broadcast("-1", ":" + creator + "!owner@" + host + " KICK " + channel + " " + nick + " :" + kickMessage);

                client.close(kickMessage);
                break;
            }
        }
    }
}