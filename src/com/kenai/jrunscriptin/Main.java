/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.kenai.jrunscriptin;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
public class Main {
    public static void main(String[] args) throws Exception {
        File toolsJar = new File(new File(new File(System.getProperty("java.home")).getParentFile(), "lib"), "tools.jar");
        if (!toolsJar.isFile()) {
            System.err.println("Cannot load " + toolsJar);
            System.exit(2);
        }
        URL self = Main.class.getProtectionDomain().getCodeSource().getLocation();
        URLClassLoader l = new URLClassLoader(new URL[] {
            self,
            toolsJar.toURI().toURL(),
        }, Main.class.getClassLoader().getParent()) {
            public @Override Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.contains("Main2")) {
                    return findClass(name);
                } else {
                    return super.loadClass(name);
                }
            }
        };
        Class<?> main = l.loadClass("com.kenai.jrunscriptin.Main2");
        main.getMethod("main", String[].class).invoke(null, new Object[] {args});
    }
}
