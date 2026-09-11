package com.vittorioprivitera.playlist;
import android.net.Uri;
public class canzone  {
    private final long id;
    private final String titolo;
    private final String autore;
    private final long dura;
    private final Uri uri;

    public canzone(long id,String titolo,String  autore,long dura,Uri uri)
    {
        this.id=id;
        this.titolo=titolo;
        this.autore=autore;
        this.dura=dura;
        this.uri=uri;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getAutore() {
        return autore;
    }
    public Uri getUri() {
        return uri;
    }

    public long getDura() {
        return dura;
    }
}