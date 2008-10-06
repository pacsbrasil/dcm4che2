package org.dcm4chee.xero.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.model.Layout;

/**
 * The PatientController manages the patient/study display layout & setup/actions.
 * 
 * @author bwallace
 *
 */
public class PatientController implements MultiAction {
   
   Map<String,Action> actions = new HashMap<String,Action>();
   
   public PatientController() {
	  actions.put("displayPatient", new DisplayPatientAction());
	  actions.put("displayStudy", new DisplayPatientAction());
   }
   
   /** Display the given patient */
   class DisplayPatientAction implements Action {
	  public Map<String, Object> action(Map<String, Object> model) {
		 model.put("layouts", createLayout());
		 return model;
	  }	  
   };
   
   public static List<Layout> createLayout() {
	  List<Layout> ret = new ArrayList<Layout>();
	  ret.add(new Layout("image/menu"));
	  return ret;
   }

   public Map<String, Action> getActions() {
	  return actions;
   }

}
