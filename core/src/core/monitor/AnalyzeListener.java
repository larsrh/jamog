
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                           *
 * Copyright 2009 Lars Hupel, Torben Maack, Sylvester Tremmel                *
 *                                                                           *
 * This file is part of Jamog.                                               *
 *                                                                           *
 * Jamog is free software: you can redistribute it and/or modify             *
 * it under the terms of the GNU General Public License as published by      *
 * the Free Software Foundation; version 3.                                  *
 *                                                                           *
 * Jamog is distributed in the hope that it will be useful,                  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of            *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the              *
 * GNU General Public License for more details.                              *
 *                                                                           *
 * You should have received a copy of the GNU General Public License         *
 * along with Jamog. If not, see <http://www.gnu.org/licenses/>.             *
 *                                                                           *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package core.monitor;

/**
 * The {@code AnalyzeListener} interface is used to get information about the
 * analysing process when a {@link core.sim.Simulator Simulator} is created.
 * The analysing process is a nondisjunct multipass process on two different
 * sets of data. Before the analyze starts, the listener is notified by the
 * {@link #initTotals(int, int) initTotals(int, int)} method about the size of
 * the data to process. During the analyze process, the two methods {@link
 * #increaseCalculators(int, int) increaseCalculators(int, int)} and {@link
 * #increasePriorityPairs(int, int) increasePriorityPairs(int, int)} are
 * invoked to notify the listerner about finished data subsets in a pass.
 *
 * @see core.sim.Simulator Simulator
 *
 * @author torben
 */
public interface AnalyzeListener
{
	/**
	 * Is invoked at the beginning of the analyze process to notify the
	 * listener about the data set sizes.
	 *
	 * @param calculators    The size of the calculators set
	 * @param priority_pairs The size of the priority pairs set
	 */
	public void initTotals(int calculators, int priority_pairs);

	/**
	 * Is invoked during the analyze process to notify the listener about
	 * finished calculator subsets in a pass. There are six passes for the
	 * calculator set, and their execution order is not sequential for the
	 * whole calculator set.
	 *
	 * @param pass     The pass of the finished subset
	 * @param finished The count of calculators in the subset
	 */
	public void increaseCalculators(int pass, int finished);
	
	/**
	 * Is invoked during the analyze process to notify the listener about
	 * finished priority pair subsets in a pass. There are two passes for the
	 * priority pair set.
	 *
	 * @param pass     The pass of the finished subset
	 * @param finished The count of the priority pairs in the subset
	 */
	public void increasePriorityPairs(int pass, int finished);
}
