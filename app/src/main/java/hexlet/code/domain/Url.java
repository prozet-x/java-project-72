package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import io.ebeaninternal.server.util.Str;

import javax.annotation.processing.Generated;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public final class Url extends Model {

    @Id
    private long id;

    private String name;

//    public Url() {
//
//    }

    public Url (String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    @WhenCreated
    private Instant createdAt;
}
