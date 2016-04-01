package xyz.cloorc.convert.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.apachecommons.CommonsLog;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.server.ExportException;

/**
 * Created by wittcnezh on 2016/04/01:09:05.
 * Title: Simple
 * Description: Example
 * Copyright: Copyright(c) 2016
 *
 * @author <a href="mailto:wittcnezh@foxmail.com"/>
 */
@CommonsLog
public class Json {

    final static ObjectMapper mapper = new ObjectMapper();

    public final static<T> String of (T o, final String defaultvalue) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.warn("Write object to JSON string failed: {}", e);
        }
        return defaultvalue;
    }

    public final static<T> byte[] of (T o) {
        try {
            return mapper.writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            log.warn("Write object to JSON bytes failed: {}", e);
        }
        return new byte[0];
    }

    public final static<T> T from (final String value, Class<T> clazz) {
        try {
            return mapper.readValue(value, clazz);
        } catch (Exception e) {
            log.warn("Read object from string failed: {}", e);
            return null;
        }
    }

    public final static<T> T from (final byte[] bytes, Class<T> clazz) {
        try {
            return mapper.readValue(bytes, clazz);
        } catch (Exception e) {
            log.warn("Read object from bytes failed: {}", e);
            return null;
        }
    }

    public final static<T> T from (final InputStream is, Class<T> clazz) {
        try {
            return mapper.readValue(is, clazz);
        } catch (Exception e) {
            log.warn("Read object from input stream failed: {}", e);
            return null;
        }
    }

    public final static StringBuffer e (final StringBuffer buf, final String key, final String value) {
        buf.append("\"").append(key).append("\":");
        if (null == value)
            return buf.append("null");
        else
            return buf.append('\"').append(value).append('\"');
    }

    public final static StringBuffer e (final StringBuffer buf, final String key, final Object value) {
        buf.append("\"").append(key).append("\":");
        if (null == value)
            return buf.append("null");
        else
            return buf.append(value.toString());
    }
}
