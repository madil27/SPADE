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
package spade.query.neo4j.execution;

import spade.query.neo4j.entities.Graph;
import spade.query.neo4j.kernel.Environment;
import spade.query.neo4j.utility.TreeStringSerializable;
import spade.storage.neo4j.Neo4jExecutor;
import spade.storage.neo4j.Neo4jExecutor;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spade.query.neo4j.utility.CommonVariables.EDGE_ALIAS;
import static spade.query.neo4j.utility.CommonVariables.PRIMARY_KEY;
import static spade.query.neo4j.utility.CommonVariables.RelationshipTypes.EDGE;
import static spade.query.neo4j.utility.CommonVariables.VERTEX_ALIAS;

/**
 * Remove all duplicated vertices and edges.
 */
public class DistinctifyGraph extends Instruction
{
	// Input graph.
	private Graph targetGraph;
	// Output graph.
	private Graph sourceGraph;

	public DistinctifyGraph(Graph targetGraph, Graph sourceGraph)
	{
		this.targetGraph = targetGraph;
		this.sourceGraph = sourceGraph;
	}

	@Override
	public void execute(Environment env, ExecutionContext ctx)
	{
		if(!targetGraph.getName().equals(sourceGraph.getName()))
		{
			UnionGraph unionGraph = new UnionGraph(targetGraph, sourceGraph);
			unionGraph.execute(env, ctx);
		}
	}

	@Override
	public String getLabel()
	{
		return "DistinctifyGraph";
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
		inline_field_names.add("targetGraph");
		inline_field_values.add(targetGraph.getName());
		inline_field_names.add("sourceGraph");
		inline_field_values.add(sourceGraph.getName());
	}
}
