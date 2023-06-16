package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.DB;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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


//        QCustomer cust = QCustomer.alias();
//        QContact cont = QContact.alias();
//
//        List<Customer> customers =
//                new QCustomer()
//                        .select(cust.name, cust.version, cust.whenCreated)    // root level properties
//                        .contacts.fetch(cont.email)                           // contacts is a OneToMany path
//
//                        .name.istartsWith("Rob")
//                        .findList();


        List<Url> urls1 =
                DB.find(Url.class)
                        .select("id, name")    // root level properties
                        .fetch("urlChecks", "url")              // contacts is a OneToMany path
                        .where()
                        .istartsWith("name", "Rob")
                        .findList();


        QUrl url = QUrl.alias();
        QUrlCheck urlCheck = QUrlCheck.alias();
        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy().id.asc()
                .select(url.id, url.name).urlChecks.fetch(urlCheck.statusCode, urlCheck.createdAt)
                .findPagedList();



//        PagedList<Url> pagedUrls = new QUrl()
//                .setFirstRow(page * rowsPerPage)
//                .setMaxRows(rowsPerPage)
//                .orderBy()
//                .id.asc()
//                .findPagedList();

        List<Url> urls = pagedUrls.getList();

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
