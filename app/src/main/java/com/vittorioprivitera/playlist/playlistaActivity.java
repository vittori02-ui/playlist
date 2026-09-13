package com.vittorioprivitera.playlist;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
public class playlistaActivity extends AppCompatActivity {
    private RecyclerView lista;
    private playlistAdapter adap;
    private final List<playlist> listaPlaylist=new ArrayList<>();
    private Button nuovaPlay;
    private void mostraNuova() {
        EditText input = new EditText(this);
        input.setHint("Nome playlist");
        new AlertDialog.Builder(this)
                .setTitle("Nuova playlist")
                .setView(input)
                .setPositiveButton("Crea", (dialog, which) -> {
                    String nome=input.getText().toString().trim();
                    if(TextUtils.isEmpty(nome))
                    {
                        dbManager.ex.execute(()->{
                            appDb db=dbManager.getDatabase(this);
                            db.playlistDao().inserisci(new playlist(nome));
                            runOnUiThread(this::caricaPlaylist);
                        });
                    }
                })
                .setNegativeButton("Annulla",null)
                .show();
    }
    private void caricaPlaylist()
    {
        dbManager.ex.execute(()->{
            appDb db=dbManager.getDatabase(this);
            List<playlist>ris=db.playlistDao().getTutte();
            runOnUiThread(()->
            {
                listaPlaylist.clear();
                listaPlaylist.addAll(ris);
                if(adap==null)
                {
                    adap=new playlistAdapter(listaPlaylist,this::apriPlaylist);
                    lista.setAdapter(adap);
                }
                else adap.notifyDataSetChanged();
            });
        });
    }
    private void apriPlaylist(playlist p)
    {
        Toast.makeText(this,"hai aperto "+p.nome+" id "+p.id,Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlista);
        lista=findViewById(R.id.listaPlaylist);
        lista.setLayoutManager(new LinearLayoutManager(this));
        nuovaPlay=findViewById(R.id.nuovaPlaylist);
        nuovaPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}