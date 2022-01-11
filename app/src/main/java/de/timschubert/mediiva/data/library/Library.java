package de.timschubert.mediiva.data.library;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * This class is stored in the Room database and holds information about user saved libraries
 */
@Entity(tableName = "library")
public class Library
{
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "name")
    @NonNull
    private final String name;
    @ColumnInfo(name = "hostname")
    @NonNull
    private final String hostname;
    @ColumnInfo(name = "smbshare")
    @NonNull
    private final String smbShare;
    @ColumnInfo(name = "user")
    @NonNull
    private final String username;
    @ColumnInfo(name = "password")
    @NonNull
    private final String password;
    @ColumnInfo(name = "path")
    @NonNull
    private final String libraryPath;
    @ColumnInfo(name = "type")
    @NonNull
    private final Type type;

    /**
     * Creates a new Library instance
     * @param name Name of the library to be displayed in the app
     * @param hostname Hostname of the SMB server
     * @param smbShare Name of the SMB share
     * @param username Username used for authentication
     * @param password Password used for authentication
     * @param libraryPath Path to the library in the given share
     * @param type Type of the library
     */
    public Library(@NonNull String name,
                   @NonNull String hostname,
                   @NonNull String smbShare,
                   @NonNull String username,
                   @NonNull String password,
                   @NonNull String libraryPath,
                   @NonNull Type type)
    {
        this.name = name;
        this.hostname = hostname;
        this.smbShare = smbShare;
        this.username = username;
        this.password = password;
        this.libraryPath = libraryPath;
        this.type = type;
    }

    public void setId(long id) { this.id = id; }

    public long getId() { return id; }
    @NonNull public String getName() { return name; }
    @NonNull public String getHostname() { return hostname; }
    @NonNull public String getSmbShare() { return smbShare; }
    @NonNull public String getUsername() { return username; }
    @NonNull public String getPassword() { return password; }
    @NonNull public String getLibraryPath() { return libraryPath; }
    @NonNull public Type getType() { return type; }

    public enum Type
    {
        IMAGE_SET, MOVIE
    }
}
