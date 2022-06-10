package de.timschubert.mediiva;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.imageset.Artist;
import de.timschubert.mediiva.data.imageset.ArtistDao;
import de.timschubert.mediiva.data.imageset.ImageSet;

public class Helper
{
    private Helper() {}

    public static boolean numberWithinBounds(float number, float min, float max)
    {
        return min <= number && max >= number;
    }

    public static String capitalizeString(String input)
    {
        char[] inputArray = input.toLowerCase(Locale.ROOT).toCharArray();
        char[] outputArray = new char[inputArray.length];

        boolean lastWhitespace = true;
        for(int i = 0; i < inputArray.length; i++)
        {
            if(lastWhitespace && Character.isLetter(inputArray[i]))
            {
                outputArray[i] = Character.toUpperCase(inputArray[i]);
                lastWhitespace = false;
            }
            else if(Character.isWhitespace(inputArray[i]))
            {
                outputArray[i] = inputArray[i];
                lastWhitespace = true;
            }
            else
            {
                outputArray[i] = inputArray[i];
            }
        }

        return String.valueOf(outputArray);
    }

    public static Bitmap loadFromUri(Uri webUri) throws IOException
    {
        URL url = new URL(webUri.toString());
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();

        InputStream inputStream = connection.getInputStream();
        return BitmapFactory.decodeStream(inputStream);
    }

    /*public static boolean checkChaptersPresent(DiskShare diskShare, String fullDir)
    {

        for(FileIdBothDirectoryInformation i : diskShare.list(fullDir))
        {
            boolean hasChapterName = Pattern.matches("chapter\\s\\d+", i.getFileName().toLowerCase(Locale.ROOT));

            if(hasChapterName) return true;
        }

        Log.w("smbviewer.test", "No Chapters found for: "+fullDir);
        return false;
    }*/

    public static String getFriendlyArtistsName(ImageSet imageSet, Context context)
    {
        if(imageSet.getArtistIds() == null || imageSet.getArtistIds().isEmpty()) return null;

        StringBuilder stringBuilder = new StringBuilder();

        ArtistDao artistDao = AppDatabase.getInstance(context).artistDao();
        for(int i = 0; i < imageSet.getArtistIds().size(); i ++)
        {
            Artist artist = artistDao.getArtistById(imageSet.getArtistIds().get(i));

            stringBuilder.append(artist.getName());
            if(i != imageSet.getArtistIds().size()-1)
            {
                stringBuilder.append(", ");
            }
        }

        return stringBuilder.toString();
    }

    @NonNull
    public static Bitmap resizePosterForSaving(@NonNull Bitmap poster, @NonNull Context context)
    {
        int maxWidth = context.getResources().getInteger(R.integer.poster_storage_max_width_px);
        int maxHeight = context.getResources().getInteger(R.integer.poster_storage_max_height_px);

        return resizeBitmapKeepRatio(poster, maxWidth, maxHeight);
    }

    @NonNull
    public static Bitmap resizeFanArtForSaving(@NonNull Bitmap fanArt, @NonNull Context context)
    {
        int maxWidth = context.getResources().getInteger(R.integer.fanart_storage_max_width_px);
        int maxHeight = context.getResources().getInteger(R.integer.fanart_storage_max_height_px);

        return resizeBitmapKeepRatio(fanArt, maxWidth, maxHeight);
    }

    @NonNull
    public static Bitmap resizePageThumbnailForSaving(@NonNull Bitmap page, @NonNull Context context)
    {
        int maxWidth = context.getResources().getInteger(R.integer.page_thumbnail_storage_max_width_px);
        int maxHeight = context.getResources().getInteger(R.integer.page_thumbnail_storage_max_height_px);

        return resizeBitmapKeepRatio(page, maxWidth, maxHeight);
    }

    @NonNull
    public static Bitmap resizePersonThumbnailForSaving(@NonNull Bitmap person, @NonNull Context context)
    {
        int maxWidth = context.getResources().getInteger(R.integer.people_storage_max_width_px);
        int maxHeight = context.getResources().getInteger(R.integer.people_storage_max_height_px);

        return resizeBitmapKeepRatio(person, maxWidth, maxHeight);
    }

    @NonNull
    public static Bitmap resizeBitmapKeepRatio(@NonNull Bitmap bitmap,
                                               int maxWidth,
                                               int maxHeight)
    {
        if(maxHeight <= 0  || maxWidth <= 0) return bitmap;
        if(maxHeight >= bitmap.getHeight() && maxWidth >= bitmap.getWidth()) return bitmap;

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        float ratioBitmap = (float) bitmapWidth / (float) bitmapHeight;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if(ratioMax > ratioBitmap)
        {
            finalWidth = (int) ((float)maxHeight * ratioBitmap);
        }
        else
        {
            finalHeight = (int) ((float)maxWidth * ratioBitmap);
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }
}
