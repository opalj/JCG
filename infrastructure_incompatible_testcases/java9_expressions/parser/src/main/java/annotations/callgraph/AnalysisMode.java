/*
 * BSD 2-Clause License:
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
 *     this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package lib.annotations.callgraph;

/**
 * 
 * This enum describes the mode which is used to analyze a project. The chosen mode reflects different
 * scenarios where different assumption can be applied.
 * 
 * Depending on the chosen mode, the following attributes should differ:
 *    1. The entry point set has to be identified according to the analysis mode.
 *    2. The analysis modes decides whether call-by-signature has to be applied on interface method calls.
 * 
 * @author Michael Reif
 */
public enum AnalysisMode {
	/**
	 * The project is analyzed as a desktop application with a fixed entry point set and a whole program analysis.
	 */
	DESKTOP_APP,
	
	/**
	 * The project is a library analyzed under the open packages assumption; e.g. the client is allowed
	 * to contribute to the library's packages. Call-by-signature has to be applied.
	 */
	OPA,
	
	/**
	 * The project is a library analyzed under the closed packages assumption; e.g. the client is not allowed
	 * to contribute to the library's packages. Call-by-signature has to be applied.
	 */
	CPA,
	
	/**
	 * The project is a pure web application developed with JavaEE 6.
	 */
	EJB6_APP,


	OSGI_BUNDLE
}
