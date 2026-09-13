package com.vittorioprivitera.playlist;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName="playlist")
public class playlist {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String nome;
    public playlist(@NonNull String nome)
    {
        this.nome=nome;
    }
}
