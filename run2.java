import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.IntStream;

public class Main {
    private static final int[] MOVE_X = {0, 0, -1, 1};
    private static final int[] MOVE_Y = {-1, 1, 0, 0};
    private static final char[] KEYS_CHAR = new char[26];
    private static final char[] DOORS_CHAR = new char[26];

    static {
        for (int i = 0; i < 26; i++) {
            KEYS_CHAR[i] = (char) ('a' + i);
            DOORS_CHAR[i] = (char) ('A' + i);
        }
    }

    private static class PathInfo {
        final int destination;
        final int length;
        final Set<Character> requiredDoors;

        PathInfo(int dest, int len, Set<Character> doors) {
            this.destination = dest;
            this.length = len;
            this.requiredDoors = doors;
        }
    }

    private static class ExplorationState implements Comparable<ExplorationState> {
        final int[] botLocations;
        final Set<Character> acquiredKeys;
        final int currentSteps;

        ExplorationState(int[] bots, Set<Character> keys, int steps) {
            this.botLocations = bots.clone();
            this.acquiredKeys = new HashSet<>(keys);
            this.currentSteps = steps;
        }

        public int compareTo(ExplorationState other) {
            return Integer.compare(this.currentSteps, other.currentSteps);
        }

        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ExplorationState)) return false;
            ExplorationState es = (ExplorationState) obj;
            return Arrays.equals(botLocations, es.botLocations) &&
                    acquiredKeys.equals(es.acquiredKeys);
        }

        public int hashCode() {
            return Arrays.hashCode(botLocations) * 31 + acquiredKeys.hashCode();
        }
    }

    private static char[][] getInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> lines = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            lines.add(line);
        }

        char[][] maze = new char[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            maze[i] = lines.get(i).toCharArray();
        }

        return maze;
    }

    public static void main(String[] args) throws IOException {
        char[][] data = getInput();
        int steps = solve(data);

        if (steps == Integer.MAX_VALUE) {
            System.out.println("No solution found");
        } else {
            System.out.println(steps);
        }
    }

    private static int solve(char[][] data) {
        char[][] grid = data;
        List<int[]> botStarts = new ArrayList<>();
        Map<Character, int[]> keyMap = new HashMap<>();

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                char cell = grid[i][j];
                if (cell == '@') {
                    botStarts.add(new int[]{i, j});
                } else if (cell >= 'a' && cell <= 'z') {
                    keyMap.put(cell, new int[]{i, j});
                }
            }
        }

        List<int[]> graphNodes = new ArrayList<>(botStarts);
        List<Character> keyList = new ArrayList<>(keyMap.keySet());
        Collections.sort(keyList);

        Map<Character, Integer> keyIndexes = new HashMap<>();
        for (int i = 0; i < keyList.size(); i++) {
            char key = keyList.get(i);
            graphNodes.add(keyMap.get(key));
            keyIndexes.put(key, i + botStarts.size());
        }

        List<List<PathInfo>> adjacencyList = buildGraph(grid, graphNodes, keyIndexes);

        PriorityQueue<ExplorationState> stateQueue = new PriorityQueue<>();
        Map<ExplorationState, Integer> visitedStates = new HashMap<>();

        int[] initialBots = IntStream.range(0, botStarts.size()).toArray();
        ExplorationState initialState = new ExplorationState(initialBots, Collections.emptySet(), 0);
        stateQueue.add(initialState);
        visitedStates.put(initialState, 0);

        while (!stateQueue.isEmpty()) {
            ExplorationState current = stateQueue.poll();

            if (current.acquiredKeys.size() == keyList.size()) {
                return current.currentSteps;
            }

            for (int botId = 0; botId < current.botLocations.length; botId++) {
                int currentNode = current.botLocations[botId];

                for (PathInfo path : adjacencyList.get(currentNode)) {
                    char targetKey = keyList.get(path.destination - botStarts.size());
                    if (current.acquiredKeys.contains(targetKey)) continue;

                    boolean canPass = true;
                    for (char door : path.requiredDoors) {
                        if (!current.acquiredKeys.contains(Character.toLowerCase(door))) {
                            canPass = false;
                            break;
                        }
                    }
                    if (!canPass) continue;

                    Set<Character> newKeys = new HashSet<>(current.acquiredKeys);
                    newKeys.add(targetKey);

                    int[] newBotPos = current.botLocations.clone();
                    newBotPos[botId] = path.destination;

                    ExplorationState newState = new ExplorationState(newBotPos, newKeys,
                            current.currentSteps + path.length);

                    if (newState.currentSteps < visitedStates.getOrDefault(newState, Integer.MAX_VALUE)) {
                        visitedStates.put(newState, newState.currentSteps);
                        stateQueue.add(newState);
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    private static List<List<PathInfo>> buildGraph(char[][] grid, List<int[]> nodes,
                                                   Map<Character, Integer> keyIndexes) {
        List<List<PathInfo>> graph = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            graph.add(new ArrayList<>());
        }

        for (int i = 0; i < nodes.size(); i++) {
            boolean[][] visited = new boolean[grid.length][grid[0].length];
            Queue<PathNode> queue = new ArrayDeque<>();

            int startRow = nodes.get(i)[0];
            int startCol = nodes.get(i)[1];
            visited[startRow][startCol] = true;
            queue.add(new PathNode(startRow, startCol, 0, new HashSet<>()));

            while (!queue.isEmpty()) {
                PathNode current = queue.poll();
                char cell = grid[current.row][current.col];
                Set<Character> doors = new HashSet<>(current.doorsPassed);

                if (cell >= 'A' && cell <= 'Z') {
                    doors.add(cell);
                }

                if (cell >= 'a' && cell <= 'z' && keyIndexes.containsKey(cell)) {
                    int targetNode = keyIndexes.get(cell);
                    if (targetNode != i) {
                        graph.get(i).add(new PathInfo(targetNode, current.steps, doors));
                    }
                }

                for (int d = 0; d < 4; d++) {
                    int newRow = current.row + MOVE_X[d];
                    int newCol = current.col + MOVE_Y[d];
                    if (newRow >= 0 && newRow < grid.length && newCol >= 0 && newCol < grid[0].length &&
                            grid[newRow][newCol] != '#' && !visited[newRow][newCol]) {
                        visited[newRow][newCol] = true;
                        queue.add(new PathNode(newRow, newCol, current.steps + 1, doors));
                    }
                }
            }
        }
        return graph;
    }

    private static class PathNode {
        final int row;
        final int col;
        final int steps;
        final Set<Character> doorsPassed;

        PathNode(int r, int c, int s, Set<Character> doors) {
            this.row = r;
            this.col = c;
            this.steps = s;
            this.doorsPassed = doors;
        }
    }
}