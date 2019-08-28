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
import spade.query.neo4j.utility.Neo4jUtil;
import spade.query.neo4j.utility.TreeStringSerializable;
import spade.storage.neo4j.Neo4jExecutor;

import java.util.ArrayList;

import static spade.query.neo4j.utility.CommonVariables.EDGE_ALIAS;
import static spade.query.neo4j.utility.CommonVariables.NodeTypes.VERTEX;
import static spade.query.neo4j.utility.CommonVariables.RelationshipTypes.EDGE;
import static spade.query.neo4j.utility.CommonVariables.VERTEX_ALIAS;
import static spade.query.neo4j.utility.Neo4jUtil.formatSymbol;

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

		Neo4jExecutor ns = ctx.getExecutor();
		String condition = "x:" + lhsVertexTable + ":" + rhsVertexTable;
		String cypherQuery = Neo4jUtil.vertexLabelQuery(condition, VERTEX.toString(), outputVertexTable);

		// allows execution of multiple queries in one statement
		cypherQuery += " WITH count(*) as dummy \n";

		cypherQuery += "MATCH ()-[" + EDGE_ALIAS + ":" + EDGE.toString() + "]->() ";
		condition = "";
		boolean isBase = false;
		if(!Environment.IsBaseGraph(lhsGraph))
		{
			condition += " x.quickgrail_symbol CONTAINS " + formatSymbol(lhsEdgeTable);
			isBase = true;
		}
		if(!Environment.IsBaseGraph(rhsGraph))
		{
			if(isBase)
			{
				condition += " AND ";
			}
			condition += " x.quickgrail_symbol CONTAINS " + formatSymbol(rhsEdgeTable);
		}
		cypherQuery += Neo4jUtil.edgeSymbolQuery(condition, outputEdgeTable);

		ns.executeQuery(cypherQuery);
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
