package com.iskollect.service;

import com.iskollect.dao.IskollectRepository;
import com.iskollect.model.DashboardStats;
import com.iskollect.model.Reward;
import com.iskollect.model.Student;
import com.iskollect.model.TransactionEntry;
import com.iskollect.util.PasswordUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

public final class IskollectService {
    public static final int MINIMUM_BOTTLES = 5;
    public static final BigDecimal POINTS_PER_BOTTLE = new BigDecimal("0.50");

    private final IskollectRepository repository;

    public IskollectService(IskollectRepository repository) {
        this.repository = repository;
    }

    public void verifyDatabase() throws SQLException {
        repository.verifySchema();
    }

    public boolean needsInitialAdmin() throws SQLException {
        return repository.adminCount() == 0;
    }

    public void createInitialAdmin(String username, String password, String confirmation)
            throws SQLException {
        if (!needsInitialAdmin()) {
            throw new IllegalStateException("An administrator account already exists.");
        }
        String cleanUsername = validateUsername(username);
        validatePassword(password, confirmation);
        repository.createAdmin(cleanUsername, PasswordUtil.hash(password));
    }

    public boolean authenticate(String username, String password) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return false;
        }
        return repository.findPasswordHash(username.trim())
            .map(hash -> PasswordUtil.matches(password, hash))
            .orElse(false);
    }

    public boolean authenticateAdminPassword(String password) throws SQLException {
        if (password == null || password.isBlank()) {
            return false;
        }
        return repository.findAdminPasswordHashes().stream()
            .anyMatch(hash -> PasswordUtil.matches(password, hash));
    }

    public DashboardStats dashboardStats() throws SQLException {
        return repository.dashboardStats();
    }

    public List<Student> students(String query) throws SQLException {
        return repository.findStudents(query);
    }

    public List<Student> topStudents(int limit) throws SQLException {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Leaderboard limit must be between 1 and 100.");
        }
        return repository.findTopStudents(limit);
    }

    public Student addStudent(String name) throws SQLException {
        return repository.createStudent(validateName(name, "Student name"));
    }

    public Student registerStudent(String name, String bottleInput) throws SQLException {
        String cleanName = validateName(name, "Student name");
        int bottles = parseBottleCount(bottleInput);
        BigDecimal points = calculatePoints(bottles);
        return repository.registerStudentWithSubmission(cleanName, bottles, points);
    }

    public void renameStudent(Student student, String name) throws SQLException {
        requireSelection(student, "student");
        repository.renameStudent(student.id(), validateName(name, "Student name"));
    }

    /**
     * Admin correction to a student's name and bottle count (Edit Student
     * popup). Recomputes points from the new bottle count at the standard
     * rate. Unlike a bottle submission, 0 is an allowed bottle count here
     * since this corrects the running total rather than recording a new
     * physical drop-off.
     */
    public Student updateStudent(Student student, String name, String bottleCountInput)
            throws SQLException {
        requireSelection(student, "student");
        String cleanName = validateName(name, "Student name");
        int bottles = parseNonNegativeBottleCount(bottleCountInput);
        BigDecimal points = calculatePoints(bottles);
        return repository.updateStudentDetails(student.id(), cleanName, bottles, points);
    }

    public static BigDecimal previewPoints(String bottleCountInput) {
        try {
            return calculatePoints(parseNonNegativeBottleCount(bottleCountInput));
        } catch (RuntimeException exception) {
            return null;
        }
    }

    public void deleteStudent(Student student) throws SQLException {
        requireSelection(student, "student");
        repository.deleteStudent(student.id());
    }

    public List<Reward> rewards() throws SQLException {
        return repository.findRewards();
    }

    public Reward addReward(String name, String description, String points)
            throws SQLException {
        return repository.createReward(
            validateName(name, "Reward name"),
            cleanDescription(description),
            parsePositivePoints(points)
        );
    }

    public void updateReward(
        Reward reward,
        String name,
        String description,
        String points,
        boolean available
    ) throws SQLException {
        requireSelection(reward, "reward");
        repository.updateReward(
            reward.id(),
            validateName(name, "Reward name"),
            cleanDescription(description),
            parsePositivePoints(points),
            available
        );
    }

    public void deleteReward(Reward reward) throws SQLException {
        requireSelection(reward, "reward");
        repository.deleteReward(reward.id());
    }

    public BigDecimal submitBottles(Student student, String bottleInput) throws SQLException {
        requireSelection(student, "student");
        int bottles = parseBottleCount(bottleInput);
        BigDecimal points = calculatePoints(bottles);
        repository.submitBottles(student.id(), bottles, points);
        return points;
    }

    public void redeem(Student student, Reward reward) throws SQLException {
        requireSelection(student, "student");
        requireSelection(reward, "reward");
        repository.redeem(student.id(), reward.id());
    }

    public List<TransactionEntry> transactions(String query) throws SQLException {
        return repository.findTransactions(query);
    }

    public static BigDecimal calculatePoints(int bottles) {
        if (bottles < 0) {
            throw new IllegalArgumentException("Bottle count cannot be negative.");
        }
        return POINTS_PER_BOTTLE
            .multiply(BigDecimal.valueOf(bottles))
            .setScale(2, RoundingMode.UNNECESSARY);
    }

    private static String validateUsername(String username) {
        String clean = username == null ? "" : username.trim();
        if (!clean.matches("[A-Za-z0-9_.-]{3,50}")) {
            throw new IllegalArgumentException(
                "Username must be 3-50 characters using letters, numbers, dot, dash, or underscore."
            );
        }
        return clean;
    }

    private static void validatePassword(String password, String confirmation) {
        if (password == null || password.length() < 10) {
            throw new IllegalArgumentException("Password must contain at least 10 characters.");
        }
        if (!password.matches(".*[A-Z].*")
            || !password.matches(".*[a-z].*")
            || !password.matches(".*\\d.*")) {
            throw new IllegalArgumentException(
                "Password must include uppercase, lowercase, and a number."
            );
        }
        if (!password.equals(confirmation)) {
            throw new IllegalArgumentException("Password confirmation does not match.");
        }
    }

    private static String validateName(String value, String label) {
        String clean = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (clean.length() < 2 || clean.length() > 100) {
            throw new IllegalArgumentException(label + " must contain 2-100 characters.");
        }
        return clean;
    }

    private static int parseBottleCount(String bottleInput) {
        int bottles;
        try {
            bottles = Integer.parseInt(
                bottleInput == null ? "" : bottleInput.trim()
            );
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Bottle count must be a whole number.");
        }
        if (bottles < MINIMUM_BOTTLES) {
            throw new IllegalArgumentException(
                "A submission must contain at least " + MINIMUM_BOTTLES + " bottles."
            );
        }
        if (bottles > 100_000) {
            throw new IllegalArgumentException("Bottle count is unusually large.");
        }
        return bottles;
    }

    private static int parseNonNegativeBottleCount(String bottleInput) {
        int bottles;
        try {
            bottles = Integer.parseInt(
                bottleInput == null ? "" : bottleInput.trim()
            );
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Bottle count must be a whole number.");
        }
        if (bottles < 0) {
            throw new IllegalArgumentException("Bottle count cannot be negative.");
        }
        if (bottles > 100_000) {
            throw new IllegalArgumentException("Bottle count is unusually large.");
        }
        return bottles;
    }

    private static BigDecimal parsePositivePoints(String value) {
        try {
            BigDecimal points = new BigDecimal(value == null ? "" : value.trim())
                .setScale(2, RoundingMode.UNNECESSARY);
            if (points.signum() <= 0) {
                throw new IllegalArgumentException("Points required must be greater than zero.");
            }
            return points;
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new IllegalArgumentException(
                "Points required must be a valid number with at most two decimal places."
            );
        }
    }

    private static String cleanDescription(String value) {
        String clean = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (clean.length() > 500) {
            throw new IllegalArgumentException(
                "Reward description cannot exceed 500 characters."
            );
        }
        return clean;
    }

    private static void requireSelection(Object value, String label) {
        if (value == null) {
            throw new IllegalArgumentException("Select a " + label + " first.");
        }
    }
}
