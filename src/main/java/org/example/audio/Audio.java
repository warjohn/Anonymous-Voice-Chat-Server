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

    private byte[] decoded;

    /**
     * Constructor to initialize the audio format and output stream.
     */
    public Audio() {
        this.audioFormat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        byteArrayOutputStream = new ByteArrayOutputStream();
    }

    /**
     * Starts recording audio from the microphone.
     * @throws LineUnavailableException if the microphone line is unavailable
     */
    public void startMicrophone() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(audioFormat);
        microphone.start();

        isRecording = true;

        Thread recordingThread = new Thread(() -> {
            byte[] buffer = new byte[4096];
            while (isRecording) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
        });
        recordingThread.start();
    }

    /**
     * Stops recording audio from the microphone.
     */
    public void stopMicrophone() {
        isRecording = false;
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
        try {
            while ((microphone.read(buffer, 0, buffer.length)) > 0) {
                byteArrayOutputStream.write(buffer, 0, buffer.length);
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Starts the audio playback system.
     * @throws LineUnavailableException if the speaker line is unavailable
     */
    public void startSpeakers() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        speakers = (SourceDataLine) AudioSystem.getLine(info);
        speakers.open(audioFormat);
        speakers.start();
    }

    /**
     * Stops the audio playback system.
     */
    public void stopSpeakers() {
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
        if (speakers == null || !speakers.isOpen()) {
            try {
                startSpeakers();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            speakers.write(audioData, 0, audioData.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays back the given audio data and stops the speaker system afterward.
     * @param audioData the audio data to play
     */
    public void playAndStopAudio(byte[] audioData) {
        playAudio(audioData);
        stopSpeakers();
    }

    /**
     * Retrieves the audio data that has been recorded so far.
     * @return a byte array containing the recorded audio data
     */
    public byte[] getCapturedAudio() {
        return byteArrayOutputStream.toByteArray();
    }

    public void clearAudioBuffer() {
        byteArrayOutputStream.reset();
    }

    /**
     * Compresses raw audio data using GZIP and encodes it in Base64.
     * @param rawAudio the raw audio data to compress
     * @return a Base64-encoded string containing the compressed audio data
     */
    public String compressAudio(byte[] rawAudio) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {

            gzipOutputStream.write(rawAudio);
            gzipOutputStream.finish();
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return Base64.getEncoder().encodeToString(rawAudio);
        }
    }

    /**
     * Decompresses Base64-encoded audio data using GZIP.
     * @param compressedAudioString the Base64-encoded string containing the compressed audio data
     * @return a byte array containing the decompressed audio data
     */
    public byte[] decompressAudio(String compressedAudioString) {
        this.decoded = Base64.getDecoder().decode(compressedAudioString);
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.decoded);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return decoded;
        }
    }

    public void clearBuffer() {
        if (this.decoded != null) {
            this.decoded = null;
            System.gc();
        }
    }
}