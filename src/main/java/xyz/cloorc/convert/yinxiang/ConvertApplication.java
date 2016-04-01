package xyz.cloorc.convert.yinxiang;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import xyz.cloorc.convert.utils.Http;
import xyz.cloorc.convert.utils.Json;

import java.io.*;
import java.util.*;

/**
 * Created by wittcnezh on 2016/04/01:08:51.
 * Title: Simple
 * Description: Example
 * Copyright: Copyright(c) 2016
 *
 * @author <a href="mailto:wittcnezh@foxmail.com"/>
 */
@CommonsLog
@EnableScheduling
@SpringBootApplication
public class ConvertApplication {

    @Bean
    public static CommandLineRunner getRunners() {
        return new ConvertRunner();
    }

    public static void main (String... args) {
        SpringApplication.run(ConvertApplication.class, args);
    }

    static class ConvertRunner implements CommandLineRunner {

        private String service;
        private final Map<String,String> queries = new HashMap<String, String>();
        private SimpleCommandLinePropertySource source;

        public void run(String... args) throws Exception {
            source = new SimpleCommandLinePropertySource(args);
            if (! source.containsProperty("root"))
                return;

            String root = source.getProperty("root");

            String outputDirectory = ".";
            if (source.containsProperty("dir"))
                outputDirectory = source.getProperty("dir");

            int and = root.indexOf('?');

            service = root.substring(0, and);
            final String query = root.substring(and + 1);

            String q = query;
            String parameter;
            int eq;
            while (q.contains("&")) {
                and = q.indexOf("&");
                parameter = q.substring(0, and);
                q = q.substring(and+1);
                eq = parameter.indexOf('=');
                queries.put(parameter.substring(0, eq), parameter.substring(eq + 1));
            }

            final String content = Http.get(service, null, queries);
            final List<Map> folders = Json.from(content, ArrayList.class);
            Note note = null;
            if (null == folders || folders.size() <= 0)
                note = Json.from(content, Note.class);
            if ((null == folders || folders.size() <= 0) && null == note)
                return;

            if (null != folders) {
                convertFolders(outputDirectory, folders);
            } else if (null != note) {
                convertNote(outputDirectory + File.separator + note.getName(), note);
            }
        }

        void convertFolders(final String dir, List<Map> folders) throws IOException {
            Map<String,String> q;
            String target;
            for (Map folder : folders) {
                q = new HashMap<String, String>();
                q.putAll(queries);
                target = dir + File.separator + get(folder, "obj.name");
                if (get(folder,"folder")) {
                    q.put("operateType", "listFiles");
                    q.put("parentPath", (String) get(folder,"key"));
                    q.put("pageSize", "26");
                    convertFolders(target, Json.from(Http.getInputStream(service, null, q), ArrayList.class));
                } else {
                    q.put("operateType", "getFile");
                    q.put("parentPath", (String) get(folder,"obj.parentPath"));
                    q.put("pageSize", "26");
                    q.put("guid", (String) get(folder,"obj.guid"));
                    convertNote(target, Json.from(Http.getInputStream(service, null, q), Note.class));
                }
            }
        }

        void convertNote(final String target, Note note) throws IOException {
            if (null == note) return;

            final File f = new File(target);
            if (f.exists() && f.isDirectory()) f.delete();
            final File p = new File(f.getParent());
            if (p.isFile()) p.delete();
            if (! p.exists()) p.mkdirs();

            String file = f.getAbsolutePath();

            PrintWriter pw;
            String body;
            if (StringUtils.hasLength(note.getMarkdownContent())) {
                if (!file.endsWith(".md")) file = file + ".md";
                body = note.getMarkdownContent();
            } else {
                if (!file.endsWith(".html")) file = file + ".html";
                body = note.getHtmlContent();
            }
            pw = new PrintWriter(new FileOutputStream(file, false));
            pw.write(StringUtils.hasLength(body) ? body : "");
            pw.flush();
            pw.close();
            log.info("note has been converted successfully :" + file);
        }

        <T> T get (final Map map, final String path) {
            final String[] paths = path.split("\\.");
            final int last = paths.length - 1;
            Map result = map;
            for (int i = 0; i <= last; ++ i) {
                if (i < last)
                    result = (Map) result.get(paths[i]);
                else
                    return (T) result.get(paths[i]);
            }
            return null;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Note implements Serializable {
        private String guid;
        private String parentPath;
        private String markdownContent;
        private Map resources;
        private String htmlContent;
        private List<String> tagNames;
        private Date createDate;
        private Date updateDate;
        private String name;
    }
}
