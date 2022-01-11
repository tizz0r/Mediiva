package de.timschubert.mediiva.data.imageset;

import android.net.Uri;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.timschubert.mediiva.Helper;

@Dao
public interface ArtistDao
{
    @Query("SELECT * from artist")
    List<Artist> getArtists();
    @Query("SELECT * from artist WHERE id = :id")
    Artist getArtistById(long id);
    @Query("SELECT EXISTS(SELECT * from artist WHERE name = :name AND profile = :profileUri)")
    boolean artistExists(String name, Uri profileUri);
    @Query("SELECT * from artist WHERE name = :name AND profile = :profileUri")
    Artist getArtistByData(String name, Uri profileUri);
    @Insert
    long insertArtist(Artist artist);
    @Update
    void updateArtist(Artist artist);
    @Delete
    void deleteArtist(Artist artist);
}
