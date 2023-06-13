package hexlet.code.domain;

import io.ebean.annotation.WhenCreated;
import io.ebean.typequery.PInstant;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import java.time.Instant;

@Entity
public class UrlCheck {
    @Id
    private long id;

    private int statusCode;

    private String title;
    private String h1;

    @Lob
    private String description;

    @OneToMany
    private Url url;

    @WhenCreated
    private Instant createdAt;
}
