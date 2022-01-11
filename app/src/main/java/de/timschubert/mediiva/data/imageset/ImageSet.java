package de.timschubert.mediiva.data.imageset;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.timschubert.mediiva.R;
import de.timschubert.mediiva.data.Rating;
import de.timschubert.mediiva.data.UniqueId;
import de.timschubert.mediiva.data.tag.Tag;
import de.timschubert.mediiva.exception.NoChapterAvailableException;

@Entity(tableName = "imageset")
public class ImageSet
{
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name="libraryid")
    private final long libraryId;
    @ColumnInfo(name="path")
    private final String path;
    @ColumnInfo(name = "title")
    private final String title;
    @ColumnInfo(name = "originaltitle")
    private final String originalTitle;
    @ColumnInfo(name = "sorttitle")
    private final String sortTitle;
    @ColumnInfo(name = "type")
    private final Type type;
    @ColumnInfo(name = "status")
    private final Status status;
    @ColumnInfo(name = "outline")
    private final String outline;
    @ColumnInfo(name = "plot")
    private final String plot;
    @ColumnInfo(name = "premiered")
    private final Date premieredDate;
    @ColumnInfo(name = "series")
    private final String series;
    @ColumnInfo(name = "group")
    private final String group;
    @ColumnInfo(name = "locale")
    private final Locale locale;
    @ColumnInfo(name = "poster")
    private final Page poster;
    @ColumnInfo(name = "artistids")
    private final List<Long> artistIds;
    @ColumnInfo(name = "characterids")
    private final List<Long> characterIds;
    @ColumnInfo(name = "uniqueids")
    private final List<UniqueId> uniqueIds;
    @ColumnInfo(name = "tags")
    private final List<Tag> tags;
    @ColumnInfo(name = "ratings")
    private final List<Rating> ratings;
    @ColumnInfo(name = "imagedata")
    private List<Chapter> chapters;

    public ImageSet(long libraryId,
                    String path,
                    String title,
                    String originalTitle,
                    String sortTitle,
                    Type type,
                    Status status,
                    String outline,
                    String plot,
                    Date premieredDate,
                    String series,
                    String group,
                    Locale locale,
                    Page poster,
                    List<Long> artistIds,
                    List<Long> characterIds,
                    List<UniqueId> uniqueIds,
                    List<Tag> tags,
                    List<Rating> ratings)
    {
        this.libraryId = libraryId;
        this.path = path;
        this.title = title;
        this.originalTitle = originalTitle;
        this.sortTitle = sortTitle;
        this.type = type;
        this.status = status;
        this.outline = outline;
        this.plot = plot;
        this.premieredDate = premieredDate;
        this.series = series;
        this.group = group;
        this.locale = locale;
        this.poster = poster;
        this.artistIds = artistIds;
        this.characterIds = characterIds;
        this.uniqueIds = uniqueIds;
        this.tags = tags;
        this.ratings = ratings;
    }

    public void setId(long id) { this.id = id; }
    public void setChapters(List<Chapter> chapters)
    {
        this.chapters = chapters;
        this.chapters.sort(Comparator.comparingInt(Chapter::getChapter));
    }

    @NonNull public Chapter getFirstChapter() throws NoChapterAvailableException
    {
        if(chapters == null || chapters.isEmpty()) throw new NoChapterAvailableException();
        return chapters.get(0);
    }

    @NonNull public Chapter getNextChapter(Chapter chapter) throws NoChapterAvailableException
    {
        if(!chapters.contains(chapter)) throw new NoChapterAvailableException();
        if(chapters.size() > chapters.indexOf(chapter) + 1) return chapters.get(chapters.indexOf(chapter)+1);
        throw new NoChapterAvailableException();
    }

    @NonNull public Chapter getPreviousChapter(Chapter chapter) throws NoChapterAvailableException
    {
        if(!chapters.contains(chapter)) throw new NoChapterAvailableException();
        if(chapters.indexOf(chapter) > 0) return chapters.get(chapters.indexOf(chapter)-1);
        throw new NoChapterAvailableException();
    }

    @NonNull public Chapter getChapter(int chapter) throws NoChapterAvailableException
    {
        for(Chapter imageChapter : chapters)
        {
            if(imageChapter.getChapter() == chapter) return imageChapter;
        }

        throw new NoChapterAvailableException();
    }

    public long getId() { return id; }
    public long getLibraryId() { return libraryId; }
    public String getPath() { return path; }
    public String getTitle() { return title; }
    public String getOriginalTitle() { return originalTitle; }
    public String getSortTitle() { return sortTitle; }
    public Type getType() { return type; }
    public Status getStatus() { return status; }
    public String getOutline() { return outline; }
    public String getPlot() { return plot; }
    public Date getPremieredDate() { return premieredDate; }
    public String getSeries() { return series; }
    public String getGroup() { return group; }
    public Locale getLocale() { return locale; }
    public Page getPoster() { return poster; }
    public List<Long> getArtistIds() { return artistIds; }
    public List<Long> getCharacterIds() { return characterIds; }
    public List<UniqueId> getUniqueIds() { return uniqueIds; }
    public List<Tag> getTags() { return tags; }
    public List<Rating> getRatings() { return ratings; }
    public List<Chapter> getChapters() { return chapters; }

    public enum Type { IMAGESET, COMIC, DOUJINSHI, MANGA, ARTISTCG, GAMECG }
    public enum Status { COMPLETED, ABANDONED, WIP }

    public static class Builder
    {
        private final long libraryId;
        @NonNull private final String path;
        @NonNull private String title;
        @Nullable private String originalTitle;
        @Nullable private String sortTitle;
        @NonNull private Type type;
        @NonNull private Status status;
        @Nullable private String outline;
        @Nullable private String plot;
        @Nullable private Date premieredDate;
        @Nullable private String series;
        @Nullable private String group;
        @NonNull private String language;
        @NonNull private String country;
        @Nullable private Page poster;
        @NonNull private final List<Long> artistIds;
        @NonNull private final List<Long> characterIds;
        @NonNull private final List<UniqueId> uniqueIds;
        @NonNull private final List<Tag> tags;
        @NonNull private final List<Rating> ratings;

        public Builder(@NonNull Context context,
                       @NonNull String path,
                       long libraryId)
        {
            this.libraryId = libraryId;
            this.path = path;
            title = context.getString(R.string.imageset_default_name);
            type = Type.IMAGESET;
            status = Status.COMPLETED;
            language = "";
            country = "";
            artistIds = new ArrayList<>();
            characterIds = new ArrayList<>();
            uniqueIds = new ArrayList<>();
            tags = new ArrayList<>();
            ratings = new ArrayList<>();
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

        public Builder setGroup(String group)
        {
            this.group = group;
            return this;
        }

        public Builder setPremieredDate(Date date)
        {
            this.premieredDate = date;
            return this;
        }

        public Builder setType(Type type)
        {
            this.type = type;
            return this;
        }

        public Builder setStatus(Status status)
        {
            this.status = status;
            return this;
        }

        public Builder setLanguage(String language)
        {
            this.language = language;
            return this;
        }

        public Builder setCountry(String country)
        {
            this.country = country;
            return this;
        }

        public Builder setPoster(@Nullable String posterPath)
        {
            this.poster = posterPath != null ? new Page(0, posterPath) : null;
            return this;
        }

        public Builder addArtistId(long artistId)
        {
            artistIds.add(artistId);
            return this;
        }

        public Builder addCharacterId(long characterId)
        {
            characterIds.add(characterId);
            return this;
        }

        public Builder addUniqueId(@NonNull UniqueId uniqueId)
        {
            uniqueIds.add(uniqueId);
            return this;
        }

        public Builder addTag(@NonNull Tag tag)
        {
            tags.add(tag);
            return this;
        }

        public Builder addRating(@NonNull Rating rating)
        {
            ratings.add(rating);
            return this;
        }

        public ImageSet build()
        {
            return new ImageSet(libraryId,
                    path,
                    title,
                    originalTitle,
                    sortTitle,
                    type,
                    status,
                    outline,
                    plot,
                    premieredDate,
                    series,
                    group,
                    new Locale(language, country),
                    poster,
                    artistIds,
                    characterIds,
                    uniqueIds,
                    tags,
                    ratings);
        }
    }
}
