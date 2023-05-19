package hexlet.code.controllers;

import io.javalin.http.Handler;

public final class RootController {
    public static Handler root = ctx -> {
        ctx.render("layouts/application.html");
    };
}
