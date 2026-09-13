package com.vittorioprivitera.playlist;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import java.util.ArrayList;
import java.util.List;
public class canzRepo {
    public static List<canzone> caricaTutte(Context cont)
    {
        List<canzone>canzoni=new ArrayList<>();
        Uri col= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[]progetto={
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID
        };
        String sele=MediaStore.Audio.Media.IS_MUSIC+"!=0 AND "+ MediaStore.Audio.Media.RELATIVE_PATH+" LIKE ?";
        String[] arg={"Music/MiaPlaylist%"};
        try(Cursor c=cont.getContentResolver().query(col,progetto,sele,arg,null))
        {
            if(c!=null)
            {
                int idcol=c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int tit=c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int arti=c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int durCol=c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int cop=c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                while(c.moveToNext())
                {
                    long id=c.getLong(idcol);
                    Uri uri= ContentUris.withAppendedId(col,id);
                    long almbId=c.getLong(cop);
                    Uri copertina=ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),almbId);
                }
            }
        }
        return canzoni;
    }
}
