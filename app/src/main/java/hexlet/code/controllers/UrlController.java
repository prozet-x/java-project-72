package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;

import java.util.List;

public final class UrlController {
    public static Handler newUrl = ctx -> {
        String urlAddress = ctx.formParam("url");

        if (urlAddress.isEmpty() || urlAddress.equals("123")) {
            //ctx.attribute("urlAddress", urlAddress);
            ctx.sessionAttribute("flash", "Необходимо ввести корректный Url");
            ctx.sessionAttribute("flash-type", "danger");
            //ctx.render("layouts/application.html");
            ctx.redirect("/");
            return;
        }

        Url url = new Url(urlAddress);
        url.save();
    };
}
