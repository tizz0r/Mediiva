package de.timschubert.mediiva.data.imageset;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CharacterDao
{
    @Query("SELECT * from character")
    List<Character> getCharacters();
    @Query("SELECT * from character where id = :id")
    Character getCharacterById(long id);
    @Query("SELECT EXISTS(SELECT * from character where name = :name AND universe = :universe)")
    boolean characterExists(String name, String universe);
    @Query("SELECT * from character where name = :name AND universe = :universe")
    Character getCharacterByData(String name, String universe);
    @Insert
    long insertCharacter(Character character);
    @Update
    void updateCharacter(Character character);
    @Delete
    void deleteCharacter(Character character);
}
