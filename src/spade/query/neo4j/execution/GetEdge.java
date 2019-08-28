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

import static spade.query.neo4j.kernel.Resolver.formatString;
import static spade.query.neo4j.utility.CommonVariables.EDGE_ALIAS;
import static spade.query.neo4j.utility.CommonVariables.RelationshipTypes.EDGE;
import static spade.query.neo4j.utility.Neo4jUtil.formatSymbol;

/**
 * Get the a set of edges in a graph.
 */
public class GetEdge extends Instruction
{
	// Output graph.
	private Graph targetGraph;
	// Input graph.
	private Graph subjectGraph;
	private String field;
	private String operation;
	private String value;

	public GetEdge(Graph targetGraph, Graph subjectGraph, String field, String operation, String value)
	{
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.field = field;
		this.operation = operation;
		this.value = value;
	}

	@Override
	public void execute(Environment env, ExecutionContext ctx)
	{
		Neo4jExecutor ns = ctx.getExecutor();
		String subjectEdgeTable = subjectGraph.getEdgeTableName();
		String targetEdgeTable = targetGraph.getEdgeTableName();

		String condition = "";
		if(!Environment.IsBaseGraph(subjectGraph))
		{
			condition += " x.quickgrail_symbol CONTAINS " + formatSymbol(subjectEdgeTable);
			if(field != null)
			{
				// TODO: handle wild card columns
				if(!field.equals("*"))
				{
					condition += " AND x." + field + operation + formatString(value, false);
				}
			}
		}
		else
		{
			if(field != null)
			{
				if(!field.equals("*"))
				{
					condition += " x." + field + operation + formatString(value, false);
				}

			}
		}
		String cypherQuery = Neo4jUtil.edgeSymbolQuery(condition, targetEdgeTable);
		ns.executeQuery(cypherQuery);
	}

	@Override
	public String getLabel()
	{
		return "GetEdge";
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
		inline_field_names.add("field");
		inline_field_values.add(field);
		inline_field_names.add("operation");
		inline_field_values.add(operation);
		inline_field_names.add("value");
		inline_field_values.add(value);
	}

}
