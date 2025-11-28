public class LevelConfig {
    private final int id;
    private final int gridRows;
    private final int gridCols;
    private final int dangerousCount;
    private final int rounds;

    public LevelConfig(int id, int gridRows, int gridCols, int dangerousCount, int rounds) {
        this.id = id;
        this.gridRows = gridRows;
        this.gridCols = gridCols;
        this.dangerousCount = dangerousCount;
        this.rounds = rounds;
    }

    public int getId() {
        return id;
    }

    public int getGridRows() {
        return gridRows;
    }

    public int getGridCols() {
        return gridCols;
    }

    public int getDangerousCount() {
        return dangerousCount;
    }

    public int getRounds() {
        return rounds;
    }

    @Override
    public String toString() {
        return "Level " + id + " (" + gridRows + "x" + gridCols + ")";
    }
}
