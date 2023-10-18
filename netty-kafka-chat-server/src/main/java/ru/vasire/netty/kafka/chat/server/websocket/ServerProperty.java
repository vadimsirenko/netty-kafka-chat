package ru.vasire.netty.kafka.chat.server.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerProperty {
    public static final int PORT;

    private static final Properties prop = new Properties();

    static {
        try {
            final InputStream in = ServerProperty.class.getClassLoader().getResourceAsStream("app.properties");
            prop.load(in);
            assert in != null;
            in.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        PORT = Integer.parseInt(prop.getProperty("port"));
    }
}
