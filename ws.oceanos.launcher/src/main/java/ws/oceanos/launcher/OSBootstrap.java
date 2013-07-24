/*
 * Copyright 2013 Rui Gil.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ws.oceanos.launcher;


import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Bootstrap the OSGi framework, based on Neil Bartlett's
 *  http://njbartlett.name/2011/03/07/embedding-osgi.html
 *  tutorial.
 */
class OSBootstrap {
    private static final Logger log = LoggerFactory.getLogger(OSBootstrap.class);
    private final Framework framework;

    OSBootstrap() throws BundleException {
        FrameworkFactory frameworkFactory = java.util.ServiceLoader.load(FrameworkFactory.class).iterator().next();
        final Map<String, String> config = new HashMap<String, String>();
        framework = frameworkFactory.newFramework(config);
        framework.start();
        log.info("OSGi framework started");
    }

    void installBundles(File fromFolder) throws BundleException {
        final String[] files = fromFolder.list();
        if(files == null) {
            log.warn("No bundles found in {}", fromFolder.getAbsolutePath());
            return;
        }

        log.info("Installing bundles from {}", fromFolder.getAbsolutePath());
        final List<Bundle> installed = new LinkedList<Bundle>();
        final BundleContext ctx = framework.getBundleContext();
        for(String filename : files) {
            if(filename.endsWith(".jar")) {
                final File f = new File(fromFolder, filename);
                final String ref = "file:" + f.getAbsolutePath();
                log.info("Installing bundle {}", ref);
                installed.add(ctx.installBundle(ref));
            }
        }

        for (Bundle bundle : installed) {
            log.info("Starting bundle {}", bundle.getSymbolicName());
            bundle.start();
        }

        log.info("{} bundles installed from {}", installed.size(), fromFolder.getAbsolutePath());
    }

    void waitForFrameworkAndQuit() throws Exception {
        try {
            framework.waitForStop(0);
        } finally {
            log.info("OSGi framework stopped, exiting");
            System.exit(0);
        }
    }

    Framework getFramework() {
        return framework;
    }

    public static void main(String [] args) throws Exception {
        final OSBootstrap osgi = new OSBootstrap();
        final Framework framework = osgi.getFramework();

        log.info("Framework bundle: {} ({})", framework.getSymbolicName(), framework.getState());
        osgi.installBundles(new File("target/bundles"));
        for(Bundle b : framework.getBundleContext().getBundles()) {
            log.info("Installed bundle: {} ({})", b.getSymbolicName(), b.getState());
        }

        osgi.waitForFrameworkAndQuit();
    }
}