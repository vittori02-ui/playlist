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
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
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
    private SeekBar seekBar;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private TextView textSuona,tempoAttuale,tempoTotale;
    private ImageButton avanti,dietro,pausa;
    private ImageView cope;
    private int indice=-1;

    private final Runnable upSpeek=new Runnable() {
        @Override
        public void run() {
            if(player!=null&&player.isPlaying())
            {

            }
            handler.postDelayed(this,100);
        }
    };
    private void caricaCanz()
    {
        dbManager.ex.execute(()->{
            try {
                appDb db=dbManager.getDatabase(this);
                List<Long>idDellaPlay=db.playlistDao().getCanzoniIds(idPlay);
                //Toast.makeText(this,"DEBUG 1 - ID della playlist: " + idDellaPlay,Toast.LENGTH_LONG).show();  //dovevano essere dei log o sout  aaaaaaa:(((
                List<canzone>tutteCanz=caricaTutte();
                //Toast.makeText(this,"DEBUG 2 - Tutte le canzoni: " + tutteCanz.size(),Toast.LENGTH_LONG).show();
                List<canzone>filtrate=new ArrayList<>();
                for(canzone c:tutteCanz)
                {
                    if(idDellaPlay.contains(c.getId()))filtrate.add(c);
                }
                runOnUiThread(()->{
                    try {
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
                        if(player != null && !items.isEmpty())
                        {
                            player.setMediaItems(items);
                            player.prepare();
                        }
                        playerState.contestoAttuale=idPlay;
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("ERRORE in runOnUiThread: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("ERRORE in caricaCanz: " + e.getMessage());
            }
        });
    }
    private List<canzone> caricaTutte()
    {
        List<canzone> canzoni=new ArrayList<>();
        Uri col= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[]proget={MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.DURATION,MediaStore.Audio.Media.ALBUM_ID};
        String sele=MediaStore.Audio.Media.IS_MUSIC+"!=0";
        try(Cursor c=getContentResolver().query(col,proget,sele,null,null))
        {
            if(c!=null)
            {
                int idcol=c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titolo=c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int arti=c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int durCol=c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int cop=c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                while(c.moveToNext())
                {
                    long id=c.getLong(idcol);
                    Uri uri= ContentUris.withAppendedId(col,id);
                    long album=c.getLong(cop);
                    Uri copertina=ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),album);
                    canzoni.add(new canzone(id,c.getString(titolo),c.getString(arti),c.getLong(durCol),uri,copertina));
                }
            }
        }
        return canzoni;
    }
    private void rimuoviCanz(canzone canz)
    {
        runOnUiThread(()->{
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
        });
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