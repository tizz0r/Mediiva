package de.timschubert.mediiva.data.movie;

import android.net.Uri;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "actor")
public class Actor
{
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "name")
    private final String name;
    @ColumnInfo(name = "originalname")
    private final String originalName;
    @ColumnInfo(name = "profile")
    private final Uri profileUri;

    public Actor(String name,
                 String originalName,
                 Uri profileUri)
    {
        this.name = name;
        this.originalName = originalName;
        this.profileUri = profileUri;
    }

    public void setId(long id) { this.id = id; }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getOriginalName() { return originalName; }
    public Uri getProfileUri() { return profileUri; }
}
