package com.vittorioprivitera.playlist;
import androidx.room.Entity;
@Entity(tableName = "playlist_canzone",primaryKeys = {"playlistId","canzoneId"})
public class playlistCanz {
    public int playlistId;
    public long canzoneId;
    public playlistCanz(int playlistId,long canzoneId)
    {
        this.playlistId=playlistId;
        this.canzoneId=canzoneId;
    }
}
