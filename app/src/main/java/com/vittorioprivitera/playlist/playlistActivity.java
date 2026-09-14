package com.vittorioprivitera.playlist;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Size;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
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
                long pos=player.getCurrentPosition();
                seekBar.setProgress((int)pos);
                tempoAttuale.setText(formatta(pos));
            }
            handler.postDelayed(this,100);
        }
    };
    private void aggiornaUi()
    {
        if(indice<0||indice>=canzoni.size())return;
        canzone canz=canzoni.get(indice);
        textSuona.setText(canz.getTitolo());
        pausa.setImageResource(R.drawable.chiudi);
        seekBar.setMax((int)canz.getDura());
        tempoTotale.setText(formatta(canz.getDura()));
        adap.setPosSelezionata(indice);
        Bitmap cover=caricaCope(canz.getUri());
        if(cover!=null) Glide.with(this).load(cover).into(cope);
        else Glide.with(this).load(R.drawable.ic_music_placeholder).into(cope);
    }
    private String formatta(long tempo)
    {
        long min=(tempo/1000)/60;
        long sec=(tempo/1000)%60;
        return String.format("%d:%02d",min,sec);
    }
    private Bitmap caricaCope(Uri uri)
    {
        try
        {
            if(Build.VERSION.SDK_INT>=29)return getContentResolver().loadThumbnail(uri,new Size(600,600),null);
        }
        catch (Exception e)
        {
        }
        return null;
    }
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
    private void collegaListe()
    {
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(MediaItem mediaItem,int reason)
            {
                indice=player.getCurrentMediaItemIndex();
                aggiornaUi();
            }
            @Override
            public void onIsPlayingChanged(boolean isPlaying)
            {
                if(isPlaying)pausa.setImageResource(R.drawable.chiudi);
                else pausa.setImageResource(R.drawable.apri);
            }
        });
    }
    private void impostaListeUi()
    {
        pausa.setOnClickListener(v->{
            if(player.isPlaying())player.pause();
            else player.play();
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b)player.seekTo(i);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        avanti.setOnClickListener(v->player.seekToNext());
        dietro.setOnClickListener(v->player.seekToPrevious());
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
    protected void onResume()
    {
        super.onResume();
        handler.post(upSpeek);
    }
    @Override
    protected void onPause()
    {
        super.onPause();
        handler.removeCallbacks(upSpeek);
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
        textSuona=findViewById(R.id.riproOra);
        pausa=findViewById(R.id.pausePlay2);
        seekBar=findViewById(R.id.seekBar2);
        tempoAttuale=findViewById(R.id.tempo1);
        tempoTotale=findViewById(R.id.tempo2);
        dietro=findViewById(R.id.dietro2);
        avanti=findViewById(R.id.avanti2);
        cope=findViewById(R.id.copertina3);
        lista.setLayoutManager(new LinearLayoutManager(this));
        SessionToken sessionToken=new SessionToken(this,new ComponentName(this,playBack.class));
        controller=new MediaController.Builder(this,sessionToken).buildAsync();
        controller.addListener(()-> {
            try
            {
                player=controller.get();
                caricaCanz();
                collegaListe();
                impostaListeUi();
                indice=player.getCurrentMediaItemIndex();
                aggiornaUi();
                handler.post(upSpeek);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
}