import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class AudioManager {
    private MusicThread musicThread;
    private Clip menuMusicClip;
    private static final String MENU_MUSIC_PATH = "assets/bgmusic.wav";

    public synchronized void playMenuMusic() {
        if (musicThread != null && musicThread.isAlive()) {
            musicThread.requestPlay();
            return;
        }

        musicThread = new MusicThread();
        musicThread.start();
    }

    public synchronized void stopMusic() {
        if (musicThread != null) {
            musicThread.requestStop();
            musicThread = null;
        }
    }

    public void shutdown() {
        stopMusic();
    }

    private void loadClipIfNeeded() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (menuMusicClip == null) {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(MENU_MUSIC_PATH));
            menuMusicClip = AudioSystem.getClip();
            menuMusicClip.open(audioStream);
            audioStream.close();
        }
    }

    private class MusicThread extends Thread {
        private volatile boolean running = true;
        private volatile boolean playRequested = true;

        MusicThread() {
            setName("MenuMusicThread");
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (running) {
                    if (playRequested) {
                        loadClipIfNeeded();

                        if (menuMusicClip != null) {
                            if (menuMusicClip.isRunning()) {
                                menuMusicClip.stop();
                            }
                            menuMusicClip.setFramePosition(0);
                            menuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                            menuMusicClip.start();
                        }
                        playRequested = false;
                    }

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ignored) {
                        // allow loop to check flags
                    }
                }
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.err.println("Failed to play menu music: " + e.getMessage());
            } finally {
                cleanupClip();
            }
        }

        void requestPlay() {
            playRequested = true;
            interrupt();
        }

        void requestStop() {
            running = false;
            interrupt();
        }

        private void cleanupClip() {
            if (menuMusicClip != null) {
                menuMusicClip.stop();
                menuMusicClip.close();
                menuMusicClip = null;
            }
        }
    }
}
