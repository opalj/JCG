package org.opalj.annotations.callgraph.properties;

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
	EJB6_APP
}
