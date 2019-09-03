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
package spade.query.quickgrail.postgresql.execution;

import spade.query.quickgrail.core.kernel.AbstractEnvironment;
import spade.query.quickgrail.core.kernel.ExecutionContext;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.utility.TreeStringSerializable;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.storage.postgresql.PostgresExecutor;

import java.util.ArrayList;

import static spade.query.quickgrail.postgresql.utility.CommonVariables.CHILD_VERTEX_KEY;
import static spade.query.quickgrail.postgresql.utility.CommonVariables.EDGE_TABLE;
import static spade.query.quickgrail.postgresql.utility.CommonVariables.PARENT_VERTEX_KEY;
import static spade.query.quickgrail.postgresql.utility.CommonVariables.PRIMARY_KEY;

/**
 * Collapse all edges whose specified fields are the same.
 */
public class CollapseEdge extends Instruction
{
	// Input graph.
	private PostgreSQLGraph targetGraph;
	// Output graph.
	private PostgreSQLGraph sourceGraph;
	// Fields to check.
	private ArrayList<String> fields;

	public CollapseEdge(PostgreSQLGraph targetGraph, PostgreSQLGraph sourceGraph, ArrayList<String> fields)
	{
		this.targetGraph = targetGraph;
		this.sourceGraph = sourceGraph;
		this.fields = fields;
	}

	@Override
	public void execute(AbstractEnvironment env, ExecutionContext ctx)
	{
		String sourceVertexTable = sourceGraph.getVertexTableName();
		String sourceEdgeTable = sourceGraph.getEdgeTableName();
		String targetVertexTable = targetGraph.getVertexTableName();
		String targetEdgeTable = targetGraph.getEdgeTableName();

		PostgresExecutor qs = (PostgresExecutor) ctx.getExecutor();
		qs.executeQuery("INSERT INTO " + targetVertexTable +
				" SELECT " + PRIMARY_KEY + " FROM " + sourceVertexTable + ";");

		StringBuilder groups = new StringBuilder();

		for(int i = 0; i < fields.size(); ++i)
		{
			groups.append(", \"" + fields.get(i) + "\"");
		}


		qs.executeQuery("INSERT INTO " + targetEdgeTable +
				" SELECT MIN(e." + PRIMARY_KEY + ") FROM " + EDGE_TABLE + " e" +
				" WHERE e." + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM " + sourceEdgeTable + ")" +
				" GROUP BY \"" + CHILD_VERTEX_KEY + "\", \"" + PARENT_VERTEX_KEY + "\"" + groups.toString() + ";");
	}

	// quickstep
	// INSERT INTO targetEdgeTable
	// SELECT MIN(e.id) FROM edge e, edge_anno ea0, edge_anno ea1
	// WHERE e.id IN (SELECT id FROM sourceEdgeTable)
	// AND e.id=ea0.id AND ea0.field='field_value1' AND e.id=ea1.id AND ea1.field='field_value2'
	// GROUP BY src, dst, ea0.value, ea1.value

	// postgres
	// INSERT INTO targetEdgeTable
	// SELECT MIN(e.hash) FROM edge e
	// WHERE e.id IN (SELECT id FROM sourceEdgeTable)
	// GROUP BY src, dst, field1, field2

	@Override
	public String getLabel()
	{
		return "CollapseEdge";
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
	}
}
