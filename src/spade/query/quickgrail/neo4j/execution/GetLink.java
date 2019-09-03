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
import spade.storage.neo4j.Neo4jExecutor;

import java.util.ArrayList;

import static spade.query.quickgrail.neo4j.utility.CommonVariables.EDGE_ALIAS;
import static spade.query.quickgrail.neo4j.utility.CommonVariables.VERTEX_ALIAS;

/**
 * Similar to GetPath but treats the graph edges as indirected.
 */
public class GetLink extends Instruction
{
	// Output graph.
	private Neo4jGraph targetGraph;
	// Input graph.
	private Neo4jGraph subjectGraph;
	// Set of source vertices.
	private Neo4jGraph sourceGraph;
	// Set of destination vertices.
	private Neo4jGraph destinationGraph;
	// Max path length.
	private Integer maxDepth;

	public GetLink(Neo4jGraph targetGraph, Neo4jGraph subjectGraph,
				   Neo4jGraph srcGraph, Neo4jGraph dstGraph,
				   Integer maxDepth)
	{
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.sourceGraph = srcGraph;
		this.destinationGraph = dstGraph;
		this.maxDepth = maxDepth;
	}

	@Override
	public void execute(AbstractEnvironment env, ExecutionContext ctx)
	{
		Neo4jExecutor ns = (Neo4jExecutor) ctx.getExecutor();

		String targetVertexTable = targetGraph.getVertexTableName();
		String targetEdgeTable = targetGraph.getEdgeTableName();
		String subjectVertexTable = subjectGraph.getVertexTableName();
		String subjectEdgeTable = subjectGraph.getEdgeTableName();
		String sourceGraphName = sourceGraph.getVertexTableName();
		String destinationGraphName = destinationGraph.getVertexTableName();

		// only difference between GatPath is '-' vs '->' at the end of this line.
		String cypherQuery = "MATCH p=(" + VERTEX_ALIAS + ":" + sourceGraphName + ")-[*0.." + maxDepth + "]-";
		cypherQuery += "(n" + ":" + destinationGraphName + ")";
		cypherQuery += " WHERE ALL(node IN nodes(p) WHERE node:" + subjectVertexTable + ") ";
		if(!env.IsBaseGraph(subjectGraph))
		{
			cypherQuery += " AND ALL(r IN relationships(p) WHERE r.quickgrail_symbol CONTAINS ',"
					+ subjectEdgeTable + ",')";
		}
		cypherQuery += " WITH p UNWIND nodes(p) AS node SET node:" + targetVertexTable + " WITH p ";
		cypherQuery += " UNWIND relationships(p) AS " + EDGE_ALIAS + " SET " + EDGE_ALIAS +
				".quickgrail_symbol = CASE WHEN NOT EXISTS(" +
				EDGE_ALIAS + ".quickgrail_symbol) THEN '," + targetEdgeTable + ",'" + " WHEN " +
				EDGE_ALIAS + ".quickgrail_symbol CONTAINS '," + targetEdgeTable + ",' THEN " +
				EDGE_ALIAS + ".quickgrail_symbol " + " ELSE " + EDGE_ALIAS + ".quickgrail_symbol + '," +
				targetEdgeTable + ",' END";

		ns.executeQuery(cypherQuery);
	}

	@Override
	public String getLabel()
	{
		return "GetLink";
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
		inline_field_names.add("subjectGraph");
		inline_field_values.add(subjectGraph.getName());
		inline_field_names.add("sourceGraph");
		inline_field_values.add(sourceGraph.getName());
		inline_field_names.add("destinationGraph");
		inline_field_values.add(destinationGraph.getName());
		inline_field_names.add("maxDepth");
		inline_field_values.add(String.valueOf(maxDepth));
	}
}
