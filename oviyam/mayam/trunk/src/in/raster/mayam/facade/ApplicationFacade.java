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
 *
 * The Initial Developer of the Original Code is
 * Raster Images
 * Portions created by the Initial Developer are Copyright (C) 2009-2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Babu Hussain A
 * Meer Asgar Hussain B
 * Prakash J
 * Suresh V
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
package in.raster.mayam.facade;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.delegate.InputArgumentsParser;
import in.raster.mayam.form.display.Display;
import in.raster.mayam.form.MainScreen;
import in.raster.mayam.form.SplashScreen;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class ApplicationFacade {

    public static SplashScreen splash;
    public static MainScreen mainScreen;
    //public String applicationName="Mayam";
    public static String binPath="";

    private ApplicationFacade() {
    }

    public void createSplash() {
        splash = new SplashScreen();
        Display.alignScreen(splash);
        splash.setVisible(true);
    }
    public void createMainScreen()
    {
        mainScreen = MainScreen.getInstance();
    }

    private void setSystemProperties() {
        if (Platform.getCurrentPlatform().equals(Platform.MAC)) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", ApplicationContext.applicationName);
            System.setProperty("apple.awt.antialiasing", "true");
            System.setProperty("apple.awt.textantialiasing", "true");
//            System.setProperty("apple.awt.brushMetalLook", "true");
//            System.setProperty("com.sun.media.jai.disableMediaLib", "true");
//            System.setProperty("apple.awt.graphics.EnableLazyPixelConversion.TYPE_3BYTE_BGR", "false");
//            System.setProperty("apple.awt.graphics.EnableLazyDrawing", "false");
//            System.setProperty("apple.awt.rendering", "VALUE_RENDER_SPEED");
//            System.setProperty("apple.awt.interpolation", "VALUE_INTERPOLATION_BILINEAR");
//            System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
//            System.setProperty("apple.awt.graphics.UseQuartz", "true");
        }
        if(Platform.getCurrentPlatform().equals(Platform.LINUX) || Platform.getCurrentPlatform().equals(Platform.SOLARIS))
            System.setProperty("sun.java2d.pmoffscreen", "false");
    }    

    public static void exitApp(String exitString) {
        if (splash != null) {
            splash.setVisible(false);
        }
        JOptionPane.showMessageDialog(null, exitString, "Application Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
    
    public static void main(String[] args) {
        try {
            InputArgumentsParser.parse(args);
            ApplicationFacade facade = new ApplicationFacade();
            facade.setSystemProperties();
            facade.createSplash();
            facade.createMainScreen();
            splash.setVisible(false);
            mainScreen.setVisible(true);
        } catch (Exception ex) {
            Logger.getLogger(ApplicationFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

