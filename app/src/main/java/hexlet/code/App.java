package hexlet.code;

import io.javalin.Javalin;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import org.thymeleaf.TemplateEngine;

public class App {
    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }

    public static void addRoutes(Javalin app) {
        app.get("/", ctx -> ctx.result("Hello, world!"));
    }

    private static Integer getPort() {
        String port = System.getenv().getOrDefault("PORT", "8080");
        return Integer.valueOf(port);
    }

    public static Javalin getApp() {
        var app = Javalin.create(config -> {
            config.enableDevLogging();
        });
        // JavalinThymeleaf
        // TemplateEngine templateEngine = new TemplateEngine();

        addRoutes(app);

        return app;
    }
}
