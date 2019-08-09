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

import static spade.query.neo4j.utility.CommonVariables.CHILD_VERTEX_KEY;
import static spade.query.neo4j.utility.CommonVariables.EDGE_ALIAS;
import static spade.query.neo4j.utility.CommonVariables.EDGE_TABLE;
import static spade.query.neo4j.utility.CommonVariables.PARENT_VERTEX_KEY;
import static spade.query.neo4j.utility.CommonVariables.PRIMARY_KEY;
import static spade.query.neo4j.utility.CommonVariables.VERTEX_ALIAS;

/**
 * Similar to GetPath but the result graph only contains vertices / edges that
 * are on the shortest paths.
 * <p>
 * Warning: This operation could be very slow when the input graph is large.
 */
public class GetShortestPath extends Instruction
{
	// Output graph.
	private Graph targetGraph;
	// Input graph.
	private Graph subjectGraph;
	// Set of source vertices.
	private Graph sourceGraph;
	// Set of destination vertices.
	private Graph destinationGraph;
	// Max path length.
	private Integer maxDepth;

	public GetShortestPath(Graph targetGraph, Graph subjectGraph,
						   Graph srcGraph, Graph dstGraph,
						   Integer maxDepth)
	{
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.sourceGraph = srcGraph;
		this.destinationGraph = dstGraph;
		this.maxDepth = maxDepth;
	}

	@Override
	public void execute(Environment env, ExecutionContext ctx)
	{
		Neo4jExecutor ns = ctx.getExecutor();

		String targetVertexTable = targetGraph.getVertexTableName();
		String targetEdgeTable = targetGraph.getEdgeTableName();
		String subjectVertexTable = subjectGraph.getVertexTableName();
		String subjectEdgeTable = subjectGraph.getEdgeTableName();
		String sourceGraphName = sourceGraph.getVertexTableName();
		String destinationGraphName = destinationGraph.getVertexTableName();

		String cypherQuery = "MATCH p=shortestPath(" + VERTEX_ALIAS + ":" + sourceGraphName + ")-[*0.." + maxDepth + "]->";
		cypherQuery += "(n" + ":" + destinationGraphName + "))";
		cypherQuery += " WHERE ALL(node IN nodes(p) WHERE node:" + subjectVertexTable + ") ";
		if(!Environment.IsBaseGraph(subjectGraph))
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
		return "GetShortestPath";
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
