package org.example.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBase {
    private Connection connection;
    private final String dbPath;

    public DataBase(String dbPath) {
        this.dbPath = dbPath;
    }

    public void openConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        }
    }

    public void createUsersTable() throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            data TEXT NOT NULL UNIQUE
        );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void createTable() throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT, 
            data TEXT NOT NULL,
            from_user TEXT NOT NULL,
            to_user TEXT NOT NULL,
            bytes_data TEXT NOT NULL,
            FOREIGN KEY (data) REFERENCES users(data) ON DELETE CASCADE
        );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void insertNewUser(String data) throws SQLException {
        String sql = "INSERT INTO users (data) VALUES (?) ON CONFLICT (data) DO NOTHING;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data);
            pstmt.executeUpdate();
        }
    }

    public void insertMessage(String data, String fromUser, String toUser, String bytesData) throws SQLException {
        String sql = """
            INSERT INTO messages (data, from_user, to_user, bytes_data)
            VALUES (?, ?, ?, ?);
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data);
            pstmt.setString(2, fromUser);
            pstmt.setString(3, toUser);
            pstmt.setString(4, bytesData);
            pstmt.executeUpdate();
        }
    }

    public List<String> getUniqueData() throws SQLException {
        List<String> uniqueData = new ArrayList<>();
        String sql = "SELECT DISTINCT data FROM users;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                uniqueData.add(rs.getString("data"));
            }
        }
        return uniqueData;
    }

    public List<String> getToUsersByData(String to_user) throws SQLException {
        List<String> toUsers = new ArrayList<>();
        String sql = "SELECT from_user FROM messages WHERE to_user = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, to_user);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    toUsers.add(rs.getString("from_user"));
                }
            }
        }
        return toUsers;
    }

    public List<Message> getDataByDataAndToUser(String data, String toUser) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE data = ? AND to_user = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data);
            pstmt.setString(2, toUser);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(new Message(
                        rs.getInt("id"),
                        rs.getString("data"),
                        rs.getString("from_user"),
                        rs.getString("to_user"),
                        rs.getBytes("bytes_data")
                ));
            }
        }
        return messages;
    }

    public String getAudio(String to_user, String from_user) throws SQLException {
        String sql = "SELECT bytes_data FROM messages WHERE to_user = ? AND from_user = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, to_user);
            pstmt.setString(2, from_user);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("bytes_data");
            }
        }
        return null;
    }

    public String clearString(String str) {
        if (!str.isEmpty()) {
            str = "";
            System.gc();
            return str;
        } else {
            return "";
        }
    }

    public void deleteMessageById(String data) throws SQLException {
        String sql = "DELETE FROM messages WHERE ROWID IN ("
                + "    SELECT ROWID FROM messages WHERE data = ? LIMIT 1"
                + ");";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data);
            pstmt.executeUpdate();
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public static class Message {
        private final int id;
        private final String data;
        private final String fromUser;
        private final String toUser;
        private final byte[] bytesData;

        public Message(int id, String data, String fromUser, String toUser, byte[] bytesData) {
            this.id = id;
            this.data = data;
            this.fromUser = fromUser;
            this.toUser = toUser;
            this.bytesData = bytesData;
        }

        public int getId() {
            return id;
        }

        public String getData() {
            return data;
        }

        public String getFromUser() {
            return fromUser;
        }

        public String getToUser() {
            return toUser;
        }

        public byte[] getBytesData() {
            return bytesData;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "id=" + id +
                    ", data='" + data + '\'' +
                    ", fromUser='" + fromUser + '\'' +
                    ", toUser='" + toUser + '\'' +
                    ", bytesData=" + (bytesData != null ? bytesData.length : 0) + " bytes" +
                    '}';
        }
    }
}