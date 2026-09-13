package com.vittorioprivitera.playlist;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import android.util.Size;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
public class canzAdapter extends RecyclerView.Adapter<canzAdapter.canzoneViewHolder> {
    public interface OnCanzoneClickListener{
        void onCanzoneClick(canzone can);
    }
    private final List<canzone> canzoni;
    private final OnCanzoneClickListener listener;
    private int posizioneSele=-1;
    public canzAdapter(List<canzone> canzoni,OnCanzoneClickListener listener)
    {
        this.canzoni=canzoni;
        this.listener=listener;
    }
    @Override
    @NonNull
    public canzoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType)
    {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.canzone,parent,false);
        return new canzoneViewHolder(v);
    }
    public void setPosSelezionata(int pos)
    {
        int vecchiaPos=posizioneSele;
        posizioneSele=pos;
        notifyItemChanged(vecchiaPos);
        notifyItemChanged(posizioneSele);
    }
    @Override
    public void onBindViewHolder(@NonNull canzoneViewHolder h,int position)
    {
        canzone canz=canzoni.get(position);
        h.titolo.setText(canz.getTitolo());
        h.artista.setText(canz.getAutore());
        h.numero.setText(String.valueOf(position+1));
        h.itemView.setBackgroundColor(ContextCompat.getColor(h.itemView.getContext(),position==posizioneSele?R.color.selezionato:R.color.trasparente));
        /*Glide.with(h.itemView.getContext())  //vechio sistema per versioni piu vecchie di android
                        .load(canz.getCopertina())
                                .placeholder(R.drawable.ic_music_placeholder)
                                        .error(R.drawable.ic_music_placeholder)
                                                .into(h.copertina);*/
        Bitmap cover=caricaCopertina(canz.getUri(),h.itemView.getContext());
        if(cover!=null)Glide.with(h.itemView.getContext()).load(cover).into(h.copertina);
        else Glide.with(h.itemView.getContext()).load(R.drawable.ic_music_placeholder).into(h.copertina);
        h.itemView.setOnClickListener(view -> {
            int vecchiaPos=posizioneSele;
            posizioneSele=h.getAdapterPosition();
            notifyItemChanged(vecchiaPos);
            notifyItemChanged(posizioneSele);
            listener.onCanzoneClick(canz);
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
        return canzoni.size();
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
