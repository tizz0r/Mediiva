package de.timschubert.mediiva.data.library;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LibraryDao
{
    @Query("SELECT * from library")
    List<Library> getLibraries();
    @Query("SELECT * from library WHERE id = :id")
    Library getLibraryById(long id);
    @Query("SELECT EXISTS(SELECT * from library WHERE hostname = :hostname AND smbshare = :smbShare AND path = :libraryPath AND type = :type)")
    boolean libraryOnSamePathExists(String hostname, String smbShare, String libraryPath, Library.Type type);
    @Insert
    long insertLibrary(Library library);
    @Update
    void updateLibrary(Library library);
    @Delete
    void deleteLibrary(Library library);
}
