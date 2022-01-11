package de.timschubert.mediiva.data.movie;

import android.net.Uri;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ActorDao
{
    @Query("SELECT * from actor")
    List<Actor> getActors();
    @Query("SELECT * from actor WHERE id = :id")
    Actor getActorById(long id);
    @Query("SELECT EXISTS(SELECT * from actor WHERE name = :name AND originalname = :originalName AND profile = :profileUri)")
    boolean actorExists(String name, String originalName, Uri profileUri);
    @Query("SELECT * from actor WHERE name = :name AND originalname = :originalName AND profile = :profileUri")
    Actor getActorByData(String name, String originalName, Uri profileUri);
    @Insert
    long insertActor(Actor actor);
    @Update
    void updateActor(Actor actor);
    @Delete
    void deleteActor(Actor actor);
}
