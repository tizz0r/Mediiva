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

import de.timschubert.mediiva.R;
import de.timschubert.mediiva.ViewerMain;
import de.timschubert.mediiva.data.imageset.Character;

public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.ViewHolder>
{

    @NonNull private final List<Character> characters;
    @NonNull private final Map<Character, Bitmap> thumbnails;

    public CharacterAdapter(@NonNull List<Character> characters,
                            @NonNull Context context)
    {
        this.characters = characters;
        thumbnails = new HashMap<>();

        ViewerMain viewerMain = ViewerMain.getInstance(context);
        for(Character character : characters)
        {
            if(character.getThumbUri() == null) continue;
            viewerMain.requestCharacterThumbLoad(character);
        }
    }

    public void addThumbnail(Character character, Bitmap thumbnail)
    {
        if(!characters.contains(character)) return;

        thumbnails.put(character, thumbnail);
        notifyItemChanged(characters.indexOf(character));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entry_character, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        holder.setContent(characters.get(position));
    }

    @Override
    public int getItemCount() { return characters.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder
    {

        @NonNull private final ImageView thumbImageView;
        @NonNull private final TextView nameTextView;
        @NonNull private final TextView universeTextView;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);

            thumbImageView = itemView.findViewById(R.id.entry_character_thumb);
            nameTextView = itemView.findViewById(R.id.entry_character_name);
            universeTextView = itemView.findViewById(R.id.entry_character_universe);
        }

        public void setContent(Character character)
        {
            thumbImageView.setClipToOutline(true);
            thumbImageView.setImageBitmap(thumbnails.get(character));
            nameTextView.setText(character.getName());
            universeTextView.setText(character.getUniverse());

            if(character.getUniverse() == null || character.getUniverse().isEmpty())
            {
                universeTextView.setVisibility(View.GONE);
            }
        }
    }
}
