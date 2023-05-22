package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;

import java.util.List;

public final class UrlController {
    public static Handler newUrl = ctx -> {
        String urlAddress = ctx.formParam("url");

        if (urlAddress.isEmpty() || urlAddress.equals("123")) {
            ctx.attribute("urlAddress", urlAddress);
            ctx.sessionAttribute("flash", "Некорректный Url");
            ctx.sessionAttribute("flash-type", "danger");
            //ctx.render("layouts/application.html");
        } else {
            Url url = new Url(urlAddress);
            url.save();
        }

        ctx.redirect("/");
    };

    public static Handler urlList = ctx -> {
        List<Url> urls = new QUrl().findList();
        ctx.attribute("urls", urls);
        ctx.render("list.html");
    };
}
