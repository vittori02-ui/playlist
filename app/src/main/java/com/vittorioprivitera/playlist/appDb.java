package com.vittorioprivitera.playlist;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {playlist.class},version = 1)
public abstract class appDb extends RoomDatabase{
    public abstract playlistDao playlistDao();
}
