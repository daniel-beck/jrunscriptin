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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
public class Agent {
    public static void agentmain(String options) throws Exception {
        int port = Integer.parseInt(options);
        //System.err.println("connecting to " + port);
        Socket s = new Socket(InetAddress.getLocalHost(), port);
        //System.err.println("got connection on " + port);
        BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
        OutputStream os = s.getOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(os);
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        ScriptContext context = engine.getContext();
        context.setWriter(w);
        context.setErrorWriter(w);
        String expr;
        while ((expr = r.readLine()) != null) {
            try {
                w.write(String.valueOf(engine.eval(expr)));
                w.write('\n');
            } catch (ScriptException e) {
                e.printStackTrace(new PrintStream(os, true));
            } finally {
                w.flush();
            }
        }
    }
}
