/**
 * OSGi framework startup from a java application
 * 
 * http://njbartlett.name/2011/03/07/embedding-osgi.html
 */

package de.kirschners.osgi.launcher;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class FrameworkLauncher {

	private static final String TMPDIR = System.getProperty("java.io.tmpdir");
	private static String BUNDLEFOLDER = "C:/Users/pekirsc/workspace/de.kirschners.osgi.launcher/frameworks/felix-framework-4.2.1/bundle";

	public static void main(String[] args) {
		FrameworkFactory frameworkFactory = ServiceLoader
				.load(FrameworkFactory.class).iterator().next();
		Map<String, String> config = new HashMap<String, String>();

		// Control where OSGi stores its persistent data:
		config.put(Constants.FRAMEWORK_STORAGE,
				TMPDIR.concat("/_osgi/fw_storage"));

		// Request OSGi to clean its storage area on startup
		config.put(Constants.FRAMEWORK_STORAGE_CLEAN, "true");

		// exposing parent packages to the OSGi framework
		// can be used inside fw via import package statements
		config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
		           "de.kirschners.osgi.launcher");
		
		// Turn on the Equinox console on port 1234 (Equinox only)
		// config.put("osgi.console", "1234");

		Framework framework = frameworkFactory.newFramework(config);
		try {
			System.out.println("starting OSGI framework\n");
			framework.start();
			BundleContext context = framework.getBundleContext();

			// install the bundles from the BUNDLEFOLDER
			List<Bundle> installedBundles = new LinkedList<Bundle>();
			List<String> bundleURIsToInstall = new FrameworkLauncher()
					.getBundleURIsToInstall();
			for (String bundleURI : bundleURIsToInstall) {
				installedBundles.add(context.installBundle(bundleURI));
			}

			// start the bundles from the BUNDLEFOLDER
			for (Bundle bundle : installedBundles) {
				bundle.start();
			}

			framework.waitForStop(0);
			System.out.println("\n\nframework has been stopped.");

		} catch (BundleException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

	private List<String> getBundleURIsToInstall() {
		List<String> bundleURIs = new LinkedList<String>();
		File bundleFolder = new File(BUNDLEFOLDER);
		File[] bundleFiles = bundleFolder.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(".jar");
			}
		});
		for (File file : bundleFiles) {
			bundleURIs.add("file:".concat(file.getAbsolutePath()));
		}
		return bundleURIs;

	}

}
