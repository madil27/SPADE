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
import spade.query.quickgrail.postgresql.utility.PostgresUtil;
import spade.storage.postgresql.PostgresExecutor;

import java.util.ArrayList;

/**
 * Create an empty QuickGrail graph.
 */
public class CreateEmptyGraph extends Instruction
{
	// Output graph.
	private PostgreSQLGraph graph;

	public CreateEmptyGraph(PostgreSQLGraph graph)
	{
		this.graph = graph;
	}

	@Override
	public void execute(AbstractEnvironment env, ExecutionContext ctx)
	{
		PostgresUtil.CreateEmptyGraph((PostgresExecutor) ctx.getExecutor(), graph);
	}

	@Override
	public String getLabel()
	{
		return "CreateEmptyGraph";
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
		inline_field_names.add("graph");
		inline_field_values.add(graph.getName());
	}
}
