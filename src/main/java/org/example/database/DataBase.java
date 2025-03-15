package org.example.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBase {

    private Connection connection; // Connection to the database
    private final String dbPath; // Path to the SQLite database file

    /**
     * Constructor to initialize the database path.
     * @param dbPath the path to the SQLite database file
     */
    public DataBase(String dbPath) {
        this.dbPath = dbPath;
    }

    /**
     * Opens a connection to the SQLite database.
     * @throws SQLException if an error occurs while connecting to the database
     */
    public void openConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Establish a connection to the SQLite database using the provided path
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                System.out.println("Database connection established.");
            } catch (SQLException e) {
                System.err.println("Error connecting to the database: " + e.getMessage());
                throw e; // Re-throw the exception to handle it elsewhere
            }
        }
    }

    /**
     * Creates the 'users' table if it does not already exist.
     * The 'users' table contains unique usernames.
     * @throws SQLException if an error occurs while creating the table
     */
    public void createUsersTable() throws SQLException {
        /*
         * Table structure:
         * id - auto-incremented primary key
         * data - unique username (primary key)
         */
        String sql = """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            data TEXT NOT NULL UNIQUE
        );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql); // Execute the SQL statement to create the table
            System.out.println("Table 'users' created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating table 'users': " + e.getMessage());
            throw e; // Re-throw the exception to handle it elsewhere
        }
    }

    /**
     * Creates the 'messages' table if it does not already exist.
     * The 'messages' table contains audio messages sent between users.
     * @throws SQLException if an error occurs while creating the table
     */
    public void createTable() throws SQLException {
        /*
         * Table structure:
         * id - auto-incremented primary key
         * data - username (foreign key referencing users.data)
         * from_user - sender of the audio message
         * to_user - recipient of the audio message
         * bytes_data - audio message data stored as text
         */
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
            stmt.execute(sql); // Execute the SQL statement to create the table
            System.out.println("Table 'messages' created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating table 'messages': " + e.getMessage());
            throw e; // Re-throw the exception to handle it elsewhere
        }
    }

    /**
     * Inserts a new user into the 'users' table.
     * If the user already exists, no action is taken.
     * @param data the username to insert
     * @throws SQLException if an error occurs while inserting the user
     */
    public void insertNewUser(String data) throws SQLException {
        String sql = "INSERT INTO users (data) VALUES (?) ON CONFLICT (data) DO NOTHING;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data); // Set the username parameter
            int affectedRows = pstmt.executeUpdate(); // Execute the insert statement
            System.out.println("Affected rows: " + affectedRows);
            if (affectedRows > 0) {
                System.out.println("User added to the 'users' table.");
            } else {
                System.out.println("Something went wrong while adding a new user.");
            }
        } catch (SQLException e) {
            System.err.println("Error inserting value into table 'users': " + e.getMessage());
            throw e; // Re-throw the exception to handle it elsewhere
        }
    }

    /**
     * Inserts a new audio message into the 'messages' table.
     * @param data the username associated with the message
     * @param fromUser the sender of the message
     * @param toUser the recipient of the message
     * @param bytesData the audio message data (stored as text)
     * @throws SQLException if an error occurs while inserting the message
     */
    public void insertMessage(String data, String fromUser, String toUser, String bytesData) throws SQLException {
        String sql = """
            INSERT INTO messages (data, from_user, to_user, bytes_data)
            VALUES (?, ?, ?, ?);
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data); // Set the username parameter
            pstmt.setString(2, fromUser); // Set the sender parameter
            pstmt.setString(3, toUser); // Set the recipient parameter
            pstmt.setString(4, bytesData); // Set the audio message data parameter
            int affectedRows = pstmt.executeUpdate(); // Execute the insert statement
            if (affectedRows > 0) {
                System.out.println("Message successfully added.");
            } else {
                System.out.println("Failed to add message.");
            }
        } catch (SQLException e) {
            System.err.println("Error inserting data: " + e.getMessage());
            throw e; // Re-throw the exception to handle it elsewhere
        }
    }

    /**
     * Retrieves all unique usernames from the 'users' table.
     * @return a list of unique usernames
     * @throws SQLException if an error occurs while retrieving the usernames
     */
    public List<String> getUniqueData() throws SQLException {
        List<String> uniqueData = new ArrayList<>();
        String sql = "SELECT DISTINCT data FROM users;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                uniqueData.add(rs.getString("data")); // Add each username to the list
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving unique values for 'data': " + e.getMessage());
            throw e; // Re-throw the exception to handle it elsewhere
        }
        return uniqueData;
    }

    /**
     * Retrieves all senders ('from_user') associated with a specific username ('data').
     * @param data the username to filter by
     * @return a list of senders
     * @throws SQLException if an error occurs while retrieving the senders
     */
    public List<String> getToUsersByData(String data) throws SQLException {
        List<String> toUsers = new ArrayList<>();
        String sql = "SELECT from_user FROM messages WHERE data = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data); // Set the username parameter
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    toUsers.add(rs.getString("from_user")); // Add each sender to the list
                }
            }
        } catch (SQLException e) {
            System.err.println("Error filtering 'from_user' by 'data': " + e.getMessage());
            throw e; // Re-throw the exception to handle it elsewhere
        }
        return toUsers;
    }

    /**
     * Retrieves all messages sent to a specific recipient ('to_user') by a specific username ('data').
     * @param data the username associated with the messages
     * @param toUser the recipient of the messages
     * @return a list of messages
     * @throws SQLException if an error occurs while retrieving the messages
     */
    public List<Message> getDataByDataAndToUser(String data, String toUser) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE data = ? AND to_user = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data); // Set the username parameter
            pstmt.setString(2, toUser); // Set the recipient parameter
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(new Message(
                        rs.getInt("id"), // Retrieve the message ID
                        rs.getString("data"), // Retrieve the username
                        rs.getString("from_user"), // Retrieve the sender
                        rs.getString("to_user"), // Retrieve the recipient
                        rs.getBytes("bytes_data") // Retrieve the audio message data
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving data: " + e.getMessage());
            throw e; // Re-throw the exception to handle it elsewhere
        }
        return messages;
    }

    /**
     * Retrieves the audio message data for a specific username and sender.
     * @param data the username associated with the message
     * @param from_user the sender of the message
     * @return the audio message data as a string, or null if no message is found
     * @throws SQLException if an error occurs while retrieving the audio data
     */
    public String getAudio(String data, String from_user) throws SQLException {
        String sql = "SELECT bytes_data FROM messages WHERE data = ? AND from_user = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data); // Set the username parameter
            pstmt.setString(2, from_user); // Set the sender parameter
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("rs.getString(\"bytes_data\") ---" + rs.getString("bytes_data"));
                return rs.getString("bytes_data"); // Return the audio message data
            } else {
                return null; // Return null if no message is found
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong: " + e.getMessage());
            return null; // Return null if an error occurs
        }
    }

    /**
     * Deletes a message from the 'messages' table by its ID.
     * @param id the ID of the message to delete
     * @throws SQLException if an error occurs while deleting the message
     */
    public void deleteMessageById(int id) throws SQLException {
        String sql = "DELETE FROM messages WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id); // Set the message ID parameter
            int affectedRows = pstmt.executeUpdate(); // Execute the delete statement
            if (affectedRows > 0) {
                System.out.println("Message with id=" + id + " successfully deleted.");
            } else {
                System.out.println("Message with id=" + id + " not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting message: " + e.getMessage());
            throw e; // Re-throw the exception to handle it elsewhere
        }
    }

    /**
     * Closes the connection to the database.
     * @throws SQLException if an error occurs while closing the connection
     */
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close(); // Close the database connection
            System.out.println("Database connection closed.");
        }
    }

    /**
     * A helper class to represent a message retrieved from the database.
     */
    public static class Message {
        private final int id; // Message ID
        private final String data; // Username associated with the message
        private final String fromUser; // Sender of the message
        private final String toUser; // Recipient of the message
        private final byte[] bytesData; // Audio message data

        /**
         * Constructor to initialize a Message object.
         * @param id the message ID
         * @param data the username associated with the message
         * @param fromUser the sender of the message
         * @param toUser the recipient of the message
         * @param bytesData the audio message data
         */
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