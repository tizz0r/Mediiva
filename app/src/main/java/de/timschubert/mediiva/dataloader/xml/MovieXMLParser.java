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
import de.timschubert.mediiva.data.imageset.Character;
import de.timschubert.mediiva.data.imageset.CharacterDao;
import de.timschubert.mediiva.data.movie.Actor;
import de.timschubert.mediiva.data.movie.ActorDao;
import de.timschubert.mediiva.data.movie.ActorInfo;
import de.timschubert.mediiva.data.movie.Movie;
import de.timschubert.mediiva.data.tag.FemaleTag;
import de.timschubert.mediiva.data.tag.HermaphroditeTag;
import de.timschubert.mediiva.data.tag.LocationTag;
import de.timschubert.mediiva.data.tag.MaleTag;
import de.timschubert.mediiva.data.tag.SimpleTag;
import de.timschubert.mediiva.data.tag.Tag;

public class MovieXMLParser extends MediivaXMLParser
{

    private final Context context;
    private final Callback callback;
    private final long libraryId;
    private final String path;
    private final String moviePath;
    @Nullable private final String posterPath;
    @Nullable private final String fanArtPath;

    private Movie.Builder builder;

    public MovieXMLParser(@NonNull Context context,
                          @NonNull Callback callback,
                          long libraryId,
                          @NonNull String path,
                          @NonNull String moviePath,
                          @Nullable String posterPath,
                          @Nullable String fanArtPath)
    {
        this.context = context;
        this.callback = callback;
        this.libraryId = libraryId;
        this.path = path;
        this.moviePath = moviePath;
        this.posterPath = posterPath;
        this.fanArtPath = fanArtPath;
    }

    @NonNull
    @Override
    String getRootTag() { return "movie"; }

    @Override
    void onBeginParse()
    {
        builder = new Movie.Builder(context, path, moviePath, libraryId).setPoster(posterPath).setFanArt(fanArtPath);
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
                case "country":
                    builder.setCountryCode(readTagAsString(tagName, pullParser));
                    break;
                case "language":
                    builder.setLanguageCode(readTagAsString(tagName, pullParser));
                    break;
                case "tag":
                case "genre":
                    readTag(tagName, pullParser);
                    break;
                case "series":
                case "set":
                    builder.setSeries(readTagAsString(tagName, pullParser));
                    break;
                case "character":
                    readCharacter(pullParser);
                    break;
                case "rating":
                    skip(pullParser); //TODO
                    break;
                case "runtime":
                    builder.setRuntimeMinutes(readTagAsInt(tagName, pullParser));
                    break;
                case "tagline":
                    builder.setTagLine(readTagAsString(tagName, pullParser));
                    break;
                case "director":
                    builder.setDirector(readTagAsString(tagName, pullParser));
                    break;
                case "studio":
                    builder.setStudio(readTagAsString(tagName, pullParser));
                    break;
                case "trailer":
                    builder.setTrailerUri(readTagAsUri(tagName, pullParser));
                    break;
                case "agerating":
                case "mpaa":
                case "certification":
                    skip(pullParser); //TODO
                    break;
                case "actor":
                    readActor(pullParser);
                    break;
                default:
                    Log.v("mediiva.moviexmlparser", "Skipping tag: "+tagName);
                    skip(pullParser);
                    break;
            }
        }
        catch (Exception e)
        {
            Log.w("mediiva.moviexmlparser", "Error while parsing xml data for tag: \""+tagName+"\": "+e.getLocalizedMessage());
        }
    }

    private void readPremiered(XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        String premieredRaw = readTagAsString("premiered", pullParser);

        if(!Pattern.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d", premieredRaw))
        {
            Log.w("mediiva.moviexmlparser", "Wrong \"premiered\" tag: "+premieredRaw);
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

    private void readTag(String tagName, XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        String tagRaw = readTagAsString(tagName, pullParser).toLowerCase(Locale.ROOT); // TODO maybe change to keep capitalization

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

    private void readActor(XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        pullParser.require(XmlPullParser.START_TAG, null, "actor");

        String actorName = "";
        String actorOriginalName = "";
        String actorRole = "";
        Uri actorThumbUri = null;
        Uri actorProfileUri = null;

        while(pullParser.next() != XmlPullParser.END_TAG)
        {
            if(pullParser.getEventType() != XmlPullParser.START_TAG) continue;

            String name = pullParser.getName();

            switch (name)
            {
                case "name":
                    actorName = readTagAsString(name, pullParser);
                    break;
                case "altname":
                case "originalname":
                    actorOriginalName = readTagAsString(name, pullParser);
                    break;
                case "role":
                    actorRole = readTagAsString(name, pullParser);
                    break;
                case "thumb":
                    actorThumbUri = readTagAsUri(name, pullParser);
                    break;
                case "profile":
                    actorProfileUri = readTagAsUri(name, pullParser);
                    break;
                default:
                    skip(pullParser);
            }
        }

        ActorDao actorDao = AppDatabase.getInstance(context).actorDao();

        if(actorDao.actorExists(actorName, actorOriginalName, actorProfileUri))
        {
            Actor actor = actorDao.getActorByData(actorName, actorOriginalName, actorProfileUri);

            builder.addActor(actor.getId(), new ActorInfo(actorRole, actorThumbUri));
        }
        else
        {
            Actor actor = new Actor(actorName, actorOriginalName, actorProfileUri);
            long actorId = actorDao.insertActor(actor);
            builder.addActor(actorId, new ActorInfo(actorRole, actorThumbUri));
        }

        pullParser.require(XmlPullParser.END_TAG, null, "actor");
    }

    @Override
    void onEndParse()
    {
        callback.onFinished(builder.build());
    }

    public interface Callback
    {
        void onFinished(Movie movie);
    }
}
