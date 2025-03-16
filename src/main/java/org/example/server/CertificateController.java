package org.example.server;

import jakarta.servlet.http.HttpServletRequest;
import org.example.audio.Audio;
import org.example.database.DataBase;
import org.example.utility.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST controller for handling various API endpoints related to user interactions, audio recording, and database operations.
 * This class is annotated with @RestController, making it a Spring-managed RESTful web service.
 */
@RestController // Marks this class as a REST controller, where all methods return data instead of views
@RequestMapping("/") // Maps all endpoints in this controller to the root path ("/")
public class CertificateController {

    private String clients; // Stores the current client username
    private String sender; // Stores the sender's email or identifier
    private List<String> blacklist = new ArrayList<>(); // A list of blacklisted users TODO make ban users

    private final Audio audioService = new Audio(); // Service for handling audio recording and playback
    private final DataBase dataBase; // Database service for interacting with the database

    /**
     * Constructor to inject the DataBase dependency using Spring's @Autowired annotation.
     * @param dataBase the database service instance
     */
    @Autowired
    public CertificateController(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    /**
     * Handles the "/hello" endpoint. Returns a simple greeting message.
     * @param request the HTTP request object containing client details
     * @return a ResponseEntity with a greeting message
     * @throws Exception if an error occurs during processing
     */
    @GetMapping("/hello")
    public ResponseEntity<String> getCongra(HttpServletRequest request) throws Exception {
        String clientIP = request.getRemoteAddr(); // Get the client's IP address
        String clientID = String.valueOf(Utils.generateRandomID()); // Generate a random ID for the client
        int clientPort = request.getRemotePort(); // Get the client's port number
        return ResponseEntity.ok("Hello World"); // Return a successful response with "Hello World"
    } // no longer used

    /**
     * Handles the "/get-users" endpoint. Retrieves a list of unique usernames from the database.
     * @param request the HTTP request object
     * @return a ResponseEntity containing a list of unique usernames
     * @throws SQLException if an error occurs while querying the database
     */
    @GetMapping("/get-users")
    public ResponseEntity<List<String>> getUsers(HttpServletRequest request) throws SQLException {
        return ResponseEntity.ok(dataBase.getUniqueData()); // Fetch and return unique usernames from the database
    }

    /**
     * Handles the "/get-mail" endpoint. Retrieves a list of senders associated with the current user.
     * @param request the HTTP request object
     * @return a ResponseEntity containing a list of senders
     * @throws SQLException if an error occurs while querying the database
     */
    @GetMapping("/get-mail")
    public ResponseEntity<List<String>> getMail(HttpServletRequest request) throws SQLException {
        return ResponseEntity.ok(dataBase.getToUsersByData(Utils.calculateHash(request.getRemoteAddr())));
        // Fetch and return senders associated with the hashed client IP address
    }

    /**
     * Handles the "/select-mail" endpoint. Sets the sender's email or identifier.
     * @param requestBody the request body containing the sender's email
     * @return a ResponseEntity indicating success
     */
    @PostMapping("/select-mail")
    public ResponseEntity<String> getUser(@RequestBody Map<String, String> requestBody) {
        sender = requestBody.get("mail"); // Extract the sender's email from the request body
        return ResponseEntity.status(202).body("All good"); // Return a success response
    }

    /**
     * Handles the "/upload-audio" endpoint. Processes uploaded audio files.
     * @param file the uploaded audio file
     * @param request the HTTP request object
     * @return a ResponseEntity indicating success or failure
     */
    @PostMapping("/upload-audio")
    public ResponseEntity<String> uploadAudio(@RequestParam("audio") MultipartFile file, HttpServletRequest request) {
        try (var inputStream = file.getInputStream()) { // Use try-with-resources to ensure proper resource management
            byte[] audioData = file.getBytes();
            if (audioData.length == 20) {
                return ResponseEntity.status(300).body("Do nothing");
            }
            String compressedAudio = audioService.compressAudio(audioData);
            dataBase.insertMessage(
                    Utils.calculateHash(request.getRemoteAddr()),
                    Utils.calculateHash(request.getRemoteAddr()),
                    clients,
                    compressedAudio
            );
            return ResponseEntity.ok("Audio uploaded successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to upload audio: " + e.getMessage());
        }
    }

    /**
     * Handles the "/get-audio" endpoint. Retrieves and decompresses audio data.
     * @param request the HTTP request object
     * @return a ResponseEntity containing the audio data or an error message
     * @throws SQLException if an error occurs while querying the database
     */
    @GetMapping("/get-audio")
    public ResponseEntity<byte[]> downloadAudio(HttpServletRequest request) throws SQLException {
        try {
            String compressedAudio = dataBase.getAudio(Utils.calculateHash(request.getRemoteAddr()), sender);
            byte[] decompressedAudio = audioService.decompressAudio(compressedAudio);
            dataBase.deleteMessageById(sender); // Delete the message after retrieval
            return ResponseEntity.status(200).body(decompressedAudio);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        } finally {
            // Ensure resources are released by clearing internal buffers
            audioService.clearBuffer(); // Clear the audio buffer explicitly
        }
    }

    /**
     * Handles the "/set-clients" endpoint. Sets the current client username.
     * @param client the client username provided in the request body
     * @return a ResponseEntity indicating success
     */
    @PostMapping("/set-clients")
    public ResponseEntity<Void> setClients(@RequestBody String[] client) {
        this.clients = client[0]; // Set the client username
        return ResponseEntity.ok().build(); // Return a success response
    }

    /**
     * Handles the "/set-blacklist" endpoint. Updates the blacklist with a new list of blacklisted users.
     * @param blackList the list of blacklisted users provided in the request body
     * @return a ResponseEntity indicating success
     */
    @PostMapping("/set-blacklist")
    public ResponseEntity<Void> setBlacklist(@RequestBody List<String> blackList) {
        this.blacklist.clear(); // Clear the existing blacklist
        this.blacklist.addAll(blackList); // Add all new blacklisted users
        return ResponseEntity.ok().build(); // Return a success response
    }
}