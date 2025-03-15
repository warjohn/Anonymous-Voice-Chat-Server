# Anonymous Voice Chat Server 
## Overview
This project is a Spring Boot-based server  designed to facilitate anonymous voice communication  between users. It allows individuals to record, share, and play back audio messages without revealing their identities. The system ensures privacy by hashing user identifiers and storing audio data securely in an SQLite database.

Whether you're building a platform for anonymous feedback, private messaging, or creative collaboration, this server provides the foundation for secure and seamless voice communication. 


## Features

- Anonymous Communication : 
  - Users can send and receive audio messages without revealing personal information.
      User identifiers are hashed to ensure complete anonymity.
- Audio Recording and Playback :
  - Capture audio from the microphone and compress it for efficient storage.
          Decompress and play back stored audio messages with ease.
- Database Management : 
  - Store user information and audio messages in an SQLite database.
        Retrieve, filter, and delete messages based on hashed user IDs.
- RESTful APIs : 
  - Endpoints for managing users, recording audio, and playing back messages.
          Simple integration with client-side applications.
- Custom Interceptor : 
  - Intercept HTTP requests to log details and insert new users into the database automatically.
- Security Configuration : 
  - Basic security setup with CSRF protection disabled (can be extended for production use).
- Utility Functions : 
  - Generate random IDs for unique identification.
          Calculate SHA-256 hashes for input strings to ensure privacy.
- Asynchronous Processing : 
  - Enable asynchronous execution for improved performance during audio operations.

## Project Structure

The project is organized into the following packages:

1) org.example.audio : 
   - Handles audio recording, compression, decompression, and playback.
           Classes: Audio
2) org.example.database : 
   - Manages database connections and operations.
           Classes: DataBase, DataBaseConfig, Message
3) org.example.server : 
   - Contains the main application logic, REST controllers, interceptors, and security configurations.
           Classes: CertificateController, Interceptor, InterceptorConfig, SecurityConfig
4) org.example.utility : 
   - Provides utility functions for generating random IDs and calculating hashes.
           Classes: Utils

## Dependencies

This project uses the following key dependencies:

- Spring Boot : For building the application and managing dependencies. 
- SQLite : As the database for storing user and message data. 
- Spring Security : For configuring security settings.
- AspectJ : For handling cross-cutting concerns like logging. 
- Java Sound API : For audio recording and playback.

# Getting Started  
## Prerequisites

- Java Development Kit (JDK) : 
  - Ensure you have JDK 17 or later installed.
- Maven : 
  - Install Maven to build and manage dependencies.
- SQLite : 
  - Ensure SQLite is installed and accessible on your system.
         
  
# Installation

### 1) Clone the Repository :
bash
```declarative
git clone https://github.com/your-repository-url.git
cd your-repository-folder
```

### 2) Build the Project :
bash 
```declarative
mvn clean install
```

### 3) Run the Application :
bash
```declarative
mvn spring-boot:run
```
### 4) Access the APIs :
The application will start on http://localhost:8080. You can access the APIs using tools like Postman or cURL. 


## Why This Project? 

- Privacy-Focused : Ensures complete anonymity by hashing user identifiers and encrypting audio data.
- Scalable : Built with modularity and scalability in mind, making it easy to extend and integrate into larger systems.
- Secure : Includes basic security configurations to protect against common vulnerabilities (e.g., CSRF).
- Easy to Use : Provides intuitive RESTful APIs for seamless integration with client-side applications.


# Conclusion

The Anonymous Voice Chat Server  is a robust and flexible solution for building privacy-focused communication platforms. Whether you're creating a space for anonymous feedback, private conversations, or collaborative projects, this server provides the tools you need to get started.

Feel free to contribute, extend, or customize the project to suit your specific requirements! 
