package org.example.server;

import jakarta.servlet.http.HttpServletRequest;
import org.example.audio.Audio;
import org.example.database.DataBase;
import org.example.utility.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private List<String> blacklist = new ArrayList<>(); // A list of blacklisted users

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
     * Handles the "/start-recording" endpoint. Starts recording audio from the microphone.
     * @param request the HTTP request object
     * @return a ResponseEntity indicating whether the recording started successfully
     */
    @PostMapping("/start-recording")
    public ResponseEntity<String> startRecording(HttpServletRequest request) {
        System.out.println("start-recording");
        if (clients == null) {
            return ResponseEntity.status(404).body("Enter username - "); // Ensure a client username is set
        }
        try {
            audioService.startMicrophone(); // Start recording audio
            return ResponseEntity.ok("Recording started successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to start recording: " + e.getMessage());
        }
    }

    /**
     * Handles the "/stop-recording" endpoint. Stops recording audio and saves it to the database.
     * @param request the HTTP request object
     * @return a ResponseEntity indicating whether the recording stopped successfully
     */
    @PostMapping("/stop-recording")
    public ResponseEntity<String> stopRecodring(HttpServletRequest request) {
        System.out.println("stop-recording");
        try {
            audioService.stopMicrophone(); // Stop recording audio
            String audio = audioService.compressAudio(audioService.getCapturedAudio()); // Compress the recorded audio
            // Save the compressed audio to the database
            dataBase.insertMessage(Utils.calculateHash(request.getRemoteAddr()), Utils.calculateHash(request.getRemoteAddr()), clients, audio);
            return ResponseEntity.ok("Recording stopped successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to stop recording: " + e.getMessage());
        }
    }

    /**
     * Handles the "/play-audio" endpoint. Plays back audio stored in the database.
     * @param request the HTTP request object
     * @return a ResponseEntity indicating whether the audio playback started successfully
     */
    @PostMapping("/play-audio")
    public ResponseEntity<String> playAudio(HttpServletRequest request) {
        System.out.println("play-audio");
        try {
            // Decompress and play the audio retrieved from the database
            audioService.playAndStopAudio(audioService.decompressAudio(dataBase.getAudio(Utils.calculateHash(request.getRemoteAddr()), sender)));
            return ResponseEntity.ok("Audio playback started successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to play audio: " + e.getMessage());
        }
    }

    /**
     * Handles the "/stop-audio" endpoint. Stops audio playback.
     * @return a ResponseEntity indicating whether the audio was stopped successfully
     */
    @PostMapping("/stop-audio")
    public ResponseEntity<String> stopAudio() {
        System.out.println("stop-audio");
        try {
            audioService.stopMicrophone(); // Stop the microphone (TODO: Currently does nothing)
            return ResponseEntity.ok("Audio stopped");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("How can you call this error, pls don't touch my code again");
        }
    }

    /**
     * Handles the "/set-clients" endpoint. Sets the current client username.
     * @param client the client username provided in the request body
     * @return a ResponseEntity indicating success
     */
    @PostMapping("/set-clients")
    public ResponseEntity<Void> setClients(@RequestBody String client) {
        this.clients = client; // Set the client username
        System.out.println("clients - " + clients.toString());
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
        System.out.println("blacklist" + blacklist);
        return ResponseEntity.ok().build(); // Return a success response
    }
}