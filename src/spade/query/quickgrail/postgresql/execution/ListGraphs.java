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
import spade.query.quickgrail.core.types.LongType;
import spade.query.quickgrail.core.types.StringType;
import spade.query.quickgrail.core.utility.ResultTable;
import spade.query.quickgrail.core.utility.Schema;
import spade.query.quickgrail.core.utility.TreeStringSerializable;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.postgresql.utility.Environment;
import spade.query.quickgrail.postgresql.utility.PostgresUtil;
import spade.storage.postgresql.PostgresExecutor;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * List all existing graphs in Postgres storage.
 */
public class ListGraphs extends Instruction
{
	private String style;

	public ListGraphs(String style)
	{
		this.style = style;
	}

	@Override
	public void execute(AbstractEnvironment env, ExecutionContext ctx)
	{
		PostgresExecutor qs = (PostgresExecutor) ctx.getExecutor();
		ResultTable table = new ResultTable();

		Map<String, String> symbols = env.getSymbols();
		for(Entry<String, String> entry : symbols.entrySet())
		{
			String symbol = entry.getKey();
			if(symbol.startsWith("$"))
			{
				addSymbol(qs, symbol, new PostgreSQLGraph(entry.getValue()), table);
			}
		}
		addSymbol(qs, "$base", Environment.kBaseGraph, table);

		Schema schema = new Schema();
		schema.addColumn("Graph Name", StringType.GetInstance());
		if(!style.equals("name"))
		{
			schema.addColumn("Number of Vertices", LongType.GetInstance());
			schema.addColumn("Number of Edges", LongType.GetInstance());
		}
		table.setSchema(schema);

		ctx.addResponse(table.toString());
	}

	private void addSymbol(PostgresExecutor qs, String symbol,
						   PostgreSQLGraph graph, ResultTable table)
	{
		ResultTable.Row row = new ResultTable.Row();
		row.add(symbol);
		if(!style.equals("name"))
		{
			row.add(PostgresUtil.GetNumVertices(qs, graph));
			row.add(PostgresUtil.GetNumEdges(qs, graph));
		}
		table.addRow(row);
	}

	@Override
	public String getLabel()
	{
		return "ListGraphs";
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
		inline_field_names.add("style");
		inline_field_values.add(style);
	}
}
