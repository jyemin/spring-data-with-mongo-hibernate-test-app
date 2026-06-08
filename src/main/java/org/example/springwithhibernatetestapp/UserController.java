package org.example.springwithhibernatetestapp;

import com.mongodb.client.MongoClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.boot.mongodb.autoconfigure.MongoConnectionDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final MongoClient mongoClient;
    private final String databaseName;

    public UserController(
            UserRepository userRepository, MongoClient mongoClient, MongoConnectionDetails connectionDetails) {
        this.userRepository = userRepository;
        this.mongoClient = mongoClient;
        this.databaseName = connectionDetails.getConnectionString().getDatabase();
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    @PostMapping("/user")
    public User createUser() {
        User u = new User("test name");
        this.userRepository.save(u);
        return u;
    }

    // Reads the same collection Hibernate persists to, but through the raw Spring-managed MongoClient bean —
    // the very client the JPA layer borrows. One client and one connection pool, two access paths. The
    // returned mongoClientInstance is the identity of that shared bean; /actuator/metrics shows its single pool.
    @GetMapping("/users/via-driver")
    public Map<String, Object> usersViaDriver() {
        List<String> documents = new ArrayList<>();
        mongoClient.getDatabase(databaseName).getCollection("User").find().forEach(doc -> documents.add(doc.toJson()));
        return Map.of(
                "mongoClientInstance",
                mongoClient.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(mongoClient)),
                "database", databaseName,
                "collection", "User",
                "count", documents.size(),
                "documents", documents);
    }
}
