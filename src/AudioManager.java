import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class AudioManager implements Runnable {
    private Thread musicThread;
    private volatile boolean running;
    private volatile boolean playRequested;
    private Clip menuMusicClip;
    private static final String MENU_MUSIC_PATH = "assets/bgmusic.wav";

    public synchronized void playMenuMusic() {
        playRequested = true;

        if (musicThread == null || !musicThread.isAlive()) {
            running = true;
            musicThread = new Thread(this, "MenuMusicThread");
            musicThread.setDaemon(true);
            musicThread.start();
        } else {
            musicThread.interrupt();
        }
    }

    public synchronized void stopMusic() {
        if (musicThread != null) {
            running = false;
            musicThread.interrupt();
            musicThread = null;
        }
        cleanupClip();
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

    @Override
    public void run() {
        try {
            while (running) {
                if (playRequested) {
                    loadClipIfNeeded();

                    if (menuMusicClip != null) {
                        if (!menuMusicClip.isRunning()) {
                            menuMusicClip.setFramePosition(0);
                            menuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                            menuMusicClip.start();
                        }
                    }
                    playRequested = false;
                }

                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {
                    //
                }
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Failed to play menu music: " + e.getMessage());
        } finally {
            cleanupClip();
        }
    }

    private void cleanupClip() {
        if (menuMusicClip != null) {
            menuMusicClip.stop();
            menuMusicClip.close();
            menuMusicClip = null;
        }
    }
}
