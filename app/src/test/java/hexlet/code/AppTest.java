package hexlet.code;

import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {
    private static Javalin app;
    private static String baseUrl;

    @Test
    void testInit() {
        assertThat(true).isTrue();
    }

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start();
        baseUrl = String.format("http://localhost:%d", app.port());
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @Test
    void mainPage() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody().toString()).contains("Бесплатно проверяйте сайты на SEO пригодность");
    }
}
