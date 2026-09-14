package com.vittorioprivitera.playlist;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;

public class playlistActivity extends AppCompatActivity {
    public static final String playlistID="playlist_id";
    public static final String playlistNome="playlist_nome";
    private RecyclerView lista;
    private canzAdapter adap;
    private MediaController player;
    private ListenableFuture<MediaController> controller;
    private final List<canzone>canzoni=new ArrayList<>();
    private int idPlay;

    private void caricaCanz()
    {
        dbManager.ex.execute(()->{
            appDb db=dbManager.getDatabase(this);
            List<Long>idDellaPlay=db.playlistDao().getCanzoniIds(idPlay);
            List<canzone>tutteCanz=canzRepo.caricaTutte(this);
            List<canzone>filtrate=new ArrayList<>();
            for(canzone c:tutteCanz)
            {
                if(idDellaPlay.contains(c.getId()))filtrate.add(c);
            }
            runOnUiThread(()->{
                canzoni.clear();
                canzoni.addAll(filtrate);
                if(canzoni.isEmpty()) Toast.makeText(this,"nessuna canzone in questa playlist",Toast.LENGTH_SHORT).show();
                adap=new canzAdapter(canzoni,this::suona,this::rimuoviCanz);
                lista.setAdapter(adap);
                List<MediaItem>items=new ArrayList<>();
                for(canzone c:canzoni)
                {
                    MediaMetadata metadata=new MediaMetadata.Builder()
                            .setTitle(c.getTitolo())
                            .setArtist(c.getAutore())
                            .build();
                    items.add(new MediaItem.Builder()
                            .setUri(c.getUri())
                            .setMediaMetadata(metadata)
                            .build());
                }
                player.setMediaItems(items);
                player.prepare();
                playerState.contestoAttuale=idPlay;
            });
        });
    }
    private void rimuoviCanz(canzone canz)
    {
        new AlertDialog.Builder(this)
                .setTitle("Rimuovere dalla playlist? ")
                .setPositiveButton("Si",(d,w)->{
                    dbManager.ex.execute(()->{
                        appDb db=dbManager.getDatabase(this);
                        db.playlistDao().rimuoviCanz(new playlistCanz(idPlay,canz.getId()));
                        runOnUiThread(this::caricaCanz);
                    });
                })
                .setNegativeButton("No",null)
                .show();
    }
    private void suona(canzone canz)
    {
        int indice=canzoni.indexOf(canz);
        player.seekTo(indice,0);
        player.play();
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        MediaController.releaseFuture(controller);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        idPlay=getIntent().getIntExtra(playlistID,-1);
        String nomePlay=getIntent().getStringExtra(playlistNome);
        TextView titolo=findViewById(R.id.nomePlaylist2);
        titolo.setText(nomePlay);
        lista=findViewById(R.id.elenco);
        lista.setLayoutManager(new LinearLayoutManager(this));
        SessionToken sessionToken=new SessionToken(this,new ComponentName(this,playBack.class));
        controller=new MediaController.Builder(this,sessionToken).buildAsync();
        controller.addListener(()-> {
            try
            {
                player=controller.get();
                caricaCanz();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
}