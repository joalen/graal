/*
 * Copyright (c) 2019, 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.svm.core.graal.nodes;

import jdk.vm.ci.meta.JavaKind;
import jdk.graal.compiler.core.common.type.Stamp;
import jdk.graal.compiler.core.common.type.StampFactory;
import jdk.graal.compiler.graph.NodeClass;
import jdk.graal.compiler.nodeinfo.NodeCycles;
import jdk.graal.compiler.nodeinfo.NodeInfo;
import jdk.graal.compiler.nodeinfo.NodeSize;
import jdk.graal.compiler.nodes.FixedWithNextNode;
import jdk.graal.compiler.nodes.spi.LIRLowerable;
import jdk.graal.compiler.nodes.spi.NodeLIRBuilderTool;

import java.util.concurrent.atomic.AtomicLong;

@NodeInfo(cycles = NodeCycles.CYCLES_1, size = NodeSize.SIZE_1)
public final class ReadExceptionObjectNode extends FixedWithNextNode implements LIRLowerable {
    public static final NodeClass<ReadExceptionObjectNode> TYPE = NodeClass.create(ReadExceptionObjectNode.class);

    /*
     * Make every node unique to prevent de-duplication. The node reads a fixed register, so it
     * needs to remain the first node immediately after the InvokeWithExceptionNode.
     */
    @SuppressWarnings("unused")//
    private final long uniqueId;
    private static final AtomicLong nextUniqueId = new AtomicLong();

    public ReadExceptionObjectNode(Stamp stamp) {
        super(TYPE, stamp);
        uniqueId = nextUniqueId.getAndIncrement();
    }

    public ReadExceptionObjectNode(JavaKind kind) {
        this(StampFactory.forKind(kind));
    }

    @Override
    public void generate(NodeLIRBuilderTool gen) {
        gen.emitReadExceptionObject(this);
    }
}
