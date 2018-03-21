package lib.annotations.properties;

import lib.annotations.callgraph.AnalysisMode;

import static lib.annotations.callgraph.AnalysisMode.CPA;
import static lib.annotations.callgraph.AnalysisMode.OPA;

/**
 * 
 * This annotation indicates whether a method is an entry point. 
 * 
 * 
 * <!--
 * <b>NOTE</b><br> 
 * The annotater should be aware of the different analysis modes and there semantic.
 * -->
 * 
 * @author Michael Reif
 */
public @interface EntryPoint {

	/*
	 * The default value is set to the analysis modes OPA and CPA since
	 * any kind of applications has only a small pre-defined set of entry points.
	 */
	AnalysisMode[] value() default {OPA, CPA};
}
