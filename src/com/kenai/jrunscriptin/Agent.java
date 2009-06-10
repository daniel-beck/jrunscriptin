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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
public class Agent {
    public static void agentmain(String options) throws Exception {
        int nul = options.indexOf(File.pathSeparatorChar);
        File data = new File(options.substring(0, nul));
        String expr = options.substring(nul + 1);
        OutputStream os = new FileOutputStream(data);
        try {
            PrintStream ps = new PrintStream(os, true);
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
            ScriptContext context = engine.getContext();
            OutputStreamWriter w = new OutputStreamWriter(ps);
            context.setWriter(w);
            context.setErrorWriter(w);
            try {
                engine.eval(expr);
            } catch (ScriptException e) {
                e.printStackTrace(ps);
            } finally {
                w.flush();
            }
        } finally {
            os.close();
        }
    }
}
