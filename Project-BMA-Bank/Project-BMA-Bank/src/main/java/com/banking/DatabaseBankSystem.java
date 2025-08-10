package com.banking;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.banking.DatabaseConnection.getConnection;

class database_BankSystem {
    private static final String DB_URL = "jdbc:sqlite:bank.db?busy_timeout=10000";
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int CODE_LENGTH = 6;

    public static class UserDetails {
        private String fullName;
        private String email;
        private String mobile;
        private String nationalId;
        private double totalBalance;
        private String profileImage;

        public UserDetails(String fullName, String email, String mobile, String nationalId, double totalBalance, String profileImage) {
            this.fullName = fullName;
            this.email = email;
            this.mobile = mobile;
            this.nationalId = nationalId;
            this.totalBalance = totalBalance;
            this.profileImage = profileImage;
        }

        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getMobile() { return mobile; }
        public String getNationalId() { return nationalId; }
        public double getTotalBalance() { return totalBalance; }
        public String getProfileImage() { return profileImage; }
    }

    public static class Transaction {
        private int id;
        private String type;
        private double amount;
        private String date;

        public Transaction(int id, String type, double amount, String date) {
            this.id = id;
            this.type = type;
            this.amount = amount;
            this.date = date;
        }

        public int getId() { return id; }
        public String getType() { return type; }
        public double getAmount() { return amount; }
        public String getDate() { return date; }

        @Override
        public String toString() {
            return "Transaction ID: " + id + ", Type: " + type + ", Amount: " + amount + ", Date: " + date;
        }
    }

    public static class Transfer {
        private int id;
        private String fromUser;
        private String toUser;
        private double amount;
        private String status;
        private String date;

        public Transfer(int id, String fromUser, String toUser, double amount, String status, String date) {
            this.id = id;
            this.fromUser = fromUser;
            this.toUser = toUser;
            this.amount = amount;
            this.status = status;
            this.date = date;
        }

        public int getId() { return id; }
        public String getFromUser() { return fromUser; }
        public String getToUser() { return toUser; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }
        public String getDate() { return date; }

        @Override
        public String toString() {
            return "Transfer ID: " + id + ", From: " + fromUser + ", To: " + toUser + ", Amount: " + amount + ", Status: " + status + ", Date: " + date;
        }
    }

