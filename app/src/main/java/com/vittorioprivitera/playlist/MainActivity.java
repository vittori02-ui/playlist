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
import android.provider.MediaStore;
import android.widget.Toast;
import java.util.ArrayList;
import android.Manifest;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExoPlayer player;
    private final List<canzone> listaCanz=new ArrayList<>();
    private final String permission= Build.VERSION.SDK_INT>=33?Manifest.permission.READ_MEDIA_AUDIO:Manifest.permission.READ_EXTERNAL_STORAGE;
    private final ActivityResultLauncher<String> resultLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(),granted->{
        if(granted)scanAndDisplaySong();
        else Toast.makeText(this,"permesso negato",Toast.LENGTH_LONG).show();
    });
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView=findViewById(R.id.canzoni);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        player=new ExoPlayer.Builder(this).build();
        if(ContextCompat.checkSelfPermission(this,permission)== PackageManager.PERMISSION_GRANTED)scanAndDisplaySong();
        else resultLauncher.launch(permission);
    }
}