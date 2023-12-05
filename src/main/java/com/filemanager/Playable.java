package com.filemanager;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public interface Playable {
    public void play() throws AlreadyPlaying;

    public void pause() throws AlreadyPaused;

    public void resume()
            throws UnsupportedAudioFileException, IOException, LineUnavailableException, NotPaused, AlreadyPlaying;

    public void stop() throws NotPlaying;
}
