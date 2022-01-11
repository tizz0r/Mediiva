package de.timschubert.mediiva.data.imageset;

import android.net.Uri;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * This class is stored in the Room Database and holds information about &lt;artist&gt; xml tags
 */
@Entity(tableName = "artist")
public class Artist
{
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "name")
    private final String name;
    @ColumnInfo(name = "profile")
    private final Uri profileUri;
    @ColumnInfo(name = "thumb")
    private final Uri thumbUri;

    /**
     * Creates a new Artist instance
     * @param name The name of the artist. Must be non-null
     * @param profileUri A uri to the profile of the artist
     * @param thumbUri A uri to a thumbnail image of the artist
     */
    public Artist(String name, Uri profileUri, Uri thumbUri)
    {
        this.name = name;
        this.profileUri = profileUri;
        this.thumbUri = thumbUri;
    }

    public void setId(long id) { this.id = id; }

    public long getId() { return id; }
    public String getName() { return name; }
    public Uri getProfileUri() { return profileUri; }
    public Uri getThumbUri() { return thumbUri; }
}
