/*
 --------------------------------------------------------------------------------
 SPADE - Support for Provenance Auditing in Distributed Environments.
 Copyright (C) 2018 SRI International

 This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.
 --------------------------------------------------------------------------------
 */
package spade.query.quickgrail.quickstep.execution;

import spade.query.quickgrail.core.kernel.AbstractEnvironment;
import spade.query.quickgrail.core.kernel.ExecutionContext;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.utility.TreeStringSerializable;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;
import spade.storage.quickstep.QuickstepExecutor;

import java.util.ArrayList;

/**
 * Intersect two graphs (i.e. find common vertices and edges).
 */

public class IntersectGraph extends Instruction
{
	// Output graph.
	private QuickstepGraph outputGraph;
	// Input graphs.
	private QuickstepGraph lhsGraph;
	private QuickstepGraph rhsGraph;

	public IntersectGraph(QuickstepGraph outputGraph, QuickstepGraph lhsGraph, QuickstepGraph rhsGraph)
	{
		this.outputGraph = outputGraph;
		this.lhsGraph = lhsGraph;
		this.rhsGraph = rhsGraph;
	}

	@Override
	public void execute(AbstractEnvironment env, ExecutionContext ctx)
	{
		String outputVertexTable = outputGraph.getVertexTableName();
		String outputEdgeTable = outputGraph.getEdgeTableName();
		String lhsVertexTable = lhsGraph.getVertexTableName();
		String lhsEdgeTable = lhsGraph.getEdgeTableName();
		String rhsVertexTable = rhsGraph.getVertexTableName();
		String rhsEdgeTable = rhsGraph.getEdgeTableName();

		QuickstepExecutor qs = (QuickstepExecutor) ctx.getExecutor();
		qs.executeQuery("\\analyzerange " + rhsVertexTable + " " + rhsEdgeTable + "\n");
		qs.executeQuery("INSERT INTO " + outputVertexTable +
				" SELECT id FROM " + lhsVertexTable +
				" WHERE id IN (SELECT id FROM " + rhsVertexTable + ");");
		qs.executeQuery("INSERT INTO " + outputEdgeTable +
				" SELECT id FROM " + lhsEdgeTable +
				" WHERE id IN (SELECT id FROM " + rhsEdgeTable + ");");
	}

	@Override
	public String getLabel()
	{
		return "IntersectGraph";
	}

	@Override
	protected void getFieldStringItems(
			ArrayList<String> inline_field_names,
			ArrayList<String> inline_field_values,
			ArrayList<String> non_container_child_field_names,
			ArrayList<TreeStringSerializable> non_container_child_fields,
			ArrayList<String> container_child_field_names,
			ArrayList<ArrayList<? extends TreeStringSerializable>> container_child_fields)
	{
		inline_field_names.add("outputGraph");
		inline_field_values.add(outputGraph.getName());
		inline_field_names.add("lhsGraph");
		inline_field_values.add(lhsGraph.getName());
		inline_field_names.add("rhsGraph");
		inline_field_values.add(rhsGraph.getName());
	}
}
