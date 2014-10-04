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

class IRCClient implements Runnable {
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
     * IP клиента
     */
    private String ip;

    /**
     * Экземпляр сервера
     */
    public IRCServer ircServer;

    /**
     * Окончание строки при отправке клиенту
     */
    public static final String CRLF = "\r\n";

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

    /**
     * Паттерн для проверки ника
     */
    public static final Pattern nickPattern = Pattern.compile("[a-zA-Zа-яА-Я0-9ёЁ_]{3,15}");

    /**
     * Объект для логирования сообщений плагина
     */
    Logger log = Logger.getLogger("Minecraft");

    /**
     * Этот пользователь является оператором
     */
    private boolean isOperator = false;

    /**
     * Кодировка клиента
     */
    public String codePage = "UTF-8";

    /**
     * Для отправки сообщений оператору из /RW
     */
    private MyCommandSender commandSender;

    /**
     * Флаг, что пользователь уже на канале
     */
    public boolean joined = false;

    /**
     * Время подключения
     */
    public long timeConnection = System.currentTimeMillis() / 1000L;

    /**
     * Idle клиента
     */
    public long timeIdle = System.currentTimeMillis() / 1000L;

    /**
     * Idle клиента
     */
    public long timeOut = System.currentTimeMillis() / 1000L;

    /**
     * Часть идентификатора пользователя, nick!userName@host
     */
    public String userName = null;

    /**
     * Соединение уже закрыто
     */
    public boolean closed = false;

    /**
     * Пользователь авторизирован с помощью AuthMe
     */
    public boolean authorized = false;

    /**
     * Пароль полученный до получения ника
     */
    public String password = null;

    /**
     * Обрабатывает подключение нового клиента
     *
     * @param server Экземпляр сервера
     * @param socket Подключение клиента
     * @param id_    Идентификатор клиента
     */
    public IRCClient(IRCServer server, Socket socket, int id_) {
        try {
            ircServer = server;
            sock = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = socket.getOutputStream();
            host = socket.getInetAddress().getHostName();
            ip = Helper.convertFullIPToIP(socket.getRemoteSocketAddress().toString());
            id = "" + id_;
            write(":" + IRCServer.host + " NOTICE AUTH :id " + id);

            authorized = !ircServer.myIRC.checkAuthMe();

            new Thread(this).start();
        } catch (IOException e) {
            log.info("failed ClientConnection " + e);
        }
    }

    public String toString() {
        return id + " " + host + " " + nick;
    }

    public String getHost() {
        return IRCServer.myIRC.host(host);
    }

