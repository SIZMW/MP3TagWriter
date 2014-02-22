package mp3tagwriter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;

/**
 * This class is used to manage the relationship between the TagWriter
 * functionality and the TagWindow GUI. It handles threading, event management,
 * and GUI views and information.
 *
 * @author Aditya Nivarthi
 */
public class TagDriver {

    private TagWindow window;
    private TagWriter writer;

    /**
     * Main driver. Creates a new driver object.
     *
     * @param args Program arguments. None are used specifically
     */
    public static void main(String[] args) {
        TagDriver tagDriver;
        tagDriver = new TagDriver();
    }

    /**
     * Constructor for TagDriver object. Creates a TagWriter, sets the interface
     * looks, and handles the action listeners.
     */
    public TagDriver() {
        this.writer = new TagWriter();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
        }

        this.window = new TagWindow();
        this.window.setVisible(true);
        this.setAllWindowFields(true);
        final JFileChooser chooser = new JFileChooser();
        window.FileListViewer.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        /**
         * Action listener on the "Choose Files" button.
         */
        this.window.ChooseFilesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooser.setMultiSelectionEnabled(true);
                if (chooser.showOpenDialog(window.FileLabel) == JFileChooser.APPROVE_OPTION) {
                    new Thread() {
                        @Override
                        public void run() {
                            for (File f : chooser.getSelectedFiles()) {
                                DefaultListModel list = (DefaultListModel) window.FileListViewer.getModel();
                                if (!list.contains(f.getAbsolutePath())) {
                                    list.addElement(f.getAbsolutePath());
                                }
                            }
                            window.FileListViewer.repaint();
                        }
                    }.start();
                }
            }
        });

        /**
         * List selection listener on the "File List Viewer".
         */
        this.window.FileListViewer.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        DefaultListModel list = (DefaultListModel) window.FileListViewer.getModel();
                        if (window.FileListViewer.getSelectedIndices().length > 1) {
                            fillInfoOnLoadMultipleFiles(writer.multiSelect);
                        } else if (window.FileListViewer.getSelectedIndices().length == 1) {
                            fillInfoOnLoad((String) list.get(window.FileListViewer.getSelectedIndices()[0]));
                        }
                    }
                }.start();
            }
        });

        /**
         * Action listener on the "Apply" button.
         */
        this.window.ApplyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        if (window.FileListViewer.getSelectedIndices().length == 1) {
                            writer.changeTags(window.FilePathTextField.getText(), window.AlbumTextField.getText(), window.AlbumArtistTextField.getText(), window.ArtistTextField.getText(), window.ComposerTextField.getText(), window.FeaturingTextField.getText(), window.GenreTextField.getText(), window.TitleTextField.getText(), window.TrackNumberTextField.getText(), window.YearTextField.getText());
                            fillInfoOnLoad(window.FilePathTextField.getText());
                            JOptionPane.showMessageDialog(null, "File saved!");
                        } else if (window.FileListViewer.getSelectedIndices().length > 1) {
                            writer.changeMultipleTags(window.FileListViewer.getSelectedIndices(), (DefaultListModel) window.FileListViewer.getModel(), window.AlbumTextField.getText(), window.AlbumArtistTextField.getText(), window.ArtistTextField.getText(), window.ComposerTextField.getText(), window.FeaturingTextField.getText(), window.GenreTextField.getText(), window.TitleTextField.getText(), window.TrackNumberTextField.getText(), window.YearTextField.getText());
                            fillInfoOnLoadMultipleFiles(writer.multiSelect);
                            JOptionPane.showMessageDialog(null, "Files saved!");
                        }
                    }
                }.start();
            }
        });

        /**
         * Action listener on the "Remove File" button.
         */
        this.window.RemoveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        DefaultListModel list = (DefaultListModel) window.FileListViewer.getModel();
                        if (list.size() > 0) {
                            clearFileListInfo(writer.emptyString);
                        }
                    }
                }.start();
            }
        });

        /**
         * Action listener on the "Fix Text Case" button.
         */
        this.window.TextCaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        if (window.FileListViewer.getSelectedIndices().length == 1) {
                            writer.fixTextCase(window.FilePathTextField.getText(), window.AlbumTextField.getText(), window.AlbumArtistTextField.getText(), window.ArtistTextField.getText(), window.ComposerTextField.getText(), window.FeaturingTextField.getText(), window.GenreTextField.getText(), window.TitleTextField.getText(), window.TrackNumberTextField.getText(), window.YearTextField.getText());
                            fillInfoOnLoad(window.FilePathTextField.getText());
                            JOptionPane.showMessageDialog(null, "File saved!");
                        } else if (window.FileListViewer.getSelectedIndices().length > 1) {
                            writer.fixTextCaseMultipleFiles(window.FileListViewer.getSelectedIndices(), (DefaultListModel) window.FileListViewer.getModel());
                            fillInfoOnLoad(writer.multiSelect);
                            JOptionPane.showMessageDialog(null, "Files saved!");
                        }
                    }
                }.start();
            }
        });

        /**
         * Action listener on the "Title From File Name" button.
         */
        this.window.TitleFromFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        if (window.FileListViewer.getSelectedIndices().length == 1) {
                            writer.titleFromFileName(window.FilePathTextField.getText());
                            fillInfoOnLoad(window.FilePathTextField.getText());
                            JOptionPane.showMessageDialog(null, "File saved!");
                        } else if (window.FileListViewer.getSelectedIndices().length > 1) {
                            writer.titleFromFileNameMultipleFiles(window.FileListViewer.getSelectedIndices(), (DefaultListModel) window.FileListViewer.getModel());
                            fillInfoOnLoadMultipleFiles(writer.multiSelect);
                            JOptionPane.showMessageDialog(null, "Files saved!");
                        }
                    }
                }.start();
            }
        });

        /**
         * Action listener on the "Add Album Art" button.
         */
        this.window.AlbumArtButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chooser.showOpenDialog(window.FileLabel) == JFileChooser.APPROVE_OPTION) {
                    new Thread() {
                        @Override
                        public void run() {
                            DefaultListModel list = (DefaultListModel) window.FileListViewer.getModel();
                            if (!window.FilePathTextField.getText().equals(writer.emptyString) && window.FileListViewer.getSelectedIndices().length == 1) {
                                File f = chooser.getSelectedFile();
                                writer.addAlbumArt(window.FilePathTextField.getText(), f);
                                fillInfoOnLoad((String) list.get(window.FileListViewer.getSelectedIndices()[0]));
                                JOptionPane.showMessageDialog(null, "File saved!");
                            } else if (window.FileListViewer.getSelectedIndices().length > 1) {
                                File f = chooser.getSelectedFile();
                                writer.addAlbumArtMultipleFiles(window.FileListViewer.getSelectedIndices(), list, f);
                                fillInfoOnLoadMultipleFiles(writer.multiSelect);
                                JOptionPane.showMessageDialog(null, "Files saved!");
                            }
                        }
                    }.start();
                }
            }
        });

        /**
         * Action listener on the "Clean Extra Tags" button
         */
        this.window.CleanTagsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        if (window.FileListViewer.getSelectedIndices().length == 1) {
                            writer.cleanFileTags(window.FilePathTextField.getText());
                            fillInfoOnLoad(window.FilePathTextField.getText());
                            JOptionPane.showMessageDialog(null, "File saved!");
                        } else if (window.FileListViewer.getSelectedIndices().length > 1) {
                            writer.cleanFileTagsMultipleFiles(window.FileListViewer.getSelectedIndices(), (DefaultListModel) window.FileListViewer.getModel());
                            JOptionPane.showMessageDialog(null, "Files saved!");
                        }
                    }
                }
                        .start();
            }
        }
        );

        /**
         * Action listener on the "Choose File To Copy From" button.
         */
        this.window.ChooseCopyFromFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooser.setMultiSelectionEnabled(false);
                if (chooser.showOpenDialog(window.FileLabel) == JFileChooser.APPROVE_OPTION) {
                    new Thread() {
                        @Override
                        public void run() {
                            File from = chooser.getSelectedFile();
                            window.CopyFromTextField.setText(from.getAbsolutePath());
                        }
                    }.start();
                }
            }
        });

        /**
         * Action listener on the "Choose File To Copy To" button.
         */
        this.window.ChooseCopyToFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooser.setMultiSelectionEnabled(false);
                if (chooser.showOpenDialog(window.FileLabel) == JFileChooser.APPROVE_OPTION) {
                    new Thread() {
                        @Override
                        public void run() {
                            File to = chooser.getSelectedFile();
                            window.CopyToTextField.setText(to.getAbsolutePath());
                        }
                    }.start();
                }
            }
        });

        /**
         * Action listener on the "Clear Copy Screen" button.
         */
        this.window.ClearCopyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        window.CopyFromTextField.setText(writer.emptyString);
                        window.CopyToTextField.setText(writer.emptyString);
                    }
                }.start();
            }
        });

        /**
         * Action listener on the "Copy Tag" button.
         */
        this.window.CopyTagButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        writer.copyTagInfo(window.CopyFromTextField.getText(), window.CopyToTextField.getText());
                    }
                }.start();
            }
        });

    }

    /**
     * Sets all the window fields to editable or not, based on given condition.
     *
     * @param set Boolean stating if fields are editable or not
     */
    private void setAllWindowFields(boolean set) {
        window.FilePathTextField.setEditable(set);
        window.TitleTextField.setEditable(set);
        window.ArtistTextField.setEditable(set);
        window.FeaturingTextField.setEditable(set);
        window.AlbumTextField.setEditable(set);
        window.FileNameTextField.setEditable(set);
        window.AlbumArtistTextField.setEditable(set);
        window.ComposerTextField.setEditable(set);
        window.TrackNumberTextField.setEditable(set);
        window.GenreTextField.setEditable(set);
        window.YearTextField.setEditable(set);
    }

    /**
     * Clears the file list when multiple files are selected to be removed.
     *
     * @param filler The string to use to fill the empty text fields after
     * removing selected files
     */
    public synchronized void clearFileListInfo(String filler) {
        try {
            DefaultListModel list = (DefaultListModel) window.FileListViewer.getModel();
            ArrayList<Object> objs = new ArrayList<>();

            for (int i : window.FileListViewer.getSelectedIndices()) {
                objs.add(list.get(i));
            }

            for (Object obj : objs) {
                list.removeElement(obj);
            }

            clearFieldInfo(filler);
            this.removeArtwork();
            window.repaint();

        } catch (Exception e) {
        }
    }

    /**
     * Clears text fields with a filler when multiple files are selected. Used
     * to clear text before updating with new tag information.
     *
     * @param filler The filler text to insert
     */
    public synchronized void clearMultipleFieldInfos(String filler) {
        window.FileNameTextField.setText(filler);
        window.FilePathTextField.setText(filler);
        window.TitleTextField.setText(filler);
        window.FeaturingTextField.setText(filler);
        window.TrackNumberTextField.setText(filler);
    }

    /**
     * Clears text fields with a filler when files are selected. Used to clear
     * text before updating with new tag information.
     *
     * @param filler The filler text to insert
     */
    public synchronized void clearFieldInfo(String filler) {
        window.FileNameTextField.setText(filler);
        window.FilePathTextField.setText(filler);
        window.TitleTextField.setText(filler);
        window.ArtistTextField.setText(filler);
        window.FeaturingTextField.setText(filler);
        window.AlbumArtistTextField.setText(filler);
        window.AlbumTextField.setText(filler);
        window.ComposerTextField.setText(filler);
        window.YearTextField.setText(filler);
        window.GenreTextField.setText(filler);
        window.TrackNumberTextField.setText(filler);
        this.removeArtwork();
    }

    /**
     * Paints album artwork in the corresponding panel.
     *
     * @param art The artwork to paint
     */
    public synchronized void paintArtwork(Artwork art) {
        try {
            if (art == null) {
                this.removeArtwork();
            } else {
                BufferedImage albumArt = art.getImage();
                PictureView pView = new PictureView(albumArt);
                pView.setSize(window.AlbumArtPane.getWidth(), window.AlbumArtPane.getHeight());
                window.AlbumArtPane.add(pView);
            }
            window.AlbumArtPane.repaint();
            window.repaint();
        } catch (IOException e) {
        }
    }

    /**
     * Remove the artwork from the panel.
     */
    public synchronized void removeArtwork() {
        this.window.AlbumArtPane.removeAll();
    }

    /**
     * Fills the tag information from a selected file.
     *
     * @param songName The name of the song file
     */
    public synchronized void fillInfoOnLoad(String songName) {
        try {
            this.setAllWindowFields(true);
            File songFile = new File(songName);
            MP3File song = (MP3File) AudioFileIO.read(songFile);
            Tag tag = song.getID3v2Tag();

            window.FilePathTextField.setText(songFile.getAbsolutePath());
            window.FileNameTextField.setText(songFile.getAbsolutePath().substring(songFile.getAbsolutePath().lastIndexOf("\\") + 1));
            window.TitleTextField.setText(tag.getFirst(FieldKey.TITLE));
            this.removeArtwork();

            window.ArtistTextField.setText(writer.getArtist(tag));
            window.FeaturingTextField.setText(writer.getFeaturedArtist(tag));
            window.AlbumTextField.setText(tag.getFirst(FieldKey.ALBUM));
            window.AlbumArtistTextField.setText(tag.getFirst(FieldKey.ALBUM_ARTIST));
            window.ComposerTextField.setText(tag.getFirst(FieldKey.COMPOSER));
            window.YearTextField.setText(tag.getFirst(FieldKey.YEAR));
            window.GenreTextField.setText(tag.getFirst(FieldKey.GENRE));
            window.TrackNumberTextField.setText(tag.getFirst(FieldKey.TRACK));

            this.paintArtwork(tag.getFirstArtwork());
        } catch (IOException | CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | KeyNotFoundException | TagException e) {
        }
    }

    /**
     * Fills the tag information from selected files.
     *
     * @param filler The filler to use for uncommon tag information across the
     * files
     */
    public synchronized void fillInfoOnLoadMultipleFiles(String filler) {
        try {
            this.clearMultipleFieldInfos(filler);
            window.AlbumTextField.setText(writer.getSimilarAlbum(window.FileListViewer.getSelectedIndices(), (DefaultListModel) window.FileListViewer.getModel()));
            window.AlbumArtistTextField.setText(writer.getSimilarAlbumArtist(window.FileListViewer.getSelectedIndices(), (DefaultListModel) window.FileListViewer.getModel()));
            window.ArtistTextField.setText(writer.getSimilarArtist(window.FileListViewer.getSelectedIndices(), (DefaultListModel) window.FileListViewer.getModel()));
            window.ComposerTextField.setText(writer.getSimilarComposer(window.FileListViewer.getSelectedIndices(), (DefaultListModel) window.FileListViewer.getModel()));
            window.GenreTextField.setText(writer.getSimilarGenre(window.FileListViewer.getSelectedIndices(), (DefaultListModel) window.FileListViewer.getModel()));
            window.YearTextField.setText(writer.getSimilarYear(window.FileListViewer.getSelectedIndices(), (DefaultListModel) window.FileListViewer.getModel()));

            Artwork albumArt = writer.getSimilarAlbumArt(window.FileListViewer.getSelectedIndices(), (DefaultListModel) window.FileListViewer.getModel());
            this.paintArtwork(albumArt);
        } catch (Exception e) {
        }
    }
}
