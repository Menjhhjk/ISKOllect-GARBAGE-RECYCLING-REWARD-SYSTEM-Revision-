package com.iskollect.dao;

import com.iskollect.model.DashboardStats;
import com.iskollect.model.Reward;
import com.iskollect.model.Student;
import com.iskollect.model.TransactionEntry;
import com.iskollect.util.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class IskollectRepository {
    private final ConnectionFactory connections;

    public IskollectRepository(ConnectionFactory connections) {
        this.connections = connections;
    }

    public void verifySchema() throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = current_schema()
              AND table_name IN (
                'users', 'students', 'bottle_records',
                'points_ledger', 'rewards_catalog', 'redemptions'
              )
            """;
        try (Connection connection = connections.open();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            result.next();
            if (result.getInt(1) != 6) {
                throw new SQLException(
                    "The ISKOllect v2 schema is incomplete. Run sql/iskollect_schema.sql first."
                );
            }
        }
    }

    public int adminCount() throws SQLException {
        try (Connection connection = connections.open();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM users")) {
            result.next();
            return result.getInt(1);
        }
    }

    public Optional<String> findPasswordHash(String username) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                    ? Optional.of(result.getString("password_hash"))
                    : Optional.empty();
            }
        }
    }

    public List<String> findAdminPasswordHashes() throws SQLException {
        List<String> hashes = new ArrayList<>();
        try (Connection connection = connections.open();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                 "SELECT password_hash FROM users ORDER BY user_id"
             )) {
            while (result.next()) {
                hashes.add(result.getString("password_hash"));
            }
        }
        return hashes;
    }

    public void createAdmin(String username, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.executeUpdate();
        }
    }

    public DashboardStats dashboardStats() throws SQLException {
        String sql = """
            SELECT
                (SELECT COUNT(*) FROM students) AS students,
                (SELECT COALESCE(SUM(bottles_collected), 0) FROM bottle_records) AS bottles,
                (SELECT COALESCE(SUM(points_earned), 0) FROM students) AS points,
                (SELECT COUNT(*) FROM redemptions) AS redemptions
            """;
        try (Connection connection = connections.open();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return new DashboardStats(
                result.getInt("students"),
                result.getInt("bottles"),
                result.getBigDecimal("points"),
                result.getInt("redemptions")
            );
        }
    }

    public List<Student> findStudents(String query) throws SQLException {
        boolean filtered = query != null && !query.isBlank();
        String sql = """
            SELECT student_id, student_name, bottle_count, points_earned, registration_date
            FROM students
            """ + (filtered ? " WHERE LOWER(student_name) LIKE LOWER(?)" : "") +
            " ORDER BY student_name";
        List<Student> students = new ArrayList<>();
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (filtered) {
                statement.setString(1, "%" + query.trim() + "%");
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    students.add(mapStudent(result));
                }
            }
        }
        return students;
    }

    public List<Student> findTopStudents(int limit) throws SQLException {
        String sql = """
            SELECT student_id, student_name, bottle_count, points_earned, registration_date
            FROM students
            ORDER BY bottle_count DESC, points_earned DESC, student_name
            LIMIT ?
            """;
        List<Student> students = new ArrayList<>();
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    students.add(mapStudent(result));
                }
            }
        }
        return students;
    }

    public Student createStudent(String name) throws SQLException {
        String sql = """
            INSERT INTO students (student_name)
            VALUES (?)
            RETURNING student_id, student_name, bottle_count, points_earned, registration_date
            """;
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return mapStudent(result);
            }
        }
    }

    public Student registerStudentWithSubmission(
        String name,
        int bottleCount,
        BigDecimal points
    ) throws SQLException {
        try (Connection connection = connections.open()) {
            connection.setAutoCommit(false);
            try {
                Student student;
                String studentSql = """
                    INSERT INTO students (student_name)
                    VALUES (?)
                    RETURNING student_id, student_name, bottle_count,
                              points_earned, registration_date
                    """;
                try (PreparedStatement statement = connection.prepareStatement(studentSql)) {
                    statement.setString(1, name);
                    try (ResultSet result = statement.executeQuery()) {
                        result.next();
                        student = mapStudent(result);
                    }
                }

                int recordId = insertBottleRecord(
                    connection, student.id(), bottleCount, points
                );
                updateStudentTotals(
                    connection, student.id(), bottleCount, points
                );
                insertLedger(
                    connection, student.id(), points, "bottle_submission", recordId
                );
                connection.commit();
                return new Student(
                    student.id(),
                    student.name(),
                    bottleCount,
                    points,
                    student.registeredAt()
                );
            } catch (Exception exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    public void renameStudent(int studentId, String name) throws SQLException {
        String sql = "UPDATE students SET student_name = ? WHERE student_id = ?";
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setInt(2, studentId);
            requireChanged(statement.executeUpdate(), "Student not found.");
        }
    }

    public Student updateStudentDetails(
        int studentId,
        String name,
        int newBottleCount,
        BigDecimal newPoints
    ) throws SQLException {
        try (Connection connection = connections.open()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal previousPoints = lockStudent(connection, studentId);
                BigDecimal pointsDelta = newPoints.subtract(previousPoints);

                String sql = """
                    UPDATE students
                    SET student_name = ?, bottle_count = ?, points_earned = ?
                    WHERE student_id = ?
                    RETURNING student_id, student_name, bottle_count,
                              points_earned, registration_date
                    """;
                Student updated;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, name);
                    statement.setInt(2, newBottleCount);
                    statement.setBigDecimal(3, newPoints);
                    statement.setInt(4, studentId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new IllegalArgumentException("Student not found.");
                        }
                        updated = mapStudent(result);
                    }
                }

                if (pointsDelta.signum() != 0) {
                    insertLedger(connection, studentId, pointsDelta, "admin_adjustment", studentId);
                }
                connection.commit();
                return updated;
            } catch (Exception exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    public void deleteStudent(int studentId) throws SQLException {
        try (Connection connection = connections.open();
             PreparedStatement statement =
                 connection.prepareStatement("DELETE FROM students WHERE student_id = ?")) {
            statement.setInt(1, studentId);
            requireChanged(statement.executeUpdate(), "Student not found.");
        }
    }

    public List<Reward> findRewards() throws SQLException {
        String sql = """
            SELECT reward_id, reward_name, description, points_required, available
            FROM rewards_catalog
            ORDER BY points_required, reward_name
            """;
        List<Reward> rewards = new ArrayList<>();
        try (Connection connection = connections.open();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                rewards.add(mapReward(result));
            }
        }
        return rewards;
    }

    public Reward createReward(
        String name,
        String description,
        BigDecimal pointsRequired
    ) throws SQLException {
        String sql = """
            INSERT INTO rewards_catalog
                (reward_name, description, points_required, available)
            VALUES (?, ?, ?, TRUE)
            RETURNING reward_id, reward_name, description, points_required, available
            """;
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setBigDecimal(3, pointsRequired);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return mapReward(result);
            }
        }
    }

    public void updateReward(
        int rewardId,
        String name,
        String description,
        BigDecimal pointsRequired,
        boolean available
    )
            throws SQLException {
        String sql = """
            UPDATE rewards_catalog
            SET reward_name = ?, description = ?,
                points_required = ?, available = ?
            WHERE reward_id = ?
            """;
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setBigDecimal(3, pointsRequired);
            statement.setBoolean(4, available);
            statement.setInt(5, rewardId);
            requireChanged(statement.executeUpdate(), "Reward not found.");
        }
    }

    public void deleteReward(int rewardId) throws SQLException {
        try (Connection connection = connections.open();
             PreparedStatement statement =
                 connection.prepareStatement("DELETE FROM rewards_catalog WHERE reward_id = ?")) {
            statement.setInt(1, rewardId);
            requireChanged(statement.executeUpdate(), "Reward not found.");
        }
    }

    public void submitBottles(int studentId, int bottleCount, BigDecimal points)
            throws SQLException {
        try (Connection connection = connections.open()) {
            connection.setAutoCommit(false);
            try {
                lockStudent(connection, studentId);
                int recordId = insertBottleRecord(connection, studentId, bottleCount, points);
                updateStudentTotals(connection, studentId, bottleCount, points);
                insertLedger(
                    connection, studentId, points, "bottle_submission", recordId
                );
                connection.commit();
            } catch (Exception exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    public void redeem(int studentId, int rewardId) throws SQLException {
        try (Connection connection = connections.open()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal balance = lockStudent(connection, studentId);
                BigDecimal cost = availableRewardCost(connection, rewardId);
                if (balance.compareTo(cost) < 0) {
                    throw new IllegalStateException(
                        "Insufficient points. Available: " + balance.stripTrailingZeros().toPlainString()
                    );
                }

                int redemptionId = insertRedemption(connection, studentId, rewardId, cost);
                deductPoints(connection, studentId, cost);
                insertLedger(connection, studentId, cost.negate(), "redemption", redemptionId);
                connection.commit();
            } catch (Exception exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    public List<TransactionEntry> findTransactions(String query) throws SQLException {
        boolean filtered = query != null && !query.isBlank();
        String filter = filtered ? " WHERE LOWER(student_name) LIKE LOWER(?)" : "";
        String sql = """
            SELECT occurred_at, student_name, type, details, points_change
            FROM (
                SELECT
                    br.submission_date + br.submission_time AS occurred_at,
                    s.student_name,
                    'Bottle submission' AS type,
                    br.bottles_collected || ' bottles' AS details,
                    br.points_credited AS points_change
                FROM bottle_records br
                JOIN students s ON s.student_id = br.student_id

                UNION ALL

                SELECT
                    r.redemption_date + r.redemption_time AS occurred_at,
                    s.student_name,
                    'Reward redemption' AS type,
                    rc.reward_name AS details,
                    -r.points_deducted AS points_change
                FROM redemptions r
                JOIN students s ON s.student_id = r.student_id
                JOIN rewards_catalog rc ON rc.reward_id = r.reward_id

                UNION ALL

                SELECT
                    pl.transaction_date AS occurred_at,
                    s.student_name,
                    'Admin correction' AS type,
                    'Bottle count adjustment' AS details,
                    pl.points_change AS points_change
                FROM points_ledger pl
                JOIN students s ON s.student_id = pl.student_id
                WHERE pl.source = 'admin_adjustment'
            ) history
            """ + filter + " ORDER BY occurred_at DESC";
        List<TransactionEntry> entries = new ArrayList<>();
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (filtered) {
                statement.setString(1, "%" + query.trim() + "%");
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    entries.add(new TransactionEntry(
                        result.getObject("occurred_at", LocalDateTime.class),
                        result.getString("student_name"),
                        result.getString("type"),
                        result.getString("details"),
                        result.getBigDecimal("points_change")
                    ));
                }
            }
        }
        return entries;
    }

    private static Student mapStudent(ResultSet result) throws SQLException {
        return new Student(
            result.getInt("student_id"),
            result.getString("student_name"),
            result.getInt("bottle_count"),
            result.getBigDecimal("points_earned"),
            result.getObject("registration_date", LocalDateTime.class)
        );
    }

    private static Reward mapReward(ResultSet result) throws SQLException {
        return new Reward(
            result.getInt("reward_id"),
            result.getString("reward_name"),
            result.getString("description"),
            result.getBigDecimal("points_required"),
            result.getBoolean("available")
        );
    }

    private static BigDecimal lockStudent(Connection connection, int studentId)
            throws SQLException {
        String sql = "SELECT points_earned FROM students WHERE student_id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException("Student not found.");
                }
                return result.getBigDecimal("points_earned");
            }
        }
    }

    private static BigDecimal availableRewardCost(Connection connection, int rewardId)
            throws SQLException {
        String sql = """
            SELECT points_required, available
            FROM rewards_catalog
            WHERE reward_id = ?
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, rewardId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException("Reward not found.");
                }
                if (!result.getBoolean("available")) {
                    throw new IllegalStateException("Reward is currently unavailable.");
                }
                return result.getBigDecimal("points_required");
            }
        }
    }

    private static int insertBottleRecord(
        Connection connection,
        int studentId,
        int bottles,
        BigDecimal points
    ) throws SQLException {
        String sql = """
            INSERT INTO bottle_records (student_id, bottles_collected, points_credited)
            VALUES (?, ?, ?)
            RETURNING record_id
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, bottles);
            statement.setBigDecimal(3, points);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private static void updateStudentTotals(
        Connection connection,
        int studentId,
        int bottles,
        BigDecimal points
    ) throws SQLException {
        String sql = """
            UPDATE students
            SET bottle_count = bottle_count + ?,
                points_earned = points_earned + ?
            WHERE student_id = ?
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bottles);
            statement.setBigDecimal(2, points);
            statement.setInt(3, studentId);
            requireChanged(statement.executeUpdate(), "Student not found.");
        }
    }

    private static int insertRedemption(
        Connection connection,
        int studentId,
        int rewardId,
        BigDecimal cost
    ) throws SQLException {
        String sql = """
            INSERT INTO redemptions (student_id, reward_id, points_deducted)
            VALUES (?, ?, ?)
            RETURNING redemption_id
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, rewardId);
            statement.setBigDecimal(3, cost);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private static void deductPoints(
        Connection connection,
        int studentId,
        BigDecimal cost
    ) throws SQLException {
        String sql = """
            UPDATE students
            SET points_earned = points_earned - ?
            WHERE student_id = ? AND points_earned >= ?
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, cost);
            statement.setInt(2, studentId);
            statement.setBigDecimal(3, cost);
            requireChanged(statement.executeUpdate(), "Insufficient points.");
        }
    }

    private static void insertLedger(
        Connection connection,
        int studentId,
        BigDecimal change,
        String source,
        int referenceId
    ) throws SQLException {
        String sql = """
            INSERT INTO points_ledger (student_id, points_change, source, ref_id)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setBigDecimal(2, change);
            statement.setString(3, source);
            if (referenceId > 0) {
                statement.setInt(4, referenceId);
            } else {
                statement.setNull(4, Types.INTEGER);
            }
            statement.executeUpdate();
        }
    }

    private static void rollback(Connection connection, Exception original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }

    private static void requireChanged(int rows, String message) {
        if (rows == 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
