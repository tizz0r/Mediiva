package de.timschubert.mediiva.network;

import android.content.Context;
import android.util.Log;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.library.Library;
import de.timschubert.mediiva.data.movie.Movie;
import de.timschubert.mediiva.data.movie.MovieDao;
import de.timschubert.mediiva.dataloader.LibraryLoader;
import de.timschubert.mediiva.dataloader.xml.MovieXMLParser;

public class SmbMovieLibraryLoader extends LibraryLoader<Movie>
{
    private final Context context;

    public SmbMovieLibraryLoader(Callback<Movie> callback, Context context)
    {
        super(callback);
        this.context = context;
    }

    @Override
    public void searchForNewItemsAsync(Library library)
    {
        AppExecutors.getInstance().networkIO().execute(() -> searchForNewMovies(library));
    }

    private void searchForNewMovies(Library library)
    {
        if(library.getType() != Library.Type.MOVIE) return; //TODO verbose log

        SMBClient smbClient = new SMBClient();

        try(Connection connection = smbClient.connect(library.getHostname()))
        {
            AuthenticationContext authenticationContext = new AuthenticationContext(library.getUsername(),
                    library.getPassword().toCharArray(), "");
            Session session = connection.authenticate(authenticationContext);

            DiskShare diskShare = (DiskShare) session.connectShare(library.getSmbShare());

            List<String> probeDirectories = new ArrayList<>();

            try
            {
                for(FileIdBothDirectoryInformation i : diskShare.list(library.getLibraryPath()))
                {
                    if(".".equals(i.getFileName()) || "..".equals(i.getFileName())) continue;

                    FileAllInformation information = diskShare.getFileInformation(library.getLibraryPath()+"/"+i.getFileName());

                    if(information.getStandardInformation().isDirectory())
                    {
                        probeDirectories.add(i.getFileName());
                    }
                }
            }
            catch (Exception e)
            {
                Log.w("mediiva.smbmovielibraryloader", "Unable to open path "+library.getLibraryPath());
            }

            if(probeDirectories.isEmpty()) return;

            List<String> newDirectories = new ArrayList<>();
            for(String directory : probeDirectories)
            {
                String fullDir = library.getLibraryPath() + "/" + directory;

                if(AppDatabase.getInstance(context).movieDao().hasMovieWithPath(fullDir))
                {
                    Log.d("mediiva.smbreader", "Already added movie found: "+directory);
                    continue;
                }

                Log.d("mediiva.smbreader", "New movie found: "+directory);
                newDirectories.add(directory);
            }

            for(String newDirectory : newDirectories)
            {
                String fullDir = library.getLibraryPath() + "/" + newDirectory;

                String xmlPath = null;
                String moviePath = null;
                String posterPath = null;
                String fanArtPath = null;

                for(FileIdBothDirectoryInformation i : diskShare.list(fullDir))
                {
                    if(Pattern.matches(".+\\.nfo", i.getFileName().toLowerCase(Locale.ROOT)))
                    {
                        xmlPath = fullDir+"/"+i.getFileName();
                        Log.v("mediiva.smbreader", "Movie file found for dir: "+newDirectory);
                        break;
                    }
                }

                if(xmlPath == null)
                {
                    Log.v("mediiva.smbmovielibraryloader", "No xml found in probe dir: "+newDirectory);
                    continue;
                }

                for(FileIdBothDirectoryInformation i : diskShare.list(fullDir))
                {
                    if(Pattern.matches(".+\\.(mp4|avi|mov|wmv|mkv)", i.getFileName().toLowerCase(Locale.ROOT)))
                    {
                        moviePath = fullDir+"/"+i.getFileName();
                        Log.v("mediiva.smbreader", "Movie file found for dir: "+newDirectory);
                        break;
                    }
                }

                if(moviePath == null)
                {
                    Log.w("mediiva.smbmovielibraryloader", "No movie file found for dir: "+newDirectory);
                    continue;
                }

                for(FileIdBothDirectoryInformation i : diskShare.list(fullDir))
                {
                    if(Pattern.matches("poster\\..+", i.getFileName()))
                    {
                        posterPath = fullDir+"/"+i.getFileName();
                        Log.v("mediiva.smbreader", "Poster found for dir: "+newDirectory);
                        break;
                    }
                }

                for(FileIdBothDirectoryInformation i : diskShare.list(fullDir))
                {
                    if(Pattern.matches("fanart\\..+", i.getFileName()))
                    {
                        fanArtPath = fullDir+"/"+i.getFileName();
                        Log.v("mediiva.smbreader", "Fan art found for dir: "+newDirectory);
                        break;
                    }
                }

                try
                {
                    readXmlMetadata(diskShare, fullDir, moviePath, posterPath, fanArtPath, xmlPath, library.getId());
                }
                catch (IOException | XmlPullParserException e)
                {
                    Log.w("mediiva.smbreader", "Unable to read xml for dir: "+fullDir);
                }
            }

            try {
                diskShare.close();
                session.close();
            } catch (IOException e) {
                Log.w("mediiva.smbreader", "failed to close smb connection: "+e.getLocalizedMessage());
            }
        }
        catch (IOException e)
        {
            // TODO better log
            Log.w("mediiva.smbmovielibraryloader", "Can't auth: "+e.toString());
        }
    }

    private void readXmlMetadata(DiskShare diskShare, String directory, String moviePath, String posterPath, String fanArtPath, String xmlFilePath, long libraryId) throws IOException, XmlPullParserException
    {
        File testXmlFile = diskShare.openFile(xmlFilePath, EnumSet.of(AccessMask.GENERIC_READ), null,
                EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ), SMB2CreateDisposition.FILE_OPEN, null);

        MovieXMLParser xmlParser = new MovieXMLParser(context, this::processMovie, libraryId, directory, moviePath, posterPath, fanArtPath);

        xmlParser.parse(testXmlFile.getInputStream());
    }

    private void processMovie(Movie movie)
    {
        MovieDao movieDao = AppDatabase.getInstance(context).movieDao();

        // Test again, just to be safe
        if(movieDao.getMovieByPath(movie.getPath()) != null)
        {
            Log.e("mediiva.smbmovielibraryloader", "Duplicate entry snug into processMovie method!");
            return;
        }

        long movieId = movieDao.insertMovie(movie);
        getCallback().onItemAdded(movieDao.getMovieById(movieId)); //TODO test if this is enough
    }
}
