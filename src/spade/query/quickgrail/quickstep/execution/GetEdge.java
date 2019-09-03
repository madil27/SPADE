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
package spade.query.quickgrail.quickstep.execution;

import spade.query.quickgrail.core.kernel.AbstractEnvironment;
import spade.query.quickgrail.core.kernel.ExecutionContext;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.utility.TreeStringSerializable;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;
import spade.storage.quickstep.QuickstepExecutor;

import java.util.ArrayList;

import static spade.query.quickgrail.quickstep.utility.QuickstepUtil.formatString;

/**
 * Get the a set of edges in a graph.
 */
public class GetEdge extends Instruction
{
	// Output graph.
	private QuickstepGraph targetGraph;
	// Input graph.
	private QuickstepGraph subjectGraph;
	private String field;
	private String operation;
	private String value;

	public GetEdge(QuickstepGraph targetGraph, QuickstepGraph subjectGraph, String field, String operation, String value)
	{
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.field = field;
		this.operation = operation;
		this.value = value;
	}

	@Override
	public void execute(AbstractEnvironment env, ExecutionContext ctx)
	{
		QuickstepExecutor qs = (QuickstepExecutor) ctx.getExecutor();
		StringBuilder sqlQuery = new StringBuilder(100);
		sqlQuery.append("INSERT INTO " + targetGraph.getEdgeTableName() +
				" SELECT id FROM " + QuickstepGraph.GetBaseEdgeAnnotationTableName());
		if(field != null)
		{
			sqlQuery.append(" WHERE");
			if(!field.equals("*"))
			{
				sqlQuery.append(" field = " + formatString(field) + " AND");
			}
			sqlQuery.append(" value " + operation + " " + formatString(value));
			if(!env.IsBaseGraph(subjectGraph))
			{
				sqlQuery.append("\\analyzerange " + subjectGraph.getEdgeTableName() + "\n");
				sqlQuery.append(" AND id IN (SELECT id FROM " +
						subjectGraph.getEdgeTableName() + ")");
			}
		}
		sqlQuery.append(" GROUP BY id;");
		qs.executeQuery(sqlQuery.toString());
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