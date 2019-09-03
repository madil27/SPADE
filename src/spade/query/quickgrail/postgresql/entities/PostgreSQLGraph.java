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
package spade.query.quickgrail.postgresql.entities;

import spade.query.quickgrail.core.entities.EntityType;
import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.utility.TreeStringSerializable;

import java.util.ArrayList;

/**
 * Intermediate representation for a PostgreSQL graph in QuickGrail optimizer.
 */
public class PostgreSQLGraph extends Graph
{
	// Each graph consists of two tables: <name>_vertex and <name>_edge.
	private String name;

	public PostgreSQLGraph(String name)
	{
		this.name = name;
	}

	public static String GetBaseVertexTableName()
	{
		return "vertex";
	}

	public static String GetBaseVertexAnnotationTableName()
	{
		return "vertex";
	}

	public static String GetBaseEdgeTableName()
	{
		return "edge";
	}

	public static String GetBaseEdgeAnnotationTableName()
	{
		return "edge";
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

	@Override
	public String getVertexTableName()
	{
		return name + "_vertex";
	}

	@Override
	public String getEdgeTableName()
	{
		return name + "_edge";
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
