package hexlet.code;

import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {
    private static Javalin app;
    private static String baseUrl;
    private static Database database;

    @Test
    void testInit() {
        assertThat(true).isTrue();
    }

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start();
        baseUrl = String.format("http://localhost:%d", app.port());
        database = DB.getDefault();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    public void beforeEach() {
        database.script().run("/truncate.sql");
        database.script().run("/seed-test-db.sql");
    }

    @Test
    void mainPage() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody().toString()).contains("Бесплатно проверяйте сайты на SEO пригодность");
    }

    @Test
    void urlList() {
        HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
        assertThat(response.getStatus()).isEqualTo(200);
        String body = response.getBody().toString();
        assertThat(body).contains("Сайты");
        assertThat(body).contains("The Man Within");
    }

    @Test
    void addUrlGood() {
        HttpResponse response = Unirest.post(baseUrl + "/urls")
                .field("url", "https://www.avito.ru/")
                .asEmpty();
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaders().getFirst("Location")).contains("/urls");


    }
}
