package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.*;
import io.ebeaninternal.server.util.Str;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.ebean.DB.find;
import static io.ebean.DB.sqlQuery;

public final class UrlController {
    public static Handler newUrl = ctx -> {
        String inputtedUrl = ctx.formParam("url");
        URL urlAddress;

        try {
            urlAddress = new URL(inputtedUrl);
        } catch (MalformedURLException ex) {
            ctx.sessionAttribute("flash", "Некорректный Url");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.render("index.html");
            return;
        }

        Url urlToAdd = new Url(
                String.format("%s://%s%s",
                    urlAddress.getProtocol(),
                    urlAddress.getHost(),
                    urlAddress.getPort() < 0
                            ? ""
                            : String.format(":%s", Integer.toString(urlAddress.getPort()))
                )
        );

        Url existingUrl = new QUrl().name.equalTo(urlToAdd.getName()).findOne();
        if (existingUrl != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "warning");
        } else {
            urlToAdd.save();
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");
        }

        ctx.redirect("/urls");
    };

    public static Handler urlList = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        QUrl url = QUrl.alias();
        QUrlCheck urlCheck = QUrlCheck.alias();
        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .urlChecks.fetch("max(id)")
                .findPagedList();

//        PagedList<Url> pagedUrls = new QUrl()
//                .setFirstRow(page * rowsPerPage)
//                .setMaxRows(rowsPerPage)
//                .orderBy()
//                .id.asc()
//                .findPagedList();








                //Этот запрос дает то, что нужно. Но тут не ясно, как делать привзку к полям класса
                final String query = "SELECT" +
                        "    url.id AS id_of_url, name, last_check_req.created_at AS last_check_datetime, status_code " +
                        "FROM" +
                        "    url" +
                        "    LEFT JOIN" +
                        "        (SELECT" +
                        "            max_id_req.url_id AS url_id," +
                        "            created_at," +
                        "            status_code" +
                        "        FROM" +
                        "            (SELECT" +
                        "                url_id," +
                        "                MAX(id) AS max_id" +
                        "            FROM url_check" +
                        "            GROUP BY url_id" +
                        "            ) AS max_id_req" +
                        "        LEFT JOIN" +
                        "            url_check" +
                        "        ON" +
                        "            max_id = url_check.id" +
                        "        ) AS last_check_req" +
                        "    ON" +
                        "        url.id = last_check_req.url_id " +
                        "ORDER BY id_of_url";
        List<SqlRow> rows = sqlQuery(query).setFirstRow(page * rowsPerPage).setMaxRows(rowsPerPage).findList();


        List<Map<String, Object>> urls = new ArrayList<>();
        for (SqlRow row: rows) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", row.getLong("id_of_url"));
            record.put("name", row.getString("name"));
            Timestamp dateOfCheck = row.getTimestamp("last_check_datetime");
            record.put("last_check_datetime", dateOfCheck == null ? null : dateOfCheck.toInstant());
            record.put("status_code", row.getInteger("status_code"));
            urls.add(record);
        }



        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        ctx.attribute("urls", urls);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("urls/list.html");
    };

    public static Handler showUrl = ctx -> {
        Integer id = ctx.pathParamAsClass("id", Integer.class).get();

        Url url = new QUrl().id.eq(id).findOne();
        List<UrlCheck> checks = url.getUrlChecks();
        Collections.reverse(checks);
        ctx.attribute("checks", checks);
        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };

    public static Handler urlCheck = ctx -> {
//        Integer id = ctx.pathParamAsClass("id", Integer.class).get();
//        Url url = new QUrl().id.equalTo(id).findOne();
//        String urlName = url.getName();
//
//        HttpResponse<String> resp = Unirest.get(urlName).asString();
//
//        int status = resp.getStatus();
//
//        Document doc = Jsoup.parse(resp.getBody());
//        String title = doc.title();
//
//        Element h1El = doc.getElementsByTag("h1").first();
//        String h1 = h1El == null ? "" : h1El.text();
//
//        Element descEl = doc.select("meta[name=description]").first();
//        String desc = descEl == null ? "" : descEl.attr("content");
//
//        UrlCheck check = new UrlCheck(status, title, h1, desc, url);
//        check.save();
//
//        Unirest.shutDown();
    };
}
