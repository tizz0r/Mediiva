package de.timschubert.mediiva.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.imageset.Chapter;
import de.timschubert.mediiva.data.imageset.ImageSet;
import de.timschubert.mediiva.data.imageset.ImageSetDao;
import de.timschubert.mediiva.data.imageset.Page;
import de.timschubert.mediiva.data.library.Library;
import de.timschubert.mediiva.dataloader.xml.ImageSetXMLParser;

public class SmbReader
{

    private final Context context;
    private final Callback callback;

    public SmbReader(Context context, Callback callback)
    {
        this.context = context;
        this.callback = callback;
    }

    public void searchForImageSets(Library library)
    {
        if(library.getType() != Library.Type.IMAGE_SET) return; //TODO verbose output

        AppExecutors.getInstance().networkIO().execute(() ->
        {
            SMBClient client = new SMBClient();

            try(Connection connection = client.connect(library.getHostname()))
            {
                AuthenticationContext authenticationContext = new AuthenticationContext(library.getUsername(),
                        library.getPassword().toCharArray(), "");
                Session session = connection.authenticate(authenticationContext);

                DiskShare diskShare = (DiskShare) session.connectShare(library.getSmbShare());


                List<String> probeDirectories = new ArrayList<>();

                for(FileIdBothDirectoryInformation i : diskShare.list(library.getLibraryPath()))
                {
                    if(".".equals(i.getFileName()) || "..".equals(i.getFileName())) continue;

                    FileAllInformation information = diskShare.getFileInformation(library.getLibraryPath()+"/"+i.getFileName());

                    if(information.getStandardInformation().isDirectory())
                    {
                        probeDirectories.add(i.getFileName());
                    }
                }

                if(probeDirectories.isEmpty()) return;

                List<String> newDirectories = new ArrayList<>();
                for(String directory : probeDirectories)
                {
                    String fullDir = library.getLibraryPath() + "/" + directory;

                    if(AppDatabase.getInstance(context).imageSetDao().hasImageSetWithPath(fullDir))
                    {
                        Log.d("mediiva.smbreader", "Already added imageset found: "+directory);
                        continue;
                    }

                    Log.d("mediiva.smbreader", "New imageset found: "+directory);
                    newDirectories.add(directory);
                }

                for(String newDirectory : newDirectories)
                {
                    String fullDir = library.getLibraryPath() + "/" + newDirectory;

                    String cleanName = newDirectory.replaceAll("\\s*\\(\\d+\\)$", "");
                    String xmlFilePath = fullDir+"/"+cleanName+".nfo";

                    boolean xmlExists = diskShare.fileExists(xmlFilePath);

                    if(!xmlExists)
                    {
                        Log.v("mediiva.smbreader", "No xml found in probe dir: "+newDirectory);
                        continue;
                    }

                    String posterPath = null;

                    for(FileIdBothDirectoryInformation i : diskShare.list(fullDir))
                    {
                        if(Pattern.matches("poster\\..+", i.getFileName()))
                        {
                            posterPath = fullDir+"/"+i.getFileName();
                            Log.v("mediiva.smbreader", "Poster found for dir: "+newDirectory);
                            break;
                        }
                    }

                    try
                    {
                        readXmlMetadata(context, diskShare, library.getId(), fullDir, posterPath, xmlFilePath);
                    }
                    catch (Exception e)
                    {
                        Log.w("mediiva.smbreader", "Unable to read xml for dir: "+newDirectory);
                    }

                }

                try {
                    diskShare.close();
                    session.close();
                } catch (IOException e) {
                    Log.w("mediiva.smbreader", "failed to close smb connection: "+e.getLocalizedMessage());
                }
            }
            catch (Exception e)
            {
                // TODO better log
                Log.w("mediiva.smbreader", "Can't auth: "+e.getLocalizedMessage());
            }
        });
    }

