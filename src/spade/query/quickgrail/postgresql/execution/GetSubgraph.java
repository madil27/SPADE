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
 * Let $S be the subject graph and $T be the skeleton graph.
 * The operation $S.getSubgraph($T) is to find all the vertices and edges that
 * are spanned by the skeleton graph.
 */
public class GetSubgraph extends Instruction
{
	private PostgreSQLGraph targetGraph;
	private PostgreSQLGraph subjectGraph;
	private PostgreSQLGraph skeletonGraph;

	public GetSubgraph(PostgreSQLGraph targetGraph, PostgreSQLGraph subjectGraph, PostgreSQLGraph skeletonGraph)
	{
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.skeletonGraph = skeletonGraph;
	}

	@Override
	public void execute(AbstractEnvironment env, ExecutionContext ctx)
	{
		PostgresExecutor qs = (PostgresExecutor) ctx.getExecutor();

		String targetVertexTable = targetGraph.getVertexTableName();
		String targetEdgeTable = targetGraph.getEdgeTableName();
		String subjectVertexTable = subjectGraph.getVertexTableName();
		String subjectEdgeTable = subjectGraph.getEdgeTableName();
		String skeletonVertexTable = skeletonGraph.getVertexTableName();
		String skeletonEdgeTable = skeletonGraph.getEdgeTableName();

		qs.executeQuery("DROP TABLE IF EXISTS m_answer;" + "CREATE TABLE m_answer (" + PRIMARY_KEY + " UUID);");

		// Get vertices.
		qs.executeQuery("INSERT INTO m_answer SELECT " + PRIMARY_KEY + " FROM " + skeletonVertexTable +
				" WHERE " + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM " + subjectVertexTable + ");" +
				"INSERT INTO m_answer SELECT \"" + CHILD_VERTEX_KEY + "\" FROM " + EDGE_TABLE +
				" WHERE " + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM " + skeletonEdgeTable + ")" +
				" AND \"" + CHILD_VERTEX_KEY + "\" IN (SELECT " + PRIMARY_KEY + " FROM " + subjectVertexTable + ");" +
				"INSERT INTO m_answer SELECT \"" + PARENT_VERTEX_KEY + "\" FROM " + EDGE_TABLE +
				" WHERE " + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM " + skeletonEdgeTable + ")" +
				" AND \"" + PARENT_VERTEX_KEY + "\" IN (SELECT " + PRIMARY_KEY + " FROM " + subjectVertexTable + ");" +
				"INSERT INTO " + targetVertexTable + " SELECT " + PRIMARY_KEY + " FROM m_answer GROUP BY " +
				PRIMARY_KEY + " ;");

		// Get edges.
		qs.executeQuery("INSERT INTO " + targetEdgeTable +
				" SELECT s." + PRIMARY_KEY + " FROM " + subjectEdgeTable + " s, " + EDGE_TABLE + " e" +
				" WHERE s." + PRIMARY_KEY + " = e." + PRIMARY_KEY + " AND e.\"" + CHILD_VERTEX_KEY + "\" IN (SELECT " + PRIMARY_KEY +
				" FROM m_answer)" +
				" AND e.\"" + PARENT_VERTEX_KEY + "\" IN (SELECT " + PRIMARY_KEY + " FROM m_answer) GROUP BY s." + PRIMARY_KEY + " ;");

		qs.executeQuery("DROP TABLE IF EXISTS m_answer;");
	}

	@Override
	public String getLabel()
	{
		return "GetSubgraph";
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
		inline_field_names.add("skeletonGraph");
		inline_field_values.add(skeletonGraph.getName());
	}
}
