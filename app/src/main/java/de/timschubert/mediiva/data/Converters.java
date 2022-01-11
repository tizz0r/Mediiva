package de.timschubert.mediiva.data;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.timschubert.mediiva.R;
import de.timschubert.mediiva.data.imageset.Chapter;
import de.timschubert.mediiva.data.imageset.Page;
import de.timschubert.mediiva.data.movie.ActorInfo;
import de.timschubert.mediiva.data.tag.FemaleTag;
import de.timschubert.mediiva.data.tag.HermaphroditeTag;
import de.timschubert.mediiva.data.tag.LocationTag;
import de.timschubert.mediiva.data.tag.MaleTag;
import de.timschubert.mediiva.data.tag.SimpleTag;
import de.timschubert.mediiva.data.tag.Tag;

@ProvidedTypeConverter
public class Converters
{
    private final Context context;

    public Converters(Context context)
    {
        this.context = context;
    }

    @TypeConverter
    public Date fromTimestamp(@Nullable Long value)
    {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public Long fromDate(@Nullable Date date)
    {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public List<Tag> fromTagString(@Nullable String value)
    {
        // Pattern: "$prefix:$base64($tag);$prefix:$base64($tag);"
        List<Tag> tags = new ArrayList<>();

        if(value == null || value.isEmpty()) return tags;
        String[] tagsRaw = value.split(";");

        for(String tagRaw : tagsRaw)
        {
            if(tagRaw == null || tagRaw.isEmpty()) continue;

            try
            {
                String prefix = tagRaw.substring(0, tagRaw.indexOf(":")+1);
                String tagEncoded = tagRaw.substring(tagRaw.indexOf(":")+1);

                String tag = fromB64(tagEncoded);

                String simplePrefix = context.getString(R.string.tag_simple_prefix);
                String locationPrefix = context.getString(R.string.tag_location_prefix);
                String femalePrefix = context.getString(R.string.tag_female_prefix);
                String hermaphroditePrefix = context.getString(R.string.tag_hermaphrodite_prefix);
                String malePrefix = context.getString(R.string.tag_male_prefix);

                if(simplePrefix.equals(prefix))
                {
                    tags.add(new SimpleTag(context, tag));
                }
                else if(locationPrefix.equals(prefix))
                {
                    tags.add(new LocationTag(context, tag));
                }
                else if(femalePrefix.equals(prefix))
                {
                    tags.add(new FemaleTag(context, tag));
                }
                else if(hermaphroditePrefix.equals(prefix))
                {
                    tags.add(new HermaphroditeTag(context, tag));
                }
                else if(malePrefix.equals(prefix))
                {
                    tags.add(new MaleTag(context, tag));
                }
                else
                {
                    Log.w("mediiva.converters", "Unknown tag prefix found for tag: "+tagRaw);
                }
            }
            catch (Exception e)
            {
                Log.w("mediiva.converters", "Error adding tag: "+tagRaw+"\t"+e.getLocalizedMessage());
            }
        }

        return tags;
    }

    @TypeConverter
    public String fromTags(@Nullable List<Tag> tags)
    {
        if(tags == null) return "";

        StringBuilder output = new StringBuilder();

        for(Tag tag : tags)
        {
            output.append(tag.getEncodedString()).append(";");
        }

        return output.toString();
    }

    @TypeConverter
    public Locale fromLanguageTag(@Nullable String value)
    {
        return Locale.forLanguageTag(value == null ? "und" : value);
    }

    @TypeConverter
    public String fromLocale(@Nullable Locale value)
    {
        return value != null ? value.toLanguageTag() : "und";
    }

    @TypeConverter
    public List<Long> fromIdsString(@Nullable String value)
    {
        // Pattern: "$id;$id;"

        List<Long> ids = new ArrayList<>();

        if(value == null || value.isEmpty()) return ids;
        String[] idsRaw = value.split(";");

        for(String idRaw : idsRaw)
        {
            if(idRaw == null || idRaw.isEmpty()) continue;

            try
            {
                ids.add(Long.parseLong(idRaw));
            }
            catch (NumberFormatException e)
            {
                Log.w("mediiva.converters", "Unable to parse long id: \""+idRaw+"\"");
            }
        }

        return ids;
    }

    @TypeConverter
    public String fromIds(@Nullable List<Long> ids)
    {
        if(ids == null) return "";
        StringBuilder output = new StringBuilder();

        for(Long id : ids)
        {
            output.append(id).append(";");
        }

        return output.toString();
    }

    @TypeConverter
    public List<UniqueId> fromUniqueIdsString(@Nullable String value)
    {
        // Pattern: $default1(0/1):$base64($type1):$base64($id1);$default2(0/1):$type2:id2;

        List<UniqueId> uniqueIds = new ArrayList<>();
        if(value == null || value.isEmpty()) return uniqueIds;

        String[] uniqueIdsRaw = value.split(";");

        for(String uniqueIdRaw : uniqueIdsRaw)
        {
            try
            {
                if(uniqueIdRaw == null || uniqueIdRaw.isEmpty()) continue;

                String[] valuesRaw = uniqueIdRaw.split(":");
                if(valuesRaw.length != 3)
                {
                    Log.w("mediiva.converters", "Invalid amount of uniqueId values found: "+uniqueIdRaw);
                    continue;
                }

                String defaultRaw = valuesRaw[0];
                String typeRaw = valuesRaw[1];
                String idRaw = valuesRaw[2];

                boolean isDefault = "1".equals(defaultRaw);
                String type = fromB64(typeRaw);
                String id = fromB64(idRaw);

                uniqueIds.add(new UniqueId(isDefault, type, id));
            }
            catch(Exception e)
            {
                Log.w("mediiva.converters", "Error reading uniqueId: "+uniqueIdRaw+"\t"+e.getLocalizedMessage());
            }
        }

        return uniqueIds;
    }

    @TypeConverter
    public String fromUniqueIds(@Nullable List<UniqueId> value)
    {
        if(value == null) return "";
        StringBuilder output = new StringBuilder();

        for(UniqueId uniqueId : value)
        {
            String defaultVal = uniqueId.isDefault() ? "1" : "0";
            String encodedType = toB64(uniqueId.getType());
            String encodedId = toB64(uniqueId.getId());

            output.append(defaultVal).append(":").append(encodedType).append(":").append(encodedId).append(";");
        }

        return output.toString();
    }

    @TypeConverter
    public List<Rating> fromRatingsString(@Nullable String value)
    {
        // Pattern: $default1(0/1):$rating1:$maxValue1;$default2(0/1):$rating2:$maxValue2;

        List<Rating> ratings = new ArrayList<>();
        if(value == null || value.isEmpty()) return ratings;

        String[] ratingsRaw = value.split(";");
        for(String ratingRaw : ratingsRaw)
        {
            String[] valuesRaw = ratingRaw.split(":");
            if(valuesRaw.length != 3)
            {
                Log.w("mediiva.converters", "Invalid amount of rating values found: "+ratingRaw);
                continue;
            }

            boolean isDefault = "1".equals(valuesRaw[0]);
            try
            {
                float ratingValue = Float.parseFloat(valuesRaw[1]);
                float maxRatingValue = Float.parseFloat(valuesRaw[2]);

                ratings.add(new Rating(isDefault, ratingValue, maxRatingValue));
            }
            catch (Exception e)
            {
                Log.w("mediiva.converters", "Mis-formatted rating values: "+ratingRaw);
            }
        }

        return ratings;
    }

    @TypeConverter
    public String fromRatings(@Nullable List<Rating> value)
    {
        if(value == null) return "";
        StringBuilder output = new StringBuilder();

        for(Rating rating : value)
        {
            String defaultVal = rating.isDefault() ? "1" : "0";
            String ratingVal = String.valueOf(rating.getRating());
            String maxVal = String.valueOf(rating.getMaxValue());

            output.append(defaultVal).append(":").append(ratingVal).append(":").append(maxVal).append(";");
        }

        return output.toString();
    }

    @TypeConverter
    public Uri fromUriString(@Nullable String value)
    {
        if(value == null) return null;
        return Uri.parse(value);
    }

    @TypeConverter
    public String fromUri(@Nullable Uri value)
    {
        if(value == null) return null;
        return value.toString();
    }

    @TypeConverter
    public List<Chapter> fromChaptersString(@Nullable String value)
    {
        // Pattern: $chapterNumber:$base64($customName):$base64($posterPath):$pageNumber,$base64($pagePath)-$page2Number,$base64($page2Path);$chapter2Number:$base64...;

        List<Chapter> chapters = new ArrayList<>();
        if(value == null || value.isEmpty()) return chapters;

        String[] chaptersRaw = value.split(";");

        for(String chapterRaw : chaptersRaw)
        {
            if(chapterRaw.isEmpty()) continue;

            String[] chapterArgs = chapterRaw.split(":");
            if(chapterArgs.length != 4)
            {
                Log.w("mediiva.converters", "Illegal chapter args amount found in database: "+value);
                continue;
            }

            String chapterNumberRaw = chapterArgs[0];
            String chapterCustomNameRaw = chapterArgs[1];
            String chapterPosterRaw = chapterArgs[2];
            String chapterPageDataRaw = chapterArgs[3];

            int chapterNumber;
            try
            {
                chapterNumber = Integer.parseInt(chapterNumberRaw);
            }
            catch (NumberFormatException e)
            {
                Log.w("mediiva.converters", "Illegal data in chapter database: Expected chapter number, got: "+chapterNumberRaw);
                continue;
            }

            String customName = null;
            if(!chapterCustomNameRaw.isEmpty())
            {
                customName = fromB64(chapterCustomNameRaw);
            }

            Page customPoster = null;
            if(!chapterPosterRaw.isEmpty())
            {
                String customPosterPath = fromB64(chapterPosterRaw);
                customPoster = new Page(0, customPosterPath);
            }

            Chapter chapter = new Chapter(chapterNumber, customPoster, customName);

            if(chapterPageDataRaw.isEmpty())
            {
                chapters.add(chapter);
                continue;
            }

            String[] pagesData = chapterPageDataRaw.split("-");

            for(String pageData : pagesData)
            {
                try
                {
                    if(pageData.isEmpty()) continue;

                    int pageNumber = Integer.parseInt(pageData.substring(0, pageData.indexOf(",")));
                    String pathRaw = pageData.substring(pageData.indexOf(",")+1);
                    String path = fromB64(pathRaw);

                    chapter.addPage(new Page(pageNumber, path));
                }
                catch (Exception e)
                {
                    Log.w("mediiva.converters", "Misformatted image data for chapter "+chapterNumber+":" +pageData);
                }
            }

            chapters.add(chapter);
        }

        return chapters;
    }

    @TypeConverter
    public String fromChapters(@Nullable List<Chapter> value)
    {
        if(value == null) return "";
        StringBuilder output = new StringBuilder();

        for(Chapter chapter : value)
        {
            output.append(chapter.getChapter()).append(":");

            if(chapter.getCustomName() != null)
            {
                output.append(toB64(chapter.getCustomName()));
            }
            output.append(":");
            if(chapter.getPoster() != null)
            {
                output.append(toB64(chapter.getPoster().getImagePath()));
            }
            output.append(":");

            for(Page page : chapter.getPages())
            {
                output.append(page.getPage()).append(",");
                String encodedPath = toB64(page.getImagePath());
                output.append(encodedPath).append("-");
            }

            output.append(";");
        }

        return output.toString();
    }

    @TypeConverter
    public Page fromPosterPath(@Nullable String value)
    {
        return value != null ? new Page(0, value) : null;
    }

    @TypeConverter
    public String fromPoster(@Nullable Page poster)
    {
        return poster != null ? poster.getImagePath() : null;
    }

    @TypeConverter
    public Map<Long, ActorInfo> fromActorInfoString(@Nullable String value)
    {
        // Pattern: $(actor1Id):$base64($(actor1Role)),$base64($(actor1Thumb));$(actor2Id)...;

        Map<Long, ActorInfo> actorInfoMap = new HashMap<>();
        if(value == null || value.isEmpty()) return actorInfoMap;

        String[] actorListRaw = value.split(";");
        if(actorListRaw.length == 0) return actorInfoMap;

        for(String actorRaw : actorListRaw)
        {
            if(actorRaw == null || actorRaw.isEmpty()) continue;

            long actorId;
            try
            {
                String idRaw = actorRaw.substring(0, actorRaw.indexOf(":"));
                actorId = Long.parseLong(idRaw);
            }
            catch (Exception e)
            {
                Log.e("mediiva.converters", "Error parsing id: "+actorRaw);
                continue;
            }

            try
            {
                String[] actorVars = actorRaw.substring(actorRaw.indexOf(":")+1).split(",");
                String actorRole = fromB64(actorVars[0]);

                Uri actorThumbUri = null;

                if(actorVars.length > 1)
                {
                    actorThumbUri = Uri.parse(fromB64(actorVars[1]));
                }

                ActorInfo actorInfo = new ActorInfo(actorRole, actorThumbUri);
                actorInfoMap.put(actorId, actorInfo);
            }
            catch (Exception e)
            {
                Log.e("mediiva.converters", "Error parsing actor info: "+actorRaw+" "+e.toString());
            }
        }

        return actorInfoMap;
    }

    @TypeConverter
    public String fromActorInfo(@Nullable Map<Long, ActorInfo> value)
    {
        if(value == null) return "";
        StringBuilder stringBuilder = new StringBuilder();

        for(Map.Entry<Long, ActorInfo> entry : value.entrySet())
        {
            String role = entry.getValue().getRole();
            Uri thumb = entry.getValue().getThumbUri();

            stringBuilder.append(entry.getKey()).append(":");
            if(role != null) stringBuilder.append(toB64(role));
            stringBuilder.append(",");
            if(thumb != null) stringBuilder.append(toB64(fromUri(thumb)));
            stringBuilder.append(";");
        }

        return stringBuilder.toString();
    }

    private String toB64(@NonNull String input)
    {
        return Base64.encodeToString(input.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
    }

    private String fromB64(@NonNull String input)
    {
        return new String(Base64.decode(input, Base64.DEFAULT), StandardCharsets.UTF_8);
    }
}
