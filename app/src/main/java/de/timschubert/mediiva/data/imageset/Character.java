package de.timschubert.mediiva.data.imageset;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import de.timschubert.mediiva.R;

@Entity(tableName = "character")
public class Character
{
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "name")
    private final String name;
    @ColumnInfo(name = "universe")
    private final String universe;
    @ColumnInfo(name = "thumb")
    private final Uri thumbUri;

    /**
     * Creates a new Character instance. This constructor may be used by the room database
     * @param name Name of the character
     * @param universe Name of the universe the character exists in
     * @param thumbUri Uri to a thumbnail image of the character
     */
    public Character(@NonNull String name,
                     @Nullable String universe,
                     @Nullable Uri thumbUri)
    {
        this.name = name;
        this.universe = universe;
        this.thumbUri = thumbUri;
    }

    public void setId(long id) { this.id = id; }

    public long getId() { return id; }
    @NonNull public String getName() { return name; }
    @Nullable public String getUniverse() { return universe; }
    @Nullable public Uri getThumbUri() { return thumbUri; }

    public static class Builder
    {
        @NonNull private String name;
        @NonNull private String universe;
        @Nullable private Uri thumbUri;

        public Builder(Context context)
        {
            name = context.getString(R.string.character_default_name);
            universe = context.getString(R.string.character_default_universe);
        }

        public Builder setName(String name)
        {
            this.name = name;
            return this;
        }

        public Builder setUniverse(String universe)
        {
            this.universe = universe;
            return this;
        }

        public Builder setThumbUri(Uri thumbUri)
        {
            this.thumbUri = thumbUri;
            return this;
        }

        public Character build()
        {
            return new Character(name, universe, thumbUri);
        }
    }
}
