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
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
public class Main2 {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage:");
            System.err.println("  java -jar jrunscriptin.jar <PID> <JavaScript>");
            System.err.println("    where <PID> is as in jps");
            System.err.println("  java -jar jrunscriptin.jar <match> <JavaScript>");
            System.err.println("    where <match> is some substring of the text after a PID visible in jps -lm");
            System.err.println("Example using NetBeans IDE (quoting as in Bourne Shell, adapt as needed):");
            System.err.println("  java -jar jrunscriptin.jar netbeans 'Packages.org.openide.awt.StatusDisplayer.getDefault().setStatusText(\"Hello from abroad!\"); java.lang.System.out.println(\"OK!\")'");
            System.err.println("Requires Mustang (JDK 6) for both this tool and the target VM.");
            System.exit(2);
        }
        String vmid = args[0];
        String expr = args[1];
        File self = new File(URI.create(Main.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm()));
        File selfCopy = File.createTempFile("jrunscriptin", ".jar");
        selfCopy.deleteOnExit();
        InputStream is = new FileInputStream(self);
        try {
            OutputStream os = new FileOutputStream(selfCopy);
            try {
                byte[] buf = new byte[4096];
                int read;
                while ((read = is.read(buf)) != -1) {
                    os.write(buf, 0, read);
                }
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
        VirtualMachine m;
        try {
            m = VirtualMachine.attach(vmid);
        } catch (AttachNotSupportedException e) {
            VirtualMachineDescriptor match = null;
            for (VirtualMachineDescriptor desc : VirtualMachine.list()) {
                if (desc.displayName().contains("jrunscriptin")) {
                    continue;
                }
                if (desc.displayName().contains(vmid)) {
                    if (match != null) {
                        System.err.println("Multiple Java processes found matching '" + vmid + "'");
                        System.exit(1);
                    } else {
                        match = desc;
                    }
                }
            }
            if (match == null) {
                System.err.println("No Java processes found matching '" + vmid + "'");
                System.exit(1);
            }
            m = VirtualMachine.attach(match);
        }
        System.err.println("Attaching to " + m.id() + "...");
        File data = File.createTempFile("jrunscriptin", ".dat");
        data.deleteOnExit();
        data.delete();
        m.loadAgent(selfCopy.getAbsolutePath(), data.getAbsolutePath() + File.pathSeparatorChar + expr);
        if (!data.isFile()) {
            System.err.println("Script did not execute");
            System.exit(2);
        }
        //System.err.println("Got data of length " + data.length());
        is = new FileInputStream(data);
        try {
            int c;
            while ((c = is.read()) != -1) {
                System.out.write(c);
            }
        } finally {
            is.close();
        }
    }
}
