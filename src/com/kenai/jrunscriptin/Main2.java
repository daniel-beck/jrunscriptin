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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
public class Main2 {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage:");
            System.err.println("  echo <JavaScript> | java -jar jrunscriptin.jar <PID>");
            System.err.println("    where <PID> is as in jps");
            System.err.println("  echo <JavaScript> | java -jar jrunscriptin.jar <match>");
            System.err.println("    where <match> is some substring of the text after a PID visible in jps -lm");
            System.err.println("Example using NetBeans IDE (quoting as in Bourne Shell, adapt as needed):");
            System.err.println("  echo 'Packages.org.openide.awt.StatusDisplayer.getDefault().setStatusText(\"Hello from abroad!\"); java.lang.System.out.println(\"OK!\")' | java -jar jrunscriptin.jar netbeans");
            System.err.println("Requires Mustang (JDK 6) for both this tool and the target VM.");
            System.exit(2);
        }
        String vmid = args[0];
        File self = new File(URI.create(Main.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm()));
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
        final ServerSocket socket = new ServerSocket();
        socket.bind(null);
        int port = socket.getLocalPort();
        new Thread() {
            public @Override void run() {
                try {
                    final Socket conn = socket.accept();
                    //System.err.println("got connection");
                    new Thread() {
                        public @Override void run() {
                            try {
                                OutputStream os = conn.getOutputStream();
                                byte[] buf = new byte[4096];
                                int read;
                                while ((read = System.in.read(buf)) != -1) {
                                    os.write(buf, 0, read);
                                    os.flush();
                                }
                            } catch (IOException x) {
                                x.printStackTrace();
                            }
                            //System.err.println("done");
                            System.exit(0);
                        }
                    }.start();
                    InputStream is = conn.getInputStream();
                    byte[] buf = new byte[4096];
                    int read;
                    try {
                        while ((read = is.read(buf)) != -1) {
                            System.out.write(buf, 0, read);
                        }
                    } catch (SocketException x) {}
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        }.start();
        System.err.println("Attaching to " + m.id()/* + " with callback to port " + port*/ + "...");
        m.loadAgent(self.getAbsolutePath(), Integer.toString(port));
        //System.err.println("loaded agent");
    }
}
