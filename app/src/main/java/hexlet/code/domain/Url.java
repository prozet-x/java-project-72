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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @WhenCreated
    private Instant createdAt;
}
