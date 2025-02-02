package org.example.voice_chat_app;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioRecorder {
    private TargetDataLine line;

    public void startRecording() {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            File outputFile = new File("voice_message.wav");
            AudioInputStream audioStream = new AudioInputStream(line);

            System.out.println("Recording started...");
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outputFile);
            System.out.println("Recording finished and saved to: " + outputFile.getAbsolutePath());

        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (line != null) {
            line.stop();
            line.close();
            System.out.println("Audio stopped");
        }
    }
}
