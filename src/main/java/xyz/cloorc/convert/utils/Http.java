package xyz.cloorc.convert.utils;

import org.springframework.web.bind.annotation.RequestMethod;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by wittcnezh on 2016/04/01:09:12.
 * Title: Simple
 * Description: Example
 * Copyright: Copyright(c) 2016
 *
 * @author <a href="mailto:wittcnezh@foxmail.com"/>
 */
public class Http {

    public static String url(String url, Map params) {
        if (null == url)
            return null;

        StringBuilder sb = new StringBuilder(url);

        if (! url.endsWith("?"))
            sb.append("?");

        if (null != params) {
            Iterator<Map.Entry<String, Object>> it = params.entrySet().iterator();
            Map.Entry<String,Object> item;
            while (it.hasNext()) {
                item = it.next();
                sb.append(item.getKey()).append("=").append(item.getValue()).append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    public static void send (URLConnection connection, byte[] data) {
        if (null == connection) {
            return ;
        }
        OutputStream out = null;
        try {
            out = connection.getOutputStream();
            out.write(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != out)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void send (URLConnection connection, File file) {
        if (null == connection) {
            return;
        }
        OutputStream ostream = null;
        FileInputStream istream = null;
        try {
            ostream = connection.getOutputStream();
            istream = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = istream.read(buf)) != -1) {
                ostream.write(buf, 0, len);
            }
            ostream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != ostream)
                    ostream.close();
                if (null != istream)
                    istream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String receive (URLConnection connection) {
        BufferedReader br = null;
        StringBuilder objStrBuilder = new StringBuilder();
        String objStr;

        if (null == connection) {
            return null;
        }

        try {
            br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            while ((objStr = br.readLine()) != null) {
                objStrBuilder.append(objStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != br)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return objStrBuilder.toString();
    }

    public static URLConnection connect (String target, RequestMethod method, Map<String,String> headers) {
        if (null == target) {
            return null;
        }

        URL url = null;
        try {
            url = new URL(target);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        URLConnection connection = null;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        connection.setDoInput(true);
        if (method.equals(RequestMethod.POST)) {
            connection.setDoOutput(true);
        }

        connection.setRequestProperty("Pragma", "no-cache");
        connection.setRequestProperty("Cache-Control", "no-cache");

        if (null != headers) {
            Iterator<Map.Entry<String, String>> it = headers.entrySet().iterator();
            Map.Entry<String,String> item;
            while (it.hasNext()) {
                item = it.next();
                connection.setRequestProperty(item.getKey(), item.getValue());
            }
        }

        return connection;
    }

    public static String post (String url, Map<String,String> headers, byte[] data) {
        URLConnection connection = connect(url, RequestMethod.POST, headers);
        if (null != connection) {
            send(connection, data);
            return receive(connection);
        }
        return null;
    }

    public static String post (String url, Map<String,String> headers, String fmt, Object... parameters) {
        URLConnection connection = connect(url, RequestMethod.POST, headers);
        if (null != connection) {
            try {
                send(connection, String.format(fmt, parameters).getBytes("UTF-8"));
                return receive(connection);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String get (String url, Map<String,String> headers, Map params) {
        String target = url(url, params);
        URLConnection connection = connect(target, RequestMethod.GET, headers);
        if (null != connection) {
            return receive(connection);
        }
        return null;
    }

    public static InputStream getInputStream (String url, Map<String,String> headers, Map params) throws IOException {
        String target = url(url, params);
        URLConnection connection = connect(target, RequestMethod.GET, headers);
        if (null != connection) {
            return connection.getInputStream();
        }
        return null;
    }
}
