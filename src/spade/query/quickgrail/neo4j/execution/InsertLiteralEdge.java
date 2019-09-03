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
package spade.query.quickgrail.neo4j.execution;

import spade.query.quickgrail.core.kernel.AbstractEnvironment;
import spade.query.quickgrail.core.kernel.ExecutionContext;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.utility.TreeStringSerializable;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;

import java.util.ArrayList;

/**
 * NOT SUPPORTED BY NEO4J
 * TODO: Support execution of general cypher query
 * Insert a list of edges into a graph by hash.
 */
public class InsertLiteralEdge extends Instruction
{
	// The target graph to insert the edges.
	private Neo4jGraph targetGraph;
	// Edge hashes to be inserted.
	private ArrayList<String> edges;

	public InsertLiteralEdge(Neo4jGraph targetGraph, ArrayList<String> edges)
	{
		this.targetGraph = targetGraph;
		this.edges = edges;
	}

	@Override
	public void execute(AbstractEnvironment env, ExecutionContext ctx)
	{
	}

	@Override
	public String getLabel()
	{
		return "InsertLiteralEdge";
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
		inline_field_names.add("edges");
		inline_field_values.add("{" + String.join(",", edges) + "}");
	}
}
