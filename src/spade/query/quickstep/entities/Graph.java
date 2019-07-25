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
package spade.query.quickstep.entities;

import spade.query.quickstep.utility.TreeStringSerializable;

import java.util.ArrayList;

/**
 * Intermediate representation for a graph in QuickGrail optimizer.
 */
public class Graph extends Entity
{
	// Each graph consists of two tables: <name>_vertex and <name>_edge.
	private String name;

	public Graph(String name)
	{
		this.name = name;
	}

	public static String GetBaseVertexTableName()
	{
		return "vertex";
	}

	public static String GetBaseVertexAnnotationTableName()
	{
		return "vertex_anno";
	}

	public static String GetBaseEdgeTableName()
	{
		return "edge";
	}

	public static String GetBaseEdgeAnnotationTableName()
	{
		return "edge_anno";
	}

	public static String GetBaseTableName(Component component)
	{
		return component == Component.kVertex ? GetBaseVertexTableName() : GetBaseEdgeTableName();
	}

	public static String GetBaseAnnotationTableName(Component component)
	{
		return component == Component.kVertex
				? GetBaseVertexAnnotationTableName() :
				GetBaseEdgeAnnotationTableName();
	}

	public String getName()
	{
		return name;
	}

	public String getVertexTableName()
	{
		return name + "_vertex";
	}

	public String getEdgeTableName()
	{
		return name + "_edge";
	}

	public String getTableName(Component component)
	{
		return component == Component.kVertex ? getVertexTableName() : getEdgeTableName();
	}

	@Override
	public spade.query.quickstep.entities.EntityType getEntityType()
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

	public enum Component
	{
		kVertex,
		kEdge
	}
}
