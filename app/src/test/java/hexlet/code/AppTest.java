package hexlet.code;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.http.entity.mime.content.StringBody;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppTest {
    @Test
    public void testInit() {
        assertTrue(false);
    }

    @Test
    public void mainPage() {
        HttpResponse<String> response = Unirest.get("http://localhost:8080/").asString();
        response.getBody().
    }
}
