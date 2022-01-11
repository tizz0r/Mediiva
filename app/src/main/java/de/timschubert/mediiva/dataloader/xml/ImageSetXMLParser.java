package de.timschubert.mediiva.dataloader.xml;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

import de.timschubert.mediiva.R;
import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.UniqueId;
import de.timschubert.mediiva.data.imageset.Artist;
import de.timschubert.mediiva.data.imageset.ArtistDao;
import de.timschubert.mediiva.data.imageset.Character;
import de.timschubert.mediiva.data.imageset.CharacterDao;
import de.timschubert.mediiva.data.imageset.ImageSet;
import de.timschubert.mediiva.data.tag.FemaleTag;
import de.timschubert.mediiva.data.tag.HermaphroditeTag;
import de.timschubert.mediiva.data.tag.LocationTag;
import de.timschubert.mediiva.data.tag.MaleTag;
import de.timschubert.mediiva.data.tag.SimpleTag;
import de.timschubert.mediiva.data.tag.Tag;

public class ImageSetXMLParser extends MediivaXMLParser
{

    private final Context context;
    private final Callback callback;
    private final long libraryId;
    private final String path;
    @Nullable private final String posterPath;

    private ImageSet.Builder builder;

    /**
     * Used to create ImageSetMetadata from XML files. Use parse() to begin parsing.
     * @param context Context of the Application
     * @param path Path of the folder containing the XML file, will be used to store the "path" variable in the database
     * @param callback Callback to receive the parsed ImageSet
     */
    public ImageSetXMLParser(@NonNull Context context,
                             long libraryId,
                             @NonNull String path,
                             @Nullable String posterPath,
                             @NonNull Callback callback)
    {
        this.context = context;
        this.callback = callback;
        this.libraryId = libraryId;
        this.path = path;
        this.posterPath = posterPath;
    }

    @NonNull
    @Override
    String getRootTag() { return "imageset"; }

    @Override
    void onBeginParse()
    {
        builder = new ImageSet.Builder(context, path, libraryId).setPoster(posterPath);
    }

    @Override
    void onProcessTag(String tagName, XmlPullParser pullParser)
    {
        try
        {
            switch (tagName)
            {
                case "title":
                    builder.setTitle(readTagAsString(tagName, pullParser));
                    break;
                case "originaltitle":
                    builder.setOriginalTitle(readTagAsString(tagName, pullParser));
                    break;
                case "sorttitle":
                    builder.setSortTitle(readTagAsString(tagName, pullParser));
                    break;
                case "type":
                    readType(pullParser);
                    break;
                case "outline":
                    builder.setOutline(readTagAsString(tagName, pullParser));
                    break;
                case "plot":
                    builder.setPlot(readTagAsString(tagName, pullParser));
                    break;
                case "premiered":
                    readPremiered(pullParser);
                    break;
                case "uniqueid":
                    readUniqueId(pullParser);
                    break;
                case "status":
                    readStatus(pullParser);
                    break;
                case "country":
                    builder.setCountry(readTagAsString(tagName, pullParser));
                    break;
                case "language":
                    builder.setLanguage(readTagAsString(tagName, pullParser));
                    break;
                case "tag":
                    readTag(pullParser);
                    break;
                case "series":
                    builder.setSeries(readTagAsString(tagName, pullParser));
                    break;
                case "group":
                    builder.setGroup(readTagAsString(tagName, pullParser));
                    break;
                case "artist":
                    readArtist(pullParser);
                    break;
                case "character":
                    readCharacter(pullParser);
                    break;
                case "rating":
                    skip(pullParser); //TODO
                    break;
                default:
                    skip(pullParser);
                    break;
            }
        }
        catch (Exception e)
        {
            Log.w("mediiva.imagesetxmlparser", "Error while parsing xml data: "+e.getLocalizedMessage());
        }
    }

    private void readType(XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        String typeRaw = readTagAsString("type", pullParser);
        if(typeRaw == null) return;
        switch (typeRaw.toLowerCase(Locale.ROOT))
        {
            case "imageset":
                builder.setType(ImageSet.Type.IMAGESET);
                break;
            case "comic":
                builder.setType(ImageSet.Type.COMIC);
                break;
            case "doujinshi":
                builder.setType(ImageSet.Type.DOUJINSHI);
                break;
            case "manga":
                builder.setType(ImageSet.Type.MANGA);
                break;
            case "artistcg":
                builder.setType(ImageSet.Type.ARTISTCG);
                break;
            case "gamecg":
                builder.setType(ImageSet.Type.GAMECG);
                break;
        }
    }

    private void readPremiered(XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        String premieredRaw = readTagAsString("premiered", pullParser);

        if(!Pattern.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d", premieredRaw))
        {
            Log.w("smbviewer.parser", "Wrong \"premiered\" tag: "+premieredRaw);
            return;
        }

        int year = Integer.parseInt(premieredRaw.substring(0, 4));
        int month = Integer.parseInt(premieredRaw.substring(5, 7));
        int day = Integer.parseInt(premieredRaw.substring(8, 10));

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day-1);

        builder.setPremieredDate(calendar.getTime());
    }

    private void readUniqueId(XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        boolean defaultTag = false;
        String type = "";

        try
        {
            String defaultRaw = pullParser.getAttributeValue(null, "default");
            defaultTag = "true".equals(defaultRaw);
        }
        catch (Exception ignore) {}

        try
        {
            type = pullParser.getAttributeValue(null, "type");
        }
        catch (Exception ignore) {}

        UniqueId uniqueId = new UniqueId(defaultTag, type, readTagAsString("uniqueid", pullParser));

        builder.addUniqueId(uniqueId);
    }

