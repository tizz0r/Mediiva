package de.timschubert.mediiva.ui.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.timschubert.mediiva.R;
import de.timschubert.mediiva.data.tag.Tag;

public class TagViewAdapter extends RecyclerView.Adapter<TagViewAdapter.ViewHolder>
{

    @NonNull private final List<Tag> tags;

    public TagViewAdapter(@NonNull List<Tag> tagList, int limit)
    {
        tags = new ArrayList<>();

        for(int i = 0; i < Math.min(tagList.size(), limit); i++)
        {
            tags.add(tagList.get(i)); // TODO possibly add selection method
        }
    }

    public TagViewAdapter(@NonNull List<Tag> tagList,
                          @NonNull String prefixFilter) // TODO bad code, change
    {
        tags = new ArrayList<>();

        for(Tag tag : tagList)
        {
            if(prefixFilter.equals(tag.getTagPrefix())) tags.add(tag);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entry_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        holder.setContent(tags.get(position));
    }

    @Override
    public int getItemCount() { return tags.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        @NonNull private final LinearLayout layout;
        @NonNull private final ImageView iconImageView;
        @NonNull private final TextView nameTextView;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);

            layout = itemView.findViewById(R.id.entry_tag_layout);
            iconImageView = itemView.findViewById(R.id.entry_tag_icon);
            nameTextView = itemView.findViewById(R.id.entry_tag_name);
        }

        private void setContent(Tag tag)
        {
            nameTextView.setText(tag.getDisplayName());

            boolean hasIcon = tag.getIcon() != -1 && tag.getIcon() != 0;
            iconImageView.setVisibility(hasIcon ? View.VISIBLE : View.GONE);
            if(hasIcon) iconImageView.setImageResource(tag.getIcon());

            ColorStateList colorState =
                    ColorStateList.valueOf(ContextCompat.getColor(layout.getContext(), tag.getColor()));
            layout.setBackgroundTintList(colorState);
        }
    }
}
