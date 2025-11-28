import java.time.LocalDateTime;

public class User {
    private final int id;
    private final String username;
    private final int unlockedLevel;
    private final LocalDateTime createdAt;

    public User(int id, String username, int unlockedLevel, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.unlockedLevel = unlockedLevel;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public int getUnlockedLevel() {
        return unlockedLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return username;
    }
}
