/*                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package com.tiani.prnscp.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
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
import java.util.regex.Pattern;
import org.dcm4che.data.Dataset;

import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;

/**
 *  <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created  January 5, 2003
 * @version  $Revision$
 */
class Annotation
{

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
    private final static Pattern SESSION_LABEL = Pattern.compile("\\$SESSION_LABEL\\$");
    private final static Pattern CALLING_AET = Pattern.compile("\\$CALLING_AET\\$");
    private final static Pattern CALLED_AET = Pattern.compile("\\$CALLED_AET\\$");
    private final static Pattern DATE = Pattern.compile("\\$DATE\\$");
    private final static Pattern TIME = Pattern.compile("\\$TIME\\$");
    private final static Pattern PAGE = Pattern.compile("\\$PAGE\\$");
    private final static Pattern PAGES = Pattern.compile("\\$PAGES\\$");
    private final static Pattern LICENSE_CN = Pattern.compile("\\$LICENSE_CN\\$");
    private final static Pattern LICENSE_ENDDATE = Pattern.compile("\\$LICENSE_ENDDATE\\$");

    // Attributes ----------------------------------------------------
    private final PrinterService service;
    private final File file;
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
        String dateStr = new SimpleDateFormat(service.getDateFormat()).format(date);
        String timeStr = new SimpleDateFormat(service.getTimeFormat()).format(date);
        String s = service.getSessionLabel();
        s = CALLING_AET.matcher(s).replaceAll(calling);
        s = CALLED_AET.matcher(s).replaceAll(service.getCalledAET());
        s = DATE.matcher(s).replaceAll(dateStr);
        s = TIME.matcher(s).replaceAll(timeStr);
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
        throws IOException
    {
        this.service = service;
        this.file = new File(service.getAnnotationDir(),
                adfID + PrinterService.ADF_FILE_EXT);
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            props.load(in);
        } finally {
            try {
                in.close();
            } catch (Exception ignore) {}
        }
        this.insetLeft =
                Float.parseFloat(props.getProperty("inset.left", "0"));
        this.insetRight =
                Float.parseFloat(props.getProperty("inset.right", "0"));
        this.insetTop =
                Float.parseFloat(props.getProperty("inset.top", "0"));
        this.insetBottom =
                Float.parseFloat(props.getProperty("inset.bottom", "0"));
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
    public float getInsetLeft()
    {
        return insetLeft;
    }


    /**
     *  Gets the insetRight attribute of the Annotation object
     *
     * @return  The insetRight value
     */
    public float getInsetRight()
    {
        return insetRight;
    }


    /**
     *  Gets the insetTop attribute of the Annotation object
     *
     * @return  The insetTop value
     */
    public float getInsetTop()
    {
        return insetTop;
    }


    /**
     *  Gets the insetBottom attribute of the Annotation object
     *
     * @return  The insetBottom value
     */
    public float getInsetBottom()
    {
        return insetBottom;
    }


    /**
     *  Gets the imageableX attribute of the Annotation object
     *
     * @param  pf Description of the Parameter
     * @return  The imageableX value
     */
    public double getImageableX(PageFormat pf)
    {
        return pf.getImageableX() + insetLeft;
    }


    /**
     *  Gets the imageableY attribute of the Annotation object
     *
     * @param  pf Description of the Parameter
     * @return  The imageableY value
     */
    public double getImageableY(PageFormat pf)
    {
        return pf.getImageableY() + insetTop;
    }


    /**
     *  Gets the imageableWidth attribute of the Annotation object
     *
     * @param  pf Description of the Parameter
     * @return  The imageableWidth value
     */
    public double getImageableWidth(PageFormat pf)
    {
        return pf.getImageableWidth() - (insetLeft + insetRight);
    }


    /**
     *  Gets the imageableHeight attribute of the Annotation object
     *
     * @param  pf Description of the Parameter
     * @return  The imageableHeight value
     */
    public double getImageableHeight(PageFormat pf)
    {
        return pf.getImageableHeight() - (insetTop + insetBottom);
    }


    private String getText(int index)
    {
        return props.getProperty("" + index);
    }


    /**
     *  Sets the text attribute of the Annotation object
     *
     * @param  index The new text value
     * @param  text The new text value
     */
    public void setText(int index, String text)
    {
        String key = "" + index;
        if (props.getProperty(key) == null) {
            throw new IndexOutOfBoundsException("index: " + index);
        }
        props.setProperty(key, text);
    }


