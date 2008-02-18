/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package com.tiani.prnscp.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import org.dcm4che.data.Dataset;

import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.jboss.logging.Logger;

/**
 *  <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created  January 5, 2003
 * @version  $Revision$
 */
class Annotation {

    // Constants -----------------------------------------------------
    private final static Color DEF_COLOR = Color.BLACK;

    private final static String DEF_FONT_NAME = "Serif";

    private final static String DEF_FONT_STYLE = "PLAIN";

    private final static String DEF_FONT_SIZE = "10";

    private final static String LEFT = "left";

    private final static String RIGHT = "right";

    private final static String TOP = "top";

    private final static String BOTTOM = "bottom";

    private final static String CENTER = "center";

    private final static String SESSION_LABEL = "$SESSION_LABEL$";

    private final static String CALLING_AET = "$CALLING_AET$";

    private final static String CALLED_AET = "$CALLED_AET$";

    private final static String DATE = "$DATE$";

    private final static String TIME = "$TIME$";

    private final static String PAGE = "$PAGE$";

    private final static String PAGES = "$PAGES$";

    // Attributes ----------------------------------------------------
    private final PrinterService service;

    private final Logger log;

    private final File file;

    private final int numBoxes;

    private final Properties props = new Properties();

    private final float insetLeft;

    private final float insetRight;

    private final float insetTop;

    private final float insetBottom;

    private final String numPagesStr;

    private final SimpleDateFormat dateFormat;

    private final SimpleDateFormat timeFormat;

    private String callingAET = "";

    private String sessionLabel = "";

    // Static --------------------------------------------------------
    static String makeDefaultSessionLabel(PrinterService service, String calling) {
        Date date = new Date();
        String s = service.getSessionLabel();
        s = replaceAll(s, CALLING_AET, calling);
        s = replaceAll(s, CALLED_AET, service.getCalledAET());
        if (s.indexOf(DATE) != -1) {
            String dateStr = new SimpleDateFormat(service.getDateFormat())
                    .format(date);
            s = replaceAll(s, DATE, dateStr);
        }
        if (s.indexOf(TIME) != -1) {
            String timeStr = new SimpleDateFormat(service.getTimeFormat())
                    .format(date);
            s = replaceAll(s, TIME, timeStr);
        }
        return s;
    }

    static String replaceAll(String s, String key, String value) {
        int index = -value.length();
        while ((index = s.indexOf(key, index + value.length())) != -1) {
            s = s.substring(0, index) + value
                    + s.substring(index + key.length());
        }
        return s;
    }