    private static void addColumnIfNotExists(Connection conn, String tableName, String columnName, String columnType) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, tableName, columnName);
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement()) {
                    String alterQuery = String.format("ALTER TABLE %s ADD COLUMN %s %s;", tableName, columnName, columnType);
                    stmt.executeUpdate(alterQuery);
                    System.out.println("✅ Added column " + columnName + " to table " + tableName);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error adding column " + columnName + " to " + tableName + ": " + e.getMessage());
        }
    }

    private static void modifyColumnTypeConstraints(Connection conn, String tableName, String columnName, String[] allowedValues) {
        try (Statement stmt = conn.createStatement()) {
            StringBuilder values = new StringBuilder();
            for (String value : allowedValues) {
                if (values.length() > 0) values.append(",");
                values.append("'").append(value).append("'");
            }

            String alterQuery = String.format("ALTER TABLE %s MODIFY COLUMN %s TEXT CHECK(%s IN (%s));",
                    tableName, columnName, columnName, values.toString());
            stmt.executeUpdate(alterQuery);
            System.out.println("✅ Modified column " + columnName + " constraints in table " + tableName);
        } catch (SQLException e) {
            // SQLite doesn't support ALTER TABLE MODIFY COLUMN, so we'll just log this
            System.out.println("ℹ️ Column constraints will be applied to new entries only");
        }
    }

    public static void createTables() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String usersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        salt TEXT NOT NULL,
                        full_name TEXT,
                        email TEXT,
                        mobile TEXT,
                        national_id TEXT,
                        profile_image TEXT,
                        total_balance REAL DEFAULT 0,
                        last_login TEXT,
                        is_verified BOOLEAN NOT NULL DEFAULT 0
                    )
                    """;

            String verificationCodesTable = """
                    CREATE TABLE IF NOT EXISTS verification_codes (
                        username TEXT PRIMARY KEY,
                        code TEXT NOT NULL,
                        FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
                    )
                    """;

            String transactionsTable = """
                    CREATE TABLE IF NOT EXISTS transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        type TEXT,
                        amount REAL,
                        date TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id) REFERENCES users(id)
                    )
                    """;

            String transfersTable = """
                    CREATE TABLE IF NOT EXISTS transfers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        from_user TEXT NOT NULL,
                        to_user TEXT NOT NULL,
                        amount REAL NOT NULL,
                        status TEXT DEFAULT 'pending',
                        date TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                    """;

            String investmentsTable = """
                    CREATE TABLE IF NOT EXISTS investments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        type TEXT,
                        amount REAL,
                        status TEXT DEFAULT 'completed',
                        date TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id) REFERENCES users(id)
                    )
                    """;

            String billsPaymentsTable = """
                    CREATE TABLE IF NOT EXISTS bills_payments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        bill_type TEXT NOT NULL,
                        customer_id TEXT NOT NULL,
                        amount REAL NOT NULL,
                        status TEXT DEFAULT 'completed',
                        date TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id) REFERENCES users(id)
                    )
                    """;

            String mobileTopUpsTable = """
                    CREATE TABLE IF NOT EXISTS mobile_top_ups (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        network TEXT NOT NULL,
                        mobile_number TEXT NOT NULL,
                        amount REAL NOT NULL,
                        status TEXT DEFAULT 'completed',
                        date TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id) REFERENCES users(id)
                    )
                    """;

            String creditCardPaymentsTable = """
                    CREATE TABLE IF NOT EXISTS credit_card_payments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        card_type TEXT NOT NULL,
                        amount REAL NOT NULL,
                        status TEXT DEFAULT 'completed',
                        date TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id) REFERENCES users(id)
                    )
                    """;

            String governmentServicesTable = """
                    CREATE TABLE IF NOT EXISTS government_services (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        service_type TEXT NOT NULL,
                        service_number TEXT NOT NULL,
                        amount REAL NOT NULL,
                        status TEXT DEFAULT 'completed',
                        date TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id) REFERENCES users(id)
                    )
                    """;

            String donationsTable = """
                    CREATE TABLE IF NOT EXISTS donations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        charity TEXT NOT NULL,
                        amount REAL NOT NULL,
                        status TEXT DEFAULT 'completed',
                        date TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id) REFERENCES users(id)
                    )
                    """;

            String educationPaymentsTable = """
                    CREATE TABLE IF NOT EXISTS education_payments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        facility TEXT NOT NULL,
                        student_id TEXT NOT NULL,
                        amount REAL NOT NULL,
                        status TEXT DEFAULT 'completed',
                        date TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id) REFERENCES users(id)
                    )
                    """;

            String insurancePaymentsTable = """
                    CREATE TABLE IF NOT EXISTS insurance_payments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        provider TEXT NOT NULL,
                        policy_number TEXT NOT NULL,
                        amount REAL NOT NULL,
                        status TEXT DEFAULT 'completed',
                        date TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id) REFERENCES users(id)
                    )
                    """;

            String otherPaymentsTable = """
                    CREATE TABLE IF NOT EXISTS other_payments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        category TEXT NOT NULL,
                        payee_name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        status TEXT DEFAULT 'completed',
                        date TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id) REFERENCES users(id)
                    )
                    """;

            String cardsTable = """
                    CREATE TABLE IF NOT EXISTS cards (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        card_type TEXT NOT NULL,
                        amount REAL NOT NULL,
                        gdate TEXT DEFAULT CURRENT_TIMESTAMP,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    )
                    """;

            stmt.execute(usersTable);
            stmt.execute(verificationCodesTable);
            stmt.execute(transactionsTable);
            stmt.execute(transfersTable);
            stmt.execute(investmentsTable);
            stmt.execute(billsPaymentsTable);
            stmt.execute(mobileTopUpsTable);
            stmt.execute(creditCardPaymentsTable);
            stmt.execute(governmentServicesTable);
            stmt.execute(donationsTable);
            stmt.execute(educationPaymentsTable);
            stmt.execute(insurancePaymentsTable);
            stmt.execute(otherPaymentsTable);
            stmt.execute(cardsTable);

            // Add columns if not exists
            addColumnIfNotExists(conn, "users", "profile_image", "TEXT");
            addColumnIfNotExists(conn, "users", "total_balance", "REAL DEFAULT 0");
            addColumnIfNotExists(conn, "users", "salt", "TEXT");
            addColumnIfNotExists(conn, "users", "last_login", "TEXT");
            addColumnIfNotExists(conn, "users", "is_verified", "BOOLEAN NOT NULL DEFAULT 0");
            addColumnIfNotExists(conn, "cards", "gdate", "TEXT");

            // Add status column to payment tables
            String[] paymentTables = {
                    "investments", "bills_payments", "mobile_top_ups", "credit_card_payments",
                    "government_services", "donations", "education_payments", "insurance_payments", "other_payments"
            };
            for (String table : paymentTables) {
                addColumnIfNotExists(conn, table, "status", "TEXT DEFAULT 'completed'");
            }

            // Update transaction types to include income and expense
            String[] transactionTypes = {
                    "income", "expense", "deposit", "withdraw", "bill_payment",
                    "mobile_top_up", "credit_card_payment", "government_service",
                    "donation", "education_payment", "insurance_payment",
                    "other_payment", "fees"
            };
            modifyColumnTypeConstraints(conn, "transactions", "type", transactionTypes);

            System.out.println("✅ Tables created and updated successfully.");
        } catch (SQLException e) {
            System.out.println("❌ Error creating/updating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static double getTotalByTable(String tableName, int userId) {
        String sql = "SELECT SUM(amount) as total FROM " + tableName + " WHERE user_id = ? AND status = 'completed'";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble("total");
                    return rs.wasNull() ? 0.0 : total;
                }
                return 0.0;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving total from " + tableName + ": " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    public static List<Object[]> getPaymentTotalsByCategory(int userId) {
        List<Object[]> data = new ArrayList<>();
        String[] tables = {
                "investments", "bills_payments", "mobile_top_ups", "credit_card_payments",
                "government_services", "donations", "education_payments", "insurance_payments", "other_payments"
        };
        String[] labels = {
                "Investments", "Bills", "Mobile Top-Ups", "Credit Card",
                "Government", "Donations", "Education", "Insurance", "Other"
        };

        double totalAmount = 0.0;
        double[] amounts = new double[tables.length];

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            for (int i = 0; i < tables.length; i++) {
                amounts[i] = getTotalByTable(tables[i], userId);
                totalAmount += amounts[i];
            }

            if (totalAmount > 0) {
                for (int i = 0; i < tables.length; i++) {
                    if (amounts[i] > 0) {
                        double percentage = (amounts[i] / totalAmount) * 100;
                        data.add(new Object[]{labels[i], percentage});
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving payment totals: " + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }

    public static double getTotalDeposits(String username) {
        int userId = getUserId(username);
        if (userId == -1) return 0.0;

        String sql = "SELECT SUM(amount) as total FROM transactions WHERE user_id = ? AND type = 'deposit'";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble("total");
                    return rs.wasNull() ? 0.0 : total;
                }
                return 0.0;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving total deposits: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    public static double getTotalWithdrawals(String username) {
        int userId = getUserId(username);
        if (userId == -1) return 0.0;

        String sql = "SELECT SUM(amount) as total FROM transactions WHERE user_id = ? AND type = 'withdraw'";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble("total");
                    return rs.wasNull() ? 0.0 : total;
                }
                return 0.0;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving total withdrawals: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    public static double getTotalPayments(String username) {
        int userId = getUserId(username);
        if (userId == -1) return 0.0;

        String[] paymentTypes = {
                "bill_payment", "mobile_top_up", "credit_card_payment", "government_service",
                "donation", "education_payment", "insurance_payment", "other_payment"
        };
        double total = 0.0;

        String sql = "SELECT SUM(amount) as total FROM transactions WHERE user_id = ? AND type = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            for (String type : paymentTypes) {
                pstmt.setString(2, type);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        double amount = rs.getDouble("total");
                        if (!rs.wasNull()) {
                            total += amount;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving total payments: " + e.getMessage());
            e.printStackTrace();
        }
        return total;
    }

    public static List<Object[]> getDailyTransactions(String username) {
        List<Object[]> dailyData = new ArrayList<>();
        int userId = getUserId(username);
        if (userId == -1) {
            System.out.println("❌ User not found: " + username);
            return dailyData;
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6); // Start of the week (7 days total)

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = today.format(formatter);

        String sql = """
        SELECT DATE(date) as transaction_date, SUM(amount) as total
        FROM transactions
        WHERE user_id = ? 
        AND type IN ('deposit', 'withdraw', 'bill_payment', 'mobile_top_up', 'credit_card_payment', 'government_service', 'donation', 'education_payment', 'insurance_payment', 'other_payment')
        AND DATE(date) BETWEEN ? AND ?
        GROUP BY DATE(date)
        ORDER BY transaction_date
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, startDateStr);
            pstmt.setString(3, endDateStr);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("transaction_date");
                    double total = rs.getDouble("total");
                    dailyData.add(new Object[]{"", total, date}); // First element empty (filled with day name later)
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving daily transactions: " + e.getMessage());
            e.printStackTrace();
            return dailyData;
        }

        List<Object[]> finalData = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            String currentDateStr = currentDate.format(formatter);
            boolean found = false;
            for (Object[] data : dailyData) {
                if (((String) data[2]).equals(currentDateStr)) {
                    finalData.add(data);
                    found = true;
                    break;
                }
            }
            if (!found) {
                finalData.add(new Object[]{"", 0.0, currentDateStr});
            }
        }

        return finalData;
    }

    public static List<Object[]> getMonthlyTransactions(String username, String type, String startDate, String endDate) {
        List<Object[]> transactions = new ArrayList<>();
        int userId = getUserId(username);
        if (userId == -1) {
            System.out.println("❌ User not found: " + username);
            return transactions;
        }

        String query = "SELECT t.id, t.amount, t.date AS transaction_date, t.type " +
                "FROM transactions t " +
                "WHERE t.user_id = ? AND t.type = ? " +
                "AND t.date BETWEEN ? AND ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, type);
            stmt.setString(3, startDate);
            stmt.setString(4, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Object[]{
                            rs.getInt("id"),
                            rs.getDouble("amount"),
                            rs.getString("transaction_date"),
                            rs.getString("type")
                    });
                }
                System.out.println("✅ Retrieved " + transactions.size() + " " + type + " transactions for user: " + username);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching transactions for username: " + username + ", type: " + type + ": " + e.getMessage());
            e.printStackTrace();
        }

        return transactions;
    }

    public static double getMonthlyIncome(String username, String month) {
        double total = 0;
        try {
            month = String.format("%02d", Integer.parseInt(month));
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid month format: " + month);
            return 0.0;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            int userId = getUserId(username);
            if (userId == -1) {
                System.out.println("❌ User not found for monthly income: " + username);
                return 0.0;
            }

            String transactionQuery = "SELECT SUM(amount) FROM transactions " +
                    "WHERE user_id = ? AND type = 'income' " +
                    "AND strftime('%m', date) = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(transactionQuery)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, month);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        total += rs.getDouble(1);
                    }
                }
            }

            String cardQuery = "SELECT SUM(amount) FROM cards " +
                    "WHERE user_id = ? AND id NOT IN (SELECT MIN(id) FROM cards WHERE user_id = ?) " +
                    "AND strftime('%m', gdate) = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(cardQuery)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, userId);
                pstmt.setString(3, month);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        total += rs.getDouble(1);
                    }
                }
            }

            System.out.println("✅ Monthly income retrieved for " + username + " (month " + month + "): " + total);
        } catch (SQLException e) {
            System.out.println("❌ Error getting monthly income for " + username + ": " + e.getMessage());
            e.printStackTrace();
        }
        return total;
    }

    public static double getMonthlyExpenses(String username, String month) {
        double total = 0;
        try {
            month = String.format("%02d", Integer.parseInt(month));
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid month format: " + month);
            return 0.0;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            int userId = getUserId(username);
            if (userId == -1) {
                System.out.println("❌ User ID not found for username: " + username);
                return 0;
            }

            String query = "SELECT SUM(amount) FROM transactions " +
                    "WHERE user_id = ? " +
                    "AND type IN ('expense', 'bill_payment', 'insurance_payment', 'donation', " +
                    "'government_service', 'other_payment', 'mobile_top_up') " +
                    "AND strftime('%m', date) = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, month);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getDouble(1);
                    }
                }
            }

            String transferQuery = "SELECT SUM(amount) FROM transfers " +
                    "WHERE from_user = ? " +
                    "AND status = 'completed' " +
                    "AND strftime('%m', date) = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(transferQuery)) {
                pstmt.setString(1, username);
                pstmt.setString(2, month);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        total += rs.getDouble(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error getting monthly expenses for " + username + ": " + e.getMessage());
            e.printStackTrace();
        }
        return total;
    }

    public static boolean registerUser(String username, String password, String name, String email,
                                       String mobile, String nationalId, String imagePath, double initialBalance) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            System.out.println("❌ Username and password cannot be empty.");
            return false;
        }
        if (initialBalance < 0) {
            System.out.println("❌ Initial balance cannot be negative.");
            return false;
        }

        byte[] salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO users (username, password, salt, full_name, email, mobile, national_id, profile_image, total_balance, is_verified) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, saltBase64);
            pstmt.setString(4, name);
            pstmt.setString(5, email);
            pstmt.setString(6, mobile);
            pstmt.setString(7, nationalId);
            pstmt.setString(8, imagePath);
            pstmt.setDouble(9, initialBalance);
            pstmt.setBoolean(10, false);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✅ User " + username + " registered successfully. Rows affected: " + rowsAffected);
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static String getUsernameByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            System.out.println("❌ Cannot retrieve username: Email is null or empty.");
            return null;
        }

        String sql = "SELECT username FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    System.out.println("✅ Retrieved username for email " + email + ": " + username);
                    return username;
                } else {
                    System.out.println("❌ No user found with email: " + email);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving username by email: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String getEmailByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("❌ Cannot retrieve email: Username is null or empty.");
            return null;
        }

        String sql = "SELECT email FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String email = rs.getString("email");
                    if (email != null) {
                        System.out.println("✅ Retrieved email for username " + username + ": " + email);
                        return email;
                    } else {
                        System.out.println("❌ Email is null for username: " + username);
                        return null;
                    }
                } else {
                    System.out.println("❌ No user found with username: " + username);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving email by username: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean updatePassword(String username, String newPassword) {
        if (username == null || username.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            System.out.println("❌ Cannot update password: Username or new password is null or empty.");
            return false;
        }

        if (!userExists(username)) {
            System.out.println("❌ User does not exist: " + username);
            return false;
        }

        byte[] salt = generateSalt();
        String hashedPassword = hashPassword(newPassword, salt);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);

        String sql = "UPDATE users SET password = ?, salt = ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, saltBase64);
            pstmt.setString(3, username);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Password updated successfully for user: " + username);
                return true;
            } else {
                System.out.println("❌ Failed to update password: No rows affected for user " + username);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error updating password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static String generateAndSaveVerificationCode(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("❌ Cannot generate verification code: Username is null or empty.");
            return null;
        }

        if (!userExists(username)) {
            System.out.println("❌ Cannot generate verification code: User " + username + " does not exist.");
            return null;
        }

        SecureRandom random = new SecureRandom();
        String code = String.valueOf(random.nextInt(999999 - 100000 + 1) + 100000);

        String sql = "INSERT OR REPLACE INTO verification_codes (username, code) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, code);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Generated and saved verification code for " + username + ": " + code);
                return code;
            } else {
                System.out.println("❌ Failed to save verification code for " + username);
                return null;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error saving verification code: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String getVerificationCode(String username) {
        String sql = "SELECT code FROM verification_codes WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String code = rs.getString("code");
                    System.out.println("✅ Retrieved verification code for " + username + ": " + code);
                    return code;
                } else {
                    System.out.println("❌ No verification code found for " + username);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving verification code: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static void deleteVerificationCode(String username) {
        String sql = "DELETE FROM verification_codes WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
            System.out.println("✅ Deleted verification code for " + username);
        } catch (SQLException e) {
            System.out.println("❌ Error deleting verification code: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean verifyCode(String username, String code) {
        String storedCode = getVerificationCode(username);
        if (storedCode == null) {
            System.out.println("❌ No valid verification code found for " + username);
            return false;
        }

        if (storedCode.equals(code)) {
            String updateSql = "UPDATE users SET is_verified = ? WHERE username = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setBoolean(1, true);
                pstmt.setString(2, username);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("✅ User " + username + " marked as verified");
                } else {
                    System.out.println("❌ Failed to mark user " + username + " as verified: No rows affected");
                    return false;
                }
            } catch (SQLException e) {
                System.out.println("❌ Error marking user as verified: " + e.getMessage());
                e.printStackTrace();
                return false;
            }

            deleteVerificationCode(username);
            System.out.println("✅ Verification successful for " + username);
            return true;
        } else {
            System.out.println("❌ Verification code mismatch for " + username + ". Entered: " + code + ", Stored: " + storedCode);
            return false;
        }
    }

    public static boolean isUserVerified(String username) {
        String sql = "SELECT is_verified FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_verified");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error checking verification status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean loginUser(String username, String password) {
        String sql = "SELECT password, salt, is_verified FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    boolean isVerified = rs.getBoolean("is_verified");
                    if (!isVerified) {
                        System.out.println("❌ User " + username + " has not verified their email.");
                        return false;
                    }

                    String storedHash = rs.getString("password");
                    String saltBase64 = rs.getString("salt");
                    if (saltBase64 == null) {
                        System.out.println("❌ No salt found for user: " + username);
                        return false;
                    }
                    byte[] salt = Base64.getDecoder().decode(saltBase64);
                    String hashedInputPassword = hashPassword(password, salt);
                    return storedHash.equals(hashedInputPassword);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error logging in: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    private static String hashPassword(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("❌ Error hashing password: " + e.getMessage(), e);
        }
    }

    public static UserDetails getUserDetails(String username) {
        String sql = "SELECT full_name, email, mobile, national_id, total_balance, profile_image FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new UserDetails(
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("mobile"),
                            rs.getString("national_id"),
                            rs.getDouble("total_balance"),
                            rs.getString("profile_image")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving user details: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static UserDetails getUserDetailsById(int userId) {
        String sql = "SELECT full_name, email, mobile, national_id, total_balance, profile_image FROM users WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new UserDetails(
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("mobile"),
                            rs.getString("national_id"),
                            rs.getDouble("total_balance"),
                            rs.getString("profile_image")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving user details by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static double getTotalBalance(String username) {
        int userId = getUserId(username);
        if (userId == -1) {
            System.out.println("❌ User not found: " + username);
            return 0.0;
        }

        String sql = "SELECT total_balance FROM users WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double balance = rs.getDouble("total_balance");
                    System.out.println("✅ Total balance retrieved for " + username + ": " + balance);
                    return balance;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving total balance for " + username + ": " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    public static List<Object[]> getDailyWithdrawals(String username) {
        List<Object[]> dailyData = new ArrayList<>();
        int userId = getUserId(username);
        if (userId == -1) {
            System.out.println("❌ User not found: " + username);
            return dailyData;
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6); // Start of the week (7 days total)

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = today.format(formatter);

        String sql = """
            SELECT DATE(date) as transaction_date, SUM(amount) as total
            FROM transactions
            WHERE user_id = ? 
            AND type = 'withdraw'
            AND DATE(date) BETWEEN ? AND ?
            GROUP BY DATE(date)
            ORDER BY transaction_date
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, startDateStr);
            pstmt.setString(3, endDateStr);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("transaction_date");
                    double total = rs.getDouble("total");
                    dailyData.add(new Object[]{"", total, date}); // First element empty (filled with day name later)
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving daily withdrawals: " + e.getMessage());
            e.printStackTrace();
            return dailyData;
        }

        List<Object[]> finalData = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            String currentDateStr = currentDate.format(formatter);
            boolean found = false;
            for (Object[] data : dailyData) {
                if (((String) data[2]).equals(currentDateStr)) {
                    finalData.add(data);
                    found = true;
                    break;
                }
            }
            if (!found) {
                finalData.add(new Object[]{"", 0.0, currentDateStr});
            }
        }

        return finalData;
    }

    public static double getBalance(String username) {
        String sql = "SELECT total_balance FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_balance");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving balance: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public static List<Transaction> getRecentTransactions(String username, int limit) {
        List<Transaction> transactions = new ArrayList<>();
        int userId = getUserId(username);
        if (userId == -1) {
            System.out.println("❌ User not found: " + username);
            return transactions;
        }

        String sql = "SELECT id, type, amount, date FROM transactions WHERE user_id = ? ORDER BY date DESC LIMIT ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                            rs.getInt("id"),
                            rs.getString("type"),
                            rs.getDouble("amount"),
                            rs.getString("date")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving transactions: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    public static List<Transfer> getRecentTransfers(String username, int limit) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT id, from_user, to_user, amount, status, date FROM transfers WHERE from_user = ? OR to_user = ? ORDER BY date DESC LIMIT ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, username);
            pstmt.setInt(3, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transfers.add(new Transfer(
                            rs.getInt("id"),
                            rs.getString("from_user"),
                            rs.getString("to_user"),
                            rs.getDouble("amount"),
                            rs.getString("status"),
                            rs.getString("date")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving transfers: " + e.getMessage());
            e.printStackTrace();
        }
        return transfers;
    }

    public static boolean deposit(String username, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Deposit amount must be positive.");
            return false;
        }
        if (!userExists(username)) {
            System.out.println("❌ User does not exist: " + username);
            return false;
        }

        double current = getBalance(username);
        if (current < 0) {
            System.out.println("❌ Error retrieving balance for user: " + username);
            return false;
        }

        double feesPercentage = 0.02; // 2% fees
        double fees = amount * feesPercentage;
        double totalAmountAfterFees = amount + fees;

        boolean balanceUpdated = updateBalance(username, current + amount);
        if (balanceUpdated) {
            int userId = getUserId(username);

            boolean depositRecorded = recordTransaction(userId, "income", amount);

            boolean feesRecorded = recordTransaction(userId, "fees", fees);

            if (depositRecorded && feesRecorded) {
                System.out.println("✅ Deposit successful for " + username + ": Amount = " + amount + ", Fees = " + fees);
                return true;
            } else {
                updateBalance(username, current);
                System.out.println("❌ Failed to record deposit or fees for user: " + username);
                return false;
            }
        }
        System.out.println("❌ Failed to update balance for user: " + username);
        return false;
    }

    public static boolean withdraw(String username, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Withdrawal amount must be positive.");
            return false;
        }
        if (!userExists(username)) {
            System.out.println("❌ User does not exist: " + username);
            return false;
        }

        double current = getBalance(username);
        if (current < 0 || current < amount) {
            System.out.println("❌ Insufficient funds or user not found: " + username);
            return false;
        }

        double feesPercentage = 0.02;
        double fees = amount * feesPercentage;
        double totalAmountAfterFees = amount + fees;

        if (current < totalAmountAfterFees) {
            System.out.println("❌ Insufficient funds after fees for user: " + username);
            return false;
        }

        boolean balanceUpdated = updateBalance(username, current - amount);
        if (balanceUpdated) {
            int userId = getUserId(username);

            boolean withdrawalRecorded = recordTransaction(userId, "expense", amount);

            boolean feesRecorded = recordTransaction(userId, "fees", fees);

            if (withdrawalRecorded && feesRecorded) {
                System.out.println("✅ Withdrawal successful for " + username + ": Amount = " + amount + ", Fees = " + fees);
                return true;
            } else {
                updateBalance(username, current);
                System.out.println("❌ Failed to record withdrawal or fees for user: " + username);
                return false;
            }
        }
        System.out.println("❌ Failed to update balance for user: " + username);
        return false;
    }

    public static boolean transfer(String fromUsername, String toUsername, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Transfer amount must be positive.");
            return false;
        }
        if (!userExists(fromUsername) || !userExists(toUsername)) {
            System.out.println("❌ One or both users do not exist.");
            return false;
        }
        double fromBalance = getBalance(fromUsername);
        if (fromBalance < 0 || fromBalance < amount) {
            System.out.println("❌ Insufficient funds for user: " + fromUsername);
            return false;
        }

        double toBalance = getBalance(toUsername);
        if (toBalance < 0) {
            System.out.println("❌ Error retrieving balance for user: " + toUsername);
            return false;
        }

        boolean fromUpdated = updateBalance(fromUsername, fromBalance - amount);
        if (!fromUpdated) {
            System.out.println("❌ Failed to update balance for user: " + fromUsername);
            return false;
        }

        boolean toUpdated = updateBalance(toUsername, toBalance + amount);
        if (!toUpdated) {
            updateBalance(fromUsername, fromBalance);
            System.out.println("❌ Failed to update balance for user: " + toUsername + ". Rolled back.");
            return false;
        }

        return recordTransfer(fromUsername, toUsername, amount, "completed");
    }

    public static boolean transferBill(String username, String billType, String customerId, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Bill amount must be positive.");
            return false;
        }
        if (!userExists(username)) {
            System.out.println("❌ User does not exist: " + username);
            return false;
        }
        double balance = getBalance(username);
        if (balance < 0 || balance < amount) {
            System.out.println("❌ Insufficient funds for user: " + username);
            return false;
        }

        boolean updated = updateBalance(username, balance - amount);
        if (!updated) {
            System.out.println("❌ Failed to update balance for user: " + username);
            return false;
        }

        int userId = getUserId(username);
        if (userId == -1) {
            updateBalance(username, balance);
            System.out.println("❌ Failed to get user ID for: " + username);
            return false;
        }

        String sql = "INSERT INTO bills_payments (user_id, bill_type, customer_id, amount, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, billType);
            pstmt.setString(3, customerId);
            pstmt.setDouble(4, amount);
            pstmt.setString(5, "completed");
            pstmt.executeUpdate();

            recordTransaction(userId, "bill_payment", amount);

            System.out.println("✅ Bill payment completed successfully: " + billType + " for " + amount);
            return true;
        } catch (SQLException e) {
            updateBalance(username, balance);
            System.out.println("❌ Error recording bill payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static String[] getUserPasswordAndSalt(String username) {
        String sql = "SELECT password, salt FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("password"), rs.getString("salt")};
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving password and salt: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static boolean transferMobileTopUp(String username, String network, String mobileNumber, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Top-up amount must be positive.");
            return false;
        }
        if (!userExists(username)) {
            System.out.println("❌ User does not exist: " + username);
            return false;
        }
        double balance = getBalance(username);
        if (balance < 0 || balance < amount) {
            System.out.println("❌ Insufficient funds for user: " + username);
            return false;
        }

        boolean updated = updateBalance(username, balance - amount);
        if (!updated) {
            System.out.println("❌ Failed to update balance for user: " + username);
            return false;
        }

        int userId = getUserId(username);
        if (userId == -1) {
            updateBalance(username, balance);
            System.out.println("❌ Failed to get user ID for: " + username);
            return false;
        }

        String sql = "INSERT INTO mobile_top_ups (user_id, network, mobile_number, amount, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, network);
            pstmt.setString(3, mobileNumber);
            pstmt.setDouble(4, amount);
            pstmt.setString(5, "completed");
            pstmt.executeUpdate();

            recordTransaction(userId, "mobile_top_up", amount);

            System.out.println("✅ Mobile top-up completed successfully: " + network + " for " + amount);
            return true;
        } catch (SQLException e) {
            updateBalance(username, balance);
            System.out.println("❌ Error recording mobile top-up: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static List<com.banking.TransferConfirm.Transaction> getUserTransactions(String username) {
        List<com.banking.TransferConfirm.Transaction> transactions = new ArrayList<>();
        int userId = getUserId(username);
        if (userId == -1) {
            System.out.println("❌ User not found: " + username);
            return transactions;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String transferSql = "SELECT id, from_user, to_user, amount, date FROM transfers WHERE from_user = ? OR to_user = ? ORDER BY date DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(transferSql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String fromUser = rs.getString("from_user");
                        String toUser = rs.getString("to_user");
                        double amount = rs.getDouble("amount");
                        String date = rs.getString("date");
                        String recipient = fromUser.equals(username) ? toUser : fromUser;
                        String type = fromUser.equals(username) ? "Transfer Sent" : "Transfer Received";
                        transactions.add(new com.banking.TransferConfirm.Transaction(id, type, amount, date, recipient));
                    }
                }
            }

            String billSql = "SELECT id, bill_type, customer_id, amount, date FROM bills_payments WHERE user_id = ? ORDER BY date DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(billSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String type = "Bill Payment (" + rs.getString("bill_type") + ")";
                        String recipient = rs.getString("customer_id");
                        double amount = rs.getDouble("amount");
                        String date = rs.getString("date");
                        transactions.add(new com.banking.TransferConfirm.Transaction(id, type, amount, date, recipient));
                    }
                }
            }

            String topUpSql = "SELECT id, network, mobile_number, amount, date FROM mobile_top_ups WHERE user_id = ? ORDER BY date DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(topUpSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String type = "Mobile Top-Up (" + rs.getString("network") + ")";
                        String recipient = rs.getString("mobile_number");
                        double amount = rs.getDouble("amount");
                        String date = rs.getString("date");
                        transactions.add(new com.banking.TransferConfirm.Transaction(id, type, amount, date, recipient));
                    }
                }
            }

            String creditSql = "SELECT id, card_type, amount, date FROM credit_card_payments WHERE user_id = ? ORDER BY date DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(creditSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String type = "Credit Card Payment (" + rs.getString("card_type") + ")";
                        String recipient = "N/A";
                        double amount = rs.getDouble("amount");
                        String date = rs.getString("date");
                        transactions.add(new com.banking.TransferConfirm.Transaction(id, type, amount, date, recipient));
                    }
                }
            }

            String govSql = "SELECT id, service_type, service_number, amount, date FROM government_services WHERE user_id = ? ORDER BY date DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(govSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String type = "Government Service (" + rs.getString("service_type") + ")";
                        String recipient = rs.getString("service_number");
                        double amount = rs.getDouble("amount");
                        String date = rs.getString("date");
                        transactions.add(new com.banking.TransferConfirm.Transaction(id, type, amount, date, recipient));
                    }
                }
            }

            String donationSql = "SELECT id, charity, amount, date FROM donations WHERE user_id = ? ORDER BY date DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(donationSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String type = "Donation (" + rs.getString("charity") + ")";
                        String recipient = rs.getString("charity");
                        double amount = rs.getDouble("amount");
                        String date = rs.getString("date");
                        transactions.add(new com.banking.TransferConfirm.Transaction(id, type, amount, date, recipient));
                    }
                }
            }

            String eduSql = "SELECT id, facility, student_id, amount, date FROM education_payments WHERE user_id = ? ORDER BY date DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(eduSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String type = "Education Payment (" + rs.getString("facility") + ")";
                        String recipient = rs.getString("student_id");
                        double amount = rs.getDouble("amount");
                        String date = rs.getString("date");
                        transactions.add(new com.banking.TransferConfirm.Transaction(id, type, amount, date, recipient));
                    }
                }
            }

            String insSql = "SELECT id, provider, policy_number, amount, date FROM insurance_payments WHERE user_id = ? ORDER BY date DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(insSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String type = "Insurance Payment (" + rs.getString("provider") + ")";
                        String recipient = rs.getString("policy_number");
                        double amount = rs.getDouble("amount");
                        String date = rs.getString("date");
                        transactions.add(new com.banking.TransferConfirm.Transaction(id, type, amount, date, recipient));
                    }
                }
            }

            String otherSql = "SELECT id, category, payee_name, amount, date FROM other_payments WHERE user_id = ? ORDER BY date DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(otherSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String type = "Other Payment (" + rs.getString("category") + ")";
                        String recipient = rs.getString("payee_name");
                        double amount = rs.getDouble("amount");
                        String date = rs.getString("date");
                        transactions.add(new com.banking.TransferConfirm.Transaction(id, type, amount, date, recipient));
                    }
                }
            }

            transactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));

            System.out.println("✅ Retrieved " + transactions.size() + " transactions for user: " + username);
            return transactions;
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving transactions: " + e.getMessage());
            e.printStackTrace();
            return transactions;
        }
    }

    public static boolean transferCreditCard(String username, String cardType, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Payment amount must be positive.");
            return false;
        }
        if (!userExists(username)) {
            System.out.println("❌ User does not exist: " + username);
            return false;
        }
        double balance = getBalance(username);
        if (balance < 0 || balance < amount) {
            System.out.println("❌ Insufficient funds for user: " + username);
            return false;
        }

        boolean updated = updateBalance(username, balance - amount);
        if (!updated) {
            System.out.println("❌ Failed to update balance for user: " + username);
            return false;
        }

        int userId = getUserId(username);
        if (userId == -1) {
            updateBalance(username, balance);
            System.out.println("❌ Failed to get user ID for: " + username);
            return false;
        }

        String sql = "INSERT INTO credit_card_payments (user_id, card_type, amount, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, cardType);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, "completed");
            pstmt.executeUpdate();

            recordTransaction(userId, "credit_card_payment", amount);

            System.out.println("✅ Credit card payment completed successfully: " + cardType + " for " + amount);
            return true;
        } catch (SQLException e) {
            updateBalance(username, balance);
            System.out.println("❌ Error recording credit card payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean transferGovernmentService(String username, String serviceType, String serviceNumber, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Service amount must be positive.");
            return false;
        }
        if (!userExists(username)) {
            System.out.println("❌ User does not exist: " + username);
            return false;
        }
        double balance = getBalance(username);
        if (balance < 0 || balance < amount) {
            System.out.println("❌ Insufficient funds for user: " + username);
            return false;
        }

        boolean updated = updateBalance(username, balance - amount);
        if (!updated) {
            System.out.println("❌ Failed to update balance for user: " + username);
            return false;
        }

        int userId = getUserId(username);
        if (userId == -1) {
            updateBalance(username, balance);
            System.out.println("❌ Failed to get user ID for: " + username);
            return false;
        }

        String sql = "INSERT INTO government_services (user_id, service_type, service_number, amount, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, serviceType);
            pstmt.setString(3, serviceNumber);
            pstmt.setDouble(4, amount);
            pstmt.setString(5, "completed");
            pstmt.executeUpdate();

            recordTransaction(userId, "government_service", amount);

            System.out.println("✅ Government service payment completed successfully: " + serviceType + " for " + amount);
            return true;
        } catch (SQLException e) {
            updateBalance(username, balance);
            System.out.println("❌ Error recording government service payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean transferDonation(String username, String charity, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Donation amount must be positive.");
            return false;
        }
        if (!userExists(username)) {
            System.out.println("❌ User does not exist: " + username);
            return false;
        }
        double balance = getBalance(username);
        if (balance < 0 || balance < amount) {
            System.out.println("❌ Insufficient funds for user: " + username);
            return false;
        }

        boolean updated = updateBalance(username, balance - amount);
        if (!updated) {
            System.out.println("❌ Failed to update balance for user: " + username);
            return false;
        }

        int userId = getUserId(username);
        if (userId == -1) {
            updateBalance(username, balance);
            System.out.println("❌ Failed to get user ID for: " + username);
            return false;
        }

        String sql = "INSERT INTO donations (user_id, charity, amount, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, charity);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, "completed");
            pstmt.executeUpdate();

            recordTransaction(userId, "donation", amount);

            System.out.println("✅ Donation completed successfully: " + charity + " for " + amount);
            return true;
        } catch (SQLException e) {
            updateBalance(username, balance);
            System.out.println("❌ Error recording donation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean transferEducationPayment(String username, String facility, String studentId, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Education payment amount must be positive.");
            return false;
        }
        if (!userExists(username)) {
            System.out.println("❌ User does not exist: " + username);
            return false;
        }
        double balance = getBalance(username);
        if (balance < 0 || balance < amount) {
            System.out.println("❌ Insufficient funds for user: " + username);
            return false;
        }

        boolean updated = updateBalance(username, balance - amount);
        if (!updated) {
            System.out.println("❌ Failed to update balance for user: " + username);
            return false;
        }

        int userId = getUserId(username);
        if (userId == -1) {
            updateBalance(username, balance);
            System.out.println("❌ Failed to get user ID for: " + username);
            return false;
        }

        String sql = "INSERT INTO education_payments (user_id, facility, student_id, amount, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, facility);
            pstmt.setString(3, studentId);
            pstmt.setDouble(4, amount);
            pstmt.setString(5, "completed");
            pstmt.executeUpdate();

            recordTransaction(userId, "education_payment", amount);

            System.out.println("✅ Education payment completed successfully: " + facility + " for " + amount);
            return true;
        } catch (SQLException e) {
            updateBalance(username, balance);
            System.out.println("❌ Error recording education payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean transferInsurancePayment(String username, String provider, String policyNumber, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Insurance payment amount must be positive.");
            return false;
        }
        if (!userExists(username)) {
            System.out.println("❌ User does not exist: " + username);
            return false;
        }
        double balance = getBalance(username);
        if (balance < 0 || balance < amount) {
            System.out.println("❌ Insufficient funds for user: " + username);
            return false;
        }

        boolean updated = updateBalance(username, balance - amount);
        if (!updated) {
            System.out.println("❌ Failed to update balance for user: " + username);
            return false;
        }

        int userId = getUserId(username);
        if (userId == -1) {
            updateBalance(username, balance);
            System.out.println("❌ Failed to get user ID for: " + username);
            return false;
        }

        String sql = "INSERT INTO insurance_payments (user_id, provider, policy_number, amount, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, provider);
            pstmt.setString(3, policyNumber);
            pstmt.setDouble(4, amount);
            pstmt.setString(5, "completed");
            pstmt.executeUpdate();

            recordTransaction(userId, "insurance_payment", amount);

            System.out.println("✅ Insurance payment completed successfully: " + provider + " for " + amount);
            return true;
        } catch (SQLException e) {
            updateBalance(username, balance);
            System.out.println("❌ Error recording insurance payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean transferOtherPayment(String username, String category, String payeeName, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Payment amount must be positive.");
            return false;
        }
        if (!userExists(username)) {
            System.out.println("❌ User does not exist: " + username);
            return false;
        }
        double balance = getBalance(username);
        if (balance < 0 || balance < amount) {
            System.out.println("❌ Insufficient funds for user: " + username);
            return false;
        }

        boolean updated = updateBalance(username, balance - amount);
        if (!updated) {
            System.out.println("❌ Failed to update balance for user: " + username);
            return false;
        }

        int userId = getUserId(username);
        if (userId == -1) {
            updateBalance(username, balance);
            System.out.println("❌ Failed to get user ID for: " + username);
            return false;
        }

        String sql = "INSERT INTO other_payments (user_id, category, payee_name, amount, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, category);
            pstmt.setString(3, payeeName);
            pstmt.setDouble(4, amount);
            pstmt.setString(5, "completed");
            pstmt.executeUpdate();

            recordTransaction(userId, "other_payment", amount);

            System.out.println("✅ Other payment completed successfully: " + category + " for " + amount);
            return true;
        } catch (SQLException e) {
            updateBalance(username, balance);
            System.out.println("❌ Error recording other payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateBalance(String username, double newBalance) {
        String sql = "UPDATE users SET total_balance = ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, username);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Balance updated for user: " + username + " to " + newBalance);
                return true;
            } else {
                System.out.println("❌ No rows updated for user: " + username);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error updating balance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public static boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("❌ Error checking user existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static int getUserId(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving user ID for " + username + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean updateUserDetails(String username, String fullName, String email, String mobile, String profileImagePath) {
        if (!userExists(username)) {
            System.out.println("❌ User does not exist: " + username);
            return false;
        }
        String sql = "UPDATE users SET full_name = ?, email = ?, mobile = ?, profile_image = ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, mobile);
            pstmt.setString(4, profileImagePath);
            pstmt.setString(5, username);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ User details updated for: " + username);
                return true;
            } else {
                System.out.println("❌ No rows updated for user: " + username);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error updating user details: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean recordTransaction(int userId, String type, double amount) {
        String sql = "INSERT INTO transactions (user_id, type, amount) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, type);
            pstmt.setDouble(3, amount);
            pstmt.executeUpdate();
            System.out.println("✅ Transaction recorded: " + type + " for amount " + amount);
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Error recording transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean recordTransfer(String fromUsername, String toUsername, double amount, String status) {
        if (!userExists(fromUsername) || !userExists(toUsername)) {
            System.out.println("❌ One or both users do not exist.");
            return false;
        }

        String sql = "INSERT INTO transfers (from_user, to_user, amount, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fromUsername);
            pstmt.setString(2, toUsername);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, status);
            pstmt.executeUpdate();

            int fromUserId = getUserId(fromUsername);
            if (fromUserId != -1) {
                recordTransaction(fromUserId, "expense", amount);
            }

            int toUserId = getUserId(toUsername);
            if (toUserId != -1) {
                recordTransaction(toUserId, "income", amount);
            }

            System.out.println("✅ Transfer recorded from " + fromUsername + " to " + toUsername + " for " + amount);
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Error recording transfer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateLastLogin(String username) {
        if (!userExists(username)) {
            System.out.println("❌ User does not exist: " + username);
            return false;
        }

        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Last login updated for user: " + username);
                return true;
            } else {
                System.out.println("❌ No rows updated for user: " + username);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error updating last login: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static String getLastLogin(String username) {
        String sql = "SELECT last_login FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("last_login");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving last login: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static String getUsernameById(String id) {
        if (id == null || id.trim().isEmpty()) {
            System.out.println("❌ Cannot retrieve username: ID is null or empty.");
            return null;
        }

        String sql = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String usernameResult = rs.getString("username");
                    if (usernameResult != null) {
                        System.out.println("✅ Retrieved username for ID " + id + ": " + usernameResult);
                        return usernameResult;
                    } else {
                        System.out.println("❌ Username is null for ID: " + id);
                        return null;
                    }
                } else {
                    System.out.println("❌ No user found with ID: " + id);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving username by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean addCard(String username, String cardType, double amount) {
        int userId = getUserId(username);
        if (userId == -1) {
            System.out.println("❌ User not found: " + username);
            return false;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false); // Start transaction

            String checkSql = """
                SELECT 
                    (SELECT COUNT(*) FROM cards WHERE user_id = ? AND amount > 0) as card_count,
                    (SELECT COUNT(*) FROM cards WHERE user_id = ? AND card_type = ? AND amount > 0) as dup_count
            """;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, userId);
                checkStmt.setString(3, cardType);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        int cardCount = rs.getInt("card_count");
                        int dupCount = rs.getInt("dup_count");

                        if (cardCount >= 4) {
                            System.out.println("❌ Maximum number of cards (4) reached for user: " + username);
                            return false;
                        }
                        if (dupCount > 0) {
                            System.out.println("❌ Card type already exists for user: " + username);
                            return false;
                        }

                        String insertSql = "INSERT INTO cards (user_id, card_type, amount, gdate) VALUES (?, ?, ?, strftime('%Y-%m-%d %H:%M:%S', 'now'))";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, userId);
                            insertStmt.setString(2, cardType);
                            insertStmt.setDouble(3, amount);
                            insertStmt.executeUpdate();

                            if (cardCount > 0) {
                                String transactionSql = "INSERT INTO transactions (user_id, type, amount) VALUES (?, 'income', ?)";
                                try (PreparedStatement transStmt = conn.prepareStatement(transactionSql)) {
                                    transStmt.setInt(1, userId);
                                    transStmt.setDouble(2, amount);
                                    transStmt.executeUpdate();
                                }
                            }

                            conn.commit();
                            System.out.println("✅ Card added successfully for user: " + username);
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            System.out.println("❌ Error adding card: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static int getUserCardCount(String username) {
        int userId = getUserId(username);
        if (userId == -1) {
            System.out.println("❌ User not found: " + username);
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM cards WHERE user_id = ? AND amount > 0";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("✅ Active cards count for user " + username + ": " + count);
                    return count;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error counting cards: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public static List<Card> getUserCards(String username) {
        List<Card> cards = new ArrayList<>();
        int userId = getUserId(username);
        if (userId == -1) {
            System.out.println("❌ User not found: " + username);
            return cards;
        }

        double totalBalance = getTotalBalance(username);

        String sql = "SELECT id, card_type, amount FROM cards WHERE user_id = ? AND amount > 0 ORDER BY amount DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean isFirstCard = true;
                while (rs.next()) {
                    if (isFirstCard) {
                        cards.add(new Card(
                                rs.getInt("id"),
                                rs.getString("card_type"),
                                totalBalance
                        ));
                        isFirstCard = false;
                    } else {
                        cards.add(new Card(
                                rs.getInt("id"),
                                rs.getString("card_type"),
                                rs.getDouble("amount")
                        ));
                    }
                }
                System.out.println("✅ Retrieved " + cards.size() + " active cards for user: " + username);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving cards: " + e.getMessage());
            e.printStackTrace();
        }
        return cards;
    }

    public static Card getCardDetails(String username, int cardId) {
        int userId = getUserId(username);
        if (userId == -1) {
            System.out.println("❌ User not found: " + username);
            return null;
        }

        String sql = "SELECT id, card_type, amount FROM cards WHERE user_id = ? AND id = ? AND amount > 0";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Card(
                            rs.getInt("id"),
                            rs.getString("card_type"),
                            rs.getDouble("amount")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving card details: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateCardAmount(String username, int cardId, double newAmount) {
        int userId = getUserId(username);
        if (userId == -1) {
            System.out.println("❌ User not found: " + username);
            return false;
        }

        boolean isFirstCard = false;
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String checkSql = "SELECT MIN(id) as first_card_id FROM cards WHERE user_id = ? AND amount > 0";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        int firstCardId = rs.getInt("first_card_id");
                        isFirstCard = (firstCardId == cardId);
                    }
                }
            }

            if (isFirstCard) {
                conn.setAutoCommit(false);
                try {
                    String cardSql = "UPDATE cards SET amount = ? WHERE user_id = ? AND id = ?";
                    try (PreparedStatement cardStmt = conn.prepareStatement(cardSql)) {
                        cardStmt.setDouble(1, newAmount);
                        cardStmt.setInt(2, userId);
                        cardStmt.setInt(3, cardId);
                        cardStmt.executeUpdate();
                    }

                    String userSql = "UPDATE users SET total_balance = ? WHERE id = ?";
                    try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                        userStmt.setDouble(1, newAmount);
                        userStmt.setInt(2, userId);
                        userStmt.executeUpdate();
                    }

                    conn.commit();
                    System.out.println("✅ Card amount and total balance updated successfully for card ID: " + cardId);
                    return true;
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } else {
                String sql = "UPDATE cards SET amount = ? WHERE user_id = ? AND id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setDouble(1, newAmount);
                    pstmt.setInt(2, userId);
                    pstmt.setInt(3, cardId);
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("✅ Card amount updated successfully for card ID: " + cardId);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error updating card amount: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public static class Card {
        private int id;
        private String cardType;
        private double amount;

        public Card(int id, String cardType, double amount) {
            this.id = id;
            this.cardType = cardType;
            this.amount = amount;
        }

        public int getId() { return id; }
        public String getCardType() { return cardType; }
        public double getAmount() { return amount; }
    }
}