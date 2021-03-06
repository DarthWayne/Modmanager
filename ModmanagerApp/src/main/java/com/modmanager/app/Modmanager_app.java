package com.modmanager.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.IniPreferences;

import com.components.ComponentFactory;
import com.modmanager.exception.LoggedException;
import com.modmanager.objects.Game;
import com.modmanager.presenter.main.Modmanager_frm_presenter;
import com.modmanager.presenter.message.MessageDialog;
import com.modmanager.ui.main.Loading_frm;
import com.modmanager.ui.main.Modmanager_frm;

public class Modmanager_app {
	
	
	public static String appPath;
	public static IniPreferences ini;
	private static Logger logger;

	public static void main(final String[] args) {
		startup();
	}

	/**
	 * Starts the Modmanager... obviously
	 */
	private static void startup() {
		Loading_frm frmLoad = new Loading_frm();
		try {
			long time = System.currentTimeMillis();
			frmLoad.getProgressbar().setValue(0);

			// set look and feel
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			frmLoad.setVisible(true);
			frmLoad.getProgressbar().setValue(10);

			// init logger
			logger = initializeLogger();
			logger.debug("Starting modmanager");
			frmLoad.getProgressbar().setValue(20);

			// Get last profile
			Preferences prefs = getIni();
			frmLoad.getProgressbar().setValue(30);

			String prof = prefs.node("Application Properties").get("profile", null);
			if (prof == null || prof.isEmpty()) {
				BackendManager.getProfilesService().setActiveProfile(
						BackendManager.getProfilesService().createDefaultProfile(Game.SKYRIM));
				frmLoad.getProgressbar().setValue(40);
			}
			else {
				BackendManager.getProfilesService().setActiveProfile(prof);
				frmLoad.getProgressbar().setValue(40);
			}
			// If no language was set in the ini, use the system language
			String lang = prefs.node("Application Properties").get("language", null);
			if (lang == null || lang.isEmpty()) {
				lang = Locale.getDefault().getLanguage();
			}
			frmLoad.getProgressbar().setValue(50);
			ComponentFactory.getInstance().changeLanguage(new Locale(lang));
			// Start the main frame
			logger.debug("Startup complete - Duration: " + (System.currentTimeMillis() - time)
					+ " millis");
			frmLoad.getProgressbar().setValue(60);
			Modmanager_frm_presenter pre = new Modmanager_frm_presenter(new Modmanager_frm());
			pre.initialize();
			if (prof == null || prof.isEmpty()) {
				frmLoad.getProgressbar().setValue(80);
				pre.createProfile();
			}
			else {
				frmLoad.getProgressbar().setValue(80);
				pre.loadProfile(BackendManager.getProfilesService().getActiveProfile());
			}
			pre.setVisible(true);
		}
		catch (LoggedException logged) {
			MessageDialog dlg = MessageDialog.createErrorDialog(null);
			dlg.details(logged).title("Application could not be started");
			dlg.message("The application could not be started.").show();
		}
		catch (Exception e) {
			logger.error("The application could not be started due to an unknown error.", e);
			MessageDialog dlg = MessageDialog.createErrorDialog(null);
			dlg.details(e).title("Application could not be started");
			dlg.message("The application could not be started due to an unknown error.").show();
			e.printStackTrace();
		}
		frmLoad.dispose();
	}

	/**
	 * Initilaizes the logger for this class and sets the logging path in the
	 * log4j.properties to the path of the application
	 *
	 * @return Logger
	 */
	private static Logger initializeLogger() {
		try {
			Properties log4jprops = new Properties();
			File propsfile = new File(getAppPath() + "/log4j.properties");
			// Read Properties
			InputStream in = new FileInputStream(propsfile);
			log4jprops.load(in);
			in.close();
			
			// Edit logging folder property
			String logfile = getAppPath() + "/Logs/application.log";
			log4jprops.setProperty("log4j.appender.file.File",
					logfile.substring(logfile.indexOf(':') + 1));
			// delete the file, so we get a new log everytime the ModManager is
			// started
			new File(logfile).delete();
			// Save Properties
			OutputStream out = new FileOutputStream(propsfile);
			log4jprops.store(out, "");
			out.close();
			return Logger.getLogger(Modmanager_app.class);
		}
		catch (Exception e) {
			MessageDialog dlg = MessageDialog.createErrorDialog(null);
			dlg.details(e).title("Failed to initialize logger");
			dlg.message("The logging directory could not be initialized. A logfile may "
					+ "be created in your C:/ directory. You can savely delete this file.");
			dlg.show();
			e.printStackTrace();
		}
		return null;
	}
	
	public static IniPreferences getIni() {
		if (ini == null) {
			try {
				ini = new IniPreferences(new Ini(new File(getAppPath() + "/Modmanager.ini")));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ini;
	}
	
	public static String getAppPath() {
		if (appPath == null) {
			try {
				appPath = URLDecoder.decode(
						ClassLoader.getSystemClassLoader().getResource(".").getPath(), "UFT-8");
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return appPath;
	}
}