    /*private void readUniqueId(XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        pullParser.require(XmlPullParser.START_TAG, null, "uniqueid");

        boolean defaultTag = false;
        String type = "";

        try
        {
            String defaultRaw = pullParser.getAttributeValue(null, "default");
            defaultTag = "true".equals(defaultRaw);
        }
        catch (Exception ignore) {}

        try
        {
            type = pullParser.getAttributeValue(null, "type");
        }
        catch (Exception ignore) {}

        UniqueId uniqueId = new UniqueId(defaultTag, type, readText(pullParser));

        builder.addUniqueId(uniqueId);
        pullParser.require(XmlPullParser.END_TAG, null, "uniqueid");
    }*/

    private void readStatus(XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        String statusRaw = readTagAsString("status", pullParser);

        if(statusRaw == null) return;
        switch (statusRaw.toLowerCase(Locale.ROOT))
        {
            case "completed":
                builder.setStatus(ImageSet.Status.COMPLETED);
                break;
            case "abandoned":
                builder.setStatus(ImageSet.Status.ABANDONED);
                break;
            case "wip":
                builder.setStatus(ImageSet.Status.WIP);
                break;
        }
    }

    private void readTag(XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        String tagRaw = readTagAsString("tag", pullParser).toLowerCase(Locale.ROOT); // TODO maybe change to keep capitalization
        builder.addTag(getSpecificTag(tagRaw));
    }

    private Tag getSpecificTag(String tagRaw)
    {
        String[] femalePrefixes = context.getResources().getStringArray(R.array.tag_female_lookout_prefixes);
        String[] hermaphroditePrefixes = context.getResources().getStringArray(R.array.tag_hermaphrodite_lookout_prefixes);
        String[] malePrefixes = context.getResources().getStringArray(R.array.tag_male_lookout_prefixes);
        String[] locationPrefixes = context.getResources().getStringArray(R.array.tag_location_lookout_prefixes);

        for(String femalePrefix : femalePrefixes)
        {
            if(tagRaw.startsWith(femalePrefix))
            {
                String tagName = tagRaw.substring(femalePrefix.length());
                return new FemaleTag(context, tagName);
            }
        }
        for(String hermaphroditePrefix : hermaphroditePrefixes)
        {
            if(tagRaw.startsWith(hermaphroditePrefix))
            {
                String tagName = tagRaw.substring(hermaphroditePrefix.length());
                return new HermaphroditeTag(context, tagName);
            }
        }
        for(String malePrefix : malePrefixes)
        {
            if(tagRaw.startsWith(malePrefix))
            {
                String tagName = tagRaw.substring(malePrefix.length());
                return new MaleTag(context, tagName);
            }
        }
        for(String locationPrefix : locationPrefixes)
        {
            if(tagRaw.startsWith(locationPrefix))
            {
                String tagName = tagRaw.substring(locationPrefix.length());
                return new LocationTag(context, tagName);
            }
        }

        return new SimpleTag(context, tagRaw);
    }

    private void readArtist(XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        pullParser.require(XmlPullParser.START_TAG, null, "artist");

        String artistName = "";
        Uri artistThumbUrl = null;
        Uri artistProfileUrl = null;

        while(pullParser.next() != XmlPullParser.END_TAG)
        {
            if(pullParser.getEventType() != XmlPullParser.START_TAG) continue;

            String name = pullParser.getName();

            switch (name)
            {
                case "name":
                    artistName = readTagAsString(name, pullParser);
                    break;
                case "thumb":
                    artistThumbUrl = readTagAsUri(name, pullParser);
                    break;
                case "profile":
                    artistProfileUrl = readTagAsUri(name, pullParser);
                    break;
                default:
                    skip(pullParser);
            }
        }

        ArtistDao artistDao = AppDatabase.getInstance(context).artistDao();

        if(artistDao.artistExists(artistName, artistProfileUrl))
        {
            builder.addArtistId(artistDao.getArtistByData(artistName, artistProfileUrl).getId());
        }
        else
        {
            Artist artist = new Artist(artistName, artistProfileUrl, artistThumbUrl);
            long artistId = artistDao.insertArtist(artist);
            builder.addArtistId(artistId);
        }

        pullParser.require(XmlPullParser.END_TAG, null, "artist");
    }

    private void readCharacter(XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        pullParser.require(XmlPullParser.START_TAG, null, "character");

        Character.Builder characterBuilder = new Character.Builder(context);

        while(pullParser.next() != XmlPullParser.END_TAG)
        {
            if(pullParser.getEventType() != XmlPullParser.START_TAG) continue;

            String name = pullParser.getName();

            switch (name)
            {
                case "name":
                    characterBuilder.setName(readTagAsString(name, pullParser));
                    break;
                case "universe":
                    characterBuilder.setUniverse(readTagAsString(name, pullParser));
                    break;
                case "thumb":
                    characterBuilder.setThumbUri(readTagAsUri(name, pullParser));
                    break;
                default:
                    skip(pullParser);
            }
        }

        Character character = characterBuilder.build();
        CharacterDao characterDao = AppDatabase.getInstance(context).characterDao();

        if(characterDao.characterExists(character.getName(), character.getUniverse()))
        {
            builder.addCharacterId(characterDao.getCharacterByData(character.getName(), character.getUniverse()).getId());
        }
        else
        {
            long characterId = characterDao.insertCharacter(character);
            builder.addCharacterId(characterId);
        }

        pullParser.require(XmlPullParser.END_TAG, null, "character");
    }

    @Override
    void onEndParse()
    {
        callback.onFinished(builder.build());
    }

    public interface Callback
    {
        void onFinished(ImageSet metadata);
    }
}
