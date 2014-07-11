package in.raster.mayam.form;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Devishree
 * @version 2.1
 */
public class MediaPlayer extends JPanel {

    private Canvas videoSurface = null;
    private MediaPlayerFactory mediaPlayerFactory = null;
    private EmbeddedMediaPlayer mediaPlayer = null;
    private VideoToolbar toolbar = null;
    private JPopupMenu toolsMenu = null;
    private boolean isPopupVisible = false;

    public MediaPlayer() {
//        LibVlc libVlc = LibVlcFactory.factory().create();
        setLayout(new GridLayout(1, 1));
        videoSurface = new Canvas();
        videoSurface.setBackground(Color.BLACK);
        // Since we're mixing lightweight Swing components and heavyweight AWT 
        // components this is probably a good idea
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        List<String> vlcArgs = new ArrayList<String>();

        vlcArgs.add("--no-plugins-cache");
        vlcArgs.add("--no-video-title-show");
        vlcArgs.add("--no-snapshot-preview");

        mediaPlayerFactory = new MediaPlayerFactory(vlcArgs.toArray(new String[vlcArgs.size()]));

        mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
        mediaPlayer.setVideoSurface(mediaPlayerFactory.newVideoSurface(videoSurface));
        mediaPlayer.setPlaySubItems(true);

        mediaPlayer.setEnableKeyInputHandling(false);
        mediaPlayer.setEnableMouseInputHandling(false);

        toolbar = new VideoToolbar(mediaPlayer);
        designContext();
        addListeners();
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        add(videoSurface);
    }

    public void playMedia(String filePath) {
        mediaPlayer.playMedia(filePath);
        setName(filePath.substring(filePath.split("_")[0].lastIndexOf(File.separator) + 1, filePath.indexOf("_")));
        toolbar.startTimer();
    }

    private void addListeners() {
        videoSurface.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (e.getY() >= getHeight() - 150) {
                    if (!isPopupVisible) {
                        toolsMenu.show(videoSurface, 30, getHeight() - 100);
                        isPopupVisible = true;
                    }
                } else {
                    isPopupVisible = false;
                    toolsMenu.setVisible(false);
                    videoSurface.validate();
                }
            }
        });
    }

    private void designContext() {
        toolsMenu = new JPopupMenu();   
//        toolsMenu.setOpaque(true);
//        toolsMenu.setBackground(Color.BLACK);
//        toolsMenu = new ToolsMenu();   
        toolsMenu.setLayout(new GridLayout(1, 1));   
//        toolsMenu.setPreferredSize(new Dimension(1000, 200));
        toolsMenu.add(toolbar);
    }

    public void stopTimer() {
        toolbar.stopTimer();
    }

    public String getVideoIdentifier() {
        return getName();
    }
}
