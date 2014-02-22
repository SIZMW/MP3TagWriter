package mp3tagwriter;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;

/**
 * This class is used for managing tagging operations on mp3 files. It handles
 * formatting, opening, closing, writing and reading tags from files.
 *
 * @author Aditya Nivarthi
 */
public class TagWriter {

    private static final Logger log = Logger.getLogger(TagWriter.class.getName());
    private ArrayList<FieldKey> tagsToClear;
    public final String multiSelect = "<multiple values>";
    public final String emptyString = "";

    /**
     * Constructor for TagWriter. Initializes tags to be saved when cleaning
     * files
     */
    public TagWriter() {
        this.initSavedTags();
    }

    /**
     * Converts given string to formatted camel case. Splits by " " and "."
     *
     * @param initString String to convert
     * @return ret.toString() The newly formatted string
     */
    private String toCamelCase(String initString) {
        if (initString == null) {
            return null;
        }
        StringBuilder ret = new StringBuilder(initString.length());
        int index = 0;
        for (String word : initString.split(" |[.]{1}")) {
            if (!word.isEmpty()) {
                ret.append(word.substring(0, 1).toUpperCase());
                ret.append((word.substring(1).toLowerCase()));
            }
            ret.append(initString.substring(initString.indexOf(word, index) + word.length(), (initString.indexOf(word, index) + word.length() + 1 >= initString.length() ? initString.length() : initString.indexOf(word, index) + word.length() + 1)));
            index += word.length() + 1;
        }
        System.out.println(ret.toString());
        return ret.toString();
    }

    /**
     * Initializes tags that should be saved when using cleanTags feature
     */
    private void initSavedTags() {
        tagsToClear = new ArrayList<>();
        tagsToClear.addAll(Arrays.asList(FieldKey.values()));
        tagsToClear.remove(FieldKey.ALBUM);
        tagsToClear.remove(FieldKey.ALBUM_ARTIST);
        tagsToClear.remove(FieldKey.ARTIST);
        tagsToClear.remove(FieldKey.COMPOSER);
        tagsToClear.remove(FieldKey.COVER_ART);
        tagsToClear.remove(FieldKey.GENRE);
        tagsToClear.remove(FieldKey.TITLE);
        tagsToClear.remove(FieldKey.TRACK);
        tagsToClear.remove(FieldKey.TRACK_TOTAL);
        tagsToClear.remove(FieldKey.YEAR);
    }

    /**
     * Gets featured artist from artist tag field
     *
     * @param tag The tag to analyze for featured artist
     * @return featuring String of artist(s) featured in song
     */
    public String getFeaturedArtist(Tag tag) {
        String fullArtist = tag.getFirst(FieldKey.ARTIST);
        String artist;
        String featuring;

        if (fullArtist.contains("Feat.")) {
            artist = fullArtist.substring(0, fullArtist.indexOf("Feat.") - 1);
            featuring = fullArtist.substring(artist.length() + " Feat. ".length(), fullArtist.length());
        } else {
            featuring = this.emptyString;
        }
        return featuring;
    }

    /**
     * Gets artist from artist tag field
     *
     * @param tag The tag to analyze for artist
     * @return artist String of artist of song
     */
    public String getArtist(Tag tag) {
        String fullArtist = tag.getFirst(FieldKey.ARTIST);
        String artist;

        if (fullArtist.contains("Feat.")) {
            artist = fullArtist.substring(0, fullArtist.indexOf("Feat.") - 1);
        } else {
            artist = fullArtist;
        }
        return artist;
    }

