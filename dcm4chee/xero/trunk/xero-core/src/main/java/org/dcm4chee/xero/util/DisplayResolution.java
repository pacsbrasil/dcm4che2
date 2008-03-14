package org.dcm4chee.xero.util;

import static org.jboss.seam.ScopeType.SESSION;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Store the display resolution information.  Currently stores at session level, but really should
 * also have a conversation level version
 */
@Name("DisplayResolution")
@Scope(SESSION)
public class DisplayResolution {
   static Logger log = LoggerFactory.getLogger(DisplayResolution.class);
   int width = 1073;
   int height=783;
   float pitch = 0.294f;
   
   /** Return the width of the displayable area, in pixels. */
   public int getWidth() {
      return width;
   }
   public void setWidth(int width) {
	  if( width < 350 ) width = 350;
      this.width = width;
   }
   
   /** Return the height of the displayable area, in pixels. */
   public int getHeight() {
	  if( height<350 ) height = 350;
      return height;
   }
   public void setHeight(int height) {
      this.height = height;
   }
   
   /**
    * Return the display pitch, that is, the distance between adjacent pixels.  Assume square pixels.
    * @return
    */
   public float getPitch() {
      return pitch;
   }
   public void setPitch(float pitch) {
      this.pitch = pitch;
   }
   
   /** Update the values. */
   public String action() {
	  log.info("Setting display resolution to "+getWidth()+","+getHeight());
	  return "OK";
   }
}
