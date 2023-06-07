package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class UrlController {
    public static Handler newUrl = ctx -> {
        String inputtedUrl = ctx.formParam("url");
        URL url;

        try {
            url = new URL(inputtedUrl);
        } catch (MalformedURLException ex) {
            ctx.sessionAttribute("flash", "Некорректный Url");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.render("index.html");
            return;
        }

        Url newUrl = new Url(
                String.format("%s://%s%s",
                    url.getProtocol(),
                    url.getHost(),
                    url.getPort() < 0
                            ? ""
                            : String.format(":%s", Integer.toString(url.getPort()))
                )
        );

        Url existingUrl = new QUrl().name.equalTo(newUrl.getName()).findOne();
        if (existingUrl != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "warning");
        } else {
            newUrl.save();
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");
        }

        ctx.redirect("/urls");
    };

    public static Handler urlList = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

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
        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };
}