    /**
     * Determines if list of songs has a common album
     *
     * @param selectedIndices List of selected indexes in window
     * @param list List of songs in window
     * @return "" if nothing common, the common string if there is one
     */
    public String getSimilarAlbum(int[] selectedIndices, DefaultListModel list) {
        String album = "";
        try {
            Tag firstTag = ((MP3File) AudioFileIO.read(new File((String) list.get(selectedIndices[0])))).getID3v2Tag();
            album = firstTag.getFirst(FieldKey.ALBUM);
            for (int i : selectedIndices) {
                Tag tag = ((MP3File) AudioFileIO.read(new File((String) list.get(i)))).getID3v2Tag();
                if (!firstTag.getFirst(FieldKey.ALBUM).equals(tag.getFirst(FieldKey.ALBUM))) {
                    return this.multiSelect;
                }
            }
        } catch (IOException | CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | KeyNotFoundException | TagException e) {
        }
        return album;
    }

    /**
     * Determines if list of songs has a common album artwork
     *
     * @param selectedIndices List of selected indexes in window
     * @param list List of songs in window
     * @return null if nothing common, the common artwork if there is one
     */
    public Artwork getSimilarAlbumArt(int[] selectedIndices, DefaultListModel list) {
        Artwork album = null;
        try {
            Tag firstTag = ((MP3File) AudioFileIO.read(new File((String) list.get(selectedIndices[0])))).getID3v2Tag();
            album = firstTag.getFirstArtwork();
            for (int i : selectedIndices) {
                Tag tag = ((MP3File) AudioFileIO.read(new File((String) list.get(i)))).getID3v2Tag();
                if (!firstTag.getFirst(FieldKey.ALBUM).equals(tag.getFirst(FieldKey.ALBUM))) {
                    return null;
                }
            }
        } catch (IOException | CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | KeyNotFoundException | TagException e) {
        }
        return album;
    }

    /**
     * Determines if list of songs has a common album artist
     *
     * @param selectedIndices List of selected indexes in window
     * @param list List of songs in window
     * @return "" if nothing common, the common string if there is one
     */
    public String getSimilarAlbumArtist(int[] selectedIndices, DefaultListModel list) {
        String albumArtist = "";
        try {
            Tag firstTag = ((MP3File) AudioFileIO.read(new File((String) list.get(selectedIndices[0])))).getID3v2Tag();
            albumArtist = firstTag.getFirst(FieldKey.ALBUM_ARTIST);
            for (int i : selectedIndices) {
                Tag tag = ((MP3File) AudioFileIO.read(new File((String) list.get(i)))).getID3v2Tag();
                if (!firstTag.getFirst(FieldKey.ALBUM_ARTIST).equals(tag.getFirst(FieldKey.ALBUM_ARTIST))) {
                    return this.multiSelect;
                }
            }
        } catch (IOException | CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | KeyNotFoundException | TagException e) {
        }
        return albumArtist;
    }

    /**
     * Determines if list of songs has a common artist
     *
     * @param selectedIndices List of selected indexes in window
     * @param list List of songs in window
     * @return "" if nothing common, the common string if there is one
     */
    public String getSimilarArtist(int[] selectedIndices, DefaultListModel list) {
        String artist = "";
        try {
            Tag firstTag = ((MP3File) AudioFileIO.read(new File((String) list.get(selectedIndices[0])))).getID3v2Tag();
            artist = firstTag.getFirst(FieldKey.ARTIST);
            for (int i : selectedIndices) {
                Tag tag = ((MP3File) AudioFileIO.read(new File((String) list.get(i)))).getID3v2Tag();
                if (!firstTag.getFirst(FieldKey.ARTIST).equals(tag.getFirst(FieldKey.ARTIST))) {
                    System.out.println(firstTag.getFirst(FieldKey.ARTIST) + tag.getFirst(FieldKey.ARTIST));
                    return this.multiSelect;
                }
            }
        } catch (IOException | CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | KeyNotFoundException | TagException e) {
        }
        return artist;
    }

