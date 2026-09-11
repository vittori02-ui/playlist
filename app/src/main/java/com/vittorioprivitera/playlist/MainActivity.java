//17:09
// 1 ora e 20
package com.vittorioprivitera.playlist;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import android.Manifest;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExoPlayer player;
    private final List<canzone> listaCanz=new ArrayList<>();
    private SeekBar seekBar;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private TextView textSuona,tempoAttuale,tempoTotale;
    private ImageButton avanti,dietro,pausa;
    private int indice=-1;
    private canzAdapter adap;
    private final String permission= Build.VERSION.SDK_INT>=33?Manifest.permission.READ_MEDIA_AUDIO:Manifest.permission.READ_EXTERNAL_STORAGE;
    private final ActivityResultLauncher<String> resultLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(),granted->{
        if(granted)
        {
            creaCartella();
            scanAndDisplaySong();
        }
        else Toast.makeText(this,"permesso negato",Toast.LENGTH_LONG).show();
    });

    private final Runnable upSeekBar=new Runnable() {
        @Override
        public void run() {
            if(player.isPlaying())
            {
                long pos=player.getCurrentPosition();
                seekBar.setProgress((int)pos);
                tempoAttuale.setText(formatta(pos));
            }
            handler.postDelayed(this,100);
        }
    };
    private void scanAndDisplaySong()
    {
        listaCanz.clear();
        listaCanz.addAll(caricaCanzoni());
        if(listaCanz.isEmpty())Toast.makeText(this,"nessun mp3 trovato nella cartella fatta dakl' app",Toast.LENGTH_SHORT).show();
        adap=new canzAdapter(listaCanz,this::suona);
        recyclerView.setAdapter(adap);
    }
    private void creaCartella()
    {
        Uri col=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] prot={MediaStore.Audio.Media._ID};
        String sele=MediaStore.Audio.Media.RELATIVE_PATH+" LIKE ?";
        String[] seleArgs={"Music/MiaPlaylist%"};
        try(Cursor c=getContentResolver().query(col,prot,sele,seleArgs,null))
        {
            if(c!=null&&c.getCount()>0)return;
        }
        ContentValues val=new ContentValues();
        val.put(MediaStore.Audio.Media.DISPLAY_NAME,".nomedia_placeholder");
        val.put(MediaStore.Audio.Media.RELATIVE_PATH,"Music/MiaPlaylist");
        val.put(MediaStore.Audio.Media.MIME_TYPE,"audio/mpeg");
        val.put(MediaStore.Audio.Media.IS_PENDING,1);
        Uri uri=getContentResolver().insert(col,val);
        if(uri!=null)
        {
            val.clear();
            val.put(MediaStore.Audio.Media.IS_PENDING,0);
            getContentResolver().update(uri,val,null,null);
        }
    }
    private void suona(canzone canz)
    {
        indice=listaCanz.indexOf(canz);
        suonaCorente();
    }
    private void suonaCorente()
    {
        if(indice<0||indice>=listaCanz.size())return;
        canzone canz2=listaCanz.get(indice);
        player.setMediaItem(MediaItem.fromUri(canz2.getUri()));
        player.prepare();
        player.play();
        textSuona.setText(canz2.getTitolo()+" - "+canz2.getAutore());
        pausa.setImageResource(R.drawable.chiudi);
        seekBar.setMax((int)canz2.getDura());
        tempoTotale.setText(formatta(canz2.getDura()));
        adap.setPosSelezionata(indice);
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        player.release();
    }
    private List<canzone> caricaCanzoni()
    {
        List<canzone>canzoni=new ArrayList<>();
        Uri col= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;     //funziona per android 10 in su NB readme
        String[] projection={MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION};
        String sele= MediaStore.Audio.Media.IS_MUSIC+"!=0 AND "+MediaStore.Audio.Media.RELATIVE_PATH+" LIKE ?";
        String[] seleArgs={"Music/MiaPlaylist%"};
        try(Cursor c=getContentResolver().query(col,projection,sele,seleArgs,null))
        {
            if(c!=null)
            {
                int idcol=c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titolo=c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artista=c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int durCol=c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                while(c.moveToNext())
                {
                    long id=c.getLong(idcol);
                    Uri uri= ContentUris.withAppendedId(col,id);
                    canzoni.add(new canzone(id,c.getString(titolo),c.getString(artista),c.getLong(durCol),uri));
                }
            }
        }
        return canzoni;
    }
    private String formatta(long tempo)
    {
        long minuti=(tempo/1000)/60;
        long secondi=(tempo/1000)%60;
        return String.format("%d:%02d",minuti,secondi);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView=findViewById(R.id.canzoni);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        textSuona=findViewById(R.id.ora);
        pausa=findViewById(R.id.pausePlay);
        seekBar=findViewById(R.id.bar);
        tempoAttuale=findViewById(R.id.tempoAttuale);
        tempoTotale=findViewById(R.id.tempoTotale);
        dietro=findViewById(R.id.dietro);
        avanti=findViewById(R.id.avanti);
        player=new ExoPlayer.Builder(this).build();
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state)
            {
                if (state == player.STATE_ENDED)
                {
                    if(indice<listaCanz.size()-1)
                    {
                        indice++;
                        suonaCorente();
                    }
                }
            }
        });
        pausa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(player.isPlaying())
                {
                    player.pause();
                    pausa.setImageResource(R.drawable.apri);
                }
                else
                {
                    player.play();
                    pausa.setImageResource(R.drawable.chiudi);
                }
            }
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
        handler.post(upSeekBar);
        if(ContextCompat.checkSelfPermission(this,permission)== PackageManager.PERMISSION_GRANTED)
        {
            creaCartella();
            scanAndDisplaySong();
        }
        else resultLauncher.launch(permission);
        avanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(indice<listaCanz.size()-1)
                {
                    indice++;
                    suonaCorente();
                }
            }
        });
        dietro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(indice>0)
                {
                    indice--;
                    suonaCorente();
                }
            }
        });
    }
}