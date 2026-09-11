package com.vittorioprivitera.playlist;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
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
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_canzone,parent,false);
        return new canzoneViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull canzoneViewHolder h,int position)
    {
        canzone canz=canzoni.get(position);
        h.titolo.setText(canz.getTitolo());
        h.artista.setText(canz.getAutore());
        h.itemView.setOnClickListener(v->listener.onCanzoneClick(canz));
        h.numero.setText(String.valueOf(position+1));
        h.itemView.setBackgroundColor(ContextCompat.getColor(h.itemView.getContext(),position==posizioneSele?R.color.selezionato:R.color.trasparente));
        h.itemView.setOnClickListener(view -> {
            int vecchiaPos=posizioneSele;
            posizioneSele=h.getAdapterPosition();
            notifyItemChanged(vecchiaPos);
            notifyItemChanged(posizioneSele);
            listener.onCanzoneClick(canz);
        });
    }
    @Override public int getItemCount(){
        return canzoni.size();
    }
    static class canzoneViewHolder extends RecyclerView.ViewHolder
    {
        TextView titolo,artista,numero;
        canzoneViewHolder(View v){
            super(v);
            titolo=v.findViewById(R.id.titoloCan);
            artista=v.findViewById(R.id.artiCan);
            numero=v.findViewById(R.id.numero);
        }
    }
}
