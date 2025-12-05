import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final int LEADERBOARD_LIMIT = 100;

    private final DatabaseManager databaseManager;
    private String lastError;

    public UserManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public String getLastError() {
        return lastError;
    }

    public User createUser(String name) {
        lastError = null;

        String checkSql = "SELECT id FROM saves WHERE name = ?";

        try (Connection conn = databaseManager.getConnection()) {

            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, name);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next()) {
                        lastError = "Username already exists!";
                        return null; // stop
                    }
                }
            }

            String insertSql = "INSERT INTO saves(name, unlocked_level, total_score) VALUES (?, 1, 0)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long saveId = rs.getLong(1);
                        return new User((int) saveId, name, 1, 0, LocalDateTime.now());
                    }
                }
            }

        } catch (SQLException e) {
            lastError = e.getMessage();
        }

        return null;
    }


    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name, unlocked_level, total_score, created_at " +
                    "FROM saves ORDER BY created_at DESC";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public User getUserById(int id) {
        String sql = "SELECT id, name, unlocked_level, total_score, created_at " +
                    "FROM saves WHERE id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("name");
        int unlocked = rs.getInt("unlocked_level");
        long totalScore = rs.getLong("total_score");
        Timestamp created = rs.getTimestamp("created_at");

        return new User(id, username, unlocked, totalScore,
            created != null ? created.toLocalDateTime() : LocalDateTime.now());
    }

    public void updateUnlockedLevel(int userId, int newLevel) {
        String sql = "UPDATE saves SET unlocked_level = ? WHERE id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newLevel);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private long getOrCreateLevelRecord(Connection conn, int saveId, int levelNumber) throws SQLException {
        String select = "SELECT id FROM levels WHERE save_id = ? AND level_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setInt(1, saveId);
            ps.setInt(2, levelNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }

        String insert = "INSERT INTO levels (save_id, level_number) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, saveId);
            ps.setInt(2, levelNumber);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return -1;
    }

    public int getSavedRound(int saveId, int levelNumber) {
        String sql = "SELECT current_round FROM levels WHERE save_id = ? AND level_number = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            ps.setInt(2, levelNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("current_round");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void saveCurrentProgress(int saveId, int levelNumber, int roundNumber) {
        try (Connection conn = databaseManager.getConnection()) {
            getOrCreateLevelRecord(conn, saveId, levelNumber);

            String sql = "UPDATE levels SET current_round = ? WHERE save_id = ? AND level_number = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, roundNumber);
                ps.setInt(2, saveId);
                ps.setInt(3, levelNumber);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void resetLevelProgress(int saveId, int levelNumber) {
        String sql = "UPDATE levels SET current_round = 1, temp_score = 0 " +
                    "WHERE save_id = ? AND level_number = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            ps.setInt(2, levelNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getLevelProgressSummary(int saveId) {
        StringBuilder summary = new StringBuilder();
        String sql = "SELECT level_number, current_round FROM levels " +
                    "WHERE save_id = ? AND current_round > 1 ORDER BY level_number";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean hasProgress = false;
                while (rs.next()) {
                    if (hasProgress) {
                        summary.append(", ");
                    }
                    summary.append("L").append(rs.getInt("level_number"))
                           .append("-R").append(rs.getInt("current_round"));
                    hasProgress = true;
                }
                if (hasProgress) {
                    return "📍 Saved: " + summary.toString();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public long getTempScore(int saveId, int levelNumber) {
        String sql = "SELECT temp_score FROM levels WHERE save_id = ? AND level_number = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            ps.setInt(2, levelNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("temp_score");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void updateTempScore(int saveId, int levelNumber, long score) {
        try (Connection conn = databaseManager.getConnection()) {
            getOrCreateLevelRecord(conn, saveId, levelNumber);

            String sql = "UPDATE levels SET temp_score = ? WHERE save_id = ? AND level_number = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, score);
                ps.setInt(2, saveId);
                ps.setInt(3, levelNumber);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getHighestAttemptedLevel(int saveId) {
        String sql = "SELECT MAX(level_number) as max_level FROM levels " +
                    "WHERE save_id = ? AND is_completed = 1";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("max_level");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean isLevelCompletedAndLocked(int saveId, int levelNumber) {
        int highestAttempted = getHighestAttemptedLevel(saveId);
        boolean isHighestLevel = (levelNumber >= highestAttempted);

        if (isHighestLevel) {
            return false;
        }

        String sql = "SELECT is_completed FROM levels WHERE save_id = ? AND level_number = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            ps.setInt(2, levelNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_completed");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void saveLevelScore(int saveId, int levelNumber, long levelScore) {
        int highestAttempted = getHighestAttemptedLevel(saveId);
        boolean isHighestLevel = (levelNumber >= highestAttempted);

        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                getOrCreateLevelRecord(conn, saveId, levelNumber);

                updateLevelScore(conn, saveId, levelNumber, levelScore, isHighestLevel);
                recalculateTotalScore(conn, saveId);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateLevelScore(Connection conn, int saveId, int levelNumber, 
                                  long levelScore, boolean isHighestLevel) throws SQLException {
        String updateLevel;

        if (isHighestLevel) {
            updateLevel = "UPDATE levels SET " +
                        "level_score = ?, " +
                        "is_completed = 1, " +
                        "completed_at = CURRENT_TIMESTAMP, " +
                        "temp_score = 0, " +
                        "current_round = 1 " +
                        "WHERE save_id = ? AND level_number = ?";
        } else {
            updateLevel = "UPDATE levels SET " +
                        "level_score = IF(is_completed = 0, ?, level_score), " +
                        "is_completed = 1, " +
                        "completed_at = IF(completed_at IS NULL, CURRENT_TIMESTAMP, completed_at), " +
                        "temp_score = 0, " +
                        "current_round = 1 " +
                        "WHERE save_id = ? AND level_number = ?";
        }

        try (PreparedStatement ps = conn.prepareStatement(updateLevel)) {
            ps.setLong(1, levelScore);
            ps.setInt(2, saveId);
            ps.setInt(3, levelNumber);
            ps.executeUpdate();
            }
        }

        private void recalculateTotalScore(Connection conn, int saveId) throws SQLException {
            String updateTotal = "UPDATE saves s SET s.total_score = (" +
                            "    SELECT COALESCE(SUM(l.level_score), 0) " +
                            "    FROM levels l " +
                            "    WHERE l.save_id = s.id AND l.is_completed = 1" +
                            ") WHERE s.id = ?";

            try (PreparedStatement ps = conn.prepareStatement(updateTotal)) {
                ps.setInt(1, saveId);
                ps.executeUpdate();
            }
        }

    public void recordRoundResult(int saveId, int levelNumber, int roundIdx, long elapsedMs,
                                  long roundScore, int steps, int hazardTouches) {
    }

    public List<User> getLeaderboard() {
        List<User> leaderboard = new ArrayList<>();
        String sql = "SELECT id, name, unlocked_level, total_score, created_at " +
                    "FROM saves " +
                    "ORDER BY total_score DESC, unlocked_level DESC " +
                    "LIMIT ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, LEADERBOARD_LIMIT);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    leaderboard.add(createUserFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaderboard;
    }

    public boolean isLastLevelInDatabase(int levelNumber, int maxLevelInDB) {
        return levelNumber == maxLevelInDB;
    }

    public void saveLevelScoreRepeatable(int saveId, int levelNumber, long levelScore) {
        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                getOrCreateLevelRecord(conn, saveId, levelNumber);

                String updateLevel = "UPDATE levels SET " +
                            "level_score = level_score + ?, " + 
                            "is_completed = 1, " +
                            "temp_score = 0, " +
                            "current_round = 1 " +
                            "WHERE save_id = ? AND level_number = ?";

                try (PreparedStatement ps = conn.prepareStatement(updateLevel)) {
                    ps.setLong(1, levelScore);
                    ps.setInt(2, saveId);
                    ps.setInt(3, levelNumber);
                    ps.executeUpdate();
                }

                recalculateTotalScore(conn, saveId);

                conn.commit();
                
                System.out.println("DEBUG - Repeatable Score Added: +" + levelScore + 
                                " to Level " + levelNumber);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}