    /**
     * Determines if list of songs has a common composer
     *
     * @param selectedIndices List of selected indexes in window
     * @param list List of songs in window
     * @return "" if nothing common, the common string if there is one
     */
    public String getSimilarComposer(int[] selectedIndices, DefaultListModel list) {
        String composer = "";
        try {
            Tag firstTag = ((MP3File) AudioFileIO.read(new File((String) list.get(selectedIndices[0])))).getID3v2Tag();
            composer = firstTag.getFirst(FieldKey.COMPOSER);
            for (int i : selectedIndices) {
                Tag tag = ((MP3File) AudioFileIO.read(new File((String) list.get(i)))).getID3v2Tag();
                if (!firstTag.getFirst(FieldKey.COMPOSER).equals(tag.getFirst(FieldKey.COMPOSER))) {
                    return this.multiSelect;
                }
            }
        } catch (IOException | CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | KeyNotFoundException | TagException e) {
        }
        return composer;
    }

    /**
     * Determines if list of songs has a common genre
     *
     * @param selectedIndices List of selected indexes in window
     * @param list List of songs in window
     * @return "" if nothing common, the common string if there is one
     */
    public String getSimilarGenre(int[] selectedIndices, DefaultListModel list) {
        String genre = "";
        try {
            Tag firstTag = ((MP3File) AudioFileIO.read(new File((String) list.get(selectedIndices[0])))).getID3v2Tag();
            genre = firstTag.getFirst(FieldKey.GENRE);
            for (int i : selectedIndices) {
                Tag tag = ((MP3File) AudioFileIO.read(new File((String) list.get(i)))).getID3v2Tag();
                if (!firstTag.getFirst(FieldKey.GENRE).equals(tag.getFirst(FieldKey.GENRE))) {
                    return this.multiSelect;
                }
            }
        } catch (IOException | CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | KeyNotFoundException | TagException e) {
        }
        return genre;
    }

    /**
     * Determines if list of songs has a common year
     *
     * @param selectedIndices List of selected indexes in window
     * @param list List of songs in window
     * @return "" if nothing common, the common string if there is one
     */
    public String getSimilarYear(int[] selectedIndices, DefaultListModel list) {
        String year = "";
        try {
            Tag firstTag = ((MP3File) AudioFileIO.read(new File((String) list.get(selectedIndices[0])))).getID3v2Tag();
            year = firstTag.getFirst(FieldKey.YEAR);
            for (int i : selectedIndices) {
                Tag tag = ((MP3File) AudioFileIO.read(new File((String) list.get(i)))).getID3v2Tag();
                if (!firstTag.getFirst(FieldKey.YEAR).equals(tag.getFirst(FieldKey.YEAR))) {
                    return this.multiSelect;
                }
            }
        } catch (IOException | CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | KeyNotFoundException | TagException e) {
        }
        return year;
    }

    /**
     * Gets name of song file and writes it to "title" tag field
     *
     * @param songName Name of song to analyze
     */
    public synchronized void titleFromFileName(String songName) {
        try {
            File songFile = new File(songName);
            MP3File song = (MP3File) AudioFileIO.read(songFile);
            Tag tag = song.getID3v2Tag();
            String temp = songFile.getAbsolutePath().substring(songFile.getAbsolutePath().lastIndexOf("\\") + 1);
            temp = temp.substring(0, temp.length() - 4);
            tag.setField(FieldKey.TITLE, temp);
            song.commit();
        } catch (IOException | CannotReadException | CannotWriteException | InvalidAudioFrameException | ReadOnlyFileException | TagException e) {
        }
    }

    /**
     * Gets name of each song in list and writes name to "title tag field
     *
     * @param selectedIndices List of selected indexes in window that should be
     * renamed
     * @param list List of all songs in window
     */
    public void titleFromFileNameMultipleFiles(int[] selectedIndices, DefaultListModel list) {
        for (int i : selectedIndices) {
            this.titleFromFileName((String) list.get(i));
        }
    }

