package snapcj;
import cjdom.HTMLAudioElement;
import cjdom.HTMLDocument;
import snap.gfx.SoundClip;
import snap.web.WebURL;

/**
 * A SnapKit SoundClip implementation for CheerpJ.
 */
public class CJSoundClip extends SoundClip {

    // The Audio Element
    private HTMLAudioElement _snd, _snd2;

    /**
     * Creates a new TVSoundClip.
     */
    public CJSoundClip(Object aSource)
    {
        WebURL url = WebURL.getURL(aSource);
        loadSound(url);
        _snd = (HTMLAudioElement) HTMLDocument.getDocument().createElement("audio");
        _snd.setAttribute("src", url.getPath().substring(1));
        _snd.setAttribute("preload", "auto");
        _snd.load();
    }

    /**
     * Loads image synchronously with wait/notify.
     */
    private synchronized void loadSound(WebURL aURL)
    {
        _snd = (HTMLAudioElement) HTMLDocument.getDocument().createElement("audio");
        _snd.setAttribute("src", aURL.getPath().substring(1));
        _snd.setAttribute("preload", "auto");
        _snd.load();
    }

    /**
     * Returns whether sound is playing.
     */
    public boolean isPlaying()
    {
        return _snd2 != null && !_snd2.isEnded();
    }

    /**
     * Plays the sound.
     */
    public void play()
    {
        _snd2 = (HTMLAudioElement) _snd.cloneNode(false);
        _snd2.play();
    }

    /**
     * Plays the sound repeatedly for given count.
     */
    public void play(int aCount)  { }

    /**
     * Tells sound to stop playing.
     */
    public void stop()  { }

    /**
     * Pauses a sound.
     */
    public void pause()  { }

    /**
     * Starts a recording.
     */
    public void recordStart()  { }

    /**
     * Stops a recording.
     */
    public void recordStop()  { }

    /**
     * Returns whether sound is recording.
     */
    public boolean isRecording()  { return false; }

    /**
     * Returns the sound length in milliseconds.
     */
    public int getLength()  { return 0; }

    /**
     * Returns the sound time in milliseconds.
     */
    public int getTime()  { return 0; }

    /**
     * Sets the sound time in milliseconds.
     */
    public void setTime(int aTime)  { }

    /**
     * Saves this sound.
     */
    public void save()  { }
}