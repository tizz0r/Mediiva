package de.timschubert.mediiva.data.imageset;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ImageSetDao
{
    @Query("SELECT * from imageset ORDER BY title")
    List<ImageSet> getImageSets();
    @Query("SELECT * from imageset WHERE id = :id")
    ImageSet getImageSetById(long id);
    @Query("SELECT * from imageset WHERE path = :path")
    ImageSet getImageSetByPath(String path);
    @Query("SELECT EXISTS(SELECT * from imageset WHERE path = :path)")
    boolean hasImageSetWithPath(String path);
    @Insert
    long insertImageSet(ImageSet imageSet);
    @Update
    void updateImageSet(ImageSet imageSet);
    @Delete
    void deleteImageSet(ImageSet imageSet);
}