    /**
     * Adds selected artwork file to "album artwork" tag field
     *
     * @param songName Name of song to analyze
     * @param artwork Artwork file to write as album artwork
     */
    public synchronized void addAlbumArt(String songName, File artwork) {
        try {
            File songFile = new File(songName);
            MP3File song = (MP3File) AudioFileIO.read(songFile);
            Tag tag = song.getID3v2Tag();
            Artwork art = new Artwork();
            art.setFromFile(artwork);
            tag.deleteArtworkField();
            tag.setField(art);
            song.commit();
        } catch (IOException | CannotReadException | CannotWriteException | InvalidAudioFrameException | ReadOnlyFileException | KeyNotFoundException | TagException e) {
        }
    }

    /**
     * Adds selected artwork file to each song in list to "album artwork" tag
     * field
     *
     * @param selectedIndices List of selected indexes in window
     * @param list List of all songs in window
     * @param artwork Artwork file to write as album artwork
     */
    public void addAlbumArtMultipleFiles(int[] selectedIndices, DefaultListModel list, File artwork) {
        for (int i : selectedIndices) {
            this.addAlbumArt((String) list.get(i), artwork);
        }
    }

    /**
     * Cleans excess tag fields from file. Maintains tags not defined in
     * tagsToClear
     *
     * @param songName Name of song to analyze
     */
    public synchronized void cleanFileTags(String songName) {
        try {
            File songFile = new File(songName);
            MP3File song = (MP3File) AudioFileIO.read(songFile);
            Tag tag = song.getID3v2Tag();
            for (FieldKey e : tagsToClear) {
                System.out.println(tag.getFirst(FieldKey.TRACK + "hi " + e.name()));
                tag.deleteField(e);
            }
            song.commit();
        } catch (HeadlessException | IOException | CannotReadException | CannotWriteException | InvalidAudioFrameException | ReadOnlyFileException | KeyNotFoundException | TagException e) {
        }
    }

    /**
     * Cleans excess tag fields from list of files. Maintains tags not defined
     * in tagsToClear
     *
     * @param selectedIndices List of selected indexes in window
     * @param list List of all songs in window
     */
    public synchronized void cleanFileTagsMultipleFiles(int[] selectedIndices, DefaultListModel list) {
        try {
            for (int i : selectedIndices) {
                this.cleanFileTags((String) list.get(i));
            }
        } catch (Exception e) {

        }
    }

    /**
     * Changes tags to new values passed as parameters and writes to tag fields
     *
     * @param songName Name of song file
     * @param album Album
     * @param albumArtist Album artist
     * @param artist Main artist
     * @param composer Composer
     * @param featuring Featured artist(s)
     * @param genre Genre
     * @param title Title of song
     * @param track Track number
     * @param year Year of release
     */
    public synchronized void changeTags(String songName, String album, String albumArtist, String artist, String composer, String featuring, String genre, String title, String track, String year) {
        try {
            File songFile = new File(songName);
            MP3File song = (MP3File) AudioFileIO.read(songFile);
            Tag tag = song.getID3v2Tag();

            if (!album.equals(multiSelect)) {
                tag.setField(FieldKey.ALBUM, album);
            }
            if (!albumArtist.equals(multiSelect)) {
                tag.setField(FieldKey.ALBUM_ARTIST, albumArtist);
            }
            if (!composer.equals(multiSelect)) {
                tag.setField(FieldKey.COMPOSER, composer);
            }
            if (featuring.equals(multiSelect) || featuring.equals(emptyString)) {
                if (!artist.equals(multiSelect)) {
                    tag.setField(FieldKey.ARTIST, artist);
                }
            } else {
                if (!artist.equals(multiSelect) && !featuring.equals(multiSelect) && !featuring.equals(emptyString)) {
                    tag.setField(FieldKey.ARTIST, artist + " Feat. " + featuring);
                }
            }
            if (!genre.equals(multiSelect)) {
                tag.setField(FieldKey.GENRE, genre);
            }
            if (!title.equals(multiSelect)) {
                tag.setField(FieldKey.TITLE, title);
            }
            if (!track.equals(multiSelect)) {
                if (Integer.parseInt(track) < 10) {
                    tag.setField(FieldKey.TRACK, "00" + track);
                } else {
                    tag.setField(FieldKey.TRACK, track);
                }
            }
            if (!year.equals(multiSelect)) {
                tag.setField(FieldKey.YEAR, year);
            }
            song.commit();
        } catch (HeadlessException | IOException | CannotReadException | CannotWriteException | InvalidAudioFrameException | ReadOnlyFileException | KeyNotFoundException | TagException e) {
        }
    }

