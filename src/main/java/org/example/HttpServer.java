package org.example;

import io.javalin.Javalin;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.List;
import java.util.function.Supplier;

public class HttpServer {

    private static final SessionFactory sessionFactory = HibernateHelper.getSessionFactory();
    public static final String symbols= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(9000);
        app.get("/", context -> context
        .contentType("text/html")
        .result("<html><head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<title>Url-Shorterner</title>\n" +
                "<style>\n" +
                "input[type=text] {\n" +
                "width: 60%;\n" +
                "padding: 12px 20px;\n" +
                "margin: 8px 0;\n" +
                "box-sizing: border-box;\n" +
                "}\n" +
                "input[type=submit] {\n" +
                "width: 20em;  height: 5em;\n" +
                "background-color: green;\n" +
                "margin-bottom: 20px;\n" +
                "}\n" +
                ".form-container {\n" +
                "text-align: center;\n" +
                "background-color: aqua;\n" +
                "padding: 80px 0 120px 0;\n" +
                "}\n" +
                "\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<form action=\"/\" class=\"form-container\" method=\"post\">\n" +
                "<img src=\"https://docrdsfx76ssb.cloudfront.net/static/1622667207/pages/wp-content/uploads/2019/02/bitly.png\" alt=\"URL Shortener\" style=\"width: 298px; height: 159px; margin: 5.85px 0px;\">\n" +
                "<p>\n" +
                "</p><h2 class=\"paragraph-text\">Short links, big results</h2>\n" +
                "<p></p>\n" +
                "<input name=\"url\" type=\"text\">\n" +
                "<h4 class=\"h4\"></h4>\n" +
                "<input type=\"submit\" value=\"Shorten\">\n" +
                "</form>\n" +
                "\n" +
                "</body></html>")
        );

        app.post("/", context -> {
            try(Session session = sessionFactory.openSession()) {
                String url = context.formParam("url");
                String shortUrl = ensureUniqueness(session, () -> generateShortLink());

                UrlService urlService = new UrlService();
                urlService.setOriginalUrl(url);
                urlService.setShortUrl(shortUrl);

                Transaction transaction = session.beginTransaction();
                Serializable urlServiceId = session.save(urlService);
                System.out.println(urlServiceId);
                transaction.commit();

                String html = ("<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <title>org.example.Url-Shorterner</title>\n" +
                        "    <style>\n" +
                        "        input[type=text] {\n" +
                        "            width: 60%;\n" +
                        "            padding: 12px 20px;\n" +
                        "            margin: 8px 0;\n" +
                        "            box-sizing: border-box;\n" +
                        "        }\n" +
                        ".refContainer{" +
                        "display: flex;" +
                        "justify-content: center;" +
                        "}\n" +
                        "        input[type=submit] {\n" +
                        "            width: 20em;  height: 5em;\n" +
                        "        background-color: green;\n" +
                        "            margin-bottom: 20px;\n" +
                        "        }\n" +
                        "        .form-container {\n" +
                        "            text-align: center;\n" +
                        "            background-color: aqua;\n" +
                        "            padding: 80px 0 120px 0;\n" +
                        "        }\n" +
                        "\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<form action=\"/\" class=\"form-container\" method=\"post\">\n" +
                        "    <img src=\"https://docrdsfx76ssb.cloudfront.net/static/1622667207/pages/wp-content/uploads/2019/02/bitly.png\" alt=\"URL Shortener\" style=\"width: 298px; height: 159px; margin: 5.85px 0px;\">\n" +
                        "</form>\n" +
                        "<a class=\"refContainer\"href=\"PLACEHOLDER\">PLACEHOLDER</a>\n" +
                        "\n" +
                        "</body>\n" +
                        "</html>").replaceAll("PLACEHOLDER", "http://localhost:9000/" + shortUrl);

                context
                        .contentType("text/html")
                        .result(html);
            }
        });

        app.get("/:shortUrl", context -> {
            String shortUrl = context.pathParam("shortUrl");

            String targetUrl = "";
            try (Session session = sessionFactory.openSession()) {
                try {
                    final Query<UrlService> query = session
                            .createQuery("select u from UrlService u where u.shortUrl = :shortUrl", UrlService.class)
                            .setParameter("shortUrl", shortUrl);
                    List<UrlService> urlService = query.getResultList();
                    if (urlService != null && !urlService.isEmpty()) {
                        targetUrl = urlService.get(0).getOriginalUrl();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            context.header("Location", targetUrl)
                    .status(302);
        });
    }






    private static String ensureUniqueness(Session session, Supplier<String> supplier) {
        boolean isUnique = false;
        String value = null;

        while (!isUnique) {
            value = supplier.get();
            try {
                final Query<UrlService> query = session
                        .createQuery("select u from UrlService u where shortUrl = :shortUrl", UrlService.class)
                        .setParameter("shortUrl", value);
                List<UrlService> urlService = query.getResultList();
                if (urlService != null && urlService.isEmpty()) {
                    isUnique = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    private static String generateShortLink() {
        int length = (int) (Math.random()*5)+5;
        StringBuilder shortLink = new StringBuilder();
        for(int i = 0; i < length; i++) {
            int index = (int) (Math.random()*62);
            shortLink.append(symbols.charAt(index));
        }
        return shortLink.toString();
    }
}