    // Constructors --------------------------------------------------
    /**
     *  Constructor for the Annotation object
     *
     * @param  service Description of the Parameter
     * @param  adfID Description of the Parameter
     * @param  numPages Description of the Parameter
     * @exception  IOException Description of the Exception
     */
    public Annotation(PrinterService service, String adfID, int numPages)
            throws IOException {
        this.service = service;
        this.log = service.getLog();
        this.file = service.getAnnotationFile(adfID);
        this.numBoxes = PrinterService.parseAnnotationBoxCount(file.getName());
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            props.load(in);
        } finally {
            try {
                in.close();
            } catch (Exception ignore) {
            }
        }
        this.insetLeft = Float.parseFloat(props.getProperty("inset.left", "0"));
        this.insetRight = Float.parseFloat(props
                .getProperty("inset.right", "0"));
        this.insetTop = Float.parseFloat(props.getProperty("inset.top", "0"));
        this.insetBottom = Float.parseFloat(props.getProperty("inset.bottom",
                "0"));
        this.numPagesStr = "" + numPages;
        this.dateFormat = new SimpleDateFormat(service.getDateFormat());
        this.timeFormat = new SimpleDateFormat(service.getTimeFormat());
    }

    // Public --------------------------------------------------------
    /**
     *  Gets the insetLeft attribute of the Annotation object
     *
     * @return  The insetLeft value
     */
    public float getInsetLeft() {
        return insetLeft;
    }

    /**
     *  Gets the insetRight attribute of the Annotation object
     *
     * @return  The insetRight value
     */
    public float getInsetRight() {
        return insetRight;
    }

    /**
     *  Gets the insetTop attribute of the Annotation object
     *
     * @return  The insetTop value
     */
    public float getInsetTop() {
        return insetTop;
    }

    /**
     *  Gets the insetBottom attribute of the Annotation object
     *
     * @return  The insetBottom value
     */
    public float getInsetBottom() {
        return insetBottom;
    }

    /**
     *  Gets the imageableX attribute of the Annotation object
     *
     * @param  pf Description of the Parameter
     * @return  The imageableX value
     */
    public double getImageableX(PageFormat pf) {
        return pf.getImageableX() + insetLeft;
    }

    /**
     *  Gets the imageableY attribute of the Annotation object
     *
     * @param  pf Description of the Parameter
     * @return  The imageableY value
     */
    public double getImageableY(PageFormat pf) {
        return pf.getImageableY() + insetTop;
    }

    /**
     *  Gets the imageableWidth attribute of the Annotation object
     *
     * @param  pf Description of the Parameter
     * @return  The imageableWidth value
     */
    public double getImageableWidth(PageFormat pf) {
        return pf.getImageableWidth() - (insetLeft + insetRight);
    }

    /**
     *  Gets the imageableHeight attribute of the Annotation object
     *
     * @param  pf Description of the Parameter
     * @return  The imageableHeight value
     */
    public double getImageableHeight(PageFormat pf) {
        return pf.getImageableHeight() - (insetTop + insetBottom);
    }

    private String getText(int index) {
        return props.getProperty("" + index, "");
    }

    private String getIcon(int index) {
        return props.getProperty("icon." + index);
    }

    /**
     *  Sets the text attribute of the Annotation object
     *
     * @param  index The new text value
     * @param  text The new text value
     */
    public void setText(int index, String text) {
        String key = "" + index;
        props.setProperty(key, text);
    }

    /**
     *  Sets the session attribute of the Annotation object
     *
     * @param  session The new session value
     */
    public void setSession(Dataset session) {
        sessionLabel = session.getString(Tags.FilmSessionLabel, "");
    }

    public void setCallingAET(String callingAET) {
        this.callingAET = callingAET;
    }

    /**
     *  Sets the annotationContentSeq attribute of the Annotation object
     *
     * @param  seq The new annotationContentSeq value
     */
    public void setAnnotationContentSeq(DcmElement seq) {
        if (seq == null) { return; }
        for (int j = 0, n = seq.countItems(); j < n; ++j) {
            Dataset item = seq.getItem(j);
            setText(item.getInt(Tags.AnnotationPosition, 1), item
                    .getString(Tags.TextString));
        }
    }

    /**
     *  Description of the Method
     *
     * @param  g Description of the Parameter
     * @param  pf Description of the Parameter
     * @param  pageIndex Description of the Parameter
     */
    public void print(Graphics g, PageFormat pf, int pageIndex) {
        Graphics2D g2 = (Graphics2D) g;
        Date now = new Date();
        for (int i = 1; i <= numBoxes; ++i) {
            g2.setColor(getColor(i));
            g2.setFont(getFont(i));
            drawText(g2, getX("", i, pf), getY("", i, pf),
                    getAlignmentX("", i), getAlignmentY("", i), replaceKeys(
                            getText(i), pageIndex, now));
        }
        String iconFile;
        for (int i = 1; (iconFile = getIcon(i)) != null; ++i) {
            drawIcon(g2, getX("icon.", i, pf), getY("icon.", i, pf),
                    getAlignmentX("icon.", i), getAlignmentY("icon.", i),
                    getIconWidth(i, pf), getIconHeight(i, pf), iconFile);
        }
    }

    private String replaceKeys(String s, int pageIndex, Date now) {
        s = replaceAll(s, SESSION_LABEL, sessionLabel);
        s = replaceAll(s, CALLING_AET, callingAET);
        s = replaceAll(s, CALLED_AET, service.getCalledAET());
        if (s.indexOf(DATE) != -1) {
            s = replaceAll(s, DATE, dateFormat.format(now));
        }
        if (s.indexOf(TIME) != -1) {
            s = replaceAll(s, TIME, timeFormat.format(now));
        }
        if (s.indexOf(PAGE) != -1) {
            s = replaceAll(s, PAGE, String.valueOf(pageIndex + 1));
        }
        s = replaceAll(s, PAGES, numPagesStr);
        return s;
    }

    private void drawText(Graphics2D g2, float x0, float y0, float alignX,
            float alignY, String s) {
        try {
            StringTokenizer stk = new StringTokenizer(s, "\r\n");
            int n = stk.countTokens();
            if (n == 0) { return; }

            Font font = g2.getFont();
            FontRenderContext frc = g2.getFontRenderContext();
            TextLayout line = new TextLayout(stk.nextToken(), font, frc);
            float dY = line.getAscent() + line.getDescent() + line.getLeading();
            float h = n * dY - line.getLeading();
            float y = y0 - (n - 1) * dY + alignY * h - line.getDescent();
            line.draw(g2, x0 - alignX * line.getAdvance(), y);
            for (int i = 1; i < n; ++i) {
                line = new TextLayout(stk.nextToken(), font, frc);
                line.draw(g2, x0 - alignX * line.getAdvance(), y + i * dY);
            }
        } catch (Exception e) {
            log.warn("Failed to render annotation text:\r\n" + s, e);
        }
    }

    private void drawIcon(Graphics2D g2, float x0, float y0, float alignX,
            float alignY, float w, float h, String iconFile) {
        AffineTransform tx = g2.getTransform();
        try {
            BufferedImage bi = service.getIcon(iconFile);
            if (w <= 0) {
                w = bi.getWidth();
                if (h <= 0) {
                    h = bi.getHeight();
                } else {
                    w *= h / bi.getHeight();
                }
            } else {
                if (h <= 0) {
                    h = bi.getHeight() * w / bi.getWidth();
                }
            }
            float x = x0 - alignX * w;
            float y = y0 - (1 - alignY) * h;
            if (!g2.getClipBounds().intersects(x, y, w, h)) { return; }
            g2.translate(x, y);
            g2.scale(w / bi.getWidth(), h / bi.getHeight());
            g2.drawImage(bi, 0, 0, null);
        } catch (Exception e) {
            log.warn("Failed to render icon: " + iconFile, e);
        } finally {
            if (tx != null) {
                g2.setTransform(tx);
            }
        }
    }

    // Private -------------------------------------------------------

    private Color getColor(int index) {
        String s = props.getProperty("" + index + ".font.color", props
                .getProperty("font.color"));
        if (s != null && s.length() != 0) {
            if (Character.isDigit(s.charAt(0))) {
                try {
                    return new Color(Integer.parseInt(s, 16) & 0xffffff);
                } catch (NumberFormatException e) {
                    service.getLog().warn(
                            "Illegal font.color value: " + s
                                    + " in annotation file:" + file.getName());
                }
            }
            try {
                return (Color) Color.class.getField(s.toUpperCase()).get(null);
            } catch (Exception e) {
                service.getLog().warn(
                        "Illegal font.color value: " + s
                                + " in annotation file:" + file.getName());
            }
        }
        return DEF_COLOR;
    }

    private Font getFont(int index) {
        return new Font(props.getProperty("" + index + ".font.name", props
                .getProperty("font.name", DEF_FONT_NAME)), toFontStyle(props
                .getProperty("" + index + ".font.style", props.getProperty(
                        "font.style", DEF_FONT_STYLE))), Integer.parseInt(props
                .getProperty("" + index + ".font.size", props.getProperty(
                        "font.size", DEF_FONT_SIZE))));
    }

    private int toFontStyle(String s) {
        String upper = s.toUpperCase();
        int style = Font.PLAIN;
        if (upper.indexOf("ITALIC") != -1) {
            style |= Font.ITALIC;
        }
        if (upper.indexOf("BOLD") != -1) {
            style |= Font.BOLD;
        }
        return style;
    }

    private float getX(String prefix, int index, PageFormat pf) {
        String s = props.getProperty(prefix + index + ".x", LEFT);
        int l = s.length();
        int off = l;
        float x = (float) pf.getImageableX() + insetLeft;
        float w = (float) pf.getImageableWidth() - (insetLeft + insetRight);
        if (s.startsWith(LEFT)) {
            off = LEFT.length();
        } else if (s.startsWith(CENTER)) {
            off = CENTER.length();
            x += w / 2;
        } else if (s.startsWith(RIGHT)) {
            off = RIGHT.length();
            x += w;
        }
        if (off < l) {
            x += parseInteger(s.substring(off));
        }
        return x;
    }

    private float getY(String prefix, int index, PageFormat pf) {
        String s = props.getProperty(prefix + index + ".y", BOTTOM);
        int l = s.length();
        int off = l;
        float y = (float) pf.getImageableY() + insetTop;
        float h = (float) pf.getImageableHeight() - (insetTop + insetBottom);
        if (s.startsWith(TOP)) {
            off = TOP.length();
        } else if (s.startsWith(CENTER)) {
            off = CENTER.length();
            y += h / 2;
        } else if (s.startsWith(BOTTOM)) {
            off = BOTTOM.length();
            y += h;
        }
        if (off < l) {
            y += parseInteger(s.substring(off));
        }
        return y;
    }

    private int parseInteger(String s) {
        try {
            return Integer.parseInt(s.substring(s.startsWith("+") ? 1 : 0));
        } catch (NumberFormatException ignore) {
            return 0;
        }
    }

    private float getAlignmentX(String prefix, int index) {
        String s = props.getProperty(prefix + index + ".align");
        return RIGHT.equals(s) ? 1.f : CENTER.equals(s) ? .5f : 0.f;
    }

    private float getAlignmentY(String prefix, int index) {
        String s = props.getProperty(prefix + index + ".valign");
        return BOTTOM.equals(s) ? 0.f : CENTER.equals(s) ? .5f : 1.f;
    }

    private float getIconWidth(int index, PageFormat pf) {
        String s = props.getProperty("icon." + index + ".width", "0");
        try {
            int l = s.length();
            if (s.charAt(l - 1) == '%') {
                float w = (float) pf.getImageableWidth()
                        - (insetLeft + insetRight);
                return w * Integer.parseInt(s.substring(0, l - 1)) * .01f;
            } else {
                return Integer.parseInt(s);
            }
        } catch (NumberFormatException nfe) {
            service.getLog().warn(
                    "Illegal icon." + index + ".width value: " + s
                            + " in annotation file:" + file.getName());
            return 0;
        }
    }

    private float getIconHeight(int index, PageFormat pf) {
        String s = props.getProperty("icon." + index + ".height", "0");
        try {
            int l = s.length();
            if (s.charAt(l - 1) == '%') {
                float h = (float) pf.getImageableHeight()
                        - (insetTop + insetBottom);
                return h * Integer.parseInt(s.substring(0, l - 1));
            } else {
                return Integer.parseInt(s);
            }
        } catch (NumberFormatException nfe) {
            service.getLog().warn(
                    "Illegal icon." + index + ".height value: " + s
                            + " in annotation file:" + file.getName());
            return 0;
        }
    }
}
