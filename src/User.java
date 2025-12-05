import java.time.LocalDateTime;

public class User {
    private final int id;
    private final String username;
    private final int unlockedLevel;
    private final long totalScore;
    private final LocalDateTime createdAt;

    public User(int id, String username, int unlockedLevel, long totalScore, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.unlockedLevel = unlockedLevel;
        this.totalScore = totalScore;
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

    public long getTotalScore() {
        return totalScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return username;
    }
}