    /*public void loadPosterAsync(ImageSet imageSet)
    {
        if(imageSet.getPoster() == null) return;

        AppExecutors.getInstance().networkIO().execute(() ->
        {
            SMBClient client = new SMBClient();

            try(Connection connection = client.connect(HOSTNAME))
            {
                AuthenticationContext authenticationContext = new AuthenticationContext(USERNAME,
                        PASSWORD.toCharArray(), "");
                Session session = connection.authenticate(authenticationContext);

                DiskShare diskShare = (DiskShare) session.connectShare(SHARE);

                File testPosterFile = diskShare.openFile(imageSet.getPoster().getImagePath(),
                        EnumSet.of(AccessMask.GENERIC_READ),
                        null, EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ),
                        SMB2CreateDisposition.FILE_OPEN, null);

                try
                {
                    InputStream testPosterInputStream = testPosterFile.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(testPosterInputStream));
                    testPosterInputStream.close();

                    callback.onLoadPoster(imageSet, bitmap);
                }
                catch (Exception e)
                {
                    Log.w("mediiva.test", "Couldn't get poster: "+imageSet.getPoster().getImagePath()+" "+e.getLocalizedMessage());
                }

                diskShare.close();
                session.close();
            }
            catch (Exception e)
            {
                // TODO better log
                Log.w("mediiva.smbreader", "Can't auth: "+e.getLocalizedMessage());
            }
        });
    }*/

    /*public void loadPageAsync(ImageSet imageSet, Page page) // TODO error handling
    {
        AppExecutors.getInstance().networkIO().execute(() ->
        {
            SMBClient client = new SMBClient();

            try(Connection connection = client.connect(HOSTNAME))
            {
                AuthenticationContext authenticationContext = new AuthenticationContext(USERNAME,
                        PASSWORD.toCharArray(), "");
                Session session = connection.authenticate(authenticationContext);

                DiskShare diskShare = (DiskShare) session.connectShare(SHARE);

                try
                {
                    File imageFile = diskShare.openFile(page.getImagePath(),
                            EnumSet.of(AccessMask.GENERIC_READ),
                            null, EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ),
                            SMB2CreateDisposition.FILE_OPEN, null);

                    try
                    {
                        InputStream imageInputStream = imageFile.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(imageInputStream));
                        imageInputStream.close();

                        callback.onLoadPage(imageSet, page, bitmap);
                    }
                    catch (Exception e)
                    {
                        Log.w("smbviewer.smbget", "Couldn't get image: "+page.getImagePath()+" "+e.getLocalizedMessage());
                    }
                }
                catch (Exception ignore) {}

                diskShare.close();
                session.close();
            }
            catch (Exception e)
            {
                // TODO better log
                Log.w("mediiva.smbreader", "Can't auth: "+e.getLocalizedMessage());
            }
        });
    }*/

    //public void forceReloadImageSets()
    //{
    //    AppExecutors.getInstance().networkIO().execute(() ->
    //    {
    //        DiskShare diskShare = null;
    //        /*try
    //        {
    //            diskShare = authenticate();
    //        } catch (IOException e)
    //        {
    //            Log.w("mediiva.smbreader", "Unable to authenticate, perhaps the network is unreachable");
    //            return;
    //        }*/
//
    //        List<String> probeDirectories = new ArrayList<>();
//
    //        for(FileIdBothDirectoryInformation i : diskShare.list(LIBRARY_PATH))
    //        {
    //            if(".".equals(i.getFileName()) || "..".equals(i.getFileName())) continue;
//
    //            FileAllInformation information = diskShare.getFileInformation(LIBRARY_PATH+"/"+i.getFileName());
//
    //            if(information.getStandardInformation().isDirectory())
    //            {
    //                probeDirectories.add(i.getFileName());
    //            }
    //        }
//
    //        if(probeDirectories.isEmpty()) return;
//
    //        for(String directory : probeDirectories)
    //        {
    //            String fullDir = LIBRARY_PATH+"/"+directory;
//
    //            String cleanName = directory.replaceAll("\\s*\\(\\d+\\)$", "");
    //            String xmlFilePath = fullDir+"/"+cleanName+".nfo";
//
    //            boolean xmlExists = diskShare.fileExists(xmlFilePath);
//
    //            String posterPath = null;
//
    //            for(FileIdBothDirectoryInformation i : diskShare.list(fullDir))
    //            {
    //                if(Pattern.matches("poster\\..+", i.getFileName()))
    //                {
    //                    posterPath = fullDir+"/"+i.getFileName();
    //                    break;
    //                }
    //            }
//
    //            boolean chaptersExist = Helper.checkChaptersPresent(diskShare, fullDir);
//
    //            Log.i("smbviewer.test", "Image set found: "+cleanName);
//
    //            if(!xmlExists || !chaptersExist) continue;
//
    //            try
    //            {
    //                readXmlMetadata(context, diskShare, directory,"", xmlFilePath);
//
    //                //Images images = TestThread.readAvailableImages(diskShare, fullDir);
////
    //                //ImageSet foundSet = new ImageSet(metadata, posterPath, images);
////
    //                //callback.onLoadImageSet(foundSet);
    //            }
    //            catch (XmlPullParserException | IOException e)
    //            {
    //                Log.w("smbviewer.test", "error loading xml: "+xmlFilePath);
    //                e.printStackTrace();
    //            }
    //        }
//
    //        try {
    //            diskShare.close();
    //        } catch (IOException e) {
    //            e.printStackTrace(); //TODO handle exception
    //        }
    //    });
    //}

