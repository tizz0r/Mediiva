package de.timschubert.mediiva.data.movie;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.timschubert.mediiva.R;
import de.timschubert.mediiva.data.Rating;
import de.timschubert.mediiva.data.UniqueId;
import de.timschubert.mediiva.data.imageset.Page;
import de.timschubert.mediiva.data.tag.Tag;

@Entity(tableName = "movie")
public class Movie
{
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "libraryid")
    private final long libraryId;
    @ColumnInfo(name = "runtimeminutes")
    private final int runtimeMinutes;
    @ColumnInfo(name = "path")
    private final String path;
    @ColumnInfo(name = "moviepath")
    private final String moviePath;
    @ColumnInfo(name = "title")
    private final String title;
    @ColumnInfo(name = "originaltitle")
    private final String originalTitle;
    @ColumnInfo(name = "sorttitle")
    private final String sortTitle;
    @ColumnInfo(name = "tagline")
    private final String tagline;
    @ColumnInfo(name = "outline")
    private final String outline;
    @ColumnInfo(name = "plot")
    private final String plot;
    @ColumnInfo(name = "series")
    private final String series;
    @ColumnInfo(name = "director")
    private final String director;
    @ColumnInfo(name = "studio")
    private final String studio;
    @ColumnInfo(name = "premiered")
    private final Date premieredDate;
    @ColumnInfo(name = "trailer")
    private final Uri trailerUri;
    @ColumnInfo(name = "agerating")
    private final AgeRating ageRating;
    @ColumnInfo(name = "actorids")
    private final List<Long> actorIds;
    @ColumnInfo(name = "characterids")
    private final List<Long> characterIds;
    @ColumnInfo(name = "tags")
    private final List<Tag> tags;
    @ColumnInfo(name = "uniqueids")
    private final List<UniqueId> uniqueIds;
    @ColumnInfo(name = "ratings")
    private final List<Rating> ratings;
    @ColumnInfo(name = "actorinfo")
    private final Map<Long, ActorInfo> actorInfo;
    @ColumnInfo(name = "poster")
    private final Page poster;
    @ColumnInfo(name = "fanart")
    private final Page fanArt;
    @ColumnInfo(name = "locale")
    private final Locale locale;

    public Movie(long libraryId,
                 int runtimeMinutes,
                 String path,
                 String moviePath,
                 String title,
                 String originalTitle,
                 String sortTitle,
                 String tagline,
                 String outline,
                 String plot,
                 String series,
                 String director,
                 String studio,
                 Date premieredDate,
                 Uri trailerUri,
                 AgeRating ageRating,
                 List<Long> actorIds,
                 List<Long> characterIds,
                 List<Tag> tags,
                 List<UniqueId> uniqueIds,
                 List<Rating> ratings,
                 Map<Long, ActorInfo> actorInfo,
                 Page poster,
                 Page fanArt,
                 Locale locale)
    {
        this.libraryId = libraryId;
        this.runtimeMinutes = runtimeMinutes;
        this.path = path;
        this.moviePath = moviePath;
        this.title = title;
        this.originalTitle = originalTitle;
        this.sortTitle = sortTitle;
        this.tagline = tagline;
        this.outline = outline;
        this.plot = plot;
        this.series = series;
        this.director = director;
        this.studio = studio;
        this.premieredDate = premieredDate;
        this.trailerUri = trailerUri;
        this.ageRating = ageRating;
        this.actorIds = actorIds;
        this.characterIds = characterIds;
        this.tags = tags;
        this.uniqueIds = uniqueIds;
        this.ratings = ratings;
        this.actorInfo = actorInfo;
        this.poster = poster;
        this.fanArt = fanArt;
        this.locale = locale;
    }

    public void setId(long id) { this.id = id; }

    public long getId() { return id; }
    public long getLibraryId() { return libraryId; }
    public int getRuntimeMinutes() { return runtimeMinutes; }
    public String getPath() { return path; }
    public String getMoviePath() { return moviePath; }
    public String getTitle() { return title; }
    public String getOriginalTitle() { return originalTitle; }
    public String getSortTitle() { return sortTitle; }
    public String getTagline() { return tagline; }
    public String getOutline() { return outline; }
    public String getPlot() { return plot; }
    public String getSeries() { return series; }
    public String getDirector() { return director; }
    public String getStudio() { return studio; }
    public Date getPremieredDate() { return premieredDate; }
    public Uri getTrailerUri() { return trailerUri; }
    public AgeRating getAgeRating() { return ageRating; }
    public List<Long> getActorIds() { return actorIds; }
    public List<Long> getCharacterIds() { return characterIds; }
    public List<Tag> getTags() { return tags; }
    public List<UniqueId> getUniqueIds() { return uniqueIds; }
    public List<Rating> getRatings() { return ratings; }
    public Map<Long, ActorInfo> getActorInfo() { return actorInfo; }
    public Page getPoster() { return poster; }
    public Page getFanArt() { return fanArt; }
    public Locale getLocale() { return locale; }

    public enum AgeRating
    {
        UNKNOWN, XXX,
        MPA_G, MPA_PG, MPA_PG13, MPA_R, MPA_NC17,
        BBFC_U, BBFC_PG, BBFC_12, BBFC_15, BBFC_18, BBFC_R18,
        EIRIN_G, EIRIN_PG12, EIRIN_R15PLUS, EIRIN_R18PLUS,
        FSK_0, FSK_6, FSK_12, FSK_16, FSK_18
    }

    public static class Builder
    {
        @NonNull private final String path;
        @NonNull private final String moviePath;
        private final long libraryId;
        private int runtimeMinutes;
        @NonNull private String title;
        @Nullable private String originalTitle;
        @Nullable private String sortTitle;
        @Nullable private String tagLine;
        @Nullable private String outline;
        @Nullable private String plot;
        @Nullable private String series;
        @Nullable private String director;
        @Nullable private String studio;
        @NonNull private String languageCode;
        @NonNull private String countryCode;
        @Nullable private Date premieredDate;
        @Nullable private Uri trailerUri;
        @Nullable private Page poster;
        @Nullable private Page fanArt;
        @NonNull private AgeRating ageRating;
        @NonNull private final List<Long> actorIds;
        @NonNull private final List<Long> characterIds;
        @NonNull private final List<Tag> tags;
        @NonNull private final List<UniqueId> uniqueIds;
        @NonNull private final List<Rating> ratings;
        @NonNull private final Map<Long, ActorInfo> actorInfo;

        public Builder(@NonNull Context context,
                       @NonNull String path,
                       @NonNull String moviePath,
                       long libraryId)
        {
            this.path = path;
            this.moviePath = moviePath;
            this.libraryId = libraryId;
            runtimeMinutes = 0;
            title = context.getString(R.string.movie_default_name);
            languageCode = "";
            countryCode = "";
            ageRating = AgeRating.UNKNOWN;
            actorIds = new ArrayList<>();
            characterIds = new ArrayList<>();
            tags = new ArrayList<>();
            uniqueIds = new ArrayList<>();
            ratings = new ArrayList<>();
            actorInfo = new HashMap<>();
        }

        public Builder setRuntimeMinutes(int runtimeMinutes)
        {
            this.runtimeMinutes = runtimeMinutes;
            return this;
        }

        public Builder setTitle(String title)
        {
            this.title = title;
            return this;
        }

        public Builder setOriginalTitle(String originalTitle)
        {
            this.originalTitle = originalTitle;
            return this;
        }

        public Builder setSortTitle(String sortTitle)
        {
            this.sortTitle = sortTitle;
            return this;
        }

        public Builder setTagLine(String tagLine)
        {
            this.tagLine = tagLine;
            return this;
        }

        public Builder setOutline(String outline)
        {
            this.outline = outline;
            return this;
        }

        public Builder setPlot(String plot)
        {
            this.plot = plot;
            return this;
        }

        public Builder setSeries(String series)
        {
            this.series = series;
            return this;
        }

        public Builder setDirector(String director)
        {
            this.director = director;
            return this;
        }

        public Builder setStudio(String studio)
        {
            this.studio = studio;
            return this;
        }

        public Builder setLanguageCode(String languageCode)
        {
            this.languageCode = languageCode;
            return this;
        }

        public Builder setCountryCode(String countryCode)
        {
            this.countryCode = countryCode;
            return this;
        }

        public Builder setPremieredDate(Date premieredDate)
        {
            this.premieredDate = premieredDate;
            return this;
        }

        public Builder setTrailerUri(Uri trailerUri)
        {
            this.trailerUri = trailerUri;
            return this;
        }

        public Builder setPoster(@Nullable String posterPath)
        {
            poster = posterPath != null ? new Page(0, posterPath) : null;
            return this;
        }

        public Builder setFanArt(@Nullable String fanArtPath)
        {
            fanArt = fanArtPath != null ? new Page(0, fanArtPath) : null;
            return this;
        }

        public Builder setAgeRating(AgeRating ageRating)
        {
            this.ageRating = ageRating;
            return this;
        }

        public Builder addActor(long actorId, ActorInfo info)
        {
            actorIds.add(actorId);
            actorInfo.put(actorId, info);
            return this;
        }

        public Builder addCharacterId(long characterId)
        {
            characterIds.add(characterId);
            return this;
        }

        public Builder addTag(Tag tag)
        {
            tags.add(tag);
            return this;
        }

        public Builder addUniqueId(UniqueId uniqueId)
        {
            uniqueIds.add(uniqueId);
            return this;
        }

        public Builder addRating(Rating rating)
        {
            ratings.add(rating);
            return this;
        }

        public Movie build()
        {
            return new Movie(libraryId, runtimeMinutes, path, moviePath, title, originalTitle, sortTitle, tagLine,
                    outline, plot, series, director, studio, premieredDate, trailerUri, ageRating,
                    actorIds, characterIds, tags, uniqueIds, ratings, actorInfo, poster, fanArt,
                    new Locale(languageCode, countryCode));
        }
    }
}
