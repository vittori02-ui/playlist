package com.vittorioprivitera.playlist;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
public class playlistAdapter extends RecyclerView.Adapter<playlistAdapter.playlistViewHolder> {
    public interface OnPlaylistClickListener
    {
        void onPlaylistClick(playlist p);
    }
    private final List<playlist> playlistList;
    private final OnPlaylistClickListener listener;

    public playlistAdapter(List<playlist> playlistList,OnPlaylistClickListener listener)
    {
        this.playlistList=playlistList;
        this.listener=listener;
    }
    @NonNull
    @Override
    public playlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType)
    {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist,parent,false);
        return new playlistViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull playlistViewHolder h,int position)
    {
        playlist p=playlistList.get(position);
        h.nome.setText(p.nome);
        h.itemView.setOnClickListener(v->listener.onPlaylistClick(p));
    }
    @Override
    public int getItemCount()
    {
        return playlistList.size();
    }
    static class playlistViewHolder extends RecyclerView.ViewHolder
    {
        TextView nome;
        playlistViewHolder(View v)
        {
            super(v);
            nome=v.findViewById(R.id.nomePlaylist);
        }
    }
}
