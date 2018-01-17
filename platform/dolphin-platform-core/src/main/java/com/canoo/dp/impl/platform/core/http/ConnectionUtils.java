package com.canoo.dp.impl.platform.core.http;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.platform.core.http.ByteArrayProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import static com.canoo.dp.impl.platform.core.http.HttpHeaderConstants.CHARSET;

public class ConnectionUtils {

    private ConnectionUtils() {}

    public static byte[] readContent(final InputStream inputStream) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int read = inputStream.read();
            while (read != -1) {
                byteArrayOutputStream.write(read);
                read = inputStream.read();
            }
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] readContent(final HttpURLConnection connection) throws IOException {
        Assert.requireNonNull(connection, "connection");
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try(final InputStream is = connection.getInputStream()) {
            int read = is.read();
            while (read != -1) {
                byteArrayOutputStream.write(read);
                read = is.read();
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static String readUTF8Content(final HttpURLConnection connection) throws IOException {
        return new String(readContent(connection), CHARSET);
    }

    public static String readUTF8Content(final InputStream inputStream) throws IOException {
        return new String(readContent(inputStream), CHARSET);
    }

    public static void writeContent(final OutputStream outputStream, final byte[] rawData) throws IOException {
        Assert.requireNonNull(outputStream, "outputStream");
        Assert.requireNonNull(rawData, "rawData");
        outputStream.write(rawData);
    }

    public static void writeContent(final HttpURLConnection connection, final byte[] rawData) throws IOException {
        Assert.requireNonNull(connection, "connection");
        Assert.requireNonNull(rawData, "rawData");
        try(final OutputStream outputStream = connection.getOutputStream()) {
            writeContent(outputStream, rawData);
        }
    }

    public static void writeContent(final HttpURLConnection connection, final ByteArrayProvider provider) throws IOException {
        Assert.requireNonNull(provider, "provider");
        writeContent(connection, provider.get());
    }

    public static void writeContent(final OutputStream outputStream, final ByteArrayProvider provider) throws IOException {
        Assert.requireNonNull(provider, "provider");
        writeContent(outputStream, provider.get());
    }

    public static void writeUTF8Content(final HttpURLConnection connection, final String content) throws IOException {
        Assert.requireNonNull(content, "content");
        writeContent(connection, content.getBytes(CHARSET));
    }

    public static void writeUTF8Content(final OutputStream outputStream, final String content) throws IOException {
        Assert.requireNonNull(content, "content");
        writeContent(outputStream, content.getBytes(CHARSET));
    }
}
