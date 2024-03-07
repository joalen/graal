/*
 * Copyright (c) 2024, 2024, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.svm.configure.config;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import com.oracle.svm.core.util.json.JsonPrintable;
import com.oracle.svm.core.util.json.JsonWriter;

/**
 * Provides a representation of a Java type based on String type names. This is used to parse types
 * in configuration files. The supported types are:
 *
 * <ul>
 * <li>Named types: regular Java types described by their fully qualified name.</li>
 * </ul>
 */
public interface ConfigurationTypeDescriptor extends Comparable<ConfigurationTypeDescriptor>, JsonPrintable {
    enum Kind {
        NAMED;
    }

    Kind getDescriptorType();

    @Override
    String toString();

    /**
     * Returns the qualified names of all named Java types (excluding proxy classes, lambda classes
     * and similar anonymous classes) required for this type descriptor to properly describe its
     * type. This is used to filter configurations based on a String-based class filter.
     */
    Collection<String> getAllQualifiedJavaNames();

    static void checkQualifiedJavaName(String javaName) {
        assert javaName.indexOf('/') == -1 : "Requires qualified Java name, not internal representation";
        assert !javaName.startsWith("[") : "Requires Java source array syntax, for example java.lang.String[]";
    }
}

record NamedConfigurationTypeDescriptor(String name) implements ConfigurationTypeDescriptor {

    public NamedConfigurationTypeDescriptor {
        ConfigurationTypeDescriptor.checkQualifiedJavaName(name);
    }

    @Override
    public Kind getDescriptorType() {
        return Kind.NAMED;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Collection<String> getAllQualifiedJavaNames() {
        return Collections.singleton(name);
    }

    @Override
    public int compareTo(ConfigurationTypeDescriptor other) {
        if (other instanceof NamedConfigurationTypeDescriptor namedOther) {
            return name.compareTo(namedOther.name);
        } else {
            return getDescriptorType().compareTo(other.getDescriptorType());
        }
    }

    @Override
    public void printJson(JsonWriter writer) throws IOException {
        writer.quote(name);
    }
}
