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
import spade.storage.kafka.Vertex;
import spade.storage.neo4j.Neo4jExecutor;

import java.util.ArrayList;

import static spade.query.neo4j.utility.CommonVariables.EDGE_ALIAS;
import static spade.query.neo4j.utility.CommonVariables.NodeTypes.VERTEX;
import static spade.query.neo4j.utility.CommonVariables.RelationshipTypes.EDGE;
import static spade.query.neo4j.utility.CommonVariables.VERTEX_ALIAS;

/**
 * Subtract one graph from the other.
 */
public class SubtractGraph extends Instruction
{
	// Output graph.
	private Graph outputGraph;
	// Minuend graph.
	private Graph minuendGraph;
	// Subtrahend graph.
	private Graph subtrahendGraph;
	// Graph components that should be involved in the operation (vertices / edges, or both).
	private Graph.Component component;

	public SubtractGraph(Graph outputGraph, Graph minuendGraph, Graph subtrahendGraph, Graph.Component component)
	{
		this.outputGraph = outputGraph;
		this.minuendGraph = minuendGraph;
		this.subtrahendGraph = subtrahendGraph;
		this.component = component;
	}

	@Override
	public void execute(Environment env, ExecutionContext ctx)
	{
		String outputVertexTable = outputGraph.getVertexTableName();
		String outputEdgeTable = outputGraph.getEdgeTableName();
		String minuendVertexTable = minuendGraph.getVertexTableName();
		String minuendEdgeTable = minuendGraph.getEdgeTableName();
		String subtrahendVertexTable = subtrahendGraph.getVertexTableName();
		String subtrahendEdgeTable = subtrahendGraph.getEdgeTableName();

		Neo4jExecutor ns = ctx.getExecutor();
		String cypherQuery = "";
		if(component == null || component == Graph.Component.kVertex)
		{
			String condition = "x:" + minuendVertexTable +
					" AND NOT x:" + subtrahendVertexTable;
			cypherQuery = Neo4jUtil.vertexLabelQuery(condition, VERTEX.toString(), outputVertexTable);
			// allows execution of multiple MATCH queries in one statement
			cypherQuery += " WITH count(*) as dummy \n";
		}
		if(component == null || component == Graph.Component.kEdge)
		{
			String condition = "";
			if(Environment.IsBaseGraph(minuendGraph))
			{
				condition += " TRUE ";
			}
			else
			{
				condition += " x.quickgrail_symbol CONTAINS " + Neo4jUtil.formatSymbol(minuendEdgeTable);
			}
			if(Environment.IsBaseGraph(subtrahendGraph))
			{
				condition += " AND FALSE";
			}
			else
			{
				condition += " AND NOT x.quickgrail_symbol CONTAINS " + Neo4jUtil.formatSymbol(subtrahendEdgeTable);
			}
			cypherQuery += Neo4jUtil.edgeSymbolQuery(condition, outputEdgeTable);
		}
		ns.executeQuery(cypherQuery);
	}

	@Override
	public String getLabel()
	{
		return "SubtractGraph";
	}

	@Override
	protected void getFieldStringItems(ArrayList<String> inline_field_names, ArrayList<String> inline_field_values,
									   ArrayList<String> non_container_child_field_names, ArrayList<TreeStringSerializable> non_container_child_fields,
									   ArrayList<String> container_child_field_names,
									   ArrayList<ArrayList<? extends TreeStringSerializable>> container_child_fields)
	{
		inline_field_names.add("outputGraph");
		inline_field_values.add(outputGraph.getName());
		inline_field_names.add("minuendGraph");
		inline_field_values.add(minuendGraph.getName());
		inline_field_names.add("subtrahendGraph");
		inline_field_values.add(subtrahendGraph.getName());
		if(this.component != null)
		{
			inline_field_names.add("component");
			inline_field_values.add(component.name());
		}
	}
}
