package org.example.audio;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import java.util.Base64;

public class Audio {

    // Buffer size for audio data chunks
    public static final int CHUNK_SIZE = 2048;

    // Sampling rate for audio (16 kHz)
    private static final float SAMPLE_RATE = 16000;

    // Size of each audio sample in bits (16-bit samples)
    private static final int SAMPLE_SIZE_IN_BITS = 16;

    // Number of audio channels (1 for mono, 2 for stereo)
    private static final int CHANNELS = 1;

    // Whether the audio format is signed (true for signed, false for unsigned)
    private static final boolean SIGNED = true;

    // Endianness of the audio data (false for little-endian, true for big-endian)
    private static final boolean BIG_ENDIAN = false;

    // Flag to indicate whether recording is in progress
    private boolean isRecording;

    // Output stream to store recorded audio data
    private ByteArrayOutputStream byteArrayOutputStream;

    // Line for capturing audio from the microphone
    private TargetDataLine microphone;

    // Line for playing back audio through speakers
    private SourceDataLine speakers;

    // Format of the audio data (sampling rate, sample size, etc.)
    private AudioFormat audioFormat;

    /**
     * Constructor to initialize the audio format and output stream.
     */
    public Audio() {
        // Initialize the audio format with the specified parameters
        this.audioFormat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);

        // Initialize the output stream to store recorded audio data
        byteArrayOutputStream = new ByteArrayOutputStream();
    }

    /**
     * Starts recording audio from the microphone.
     * @throws LineUnavailableException if the microphone line is unavailable
     */
    public void startMicrophone() throws LineUnavailableException {
        // Create a description of the desired audio line
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

        // Get the microphone line from the system
        microphone = (TargetDataLine) AudioSystem.getLine(info);

        // Open and start the microphone line
        microphone.open(audioFormat);
        microphone.start();

        // Set the recording flag to true
        isRecording = true;

        // Start a thread to continuously read audio data from the microphone
        Thread recordingThread = new Thread(() -> {
            byte[] buffer = new byte[4096]; // Buffer to temporarily store audio data

            // Continuously read data while recording is active
            while (isRecording) {
                int bytesRead = microphone.read(buffer, 0, buffer.length); // Read audio data into the buffer
                byteArrayOutputStream.write(buffer, 0, bytesRead); // Write the data to the output stream
            }
        });
        recordingThread.start(); // Start the recording thread
    }

    /**
     * Stops recording audio from the microphone.
     */
    public void stopMicrophone() {
        isRecording = false; // Stop the recording loop

        // Close the microphone line if it is open
        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }
    }

    /**
     * Captures a single chunk of audio data from the microphone.
     * @return a byte array containing the captured audio data
     */
    public byte[] captureAudio() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[CHUNK_SIZE];
        int bytesRead;

        try {
            // Read one chunk of audio data from the microphone
            while ((bytesRead = microphone.read(buffer, 0, buffer.length)) > 0) {
                byteArrayOutputStream.write(buffer, 0, bytesRead); // Write the data to the output stream
                break; // Exit after capturing one chunk
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print any errors that occur
        }

        return byteArrayOutputStream.toByteArray(); // Return the captured audio data
    }

    /**
     * Starts the audio playback system.
     * @throws LineUnavailableException if the speaker line is unavailable
     */
    public void startSpeakers() throws LineUnavailableException {
        // Create a description of the desired audio line
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        // Get the speaker line from the system
        speakers = (SourceDataLine) AudioSystem.getLine(info);

        // Open and start the speaker line
        speakers.open(audioFormat);
        speakers.start();
    }

    /**
     * Stops the audio playback system.
     */
    public void stopSpeakers() {
        // Close the speaker line if it is open
        if (speakers != null) {
            speakers.stop();
            speakers.close();
        }
    }

    /**
     * Plays back the given audio data through the speakers.
     * @param audioData the audio data to play
     */
    public void playAudio(byte[] audioData) {
        // Initialize the speaker system if it is not already open
        if (speakers == null || !speakers.isOpen()) {
            try {
                startSpeakers();
            } catch (LineUnavailableException e) {
                e.printStackTrace(); // Print any errors that occur
                return;
            }
        }

        try {
            // Write the audio data to the speaker line for playback
            speakers.write(audioData, 0, audioData.length);
        } catch (Exception e) {
            e.printStackTrace(); // Print any errors that occur
        }
    }

    /**
     * Plays back the given audio data and stops the speaker system afterward.
     * @param audioData the audio data to play
     */
    public void playAndStopAudio(byte[] audioData) {
        playAudio(audioData); // Play the audio data
        stopSpeakers(); // Stop the speaker system after playback
    }

    /**
     * Retrieves the audio data that has been recorded so far.
     * @return a byte array containing the recorded audio data
     */
    public byte[] getCapturedAudio() {
        return byteArrayOutputStream.toByteArray(); // Return the recorded audio data
    }

    /**
     * Compresses raw audio data using GZIP and encodes it in Base64.
     * @param rawAudio the raw audio data to compress
     * @return a Base64-encoded string containing the compressed audio data
     */
    public String compressAudio(byte[] rawAudio) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {

            gzipOutputStream.write(rawAudio); // Compress the raw audio data
            gzipOutputStream.finish(); // Finalize the compression process

            // Encode the compressed data in Base64 and return it as a string
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace(); // Print any errors that occur

            // If compression fails, return the raw audio data encoded in Base64
            return Base64.getEncoder().encodeToString(rawAudio);
        }
    }

    /**
     * Decompresses Base64-encoded audio data using GZIP.
     * @param compressedAudioString the Base64-encoded string containing the compressed audio data
     * @return a byte array containing the decompressed audio data
     */
    public byte[] decompressAudio(String compressedAudioString) {
        byte[] decoded = Base64.getDecoder().decode(compressedAudioString); // Decode the Base64 string

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decoded);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            // Read the decompressed data in chunks
            while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead); // Write the decompressed data to the output stream
            }

            return byteArrayOutputStream.toByteArray(); // Return the decompressed audio data
        } catch (Exception e) {
            e.printStackTrace(); // Print any errors that occur

            // If decompression fails, return the decoded (but still compressed) data
            return decoded;
        }
    }
}