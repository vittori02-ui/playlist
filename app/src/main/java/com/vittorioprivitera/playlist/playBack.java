package com.vittorioprivitera.playlist;
import android.widget.Toast;

import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.Player;
import androidx.media3.common.PlaybackException;
import androidx.media3.session.MediaSession;
import androidx.annotation.Nullable;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSessionService;
public class playBack extends MediaSessionService {
    private ExoPlayer player;
    private MediaSession session;
    private int tent=0;

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
        player.addListener(new Player.Listener(){
            public void onPlayerError(PlaybackException e)
            {
                //serve se il brano corrotto,cancellato o illegibile e si prende quello dopo
                tent++;
                if(tent>=player.getMediaItemCount())
                {
                    Toast.makeText(playBack.this,"nessun audio ripro",Toast.LENGTH_SHORT).show();
                    player.pause();
                    return;
                }
                if(player.hasNextMediaItem())
                {
                    player.seekToNext();
                    player.play();
                }
                else player.pause();
            }
            @Override
            public void onIsPlayingChanged(boolean isPlaying)
            {
                if(isPlaying)tent=0;
            }
        });
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
