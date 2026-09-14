package com.vittorioprivitera.playlist;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
@Dao
public interface playlistDao {
    @Insert
    void inserisci(playlist play);
    @Insert
    long inserisciEritornaId(playlist play);
    @Delete
    void elimina(playlist play);
    @Query("SELECT* FROM playlist ORDER BY nome ASC")
    List<playlist> getTutte();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void aggCanzone(playlistCanz pc);
    @Delete
    void rimuoviCanz(playlistCanz pc);
    @Query("SELECT canzoneId FROM playlist_canzone WHERE playlistId = :playlistId")
    List<Long>getCanzoniIds(int playlistId);
    @Query("SELECT DISTINCT canzoneId FROM playlist_canzone")
    List<Long> getTutteInPlaylist();
}
