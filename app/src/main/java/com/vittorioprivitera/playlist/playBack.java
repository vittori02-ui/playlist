package com.vittorioprivitera.playlist;
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
        player = new ExoPlayer.Builder(this).build();
        session = new MediaSession.Builder(this, player).build();
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
