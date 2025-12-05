import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LevelManager {
    private static final int MAX_RETRY_ATTEMPTS = 100;
    private static final int SPREAD_RETRY_ATTEMPTS = 200;
    private static final int STANDARD_RETRY_ATTEMPTS = 200;
    private static final double MIN_DISTANCE_RATIO = 0.6;
    private static final int HAZARD_SPREAD_DISTANCE = 3;
    private static final int HAZARD_STANDARD_DISTANCE = 2;
    private static final int MAX_HAZARD_RATIO = 3;

    private final DatabaseManager databaseManager;
    private final Random random = new Random();

    public LevelManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public LevelConfig loadLevel(int levelNumber) {
        String sql = "SELECT level_number, grid_rows, grid_cols, hazard_count, rounds " +
                    "FROM level_configs WHERE level_number = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, levelNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LevelConfig(
                        rs.getInt("level_number"),
                        rs.getInt("grid_rows"),
                        rs.getInt("grid_cols"),
                        rs.getInt("hazard_count"),
                        rs.getInt("rounds")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new LevelConfig(levelNumber, 5, 5, 4, 6);
    }

    public Dimension getGridSize(int levelNumber) {
        LevelConfig config = loadLevel(levelNumber);
        return new Dimension(config.getGridCols(), config.getGridRows());
    }

    public int getDangerousTileCount(int levelNumber) {
        return loadLevel(levelNumber).getDangerousCount();
    }

    public List<Point> generateTwoBluePoints(LevelConfig level, Set<Point> exclusions) {
        List<Point> points = new ArrayList<>();
        int rows = level.getGridRows();
        int cols = level.getGridCols();

        int gridDiagonal = (int) Math.sqrt(rows * rows + cols * cols);
        int minDistance = Math.max((int) (gridDiagonal * MIN_DISTANCE_RATIO), Math.max(rows, cols) - 1);

        Point first = generateFirstPoint(cols, rows, exclusions);
        points.add(first);

        Point second = generateSecondPoint(cols, rows, first, minDistance, exclusions);
        points.add(second);

        return points;
    }

    private Point generateFirstPoint(int cols, int rows, Set<Point> exclusions) {
        Point first = null;
        int attempts = 0;

        while (first == null && attempts < MAX_RETRY_ATTEMPTS) {
            Point p = new Point(random.nextInt(cols), random.nextInt(rows));
            if (exclusions == null || !exclusions.contains(p)) {
                first = p;
            }
            attempts++;
        }

        return first != null ? first : new Point(0, 0);
    }

    private Point generateSecondPoint(int cols, int rows, Point first, int minDistance, Set<Point> exclusions) {
        Point second = null;
        int attempts = 0;

        while (second == null && attempts < SPREAD_RETRY_ATTEMPTS) {
            Point p = new Point(random.nextInt(cols), random.nextInt(rows));
            int distance = Math.abs(p.x - first.x) + Math.abs(p.y - first.y);

            if (distance >= minDistance && !p.equals(first)) {
                if (exclusions == null || !exclusions.contains(p)) {
                    second = p;
                }
            }
            attempts++;
        }

        if (second == null) {
            second = getFallbackSecondPoint(cols, rows, first);
        }

        return second;
    }

    private Point getFallbackSecondPoint(int cols, int rows, Point first) {
        Point second;

        if (first.x < cols / 2 && first.y < rows / 2) {
            second = new Point(cols - 1, rows - 1);
        } else if (first.x >= cols / 2 && first.y < rows / 2) {
            second = new Point(0, rows - 1);
        } else if (first.x < cols / 2 && first.y >= rows / 2) {
            second = new Point(cols - 1, 0);
        } else {
            second = new Point(0, 0);
        }

        if (second.equals(first)) {
            second = new Point(cols - 1, rows - 1);
        }

        return second;
    }

    public Set<Point> generateDangerousTiles(LevelConfig level) {
        Set<Point> hazards = new HashSet<>();
        int rows = level.getGridRows();
        int cols = level.getGridCols();
        int targetCount = level.getDangerousCount();

        int maxSafeHazards = (rows * cols) / MAX_HAZARD_RATIO;
        targetCount = Math.min(targetCount, maxSafeHazards);

        generateSpreadHazards(hazards, cols, rows, targetCount / 2);
        generateStandardHazards(hazards, cols, rows, targetCount);

        return hazards;
    }

    private void generateSpreadHazards(Set<Point> hazards, int cols, int rows, int spreadCount) {
        int attempts = 0;

        while (hazards.size() < spreadCount && attempts < SPREAD_RETRY_ATTEMPTS) {
            Point p = new Point(random.nextInt(cols), random.nextInt(rows));

            if (!isTooCloseToExisting(p, hazards, HAZARD_SPREAD_DISTANCE)) {
                hazards.add(p);
            }
            attempts++;
        }
    }

    private void generateStandardHazards(Set<Point> hazards, int cols, int rows, int targetCount) {
        int attempts = 0;

        while (hazards.size() < targetCount && attempts < STANDARD_RETRY_ATTEMPTS) {
            Point p = new Point(random.nextInt(cols), random.nextInt(rows));

            if (!isTooCloseToExisting(p, hazards, HAZARD_STANDARD_DISTANCE)) {
                hazards.add(p);
            }
            attempts++;
        }
    }

    private boolean isTooCloseToExisting(Point p, Set<Point> existing, int minDistance) {
        for (Point existingPoint : existing) {
            int distance = Math.abs(p.x - existingPoint.x) + Math.abs(p.y - existingPoint.y);
            if (distance < minDistance) {
                return true;
            }
        }
        return false;
    }

    public List<LevelConfig> getAvailableLevels(User user) {
        List<LevelConfig> levels = new ArrayList<>();
        String sql = "SELECT level_number, grid_rows, grid_cols, hazard_count, rounds " +
                    "FROM level_configs WHERE level_number <= ? ORDER BY level_number";
        int unlocked = Math.max(user.getUnlockedLevel(), 1);

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, unlocked);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    levels.add(createLevelConfigFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (levels.isEmpty()) {
            levels.add(loadLevel(1));
        }

        return levels;
    }

    public List<LevelConfig> getAllLevels() {
        List<LevelConfig> levels = new ArrayList<>();
        String sql = "SELECT level_number, grid_rows, grid_cols, hazard_count, rounds " +
                    "FROM level_configs ORDER BY level_number";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                levels.add(createLevelConfigFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (levels.isEmpty()) {
            levels.add(loadLevel(1));
        }

        return levels;
    }

    private LevelConfig createLevelConfigFromResultSet(ResultSet rs) throws SQLException {
        return new LevelConfig(
            rs.getInt("level_number"),
            rs.getInt("grid_rows"),
            rs.getInt("grid_cols"),
            rs.getInt("hazard_count"),
            rs.getInt("rounds")
        );
    }

    public int getRoundCount(int levelNumber) {
        return loadLevel(levelNumber).getRounds();
    }

    public int getMaxLevelInDatabase() {
    String sql = "SELECT MAX(level_number) as max_level FROM level_configs";
    try (Connection conn = databaseManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            return rs.getInt("max_level");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 1; // Default jika gagal
}
}