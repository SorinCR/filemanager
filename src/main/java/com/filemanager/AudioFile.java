package com.filemanager;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioFile extends RegularFile implements Playable {
    private String path;
    private String status;
    private AudioInputStream stream;
    private Clip clip;
    Long currentFrame;

    public AudioFile(String name, String type, double size, String path)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(name, type, size);
        this.path = path;
        this.status = "Stopped";

        // System.out.println(path);

        stream = AudioSystem.getAudioInputStream(new File(path).getAbsoluteFile());

        clip = AudioSystem.getClip();
        currentFrame = 0L;

        clip.open(stream);
        // clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void play() throws AlreadyPlaying {
        if (!status.equals("Playing")) {
            clip.setMicrosecondPosition(currentFrame);
            clip.start();
            status = "Playing";
        } else
            throw new AlreadyPlaying("Audio file is already playing");
    }

    public void pause() throws AlreadyPaused {
        if (status == "Playing") {
            currentFrame = clip.getMicrosecondPosition();
            clip.stop();
            status = "Paused";
        } else
            throw new AlreadyPaused("Audio file is already paused");
    }

    public void resume()
            throws UnsupportedAudioFileException, IOException, LineUnavailableException, NotPaused, AlreadyPlaying {
        if (status == "Paused") {
            stream = AudioSystem.getAudioInputStream(new File(path).getAbsoluteFile());

            clip = AudioSystem.getClip();

            clip.open(stream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);

            clip.setMicrosecondPosition(currentFrame);
            this.play();
            status = "Playing";
        } else
            throw new NotPaused("Audio file is not paused");
    }

    public void stop() throws NotPlaying {
        if (status != "Stopped") {
            currentFrame = 0L;
            clip.stop();
            status = "Stopped";
        } else
            throw new NotPlaying("Audio file is not playing");
    }

    public String getStatus() {
        return status;
    }

}
