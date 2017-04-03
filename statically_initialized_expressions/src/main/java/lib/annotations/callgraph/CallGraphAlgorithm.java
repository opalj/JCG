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

import static lib.annotations.callgraph.CallGraphAlgorithmOrder.*;

/**
 * Represents the different call graph algorithms. The algorithms form a lattice
 * with the precision relationship as partial order.
 * 
 * @author Michael Reif
 * @author Michael Eichberg
 * @author Florian Kuebler
 */
public enum CallGraphAlgorithm {

	/** Bottom element of the lattice. Considered to make every possible call. */
	BOT(new CallGraphAlgorithm[] {}),

	/** Class Hierarchy Analysis */
	CHA(new CallGraphAlgorithm[] {}),

	/** Variable Type Analysis */
	BasicVTA(new CallGraphAlgorithm[] { CHA }),

	/**
	 * Variable Type Analysis with field and return type refinement and local
	 * reference values tracking
	 */
	DefaultVTA(new CallGraphAlgorithm[] { BasicVTA }),

	/**
	 * Variable Type Analysis with field and return type refinement and local
	 * values tracking.
	 */
	ExtVTA(new CallGraphAlgorithm[] { DefaultVTA }),

	/**
	 * Context-sensitive Variable Type Analysis with field and return type
	 * refinement and local reference values tracking.
	 */
	CFA(new CallGraphAlgorithm[] { ExtVTA }),

	/**
	 * Rapid Type Analysis based call graph algorithm.
	 */
	RTA(new CallGraphAlgorithm[] { CHA }),
	
	SPARK(new CallGraphAlgorithm[] {CHA}),

	/**
	 * The 'perfect' call graph algorithm, which is considered as human
	 * generated call graph. This is used as top element.
	 */
	TOP(new CallGraphAlgorithm[] { CFA, RTA, SPARK });

	/**
	 * The set of call graph algorithms that are directly in a 'less precise'
	 * relation to <code>this</code>.
	 */
	private CallGraphAlgorithm[] directlyLessPrecise;

	CallGraphAlgorithm(CallGraphAlgorithm[] lessPrecise) {
		this.directlyLessPrecise = lessPrecise;
	}

	/**
	 * Getter for the directly less precise call graph algorithms.
	 * 
	 * @return An array of call graph algorithms that are in a direct 'less
	 *         pricise' relation to <code>this</code>.
	 */
	public CallGraphAlgorithm[] getDirectlyLessPreciseCallGraphAlgorithms() {
		return directlyLessPrecise;
	}

	/**
	 * Compares <code>this<code> call graph algorithms with <code>other</code>
	 * in terms of precision.
	 * 
	 * @param other
	 *            the call graph algorithm to be compared to <code>this</code>
	 * @return a {@link CallGraphAlgorithmOrder} identifying whether and if how
	 *         <code>this</code> and <code>other</code> are in relation.
	 * 
	 * @see CallGraphAlgorithmOrder
	 */
	public CallGraphAlgorithmOrder compare(CallGraphAlgorithm other) {
		if (this.equals(other)) {
			return EqualPrecision;
		}

		for (CallGraphAlgorithm cga : other.getDirectlyLessPreciseCallGraphAlgorithms()) {
			if (this.equals(cga) || hasSmallerPrecision(cga)) {
				return SmallerPrecision;
			}
		}

		for (CallGraphAlgorithm cga : getDirectlyLessPreciseCallGraphAlgorithms()) {
			if (cga.equals(other) || cga.hasGreaterPrecision(other)) {
				return GreaterPrecision;
			}
		}

		return Incomparable;
	}

	/**
	 * Calculates the supremum of <code>this</code> and <code>other</code>.
	 * 
	 * @param other
	 * @return the common supremum of <code>this</code> and <code>other</code>.
	 */
	public CallGraphAlgorithm getSupremum(CallGraphAlgorithm other) {
		CallGraphAlgorithm supremum = TOP;

		for (CallGraphAlgorithm newSup : CallGraphAlgorithm.values()) {
			if (newSup.hasSmallerPrecision(supremum) && other.hasSmallerOrEqualPrecision(newSup)
					&& hasSmallerOrEqualPrecision(newSup)) {
				supremum = newSup;
			}
		}

		return supremum;
	}

	/**
	 * Calculates the infimum of <code>this</code> and <code>other</code>.
	 * 
	 * @param other
	 * @return the common infimum of <code>this</code> and <code>other</code>.
	 */
	public CallGraphAlgorithm getInfimum(CallGraphAlgorithm other) {
		CallGraphAlgorithm infimum = BOT;

		for (CallGraphAlgorithm newInf : CallGraphAlgorithm.values()) {
			if (newInf.hasGreaterPrecision(infimum) && other.hasGreaterOrEqualPrecision(newInf)
					&& hasGreaterOrEqualPrecision(newInf)) {
				infimum = newInf;
			}
		}

		return infimum;
	}

	/**
	 * Determines whether <code>this</code> call graph algorithm has a strictly
	 * smaller precision then <code>other</code>.
	 * 
	 * @param other
	 *            the call graph algorithm to be compared with.
	 * @return is <code>this</code> less precise then <code>other</code>.
	 */
	public boolean hasSmallerPrecision(CallGraphAlgorithm other) {
		return compare(other) == SmallerPrecision;
	}

	/**
	 * Determines whether <code>this</code> call graph algorithm has a
	 * smaller or equal precision then <code>other</code>.
	 * 
	 * @param other
	 *            the call graph algorithm to be compared with.
	 * @return is <code>this</code> less or equal precise then <code>other</code>.
	 */
	public boolean hasSmallerOrEqualPrecision(CallGraphAlgorithm other) {
		CallGraphAlgorithmOrder order = compare(other);
		return order == SmallerPrecision || order == EqualPrecision;
	}

	/**
	 * Determines whether <code>this</code> call graph algorithm has a strictly
	 * greater precision then <code>other</code>.
	 * 
	 * @param other
	 *            the call graph algorithm to be compared with.
	 * @return is <code>this</code> more precise then <code>other</code>.
	 */
	public boolean hasGreaterPrecision(CallGraphAlgorithm other) {
		return compare(other) == GreaterPrecision;
	}

	/**
	 * Determines whether <code>this</code> call graph algorithm has a strictly
	 * greater or equal precision then <code>other</code>.
	 * 
	 * @param other
	 *            the call graph algorithm to be compared with.
	 * @return is <code>this</code> equal or more precise then <code>other</code>.
	 */
	public boolean hasGreaterOrEqualPrecision(CallGraphAlgorithm other) {
		CallGraphAlgorithmOrder order = compare(other);
		return order == GreaterPrecision || order == EqualPrecision;
	}
}