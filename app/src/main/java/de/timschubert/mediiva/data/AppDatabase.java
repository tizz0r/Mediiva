package de.timschubert.mediiva.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import de.timschubert.mediiva.data.imageset.Artist;
import de.timschubert.mediiva.data.imageset.ArtistDao;
import de.timschubert.mediiva.data.imageset.Character;
import de.timschubert.mediiva.data.imageset.CharacterDao;
import de.timschubert.mediiva.data.imageset.ImageSet;
import de.timschubert.mediiva.data.imageset.ImageSetDao;
import de.timschubert.mediiva.data.library.Library;
import de.timschubert.mediiva.data.library.LibraryDao;
import de.timschubert.mediiva.data.movie.Actor;
import de.timschubert.mediiva.data.movie.ActorDao;
import de.timschubert.mediiva.data.movie.Movie;
import de.timschubert.mediiva.data.movie.MovieDao;

@Database(entities = {ImageSet.class, Artist.class, Character.class, Movie.class, Actor.class, Library.class}, version = 5)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase
{
    public static final String DB_NAME = "content";
    private static AppDatabase instance;

    public abstract ImageSetDao imageSetDao();
    public abstract ArtistDao artistDao();
    public abstract CharacterDao characterDao();
    public abstract ActorDao actorDao();
    public abstract MovieDao movieDao();
    public abstract LibraryDao libraryDao();

    public static synchronized AppDatabase getInstance(Context context)
    {
        if(instance == null)
        {
            instance = Room.databaseBuilder(context, AppDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .addTypeConverter(new Converters(context))
                    .build();
        }

        return instance;
    }
}
