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

import spade.query.quickgrail.core.entities.Graph.Direction;
import spade.query.quickgrail.core.kernel.AbstractEnvironment;
import spade.query.quickgrail.core.kernel.ExecutionContext;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.utility.TreeStringSerializable;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.storage.neo4j.Neo4jExecutor;

import java.util.ArrayList;

import static spade.query.quickgrail.neo4j.utility.CommonVariables.EDGE_ALIAS;
import static spade.query.quickgrail.neo4j.utility.CommonVariables.VERTEX_ALIAS;
import static spade.query.quickgrail.neo4j.utility.Neo4jUtil.formatSymbol;

/**
 * Get the lineage of a set of vertices in a graph.
 */
public class GetLineage extends Instruction
{
	// Output graph.
	private Neo4jGraph targetGraph;
	// Input graph.
	private Neo4jGraph subjectGraph;
	// Set of starting vertices.
	private Neo4jGraph startGraph;
	// Max depth.
	private Integer depth;
	// Direction (ancestors / descendants, or both).
	private Direction direction;

	public GetLineage(Neo4jGraph targetGraph, Neo4jGraph subjectGraph,
					  Neo4jGraph startGraph, Integer depth, Direction direction)
	{
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.startGraph = startGraph;
		this.depth = depth;
		this.direction = direction;
	}

	@Override
	public void execute(AbstractEnvironment env, ExecutionContext ctx)
	{
		Neo4jExecutor ns = (Neo4jExecutor) ctx.getExecutor();

		String targetVertexTable = targetGraph.getVertexTableName();
		String targetEdgeTable = targetGraph.getEdgeTableName();
		String subjectVertexTable = subjectGraph.getVertexTableName();
		String subjectEdgeTable = subjectGraph.getEdgeTableName();
		String startGraphName = startGraph.getVertexTableName();

		String cypherQuery = "MATCH p=(" + VERTEX_ALIAS;
		if(direction == Direction.kAncestor || direction == Direction.kBoth)
		{
			cypherQuery += ":" + startGraphName;
		}
		cypherQuery += ")-[*0.." + depth + "]-";
		if(direction != Direction.kBoth)
		{
			cypherQuery += ">";
		}
		cypherQuery += "(n";
		if(direction == Direction.kDescendant)
		{
			cypherQuery += ":" + startGraphName;
		}
		cypherQuery += ")";
		cypherQuery += " WHERE ALL(node IN nodes(p) WHERE node:" + subjectVertexTable + ") ";
		if(!env.IsBaseGraph(subjectGraph))
		{
			cypherQuery += " AND ALL(r IN relationships(p) WHERE r.quickgrail_symbol CONTAINS " +
					formatSymbol(subjectEdgeTable) + ")";
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
		return "GetLineage";
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
		inline_field_names.add("startGraph");
		inline_field_values.add(startGraph.getName());
		inline_field_names.add("depth");
		inline_field_values.add(String.valueOf(depth));
		inline_field_names.add("direction");
		inline_field_values.add(direction.name().substring(1));
	}
}
