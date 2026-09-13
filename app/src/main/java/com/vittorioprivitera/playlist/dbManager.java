package com.vittorioprivitera.playlist;
import android.content.Context;

import androidx.room.Room;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class dbManager {
    private static appDb istanza;
    public static final ExecutorService ex= Executors.newSingleThreadExecutor();
    public static appDb getDatabase(Context cont)
    {
        if(istanza==null)istanza= Room.databaseBuilder(cont.getApplicationContext(),appDb.class,"playlist_database")
                .fallbackToDestructiveMigration() //distrugge i dati quando cambia la versione, in versione definitiva mettere una migrazione sicura
                .build();
        return istanza;
    }
}