    /**
     *  Sets the session attribute of the Annotation object
     *
     * @param  session The new session value
     */
    public void setSession(Dataset session)
    {
        sessionLabel = session.getString(Tags.FilmSessionLabel, "");
    }

    public void setCallingAET(String callingAET)
    {
        this.callingAET = callingAET;
    }
    

    /**
     *  Sets the annotationContentSeq attribute of the Annotation object
     *
     * @param  seq The new annotationContentSeq value
     */
    public void setAnnotationContentSeq(DcmElement seq)
    {
        if (seq == null) {
            return;
        }
        for (int j = 0, n = seq.vm(); j < n; ++j) {
            Dataset item = seq.getItem(j);
            setText(
                    item.getInt(Tags.AnnotationPosition, 1),
                    item.getString(Tags.TextString));
        }
    }


    /**
     *  Description of the Method
     *
     * @param  g Description of the Parameter
     * @param  pf Description of the Parameter
     * @param  pageIndex Description of the Parameter
     */
    public void print(Graphics g, PageFormat pf, int pageIndex)
    {
        Graphics2D g2 = (Graphics2D) g;
        String pageNoStr = "" + (pageIndex + 1);
        Date date = new Date();
        String dateStr = dateFormat.format(date);
        String timeStr = timeFormat.format(date);
        String s;
        for (int i = 1; (s = getText(i)) != null; ++i) {
            s = SESSION_LABEL.matcher(s).replaceAll(sessionLabel);
            s = CALLING_AET.matcher(s).replaceAll(callingAET);
            s = CALLED_AET.matcher(s).replaceAll(service.getCalledAET());
            s = DATE.matcher(s).replaceAll(dateStr);
            s = TIME.matcher(s).replaceAll(timeStr);
            s = PAGE.matcher(s).replaceAll(pageNoStr);
            s = PAGES.matcher(s).replaceAll(numPagesStr);
            s = LICENSE_CN.matcher(s).replaceAll(service.getLicenseCN());
            s = LICENSE_ENDDATE.matcher(s).replaceAll(
                    dateFormat.format(service.getLicenseEndDate()));
            g2.setColor(getColor(i));
            g2.setFont(getFont(i));
            drawText(g2,
                    getX(i, pf), getY(i, pf), getAlignmentX(i), getAlignmentY(i), s);
        }
    }


    private void drawText(Graphics2D g2,
            float x0, float y0, float alignX, float alignY, String s)
    {
        StringTokenizer stk = new StringTokenizer(s, "\r\n");
        int n = stk.countTokens();
        if (n == 0) {
            return;
        }

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
    }


    // Private -------------------------------------------------------

    private Color getColor(int index)
    {
        String s = props.getProperty("" + index + ".font.color",
                props.getProperty("font.color"));
        if (s != null) {
            try {
                return new Color(Integer.parseInt(s, 16) & 0xffffff);
            } catch (NumberFormatException e) {
                service.getLog().warn("Illegal font.color value: " + s
                         + " in annotation file:" + file.getName());
            }
        }
        return DEF_COLOR;
    }


    private Font getFont(int index)
    {
        return new Font(
                props.getProperty("" + index + ".font.name",
                props.getProperty("font.name", DEF_FONT_NAME)),
                toFontStyle(props.getProperty("" + index + ".font.style",
                props.getProperty("font.style", DEF_FONT_STYLE))),
                Integer.parseInt(props.getProperty("" + index + ".font.size",
                props.getProperty("font.size", DEF_FONT_SIZE))));
    }


    private int toFontStyle(String s)
    {
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


    private float getX(int index, PageFormat pf)
    {
        String s = props.getProperty("" + index + ".x", LEFT);
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
            try {
                x += Integer.parseInt(s.substring(off));
            } catch (NumberFormatException ignore) {}
        }
        return x;
    }


    private float getY(int index, PageFormat pf)
    {
        String s = props.getProperty("" + index + ".y", BOTTOM);
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
            try {
                y += Integer.parseInt(s.substring(off));
            } catch (NumberFormatException ignore) {}
        }
        return y;
    }


    private float getAlignmentX(int index)
    {
        String s = props.getProperty("" + index + ".align");
        return RIGHT.equals(s) ? 1.f : CENTER.equals(s) ? .5f : 0.f;
    }


    private float getAlignmentY(int index)
    {
        String s = props.getProperty("" + index + ".valign");
        return BOTTOM.equals(s) ? 0.f : CENTER.equals(s) ? .5f : 1.f;
    }
}