    private void readXmlMetadata(Context context, DiskShare diskShare, long libraryId, String directory, String posterPath, String xmlPath) throws IOException, XmlPullParserException
    {
        File testXmlFile = diskShare.openFile(xmlPath, EnumSet.of(AccessMask.GENERIC_READ), null,
                EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ), SMB2CreateDisposition.FILE_OPEN, null);

        ImageSetXMLParser xmlParser = new ImageSetXMLParser(context, libraryId, directory, posterPath, metadata -> processMetadata(metadata, diskShare));

        xmlParser.parse(testXmlFile.getInputStream());
    }

    private void processMetadata(ImageSet imageSet, DiskShare diskShare)
    {
        ImageSetDao imageSetDao = AppDatabase.getInstance(context).imageSetDao();

        // Test again, just to be safe
        if(imageSetDao.getImageSetByPath(imageSet.getPath()) != null)
        {
            Log.w("mediiva.smbreader", "duplicate entry snug into processMetadata method!");
            return;
        }

        List<Chapter> setChapters = readChapters(imageSet, diskShare);

        imageSet.setChapters(setChapters);

        long imageSetId = imageSetDao.insertImageSet(imageSet);

        callback.onLoadImageSet(imageSetId);
    }

    private List<Chapter> readChapters(ImageSet metadata, DiskShare diskShare)
    {
        Map<Chapter, String> foundChaptersTest = new HashMap<>(); // Chapter, Folder Dir

        for(FileIdBothDirectoryInformation i : diskShare.list(metadata.getPath()))
        {
            boolean matchesChapter = Pattern.matches("chapter\\s\\d+", i.getFileName().toLowerCase(Locale.ROOT));
            if(!matchesChapter) continue;

            try
            {
                String rawChapterNumber = i.getFileName().toLowerCase(Locale.ROOT).replaceAll("chapter\\s+", "");

                int chapter = Integer.parseInt(rawChapterNumber);

                foundChaptersTest.put(new Chapter(chapter, null, null), metadata.getPath()+"/"+i.getFileName());
            }
            catch (Exception e)
            {
                Log.w("mediiva.test", "Couldn't get chapter number for: "+i.getFileName());
            }
        }

        for(Map.Entry<Chapter, String> entry : foundChaptersTest.entrySet())
        {
            Pattern pagePattern = Pattern.compile("c(\\d+)i(\\d+)");

            for(FileIdBothDirectoryInformation i : diskShare.list(entry.getValue()))
            {
                String fileName = i.getFileName();
                String filePath = entry.getValue()+"/"+fileName;



                Matcher matcher = pagePattern.matcher(fileName);
                if(!matcher.find())
                {
                    Log.v("mediiva.test", "Non image file found in directory: "+filePath);
                    continue;
                }

                int pageNumber = Integer.parseInt(matcher.group(2));

                Page foundPage = new Page(pageNumber, filePath);

                entry.getKey().addPage(foundPage);
            }
        }

        return new ArrayList<>(foundChaptersTest.keySet());
    }

    public interface Callback
    {
        void onLoadImageSet(long imageSet);
        void onLoadPoster(ImageSet imageSet, Bitmap poster);
        void onLoadPage(ImageSet imageSet, Page page, Bitmap bitmap);
    }
}
