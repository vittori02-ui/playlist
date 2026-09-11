//17:09
// 1 ora e 20
package com.vittorioprivitera.playlist;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.ContentUris;
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
    private Button pausa;
    private final String permission= Build.VERSION.SDK_INT>=33?Manifest.permission.READ_MEDIA_AUDIO:Manifest.permission.READ_EXTERNAL_STORAGE;
    private final ActivityResultLauncher<String> resultLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(),granted->{
        if(granted)scanAndDisplaySong();
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
        if(listaCanz.isEmpty())Toast.makeText(this,"nessun mp3 trovato",Toast.LENGTH_SHORT).show();
        recyclerView.setAdapter(new canzAdapter(listaCanz,this::suona));
    }
    private void suona(canzone canz)
    {
        player.setMediaItem(MediaItem.fromUri(canz.getUri()));
        player.prepare();
        player.play();
        textSuona.setText(canz.getTitolo()+" - "+canz.getAutore());
        pausa.setText("Pausa");
        seekBar.setMax((int)canz.getDura());
        tempoTotale.setText(formatta(canz.getDura()));
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
        Uri col= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection={MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION};
        String sele= MediaStore.Audio.Media.IS_MUSIC+"!=0";
        try(Cursor c=getContentResolver().query(col,projection,sele,null,null))
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
        player=new ExoPlayer.Builder(this).build();
        pausa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(player.isPlaying())
                {
                    player.pause();
                    pausa.setText("Play");
                }
                else
                {
                    player.play();
                    pausa.setText("Pausa");
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
        if(ContextCompat.checkSelfPermission(this,permission)== PackageManager.PERMISSION_GRANTED)scanAndDisplaySong();
        else resultLauncher.launch(permission);
    }
}