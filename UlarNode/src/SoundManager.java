import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    private Clip bgmClip;

    public void playBackgroundMusic(String filePath) {
        try {
            File musicFile = new File(filePath);
            if (!musicFile.exists()) return;

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioInput);

            if (bgmClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(-15.0f);
            }

            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playDiceSound(String filePath) {
        playSoundEffect(filePath, -5.0f);
    }

    public void playStepSound(String filePath) {
        playSoundEffect(filePath, 0.0f);
    }

    public void playVictorySound(String filePath) {
        playSoundEffect(filePath, 0.0f);
    }

    public void playScoreSound(String filePath) {
        playSoundEffect(filePath, 0.0f);
    }

    public void playPrimeSound(String filePath) {
        playSoundEffect(filePath, 0.0f);
    }


    public void playBonusSound(String filePath) {
        playSoundEffect(filePath, -5.0f); // Volume sedikit keras
    }

    private void playSoundEffect(String filePath, float volume) {
        try {
            File soundFile = new File(filePath);
            if (!soundFile.exists()) return;

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);

            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(volume);
            }

            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopMusic() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }
}