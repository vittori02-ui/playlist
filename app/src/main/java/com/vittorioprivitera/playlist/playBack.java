package com.vittorioprivitera.playlist;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.Player;
import androidx.media3.session.MediaSession;
import androidx.annotation.Nullable;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSessionService;
public class playBack extends MediaSessionService {
    private ExoPlayer player;
    private MediaSession session;

    @Override
    public void onCreate() {
        super.onCreate();
        AudioAttributes audio=new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build();
        player=new ExoPlayer.Builder(this)
                .setAudioAttributes(audio,true)
                .setHandleAudioBecomingNoisy(true)
                .build();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        session=new MediaSession.Builder(this,player).build();
    }
    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controller)
    {
        return session;
    }
    @Override
    public void onDestroy()
    {
        session.getPlayer().release();
        session.release();
        session=null;
        super.onDestroy();
    }
}
