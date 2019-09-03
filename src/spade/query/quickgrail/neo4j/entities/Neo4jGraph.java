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
package spade.query.quickgrail.neo4j.entities;

import spade.query.quickgrail.core.entities.EntityType;
import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.utility.TreeStringSerializable;
import spade.query.quickgrail.neo4j.utility.CommonVariables;

import java.util.ArrayList;

import static spade.query.quickgrail.neo4j.utility.Neo4jUtil.removeDollar;

/**
 * Intermediate representation for a graph in QuickGrail optimizer.
 */
public class Neo4jGraph extends Graph
{
	// Each graph consists of two tables: <name>_vertex and <name>_edge.
	private String name;

	public Neo4jGraph(String name)
	{
		this.name = name;
	}

	public static String GetBaseVertexTableName()
	{
		return CommonVariables.NodeTypes.VERTEX.toString();
	}

	public static String GetBaseVertexAnnotationTableName()
	{
		return CommonVariables.NodeTypes.VERTEX.toString();
	}

	public static String GetBaseEdgeTableName()
	{
		return CommonVariables.RelationshipTypes.EDGE.toString();
	}

	public static String GetBaseEdgeAnnotationTableName()
	{
		return CommonVariables.RelationshipTypes.EDGE.toString();
	}

	public static String GetBaseTableName(GraphComponent component)
	{
		return component == GraphComponent.kVertex ? GetBaseVertexTableName() : GetBaseEdgeTableName();
	}

	public static String GetBaseAnnotationTableName(GraphComponent component)
	{
		return component == GraphComponent.kVertex
				? GetBaseVertexAnnotationTableName() :
				GetBaseEdgeAnnotationTableName();
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String getVertexTableName()
	{
		if(name.equals("$base"))
			return CommonVariables.NodeTypes.VERTEX.toString();
		return removeDollar(name);
	}

	@Override
	public String getEdgeTableName()
	{
		if(name.equals("$base"))
			return CommonVariables.RelationshipTypes.EDGE.toString();
		return removeDollar(name);
	}

	@Override
	public String getTableName(GraphComponent component)
	{
		return component == GraphComponent.kVertex ? getVertexTableName() : getEdgeTableName();
	}

	@Override
	public EntityType getEntityType()
	{
		return EntityType.kGraph;
	}

	@Override
	public String getLabel()
	{
		return "Graph";
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
		inline_field_names.add("name");
		inline_field_values.add(name);
	}
}