    /**
     * Changes tags to new values passed as parameters for each file in song
     * list
     *
     * @param selectedIndices List of selected indexes in window
     * @param list List of all songs in window
     * @param album Album
     * @param albumArtist Album artist
     * @param artist Main artist
     * @param composer Composer
     * @param featuring Featured artist(s)
     * @param genre Genre
     * @param title Title of song
     * @param track Track number
     * @param year Year of release
     */
    public void changeMultipleTags(int[] selectedIndices, DefaultListModel list, String album, String albumArtist, String artist, String composer, String featuring, String genre, String title, String track, String year) {
        try {
            for (int i : selectedIndices) {
                this.changeTags((String) list.get(i), album, albumArtist, artist, composer, featuring, genre, title, track, year);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Converts all tags of given song to proper camel case
     *
     * @param songName Name of song file
     * @param album Album
     * @param albumArtist Album artist
     * @param artist Main artist
     * @param composer Composer
     * @param featuring Featured artist(s)
     * @param genre Genre
     * @param title Title of song
     * @param track Track number
     * @param year Year of release
     */
    public void fixTextCase(String songName, String album, String albumArtist, String artist, String composer, String featuring, String genre, String title, String track, String year) {
        try {
            this.changeTags(songName, this.toCamelCase(album), this.toCamelCase(albumArtist), this.toCamelCase(artist), this.toCamelCase(composer), this.toCamelCase(featuring), this.toCamelCase(genre), this.toCamelCase(title), this.toCamelCase(track), this.toCamelCase(year));
        } catch (Exception e) {
        }
    }

    /**
     * Converts all tags of all given songs to proper camel case
     *
     * @param selectedIndices List of indexes selected in window that should be
     * converted
     * @param list List of all songs in window
     */
    public void fixTextCaseMultipleFiles(int[] selectedIndices, DefaultListModel list) {
        try {
            for (int i : selectedIndices) {
                Tag tag = ((MP3File) AudioFileIO.read(new File((String) list.get(i)))).getID3v2Tag();
                this.fixTextCase((String) list.get(i), tag.getFirst(FieldKey.ALBUM), tag.getFirst(FieldKey.ALBUM_ARTIST), this.getArtist(tag), tag.getFirst(FieldKey.COMPOSER), this.getFeaturedArtist(tag), tag.getFirst(FieldKey.GENRE), tag.getFirst(FieldKey.TITLE), tag.getFirst(FieldKey.TRACK), tag.getFirst(FieldKey.YEAR));
            }
        } catch (IOException | CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | KeyNotFoundException | TagException e) {
        }
    }

    /**
     * Copies tags from the given file to a specified file
     *
     * @param fromFile File to copy from
     * @param toFile File to copy to
     */
    public void copyTagInfo(String fromFile, String toFile) {
        try {
            Tag fromTag = ((MP3File) AudioFileIO.read(new File(fromFile))).getID3v2Tag();
            this.changeTags(toFile, fromTag.getFirst(FieldKey.ALBUM), fromTag.getFirst(FieldKey.ALBUM_ARTIST), fromTag.getFirst(FieldKey.ARTIST), fromTag.getFirst(FieldKey.COMPOSER), "", fromTag.getFirst(FieldKey.GENRE), fromTag.getFirst(FieldKey.TITLE), fromTag.getFirst(FieldKey.TRACK), fromTag.getFirst(FieldKey.YEAR));
        } catch (IOException | CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | KeyNotFoundException | TagException e) {
        }
    }
}
