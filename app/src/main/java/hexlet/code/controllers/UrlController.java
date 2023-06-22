package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.*;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.ebean.DB.find;

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

//        final String query = "SELECT"
//                        + " u.id AS id_of_url, u.name, last_check_req.created_at AS last_check_datetime, c.status_code "
//                    + "FROM "
//                        + "url u"
//                    + "LEFT JOIN "
//                        + "(SELECT "
//                            + "max_id_req.url_id AS url_id, created_at, status_code "
//                        + "FROM "
//                            + "(SELECT "
//                                + "c.url_id, MAX(c.id) AS max_id "
//                            + "FROM "
//                                + "url_check c"
//                            + "GROUP BY url_id "
//                            + ") AS max_id_req "
//                        + "LEFT JOIN "
//                            + "url_check "
//                        + "ON "
//                            + "max_id = url_check.id "
//                        + ") AS last_check_req "
//                    + "ON "
//                        + "url.id = last_check_req.url_id "
//                    + "ORDER BY "
//                        + "id_of_url DESC";









//        final String query = "SELECT last_check_req.mid AS lmid FROM url u LEFT JOIN "
//                + "(SELECT MAX(c.id) AS mid, c.url_id AS uid FROM url_check c GROUP BY uid) AS last_check_req "
//                + "ON id = last_check_req.uid";

        //        final RawSql rawSql = RawSqlBuilder.unparsed(query)
//                .columnMapping("u.id", "id")
//                .columnMapping("mca", "urlChecks")
////                .columnMapping("c.id", "urlChecks")
////                .columnMapping("c.status_code", "urlChecks.statusCode")
////                .columnMapping("c.url_id", "urlChecks.id")
//                .create();








//        final String query = "SELECT MAX(c.id) AS mid, c.url_id AS uid FROM url_check c GROUP BY uid";
//
//        final RawSql rawSql = RawSqlBuilder.unparsed(query)
//                .columnMapping("mid", "id")
//                .columnMapping("uid", "url.id")
////                .columnMapping("c.id", "urlChecks")
////                .columnMapping("c.status_code", "urlChecks.statusCode")
////                .columnMapping("c.url_id", "urlChecks.id")
//                .create();





        List<Url> urls
                = new QUrl()
                .select("id, name,")
                .urlChecks.fetchQuery("statusCode")
                .urlChecks.fetchQuery("max(createdAt)")                 // (2) fetchQuery ...
//                .status.notEqualTo(Order.Status.NEW)
                .findList();







        //List<UrlCheck> urlChecks = DB.find(UrlCheck.class).setRawSql(rawSql).findList();
        //List<Url> urls = DB.find(Url.class).setRawSql(rawSql).findList();
        //List<Url> urls = pagedUrls.getList();

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
