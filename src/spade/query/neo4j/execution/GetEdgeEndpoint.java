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
import static spade.query.neo4j.utility.CommonVariables.EDGE_TABLE;
import static spade.query.neo4j.utility.CommonVariables.PARENT_VERTEX_KEY;
import static spade.query.neo4j.utility.CommonVariables.PRIMARY_KEY;

/**
 * Get end points of all edges in a graph.
 */
public class GetEdgeEndpoint extends Instruction
{
	// Output graph.
	private Graph targetGraph;
	// Input graph.
	private Graph subjectGraph;
	// End-point component (source / destination, or both)
	private Component component;

	public GetEdgeEndpoint(Graph targetGraph, Graph subjectGraph, Component component)
	{
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.component = component;
	}

	@Override
	public void execute(Environment env, ExecutionContext ctx)
	{
		Neo4jExecutor ns = ctx.getExecutor();

		String targetVertexTable = targetGraph.getVertexTableName();
		String subjectEdgeTable = subjectGraph.getEdgeTableName();

		ns.executeQuery("DROP TABLE IF EXISTS m_answer;" +
				"CREATE TABLE m_answer (" + PRIMARY_KEY + " UUID);");

		if(component == Component.kSource || component == Component.kBoth)
		{
			ns.executeQuery("INSERT INTO m_answer SELECT \"" + CHILD_VERTEX_KEY + "\" FROM " + EDGE_TABLE +
					" WHERE " + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM " + subjectEdgeTable + ");");
		}

		if(component == Component.kDestination || component == Component.kBoth)
		{
			ns.executeQuery("INSERT INTO m_answer SELECT \"" + PARENT_VERTEX_KEY + "\" FROM " + EDGE_TABLE +
					" WHERE " + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM " + subjectEdgeTable + ");");
		}

		ns.executeQuery("INSERT INTO " + targetVertexTable + " SELECT " + PRIMARY_KEY + " FROM m_answer GROUP BY " +
				PRIMARY_KEY + ";" + "DROP TABLE IF EXISTS m_answer;");
	}

	@Override
	public String getLabel()
	{
		return "GetEdgeEndpoint";
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
		inline_field_names.add("component");
		inline_field_values.add(component.name().substring(1));
	}

	public enum Component
	{
		kSource,
		kDestination,
		kBoth
	}

}
