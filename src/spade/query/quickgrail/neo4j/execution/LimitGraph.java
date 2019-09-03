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
import static spade.query.quickgrail.neo4j.utility.CommonVariables.NodeTypes.VERTEX;
import static spade.query.quickgrail.neo4j.utility.CommonVariables.RelationshipTypes.EDGE;
import static spade.query.quickgrail.neo4j.utility.CommonVariables.VERTEX_ALIAS;
import static spade.query.quickgrail.neo4j.utility.Neo4jUtil.formatSymbol;

/**
 * Sample a subset of vertices / edges from a graph.
 */
public class LimitGraph extends Instruction
{
	// Output graph.
	private Neo4jGraph targetGraph;
	// Input graph.
	private Neo4jGraph sourceGraph;
	// The maximum number of vertices / edges to sample.
	private int limit;

	public LimitGraph(Neo4jGraph targetGraph, Neo4jGraph sourceGraph, int limit)
	{
		this.targetGraph = targetGraph;
		this.sourceGraph = sourceGraph;
		this.limit = limit;
	}

	@Override
	public void execute(AbstractEnvironment env, ExecutionContext ctx)
	{
		Neo4jExecutor ns = (Neo4jExecutor) ctx.getExecutor();
		String sourceVertexTable = sourceGraph.getVertexTableName();
		String sourceEdgeTable = sourceGraph.getEdgeTableName();
		String targetVertexTable = targetGraph.getVertexTableName();
		String targetEdgeTable = targetGraph.getEdgeTableName();

		String cypherQuery = "MATCH (" + VERTEX_ALIAS + ":" + sourceVertexTable + ") WITH DISTINCT " + VERTEX_ALIAS +
				" LIMIT " + limit + " WITH collect(" + VERTEX_ALIAS + ") AS nodes" +
				" MATCH (n:" + VERTEX.toString() + ")" +
				" WITH REDUCE(s = {a:[], d:[]}, x IN COLLECT(n) | " +
				" CASE  WHEN x IN nodes THEN {a: s.a+x, d: s.d} " +
				" WHEN '" + targetVertexTable + "' IN labels(x) THEN {a: s.a, d: s.d+x} " +
				" ELSE {a:s.a, d:s.d} END) AS actions, nodes " +
				" FOREACH (d IN actions.d | REMOVE d:" + targetVertexTable + ")" +
				" FOREACH(a IN actions.a | SET a:" + targetVertexTable + ")";

		// allows execution of multiple queries in one statement
		cypherQuery += " WITH count(*) as dummy \n";

		String addSymbol = " SET a.quickgrail_symbol = CASE WHEN NOT EXISTS(a.quickgrail_symbol) THEN " +
				formatSymbol(targetEdgeTable) + " WHEN a.quickgrail_symbol CONTAINS " +
				formatSymbol(targetEdgeTable) + " THEN a.quickgrail_symbol ELSE a.quickgrail_symbol + " +
				formatSymbol(targetEdgeTable) + " END";
		String removeSymbol = "SET d.quickgrail_symbol = " +
				"replace(d.quickgrail_symbol, " + formatSymbol(targetEdgeTable) + ", '')";

		String condition = "";
		if(!env.IsBaseGraph(sourceGraph))
		{
			condition = " WHERE " + EDGE_ALIAS + ".quickgrail_symbol CONTAINS " + formatSymbol(sourceEdgeTable);
		}
		cypherQuery += "MATCH ()-[" + EDGE_ALIAS + ":" + EDGE.toString() + "]-() " +
				condition +
				" WITH DISTINCT " + EDGE_ALIAS + " LIMIT " + limit +
				" WITH collect(e) AS edges " +
				" MATCH ()-[n]->()" +
				" WITH REDUCE(s = {a:[], d:[]}, x IN COLLECT(n) | " +
				" CASE  WHEN x IN edges THEN {a: s.a+x, d: s.d} " +
				" WHEN x.quickgrail_symbol CONTAINS " + formatSymbol(targetEdgeTable) + " THEN {a: s.a, d: s.d+x} " +
				" ELSE {a:s.a, d:s.d} END) AS actions, edges " +
				" FOREACH (d IN actions.d | " + removeSymbol + ")" +
				" FOREACH(a IN actions.a | " + addSymbol + ")";

		ns.executeQuery(cypherQuery);
	}

	@Override
	public String getLabel()
	{
		return "LimitGraph";
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
		inline_field_names.add("limit");
		inline_field_values.add(String.valueOf(limit));
	}
}
