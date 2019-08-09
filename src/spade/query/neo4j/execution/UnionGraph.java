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

import java.util.ArrayList;

import static spade.query.neo4j.utility.CommonVariables.EDGE_ALIAS;
import static spade.query.neo4j.utility.CommonVariables.RelationshipTypes.EDGE;
import static spade.query.neo4j.utility.CommonVariables.VERTEX_ALIAS;

/**
 * Union one graph into the other.
 */
public class UnionGraph extends Instruction
{
	// The target graph.
	private Graph targetGraph;
	// The source graph to be union-ed into the target graph.
	private Graph sourceGraph;

	public UnionGraph(Graph targetGraph, Graph sourceGraph)
	{
		this.targetGraph = targetGraph;
		this.sourceGraph = sourceGraph;
	}

	@Override
	public void execute(Environment env, ExecutionContext ctx)
	{
		// go into sourceGraph and put labels of targetGraph in all its edges and vertices
		String sourceVertexTable = sourceGraph.getVertexTableName();
		String sourceEdgeTable = sourceGraph.getEdgeTableName();
		String targetVertexTable = targetGraph.getVertexTableName();
		String targetEdgeTable = targetGraph.getEdgeTableName();

		Neo4jExecutor ns = ctx.getExecutor();
		// union vertices
		String cypherQuery = "MATCH (" + VERTEX_ALIAS + ":" + sourceVertexTable + ") SET " +
				VERTEX_ALIAS + ":" + targetVertexTable;

		// allows execution of multiple queries in one statement
		cypherQuery += " WITH count(*) as dummy \n";

		cypherQuery += "MATCH (child:" + sourceVertexTable + ")-[" + EDGE_ALIAS + ":" + EDGE.toString() + "]->(parent:" +
				sourceVertexTable + ") ";
		if(!Environment.IsBaseGraph(sourceGraph))
		{
			cypherQuery += " WHERE " + EDGE_ALIAS + ".quickgrail_symbol CONTAINS '," + sourceEdgeTable + ",'";
		}
		// add vertex label
		cypherQuery += " SET child:" + targetVertexTable + " SET parent:" + targetVertexTable;
		// add edge label
		cypherQuery += " SET " + EDGE_ALIAS + ".quickgrail_symbol = CASE WHEN NOT EXISTS(" + EDGE_ALIAS +
				".quickgrail_symbol) THEN '," + targetEdgeTable + ",'" +
				" WHEN " + EDGE_ALIAS + ".quickgrail_symbol CONTAINS '," +
				targetEdgeTable + ",' THEN " + EDGE_ALIAS + ".quickgrail_symbol " +
				" ELSE " + EDGE_ALIAS + ".quickgrail_symbol + '," + targetEdgeTable + ",' END";

		ns.executeQuery(cypherQuery);
	}

	@Override
	public String getLabel()
	{
		return "UnionGraph";
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
