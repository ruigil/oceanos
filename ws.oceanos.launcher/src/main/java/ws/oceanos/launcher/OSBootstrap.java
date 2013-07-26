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
import java.text.SimpleDateFormat;
import java.util.*;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/** Bootstrap the OSGi framework, based on Neil Bartlett's
 *  http://njbartlett.name/2011/03/07/embedding-osgi.html
 *  tutorial.
 */
class OSBootstrap {
    private final Framework framework;

    OSBootstrap() throws BundleException {
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(" ~~~ OceanOS ~ "+ SimpleDateFormat.getDateTimeInstance().format(new Date())+" ~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        FrameworkFactory frameworkFactory = java.util.ServiceLoader.load(FrameworkFactory.class).iterator().next();
        final Map<String, String> config = new HashMap<String, String>();
        // for Akka
        config.put("org.osgi.framework.bootdelegation","sun.misc");
        framework = frameworkFactory.newFramework(config);
        framework.start();
    }

    void installBundles(File fromFolder) throws BundleException {
        final String[] files = fromFolder.list();
        if(files == null) {
            System.out.println("No bundles found in " + fromFolder.getAbsolutePath());
            return;
        }

        // installing bundles
        final List<Bundle> installed = new LinkedList<Bundle>();
        final BundleContext ctx = framework.getBundleContext();
        for(String filename : files) {
            if(filename.endsWith(".jar")) {
                final File f = new File(fromFolder, filename);
                final String ref = "file:" + f.getAbsolutePath();
                installed.add(ctx.installBundle(ref));
            }
        }
        // starting bundles
        for (Bundle bundle : installed) bundle.start();

    }

    void waitForFrameworkAndQuit() throws Exception {
        try {
            framework.waitForStop(0);
        } finally {
            System.out.println("OceanOS stopped, exiting.");
            System.exit(0);
        }
    }

    public static void main(String [] args) throws Exception {
        final OSBootstrap osgi = new OSBootstrap();

        osgi.installBundles(new File("target/bundles"));
        osgi.waitForFrameworkAndQuit();
    }
}