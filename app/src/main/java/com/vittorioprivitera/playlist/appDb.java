package com.vittorioprivitera.playlist;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {playlist.class,playlistCanz.class},version = 2)
public abstract class appDb extends RoomDatabase{
    public abstract playlistDao playlistDao();
}
