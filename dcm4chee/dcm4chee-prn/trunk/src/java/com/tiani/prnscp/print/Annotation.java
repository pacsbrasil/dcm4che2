/*****************************************************************************
 *                                                                           *
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
 *                                                                           *
 *****************************************************************************/

package com.tiani.prnscp.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.print.PageFormat;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 * @since January 5, 2003
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
class Annotation {
   
   // Constants -----------------------------------------------------
   private static final Color DEF_COLOR = Color.BLACK;
   private static final String DEF_FONT_NAME = "Serif";
   private static final String DEF_FONT_STYLE = "PLAIN";
   private static final String DEF_FONT_SIZE = "10";
   private static final String LEFT = "left";
   private static final String RIGHT = "right";
   private static final String TOP = "top";
   private static final String BOTTOM = "bottom";
   private static final String CENTER = "center";
   private static final Pattern DATE = Pattern.compile("\\$DATE\\$");
   private static final Pattern PAGE = Pattern.compile("\\$PAGE\\$");
   private static final Pattern PAGES = Pattern.compile("\\$PAGES\\$");
   
   // Attributes ----------------------------------------------------
   private final PrinterService service;
   private final File file;
   private final Properties props = new Properties();
   private final float insetLeft;
   private final float insetRight;
   private final float insetTop;
   private final float insetBottom;
   private final SimpleDateFormat dateFormat;
   private String numPagesStr = "?";
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public Annotation(PrinterService service, File file) throws IOException
   {
      this.service = service;
      this.file = file;
      InputStream in = new BufferedInputStream(new FileInputStream(file));
      try {
         props.load(in);
      } finally {
         try { in.close(); } catch (Exception ignore) {}
      }
      this.insetLeft =
         Float.parseFloat(props.getProperty("inset.left", "0"));
      this.insetRight =
         Float.parseFloat(props.getProperty("inset.right", "0"));
      this.insetTop =
         Float.parseFloat(props.getProperty("inset.top", "0"));
      this.insetBottom =
         Float.parseFloat(props.getProperty("inset.bottom", "0"));
      this.dateFormat = new SimpleDateFormat(
         props.getProperty("dateFormat", "yyyy-MM-dd"));
      
   }
   
   // Public --------------------------------------------------------
   public void setNumberOfPages(int numPages) {
      numPagesStr = "" + numPages;
   }

   public float getInsetLeft() {
      return insetLeft;
   }

   public float getInsetRight() {
      return insetRight;
   }

   public float getInsetTop() {
      return insetTop;
   }

   public float getInsetBottom() {
      return insetBottom;
   }
   
   private String getText(int index) {
      return props.getProperty("" + index);
   }

   public void setText(int index, String text) {
      String key = "" + index;
      if (props.getProperty(key) == null) {
         throw new IndexOutOfBoundsException("index: " + index);
      }
      props.setProperty(key, text);
   }
   
   public void print(Graphics g, PageFormat pf, int pageIndex) {
      Graphics2D g2 = (Graphics2D) g;
      String pageNoStr = "" + (pageIndex+1); 
      String dateStr = dateFormat.format(new Date());
      String s;
      for (int i = 1; (s = getText(i)) != null; ++i) {
         s = DATE.matcher(s).replaceAll(dateStr);
         s = PAGE.matcher(s).replaceAll(pageNoStr);
         s = PAGES.matcher(s).replaceAll(numPagesStr);
         g2.setColor(getColor(i));
         g2.setFont(getFont(i));
         drawText(g2,
            getX(i, pf), getY(i, pf), getAlignmentX(i), getAlignmentY(i),  s);
      }
   }

   private void drawText(Graphics2D g2, 
      float x0, float y0, float alignX, float alignY, String s)
   {
      StringTokenizer stk = new StringTokenizer(s, "\r\n");
      int n = stk.countTokens();
      if (n == 0)
         return;
      
      Font font = g2.getFont();
      FontRenderContext frc = g2.getFontRenderContext();
      TextLayout line =  new TextLayout(stk.nextToken(), font, frc);
      float dY = line.getAscent() + line.getDescent() + line.getLeading();
      float h = n * dY - line.getLeading();
      float y = y0 - (n - 1)* dY + alignY * h - line.getDescent();
      line.draw(g2, x0 - alignX * line.getAdvance(), y);
      for (int i = 1; i < n; ++i) {
         line =  new TextLayout(stk.nextToken(), font, frc);
         line.draw(g2, x0 - alignX * line.getAdvance(), y + i * dY);
      }
   }
   
   // Private -------------------------------------------------------

   private Color getColor(int index) {
      String s = props.getProperty("" + index + ".font.color",
               props.getProperty("font.color"));
      if (s != null) {
         try {
            return new Color(Integer.parseInt(s,16) & 0xffffff);
         } catch (NumberFormatException e) {
            service.getLog().warn("Illegal font.color value: " + s 
               + " in annotation file:" + file.getName());
         }
      }
      return DEF_COLOR;
   }
   
   private Font getFont(int index) {
      return new Font(
         props.getProperty("" + index + ".font.name",
            props.getProperty("font.name", DEF_FONT_NAME)),
         toFontStyle(props.getProperty("" + index + ".font.style",         
            props.getProperty("font.style", DEF_FONT_STYLE))),
         Integer.parseInt(props.getProperty("" + index + ".font.size",         
            props.getProperty("font.size", DEF_FONT_SIZE))));
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
   
   private float getX(int index, PageFormat pf) {
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
   
   private float getY(int index, PageFormat pf) {
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
   
   private float getAlignmentX(int index) {
      String s = props.getProperty("" + index + ".align");
      return RIGHT.equals(s) ? 1.f : CENTER.equals(s) ? .5f : 0.f;
   }
   
   private float getAlignmentY(int index) {
      String s = props.getProperty("" + index + ".valign");
      return BOTTOM.equals(s) ? 0.f : CENTER.equals(s) ? .5f : 1.f;      
   }   
}
