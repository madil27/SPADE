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
package spade.query.postgresql.execution;

import spade.query.postgresql.entities.Graph;
import spade.query.postgresql.kernel.Environment;
import spade.query.postgresql.utility.TreeStringSerializable;
import spade.storage.postgresql.PostgresExecutor;

import java.util.ArrayList;

import static spade.query.postgresql.utility.CommonVariables.PRIMARY_KEY;

/**
 * Intersect two graphs (i.e. find common vertices and edges).
 */

public class IntersectGraph extends Instruction
{
	// Output graph.
	private Graph outputGraph;
	// Input graphs.
	private Graph lhsGraph;
	private Graph rhsGraph;

	public IntersectGraph(Graph outputGraph, Graph lhsGraph, Graph rhsGraph)
	{
		this.outputGraph = outputGraph;
		this.lhsGraph = lhsGraph;
		this.rhsGraph = rhsGraph;
	}

	@Override
	public void execute(Environment env, ExecutionContext ctx)
	{
		String outputVertexTable = outputGraph.getVertexTableName();
		String outputEdgeTable = outputGraph.getEdgeTableName();
		String lhsVertexTable = lhsGraph.getVertexTableName();
		String lhsEdgeTable = lhsGraph.getEdgeTableName();
		String rhsVertexTable = rhsGraph.getVertexTableName();
		String rhsEdgeTable = rhsGraph.getEdgeTableName();

		PostgresExecutor qs = ctx.getExecutor();
		qs.executeQuery("INSERT INTO " + outputVertexTable +
				" SELECT " + PRIMARY_KEY + " FROM " + lhsVertexTable +
				" WHERE " + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM " + rhsVertexTable + ");");
		qs.executeQuery("INSERT INTO " + outputEdgeTable +
				" SELECT " + PRIMARY_KEY + " FROM " + lhsEdgeTable +
				" WHERE " + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM " + rhsEdgeTable + ");");
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
