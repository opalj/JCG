/* BSD 2-Clause License:
 * Copyright (c) 2009 - 2016
 * Software Technology Group
 * Department of Computer Science
 * Technische Universität Darmstadt
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package lib.annotations.callgraph;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Describes a method call at a specific call site and states which methods
 * the call must be resolved to.
 * Using this annotation implies that the call edges must be
 * directly available within the call graph from the specified call site. Therefore, this annoation
 * can be used to specify monomorphic or polymorphic method calls but are not suited to specify
 * indirect call targets, e.g., reflective call targets.
 * Furthermore, it is possible to exclude certain target methods.
 *
 *
 * @author Florian Kuebler
 * @author Michael Reif
 */
@Retention(RUNTIME)
@Target({METHOD, CONSTRUCTOR})
@Repeatable(DirectCalls.class)
public @interface DirectCall {

    String name();

    Class<?> returnType() default Void.class;

    Class<?>[] parameterTypes() default {};

    int line() default -1;

    /**
     * Must be given in JVM binary notation (e.g. Ljava/lang/Object;)
     */
    String[] resolvedTargets();

    /**
     * Must be given in JVM binary notation (e.g. Ljava/lang/Object;)
     */
    String[] prohibitedTargets() default {};
}
