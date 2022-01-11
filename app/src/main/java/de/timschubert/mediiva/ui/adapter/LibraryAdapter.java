package de.timschubert.mediiva.ui.adapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.R;
import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.library.Library;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder>
{

    @NonNull private final List<Library> libraries;

    public LibraryAdapter(@NonNull List<Library> libraries)
    {
        this.libraries = libraries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entry_library, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        holder.setContent(libraries.get(position));
    }

    @Override
    public int getItemCount() {
        return libraries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        @NonNull private final ImageView iconImageView;
        @NonNull private final TextView titleTextView;
        @NonNull private final TextView typeTextView;
        @NonNull private final ImageButton overflowButton;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);

            iconImageView = itemView.findViewById(R.id.entry_library_icon);
            titleTextView = itemView.findViewById(R.id.entry_library_title);
            typeTextView = itemView.findViewById(R.id.entry_library_type);
            overflowButton = itemView.findViewById(R.id.entry_library_overflow);
        }

        public void setContent(@NonNull Library library)
        {
            titleTextView.setText(library.getName());

            switch (library.getType())
            {
                default:
                case IMAGE_SET:
                    iconImageView.setImageResource(R.drawable.ic_imageset);
                    typeTextView.setText("Image Sets"); // TODO change to string resource
                    break;
                case MOVIE:
                    iconImageView.setImageResource(R.drawable.ic_movie);
                    typeTextView.setText("Movies"); // TODO change to string resource
                    break;
            }

            PopupMenu optionsMenu = new PopupMenu(itemView.getContext(), overflowButton);
            optionsMenu.getMenuInflater().inflate(R.menu.popup_libraries_library, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(item ->
            {
                if(item.getItemId() == R.id.popup_libraries_library_edit)
                {
                    return true;
                }
                if(item.getItemId() == R.id.popup_libraries_library_delete)
                {
                    notifyItemRemoved(libraries.indexOf(library));
                    libraries.remove(library);

                    AppExecutors.getInstance().diskIO().execute(() ->
                    {
                        AppDatabase database = AppDatabase.getInstance(itemView.getContext());
                        database.libraryDao().deleteLibrary(library);

                        AppExecutors.getInstance().mainThread().execute(() ->
                        {
                            Toast.makeText(itemView.getContext(), "Library removed", Toast.LENGTH_SHORT).show(); //TODO
                        });
                    });

                    return true;
                }

                return false;
            });

            overflowButton.setOnClickListener(view -> optionsMenu.show());
        }
    }
}