    public String getIP() {
        return IRCServer.myIRC.host(ip);
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

    public void setNick(String nick) {
        this.nick = nick;
        authorized = !ircServer.myIRC.checkAuthMe();

        if (this.password != null) {
            checkAuthMePass(nick, this.password);
        }
    }

    public String getFullName() {
        return nick + "!" + id + "-" + userName + "@" + getHost();
    }

    /**
     * Закрывает подключение
     *
     * @param reason Сообщение выхода
     */
    public void close(String reason) {
        if (closed) {

            return;
        }
        closed = true;

        log.info("= " + id + ": Quit (" + reason + ")");

        try {
            write("QUIT :" + reason);
        } catch (Exception e) {
            log.info("Write error on close socket");
        }

        ircServer.kill(this, reason);
        try {
            sock.close();
        } catch (IOException e) {
            log.info("Error on close socket");
        }
    }

    /**
     * Отправляет сообщение клиенту
     *
     * @param message Сообщение
     */
    public void write(String message) {
        if (ircServer.myIRC.config.getBoolean("debug")) {
            log.info("> " + id + ": «" + message + "»");
        }

        message = message + CRLF;
        byte buf[];
        buf = message.getBytes(Charset.forName(codePage));

        try {
            out.write(buf, 0, buf.length);
        } catch (IOException e) {
            close("Write error, closing link");
        }
    }

    /**
     * Читает с клиента строку
     *
     * @return Строка
     */
    private String readLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    static private final int NICK = 1;

    static private final int QUIT = 2;

    static private final int PRIVMSG = 3;

    static private final int PART = 4;

    static private final int WHO = 5;

    static private final int WHOIS = 6;

    static private final int PING = 7;

    static private final int PONG = 8;

    static private final int MODE = 9;

    static private final int CODEPAGE = 10;

    static private final int OPER = 11;

    static private final int RW = 12;

    static private final int USER = 13;

    static private final int PASS = 14;

    static private Hashtable<String, Integer> keys = new Hashtable<String, Integer>();

    static private String keyStrings[] = {"", "nick", "quit", "privmsg", "part", "who", "whois", "ping", "pong", "mode", "codepage", "oper", "rw", "user", "pass"};

    static {
        for (int i = 0; i < keyStrings.length; i++)
            keys.put(keyStrings[i], new Integer(i));
    }

    /**
     * Возвращает идентификатор команды
     *
     * @param key Ключ
     * @return Идентификатор ключа
     */
    private int lookup(String key) {
        Integer i = keys.get(key.toLowerCase());
        return i == null ? -1 : i.intValue();
    }

    /**
     * Главный цикл ожидающий данные с клиента
     */
    public void run() {
        String s;
        StringTokenizer st;
        while ((s = readLine()) != null) {
            if (ircServer.myIRC.config.getBoolean("debug")) {
                log.info("< " + id + ": «" + s + "»");
            }

            st = new StringTokenizer(s);

            if (!st.hasMoreTokens()) continue;

            String keyword = st.nextToken();
            switch (lookup(keyword)) {
                default:
                    log.info("bogus command: " + s + "\r");
                    break;
                case NICK:
                    if (!st.hasMoreTokens()) continue;
                    String newNick = st.nextToken();

                    Matcher nickMatcher = nickPattern.matcher(newNick);
                    if (!nickMatcher.matches()) {
                        write(":" + IRCServer.host + " 432 * " + newNick + " :" + IRCServer.myIRC.config.getString("messages.irc.erroneusNickname"));

                        continue;
                    }

                    if (!IRCServer.myIRC.isUniqueNick(newNick)) {
                        write(":" + IRCServer.host + " 433 * " + newNick + " :" + IRCServer.myIRC.config.getString("messages.irc.nicknameInUse"));

                        continue;
                    }

                    if (nick != null) {
                        ircServer.changeNick(getFullName(), newNick);
                        setNick(newNick);

                        continue;
                    }

                    log.info("[" + new Date() + "] " + this + "\r");

                    setNick(newNick);

                    break;
                case PART:
                case QUIT:
                    close("Quit");
                    return;
                case PRIVMSG:
                    if (!st.hasMoreTokens()) continue;
                    String to = st.nextToken();
                    if (!st.hasMoreTokens()) continue;
                    String message = st.nextToken(CRLF).trim().replaceAll("^:", "");

                    timeIdle = System.currentTimeMillis() / 1000L;

                    if (to.equals(ircServer.myIRC.config.getString("irc.creator"))) {
                        executeCommand(message);

                        continue;
                    }

                    if (!authorized) {
                        unAuthorizedMessages(to);

                        continue;
                    }

                    if (!to.equals(IRCServer.channel)) {
                        ircServer.myIRC.log.info("Private message, " + getNick() + " to " + to + ": " + message);

                        if (ircServer.sendPrivate(message, getFullName(), to) != null) {
                            continue;
                        }

                        if (ircServer.myIRC.sendPrivate(message, nick, to) != null) {
                            continue;
                        }

                        write(":" + host + " 401 " + nick + " " + to + " :" + ircServer.myIRC.config.getString("messages.irc.noSuchNick"));
                        continue;
                    }

                    // ircServer.sendTo(dest, body);
                    ircServer.privmsg(id, getFullName(), message);
                    break;
                /*
                case PART:
                    busy = true;
                    ircServer.part(id);
                    break;
                */
                case WHO:
                    if (!st.hasMoreTokens()) continue;
                    String who = st.nextToken();
                    ircServer.who(this, who);

                    break;

                case WHOIS:
                    if (!st.hasMoreTokens()) continue;
                    String whois = st.nextToken();
                    ircServer.whois(this, whois);

                    break;

                case PING:
                    timeOut = System.currentTimeMillis() / 1000L;

                    if (!st.hasMoreTokens()) continue;
                    String idPing = st.nextToken();

                    write(":" + IRCServer.host + " PONG " + IRCServer.host + " :" + idPing);

                    break;

                case PONG:
                    timeOut = System.currentTimeMillis() / 1000L;

                    if (!st.hasMoreTokens()) continue;
                    // String idPong = st.nextToken();
                    // write("PING " + idPong);

                    break;
                case MODE:
                    if (!st.hasMoreTokens()) continue;
                    String channel = st.nextToken();

                    if (!st.hasMoreTokens()) continue;
                    String flag = st.nextToken();

                    String user = "";
                    if (st.hasMoreTokens()) {
                        user = st.nextToken();
                    }

                    ircServer.getMode(this, channel, flag, user);

                    break;
                case CODEPAGE:
                    if (!st.hasMoreTokens()) continue;
                    codePage = st.nextToken();
                    try {
                        in = new BufferedReader(new InputStreamReader(sock.getInputStream(), Charset.forName(codePage)));
                    } catch (Exception e) {
                        log.info("Wrong codepage: " + codePage);
                    }
                    write(":" + IRCServer.host + " NOTICE " + nick + " :New code page is: " + codePage + ".");

                    break;
                case OPER:
                    if (!st.hasMoreTokens()) continue;
                    String password = st.nextToken();

                    if (password.equals(ircServer.myIRC.config.getString("irc.operPassword"))) {
                        isOperator = true;
                        write(":" + IRCServer.host + " NOTICE " + nick + " :You are operator now. You can use: /RW [BUKKIT COMMAND]");
                    }

                    break;
                case RW:
                    String command = st.nextToken(CRLF).trim();

                    executeCommand(command);
                    break;
                case USER:
                    if(nick == null){
                        continue;
                    }

                    if (userName != null) {
                        continue;
                    }

                    if (!st.hasMoreTokens()) continue;
                    userName = st.nextToken();

                    // Nick collision
                    if (!IRCServer.myIRC.isUniqueNick(nick)) {
                        close("Nick collision");

                        continue;
                    }

                    ircServer.set(id, this);
                    sendStatistic();
                    ircServer.myIRC.getServer().getScheduler().scheduleSyncDelayedTask(ircServer.myIRC, new Runnable() {
                        @Override
                        public void run() {
                            join();
                        }

                    }, 20);

                    break;
                case PASS:
                    if (!st.hasMoreTokens()) continue;
                    this.password = st.nextToken();

                    if (nick != null) {
                        checkAuthMePass(nick, this.password);
                    }

                    break;
            }
        }
        close("Connection reset by peer");
    }

    /**
     * Отправляет статистику пользователю
     */
    public void sendStatistic() {
        write(":" + IRCServer.host + " 001 " + nick + " :Welcome to the Internet Relay Network, " + getFullName());
        write(":" + IRCServer.host + " 002 " + nick + " :Your host is " + getHost() + ", running version MyIRC");
        write(":" + IRCServer.host + " 003 " + nick + " :This server was created " + ircServer.createTime); // Wed Dec 25 2013 at 08:52:10 UTC

        write(":" + IRCServer.host + " 004 " + nick + " " + IRCServer.host + " MyIRC O nt");

        write(":" + IRCServer.host + " 005 " + nick + " PREFIX=(ohv)@%+ :are supported by this server");

        write(":" + IRCServer.host + " 375 Sam :- " + IRCServer.host + " Message of the day -");
        write(":" + IRCServer.host + " 372 Sam :- Hello");
        write(":" + IRCServer.host + " 376 Sam :End of /MOTD command.");

        write(":" + IRCServer.host + " NOTICE " + nick + " :Ingame " + IRCServer.myIRC.userList());
        write(":" + IRCServer.host + " NOTICE " + nick + " :Inchat " + ircServer.userList());
    }

    /**
     * Добавляет пользователя на канал
     */
    public void join() {
        // Сообщение для пользователя
        write(":" + getFullName() + " JOIN :" + IRCServer.channel);

        write(":" + IRCServer.host + " 332 " + nick + " " + IRCServer.channel + " :" + ircServer.topic);
        write(":" + IRCServer.host + " 333 " + nick + " " + IRCServer.channel + " " + ircServer.creator + " " + ircServer.createTime);


        write(":" + IRCServer.host + " 353 " + nick + " = " + IRCServer.channel + " :" +
                        Helper.convertArrayList(
                                ircServer.userList(),
                                "",
                                ircServer.myIRC
                        ) + " " +
                        Helper.convertArrayList(
                                IRCServer.myIRC.userList(),
                                Helper.voicePrefix,
                                ircServer.myIRC
                        )
        );
        write(":" + IRCServer.host + " 366 " + nick + " " + IRCServer.channel + " :End of /NAMES list.");

        joined = true;

        // Сообщение для пользователей IRC
        ircServer.join(id, getFullName());
    }

    /**
     * Выполняет консольную команду
     *
     * @param command
     */
    private void executeCommand(String command) {
        if (!isOperator) return;

        try {
            if (commandSender == null) {

                commandSender = new MyCommandSender(ircServer.myIRC.getServer(), this);
            }

            ircServer.myIRC.getServer().dispatchCommand(commandSender, command);
        } catch (Exception e) {
            ircServer.sendPrivate(nick, "error " + e.getMessage());
        }

    }

    /**
     * Проверяет пароль и отправляет сообщения
     *
     * @param nick
     * @param pass
     */
    private void checkAuthMePass(String nick, String pass) {
        if (authorized) {

            return;
        }

        if (ircServer.myIRC.checkAuthMePass(nick, pass)) {
            authorized = true;

            write(":" + IRCServer.host + " NOTICE " + nick + " :" + nick + ": " + ircServer.myIRC.config.getString("messages.irc.passwordAccepted"));
        } else {
            write(":" + IRCServer.host + " NOTICE " + nick + " :" + nick + ": " + ircServer.myIRC.config.getString("messages.irc.passwordWrong"));
        }
    }

    /**
     * Отправляет сообщение о необходимости авторизации
     *
     * @param to
     */
    private void unAuthorizedMessages(String to) {
        if (to.equals(IRCServer.channel)) {
            // Отправлено на канал
            write(":" + ircServer.getBotNick() + " PRIVMSG " + IRCServer.channel + " :" + ircServer.myIRC.config.getString("messages.irc.unAuthorized"));
            write(":" + ircServer.getBotNick() + " PRIVMSG " + IRCServer.channel + " :" + ircServer.myIRC.config.getString("messages.irc.unAuthorized2"));

        } else {
            // Отправлено в приват
            write(":" + ircServer.myIRC.getFullNameBy(to) + " PRIVMSG " + nick + " :" + ircServer.myIRC.config.getString("messages.irc.unAuthorized"));
            write(":" + ircServer.myIRC.getFullNameBy(to) + " PRIVMSG " + nick + " :" + ircServer.myIRC.config.getString("messages.irc.unAuthorized2"));
        }
    }
}