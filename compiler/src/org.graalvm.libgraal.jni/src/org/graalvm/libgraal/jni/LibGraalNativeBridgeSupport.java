/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.graalvm.libgraal.jni;

import jdk.vm.ci.services.Services;
import org.graalvm.compiler.debug.TTY;
import org.graalvm.nativebridge.jni.NativeBridgeSupport;

public class LibGraalNativeBridgeSupport implements NativeBridgeSupport {

    private static final String JNI_LIBGRAAL_TRACE_LEVEL_PROPERTY_NAME = "JNI_LIBGRAAL_TRACE_LEVEL";

    private final ThreadLocal<Boolean> inTrace = ThreadLocal.withInitial(() -> false);

    private Integer traceLevel;

    @Override
    public String getFeatureName() {
        return "LIBGRAAL";
    }

    @Override
    public boolean isTracingEnabled(int level) {
        return traceLevel() >= level;
    }

    @Override
    public void trace(String message) {
        // Prevents nested tracing of JNI calls originated from this method.
        // The TruffleCompilerImpl redirects the TTY using a TTY.Filter to the
        // TruffleCompilerRuntime#log(). In libgraal the HSTruffleCompilerRuntime#log() uses a
        // FromLibGraalCalls#callVoid() to do the JNI call to the GraalTruffleRuntime#log().
        // The FromLibGraalCalls#callVoid() also traces the JNI call by calling trace().
        // The nested trace call should be ignored.
        if (!inTrace.get()) {
            inTrace.set(true);
            try {
                TTY.println(message);
            } finally {
                inTrace.remove();
            }
        }
    }

    private int traceLevel() {
        if (traceLevel == null) {
            String var = Services.getSavedProperties().get(JNI_LIBGRAAL_TRACE_LEVEL_PROPERTY_NAME);
            if (var != null) {
                try {
                    traceLevel = Integer.parseInt(var);
                } catch (NumberFormatException e) {
                    TTY.printf("Invalid value for %s: %s%n", JNI_LIBGRAAL_TRACE_LEVEL_PROPERTY_NAME, e);
                    traceLevel = 0;
                }
            } else {
                traceLevel = 0;
            }
        }
        return traceLevel;
    }
}
