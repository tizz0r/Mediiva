package de.timschubert.mediiva.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.R;
import de.timschubert.mediiva.ViewerMain;
import de.timschubert.mediiva.data.movie.Actor;
import de.timschubert.mediiva.data.movie.ActorInfo;
import de.timschubert.mediiva.data.movie.Movie;

public class ActorAdapter extends RecyclerView.Adapter<ActorAdapter.ViewHolder>
{

    @NonNull private final List<Actor> actors;
    @NonNull private final Map<Long, ActorInfo> actorInfoMap;
    @NonNull private final Map<Actor, Bitmap> thumbnails;
    @NonNull private final Context context;

    public ActorAdapter(@NonNull List<Actor> actors,
                        @NonNull Map<Long, ActorInfo> actorInfoMap,
                        @NonNull Context context)
    {
        this.actors = actors;
        this.actorInfoMap = actorInfoMap;
        this.context = context;
        thumbnails = new HashMap<>();

        ViewerMain viewerMain = ViewerMain.getInstance(context);
        for(Actor actor : actors)
        {
            ActorInfo info = actorInfoMap.get(actor.getId());
            if(info == null) continue;

            viewerMain.requestActorThumbLoad(actor, info);
        }
    }

    public void addThumbnail(Actor actor, ActorInfo actorInfo, Bitmap thumbnail)
    {
        if(!actors.contains(actor)) return;

        thumbnails.put(actor, thumbnail);
        notifyItemChanged(actors.indexOf(actor));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entry_actor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        holder.setContent(actors.get(position));
    }

    @Override
    public int getItemCount() { return actors.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        @NonNull private final ImageView actorThumb;
        @NonNull private final TextView actorName;
        @NonNull private final TextView actorRole;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);

            actorThumb = itemView.findViewById(R.id.entry_actor_thumb);
            actorName = itemView.findViewById(R.id.entry_actor_name);
            actorRole = itemView.findViewById(R.id.entry_actor_role);
        }

        private void setContent(Actor actor)
        {
            actorThumb.setClipToOutline(true);
            actorThumb.setImageBitmap(thumbnails.get(actor));
            actorName.setText(actor.getName());

            ActorInfo info = actorInfoMap.get(actor.getId());
            if(info != null) actorRole.setText(context.getString(R.string.actor_as_placeholder, info.getRole()));
        }
    }
}
