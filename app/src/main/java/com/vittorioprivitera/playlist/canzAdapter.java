package com.vittorioprivitera.playlist;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import android.util.Size;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
public class canzAdapter extends RecyclerView.Adapter<canzAdapter.canzoneViewHolder> {
    public interface OnCanzoneClickListener{
        void onCanzoneClick(canzone can);
    }
    public interface OnCanzoneLongClickListener
    {
        void onLongClick(canzone canz);
    }
    private final List<canzone> canzoni;
    private List<canzone> canzoniMostrate;  //filtro ricerca
    private final OnCanzoneClickListener listener;
    private long posizioneSele=-1;
    private OnCanzoneLongClickListener listener2;
    public canzAdapter(List<canzone> canzoni,OnCanzoneClickListener listener,OnCanzoneLongClickListener listener2)
    {
        this.canzoni=canzoni;
        this.listener=listener;
        this.canzoniMostrate=new ArrayList<>(canzoni);
        this.listener2=listener2;
    }

    public void filtra(String testo)
    {
        canzoniMostrate.clear();
        if(TextUtils.isEmpty(testo))canzoniMostrate.addAll(canzoni);
        else
        {
            String cerca=testo.toLowerCase();
            for(canzone c:canzoni)
            {
                if(c.getTitolo().toLowerCase().contains(cerca)||c.getAutore().toLowerCase().contains(cerca))canzoniMostrate.add(c);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public canzoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType)
    {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.canzone,parent,false);
        return new canzoneViewHolder(v);
    }
    public void setCanzoneSelezionata(long pos)
    {
        posizioneSele=pos;
        notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(@NonNull canzoneViewHolder h,int position)
    {
        canzone canz=canzoniMostrate.get(position);
        h.titolo.setText(canz.getTitolo());
        h.artista.setText(canz.getAutore());
        h.numero.setText(String.valueOf(position+1));
        h.itemView.setBackgroundColor(ContextCompat.getColor(h.itemView.getContext(),canz.getId()==posizioneSele?R.color.selezionato:R.color.trasparente));
        /*Glide.with(h.itemView.getContext())  //vechio sistema per versioni piu vecchie di android
                        .load(canz.getCopertina())
                                .placeholder(R.drawable.ic_music_placeholder)
                                        .error(R.drawable.ic_music_placeholder)
                                                .into(h.copertina);*/
        Bitmap cover=caricaCopertina(canz.getUri(),h.itemView.getContext());
        if(cover!=null)Glide.with(h.itemView.getContext()).load(cover).into(h.copertina);
        else Glide.with(h.itemView.getContext()).load(R.drawable.ic_music_placeholder).into(h.copertina);
        h.itemView.setOnClickListener(view -> {
            posizioneSele=canz.getId();
            notifyDataSetChanged();
            listener.onCanzoneClick(canz);
        });
        h.itemView.setOnLongClickListener(v->{
            if(listener2!=null) listener2.onLongClick(canz);
            else Toast.makeText(h.itemView.getContext(),"Long non disponibile",Toast.LENGTH_SHORT).show();
            return true;
        });
    }
    private Bitmap caricaCopertina(Uri canzone,Context c)
    {
        try
        {
            if(Build.VERSION.SDK_INT>=29)return c.getContentResolver().loadThumbnail(canzone, new Size(300,300), null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    @Override public int getItemCount(){
        return canzoniMostrate.size();
    }
    static class canzoneViewHolder extends RecyclerView.ViewHolder
    {
        TextView titolo,artista,numero;
        ImageView copertina;
        canzoneViewHolder(View v){
            super(v);
            titolo=v.findViewById(R.id.titoloCan);
            artista=v.findViewById(R.id.artiCan);
            numero=v.findViewById(R.id.numero);
            copertina=v.findViewById(R.id.copertina);
        }
    }
}
