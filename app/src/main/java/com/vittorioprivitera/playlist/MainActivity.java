//15:41
//1;28
package com.vittorioprivitera.playlist;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import android.content.ComponentName;
import androidx.media3.session.SessionToken;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Size;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.media3.session.MediaController;
import com.bumptech.glide.Glide;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import android.Manifest;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MediaController player;
    private ListenableFuture<MediaController>controller;
    private final List<canzone> listaCanz=new ArrayList<>();
    private SeekBar seekBar;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private TextView textSuona,tempoAttuale,tempoTotale;
    private ImageButton avanti,dietro,pausa;
    private int indice=-1;
    private canzAdapter adap;
    private ImageView cope;
    private EditText cerca;
    private View pannelloBar;
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
    private final ActivityResultLauncher<String>notifica=registerForActivityResult(new ActivityResultContracts.RequestPermission(),granted -> {
    });

    private void scanAndDisplaySong()
    {
        listaCanz.clear();
        listaCanz.addAll(caricaCanzoni());
        if(listaCanz.isEmpty())Toast.makeText(this,"nessun mp3 trovato nella cartella fatta dakl' app",Toast.LENGTH_SHORT).show();
        adap=new canzAdapter(listaCanz,this::suona);
        recyclerView.setAdapter(adap);
        List<MediaItem> item=new ArrayList<>();
        for(canzone c:listaCanz)
        {
            MediaMetadata metadata=new MediaMetadata.Builder()
                    .setTitle(c.getTitolo())
                    .setArtist(c.getAutore())
                    .build();

            MediaItem itemMedia=new MediaItem.Builder()
                    .setUri(c.getUri())
                    .setMediaMetadata(metadata)
                    .build();

            item.add(itemMedia);
        }
        player.setMediaItems(item);
        player.prepare();
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
        player.seekTo(indice,0);
        player.play();
        //suonaCorente();
        aggiornaUI();
    }
    private void aggiornaUI()
    {
        if(indice<0||indice>=listaCanz.size())return;
        canzone canz=listaCanz.get(indice);
        textSuona.setText(canz.getTitolo()+" - "+canz.getAutore());
        pausa.setImageResource(R.drawable.apri);
        seekBar.setMax((int)canz.getDura());
        tempoTotale.setText(formatta(canz.getDura()));
        adap.setPosSelezionata(indice);
        Bitmap cover=caricaCopertina(canz.getUri());
        if(cover!=null)Glide.with(this).load(cover).into(cope);
        else Glide.with(this).load(R.drawable.ic_music_placeholder).into(cope);
    }
    private Bitmap caricaCopertina(Uri canz)
    {
        try
        {
            if(Build.VERSION.SDK_INT>=29)return getContentResolver().loadThumbnail(canz,new Size(600,600),null);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    //android:windowSoftInputMode="adjustPan">
    //nel manifest cosi il layout non si alza
    /*
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
    }*/
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        MediaController.releaseFuture(controller);
    }
    private List<canzone> caricaCanzoni()
    {
        List<canzone>canzoni=new ArrayList<>();
        Uri col= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;     //funziona per android 10 in su NB readme
        String[] projection={MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION,MediaStore.Audio.Media.ALBUM_ID};
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
                int cop=c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                while(c.moveToNext())
                {
                    long id=c.getLong(idcol);
                    Uri uri= ContentUris.withAppendedId(col,id);
                    long albumid=c.getLong(cop);
                    Uri copertina=ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),albumid);
                    canzoni.add(new canzone(id,c.getString(titolo),c.getString(artista),c.getLong(durCol),uri,copertina));
                    System.out.println("copertina URI "+copertina.toString());
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
    private void collagaListe()
    {
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(MediaItem mediaItem,int reason)
            {
                indice=player.getCurrentMediaItemIndex();
                aggiornaUI();
            }
            @Override
            public void onIsPlayingChanged(boolean isPlaying)
            {
                if(isPlaying)pausa.setImageResource(R.drawable.chiudi);
                else pausa.setImageResource(R.drawable.apri);
            }
            @Override
            public void onPlayerError(PlaybackException e)
            {
                Toast.makeText(MainActivity.this,"impossibile ripro il brano",Toast.LENGTH_SHORT).show();
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
        avanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.seekToNext();
            }
        });
        dietro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.seekToPrevious();
            }
        });
        cerca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if(adap!=null)adap.filtra(s.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        /*cerca.setOnFocusChangeListener((v,hasFocus)->{
            pannelloBar.setVisibility(hasFocus?View.GONE:View.VISIBLE);
            if(cerca.getText().toString().isEmpty())pannelloBar.setVisibility(View.VISIBLE);
        });*/
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(Build.VERSION.SDK_INT>=33)
        {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            {
                notifica.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        recyclerView=findViewById(R.id.canzoni);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        textSuona=findViewById(R.id.ora);
        pausa=findViewById(R.id.pausePlay);
        seekBar=findViewById(R.id.bar);
        tempoAttuale=findViewById(R.id.tempoAttuale);
        tempoTotale=findViewById(R.id.tempoTotale);
        dietro=findViewById(R.id.dietro);
        avanti=findViewById(R.id.avanti);
        cope=findViewById(R.id.copertina2);
        cerca=findViewById(R.id.cercaCnz);
        pannelloBar=findViewById(R.id.layoutBar);
        SessionToken session=new SessionToken(this,new ComponentName(this,playBack.class));
        controller=new MediaController.Builder(this,session).buildAsync();
        controller.addListener(()->{
            try {
                player=controller.get();
                collagaListe();
                impostaListeUi();
                if(ContextCompat.checkSelfPermission(this,permission)==PackageManager.PERMISSION_GRANTED)
                {
                    creaCartella();
                    scanAndDisplaySong();
                }
                else resultLauncher.launch(permission);
                handler.post(upSeekBar);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        },ContextCompat.getMainExecutor(this));
    }
}