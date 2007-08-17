package org.dcm4chee.xero.display;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines how to handle navigation at the series and image/frame
 * level
 */
@Name("NavigateAction")
@Scope(ScopeType.EVENT)
public class NavigateAction {
   private static Logger log = LoggerFactory.getLogger(NavigateAction.class);

   @In(value = "ConversationStudyModel", create = true)
   ConversationStudyModel studyModel;

   @In(value = "DisplayMode", create = true)
   DisplayMode mode;

   @In(value = "ActionStudyLevel")
   StudyLevel actionStudyLevel;

   Integer position;

   Integer dir;

   /**
     * Navigate to a new study, series or image/frame. May eventually also
     * handle patient level navigation.
     */
   public String action() {
	  DisplayMode.ApplyLevel applyLevel;
	  if (mode != null)
		 applyLevel = mode.getApplyLevel();
	  else
		 applyLevel = DisplayMode.ApplyLevel.SERIES;
	  if (applyLevel == DisplayMode.ApplyLevel.SERIES) {
		 NavigateMacro macro = new NavigateMacro(actionStudyLevel.getSeriesUID());
		 log.info("Navigating series "+macro);
		 studyModel.apply(DisplayMode.ApplyLevel.STUDY, macro);
	  } else if (applyLevel == DisplayMode.ApplyLevel.IMAGE) {
		 NavigateMacro macro = new NavigateMacro(Integer.toString(position+dir));
		 log.info("Navigating to image "+macro);
		 studyModel.apply(DisplayMode.ApplyLevel.SERIES, macro);
	  } else {
		 log.warn("Unknown study level for navigation.");
		 return "failure";
	  }
	  return "success";
   }

   public Integer getDir() {
	  return dir;
   }

   public void setDir(Integer dir) {
	  this.dir = dir;
   }

   /**
     * Returns the position of the image within the sequence - not always
     * provided or used, but needed for image level navigation.
     * 
     * @return
     */
   public Integer getPosition() {
	  return position;
   }

   public void setPosition(Integer position) {
	  this.position = position;
   }

}
