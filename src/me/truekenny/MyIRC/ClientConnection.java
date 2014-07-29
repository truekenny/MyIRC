package me.truekenny.MyIRC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ClientConnection implements Runnable {
    /**
     * Сокет клиента
     */
    private Socket sock;

    /**
     * Входящий поток
     */
    private BufferedReader in;

    /**
     * Исходящий поток
     */
    private OutputStream out;

    /**
     * Хост клиента
     */
    private String host;

    /**
     * Экземпляр сервера
     */
    private Server server;

    /**
     * Окончание строки при отправке клиенту
     */
    private static final String CRLF = "\r\n";

    /**
     * Имя клиента
     */
    private String nick = null;

    /**
     * Идентификатор клиента
     */
    private String id;

    /**
     * Занятость клиента
     */
    private boolean busy = false;

    public static final Pattern nickPattern = Pattern.compile("[a-zA-Zа-яА-Я0-9ёЁ]{3,15}");

    /**
     * Объект для логирования сообщений плагина
     */
    Logger log = Logger.getLogger("Minecraft");

    /**
     * Обрабатывает подключение нового клиента
     *
     * @param srv
     * @param s
     * @param i
     */
    public ClientConnection(Server srv, Socket s, int i) {
        try {
            server = srv;
            sock = s;
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = s.getOutputStream();
            host = s.getInetAddress().getHostName();
            id = "" + i;
            write("NOTICE AUTH :id " + id);
            new Thread(this).start();
        } catch (IOException e) {
            log.info("failed ClientConnection " + e);
        }
    }

    public String toString() {
        return id + " " + host + " " + nick;
    }

    public String getHost() {
        return host;
    }

    public String getId() {
        return id;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean b) {
        busy = b;
    }

    public String getNick() {
        return nick;
    }

    public String getFullName() {
        return nick + "!" + id + "@" + getHost();
    }

    /**
     * Закрывает подключение
     */
    public void close() {
        log.info("= " + id + ": disconnected");

        server.kill(this);
        try {
            sock.close();
        } catch (IOException e) {
        }
    }

    /**
     * Отправляет сообщение клиенту
     *
     * @param s
     */
    public void write(String s) {
        log.info("> " + id + ": «" + s + "»");

        s = s + CRLF;
        byte buf[];
        buf = s.getBytes(Charset.forName("UTF-8"));

        try {
            out.write(buf, 0, buf.length);
        } catch (IOException e) {
            close();
        }
    }

    /**
     * Читает с клиента строку
     *
     * @return
     */
    private String readline() {
        try {
            return in.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    static private final int NICK = 1;

    static private final int QUIT = 2;

    static private final int TO = 3;

    static private final int DELETE = 4;

    static private Hashtable<String, Integer> keys = new Hashtable<String, Integer>();

    static private String keystrings[] = {"", "nick", "quit", "to", "delete"};

    static {
        for (int i = 0; i < keystrings.length; i++)
            keys.put(keystrings[i], new Integer(i));
    }

    /**
     * Возвращает идентификатор команды
     *
     * @param s
     * @return
     */
    private int lookup(String s) {
        Integer i = keys.get(s.toLowerCase());
        return i == null ? -1 : i.intValue();
    }

    /**
     * Главный цикл ожидающий данные с клиента
     */
    public void run() {
        String s;
        StringTokenizer st;
        while ((s = readline()) != null) {
            log.info("< " + id + ": «" + s + "»");

            st = new StringTokenizer(s);
            String keyword = st.nextToken();
            switch (lookup(keyword)) {
                default:
                    log.info("bogus keyword: " + keyword + "\r");
                    break;
                case NICK:
                    String newNick = st.nextToken();

                    Matcher nickMatcher = nickPattern.matcher(newNick);
                    if (nickMatcher.matches() == false) {
                        write("432 " + newNick + " :Erroneus Nickname");

                        continue;
                    }

                    if (server.myirc.isUniqueNick(newNick) == false) {
                        write("433 " + newNick + " :Nickname is already in use");

                        continue;
                    }

                    //nick = st.nextToken() + (st.hasMoreTokens() ? " " + st.nextToken(CRLF) : "");
                    nick = newNick;
                    log.info("[" + new Date() + "] " + this + "\r");
                    server.set(id, this);

                    write("001 " + nick + " :Welcome to the MyIRC Network " + getFullName());
                    write("005 " + nick + " PREFIX=(ohv)@%+");
                    write("NOTICE " + nick + " Ingame: " + server.myirc.userList().toString());
                    write("NOTICE " + nick + " Inchat: " + server.userList().toString());

                    break;
                case QUIT:
                    close();
                    return;
                case TO:
                    String dest = st.nextToken();
                    String body = st.nextToken(CRLF);
                    server.sendto(dest, body);
                    break;
                case DELETE:
                    busy = true;
                    server.delete(id);
                    break;
            }
        }
        close();
    }
}   