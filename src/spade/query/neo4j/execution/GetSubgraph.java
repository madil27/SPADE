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

import static spade.query.neo4j.utility.CommonVariables.CHILD_VERTEX_KEY;
import static spade.query.neo4j.utility.CommonVariables.EDGE_ALIAS;
import static spade.query.neo4j.utility.CommonVariables.EDGE_TABLE;
import static spade.query.neo4j.utility.CommonVariables.PARENT_VERTEX_KEY;
import static spade.query.neo4j.utility.CommonVariables.PRIMARY_KEY;
import static spade.query.neo4j.utility.CommonVariables.RelationshipTypes.EDGE;
import static spade.query.neo4j.utility.CommonVariables.VERTEX_ALIAS;

/**
 * Let $S be the subject graph and $T be the skeleton graph.
 * The operation $S.getSubgraph($T) is to find all the vertices and edges that
 * are spanned by the skeleton graph.
 */
public class GetSubgraph extends Instruction
{
	private Graph targetGraph;
	private Graph subjectGraph;
	private Graph skeletonGraph;

	public GetSubgraph(Graph targetGraph, Graph subjectGraph, Graph skeletonGraph)
	{
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.skeletonGraph = skeletonGraph;
	}

	@Override
	public void execute(Environment env, ExecutionContext ctx)
	{
		Neo4jExecutor ns = ctx.getExecutor();
		String targetVertexTable = targetGraph.getVertexTableName();
		String targetEdgeTable = targetGraph.getEdgeTableName();
		String subjectVertexTable = subjectGraph.getVertexTableName();
		String skeletonVertexTable = skeletonGraph.getVertexTableName();

		String cypherQuery = "MATCH (" + VERTEX_ALIAS + ":" + subjectVertexTable + ":" + skeletonVertexTable + ")" +
				"-[" + EDGE_ALIAS + ":" + EDGE.toString() + "]-" +
				"(n:" + subjectVertexTable + ":" + skeletonVertexTable + ")" +
				" SET " + VERTEX_ALIAS + ":" + targetVertexTable +

				" SET " + EDGE_ALIAS + ".quickgrail_symbol = CASE WHEN NOT EXISTS(" + EDGE_ALIAS +
				".quickgrail_symbol) THEN '," + targetEdgeTable + ",'" +
				" WHEN " + EDGE_ALIAS + ".quickgrail_symbol CONTAINS '," +
				targetEdgeTable + ",' THEN " + EDGE_ALIAS + ".quickgrail_symbol " +
				" ELSE " + EDGE_ALIAS + ".quickgrail_symbol + '," + targetEdgeTable + ",' END";
		ns.executeQuery(cypherQuery);
	}

	@Override
	public String getLabel()
	{
		return "GetSubgraph";
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
		inline_field_names.add("skeletonGraph");
		inline_field_values.add(skeletonGraph.getName());
	}
}
