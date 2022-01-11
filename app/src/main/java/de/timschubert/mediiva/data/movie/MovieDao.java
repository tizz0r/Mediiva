package de.timschubert.mediiva.data.movie;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.timschubert.mediiva.data.imageset.ImageSet;

@Dao
public interface MovieDao
{
    @Query("SELECT * from movie ORDER BY title")
    List<Movie> getMovies();
    @Query("SELECT * from movie WHERE libraryid = :libraryId ORDER BY title")
    List<Movie> getMoviesByLibraryId(long libraryId);
    @Query("SELECT * from movie WHERE id = :id")
    Movie getMovieById(long id);
    @Query("SELECT * from movie WHERE path = :path")
    Movie getMovieByPath(String path);
    @Query("SELECT EXISTS(SELECT * from movie WHERE path = :path)")
    boolean hasMovieWithPath(String path);
    @Insert
    long insertMovie(Movie movie);
    @Update
    void updateMovie(Movie movie);
    @Delete
    void deleteMovie(Movie movie);
}
