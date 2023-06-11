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
        HttpResponse responsePost = Unirest.post(baseUrl + "/urls")
                .field("url", "https://www.avito.ru:3456/pp/abc?efg=qwe&rty=123")
                .asEmpty();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).contains("/urls");

        HttpResponse<String> responseGet = Unirest.get(baseUrl + "/urls").queryString("page", "2").asString();
        assertThat(responseGet.getStatus()).isEqualTo(200);
        String body = responseGet.getBody().toString();
        assertThat(body).contains("https://www.avito.ru:3456");
        assertThat(body).contains("Страница успешно добавлена");
    }

    @Test
    void addUrlBad1() {
        HttpResponse<String> responsePost = Unirest.post(baseUrl + "/urls")
                .field("url", "https://www.avito.ru:letters")
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(200);
        assertThat(responsePost.getBody().toString()).contains("Некорректный Url");

        HttpResponse<String> responseGet = Unirest.get(baseUrl + "/urls").queryString("page", "2").asString();
        assertThat(responseGet.getStatus()).isEqualTo(200);
        assertThat(responseGet.getBody().toString()).doesNotContain("www.avito.ru");
    }

    @Test
    void addUrlBad2() {
        HttpResponse<String> responsePost = Unirest.post(baseUrl + "/urls")
                .field("url", "Just text. Bad URL.")
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(200);
        assertThat(responsePost.getBody().toString()).contains("Некорректный Url");

        HttpResponse<String> responseGet = Unirest.get(baseUrl + "/urls").queryString("page", "2").asString();
        assertThat(responseGet.getStatus()).isEqualTo(200);
        assertThat(responsePost.getBody().toString()).doesNotContain("Just text. Bad URL.");
    }
}
