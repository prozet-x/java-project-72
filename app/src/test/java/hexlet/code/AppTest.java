package hexlet.code;

import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Scanner;
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
        app.start(App.getPort());
        baseUrl = String.format("http://localhost:%d", app.port());
        database = DB.getDefault();
    }

    @AfterAll
    public static final void afterAll() {
        app.stop();
    }

    @BeforeEach
    public final void beforeEach() {
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

        HttpResponse responsePost2 = Unirest.post(baseUrl + "/urls")
                .field("url", "https://www.mail.ru/pp/abc?efg=qwe&rty=123")
                .asEmpty();
        assertThat(responsePost2.getStatus()).isEqualTo(302);
        assertThat(responsePost2.getHeaders().getFirst("Location")).contains("/urls");

        HttpResponse<String> responseGet = Unirest.get(baseUrl + "/urls").queryString("page", "2").asString();
        assertThat(responseGet.getStatus()).isEqualTo(200);
        String body = responseGet.getBody().toString();
        assertThat(body).contains("https://www.avito.ru:3456");
        assertThat(body).contains("https://www.mail.ru");
        assertThat(body).doesNotContain("https://www.mail.ru:");
        assertThat(body).contains("Страница успешно добавлена");
    }

    @Test
    void addUrlBadPort() {
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
    void addUrlBadUrl() {
        HttpResponse<String> responsePost = Unirest.post(baseUrl + "/urls")
                .field("url", "Just text. Bad URL.")
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(200);
        assertThat(responsePost.getBody().toString()).contains("Некорректный Url");

        HttpResponse<String> responseGet = Unirest.get(baseUrl + "/urls").queryString("page", "2").asString();
        assertThat(responseGet.getStatus()).isEqualTo(200);
        assertThat(responsePost.getBody().toString()).doesNotContain("Just text. Bad URL.");
    }

    @Test
    void addUrlBadSameUrl() {
        Unirest.post(baseUrl + "/urls")
            .field("url", "https://www.avito.ru:3456/pp/abc?efg=qwe&rty=123")
            .asEmpty();

        Unirest.post(baseUrl + "/urls")
                .field("url", "https://www.avito.ru:3456")
                .asEmpty();

        HttpResponse<String> responseGet = Unirest.get(baseUrl + "/urls").queryString("page", "2").asString();
        String body = responseGet.getBody().toString();
        assertThat(body).containsOnlyOnce("https://www.avito.ru:3456");
        assertThat(body).contains("Страница уже существует");
    }

    @Test
    void detailShowUrl() {
        HttpResponse<String> response = Unirest.get(baseUrl + "/urls/1").asString();
        assertThat(response.getStatus()).isEqualTo(200);
        String body = response.getBody().toString();
        assertThat(body).contains("The Man Within");
        assertThat(body).contains("Проверки");
    }

    @Test
    void check() throws IOException {
        String fixturePath = "./src/test/resources/fixtures/response.html";

        Scanner scanner = new Scanner(Paths.get(fixturePath), StandardCharsets.UTF_8.name());
        String data = scanner.useDelimiter("\\A").next();
        scanner.close();

        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(data));
        server.start();
        String address = server.url("/").toString();

        HttpResponse addUrlPost = Unirest.post(baseUrl + "/urls")
                .field("url", address)
                .asEmpty();
        assertThat(addUrlPost.getStatus()).isEqualTo(302);

        HttpResponse checkPost = Unirest.post(baseUrl + "/urls/18/checks").asEmpty();
        assertThat(checkPost.getStatus()).isEqualTo(302);
        assertThat(checkPost.getHeaders().getFirst("Location")).contains("/urls/18");

        HttpResponse get = Unirest.get(baseUrl + "/urls/18").asString();
        String body = get.getBody().toString();
        assertThat(body).contains("Test title");
        assertThat(body).contains("Description test text");
        assertThat(body).contains("H1 tag test text");
        assertThat(body).doesNotContain("Other text");
        assertThat(body).contains("Страница успешно проверена");

        server.shutdown();
    }
}
