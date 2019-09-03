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
 * Similar to GetPath but the result graph only contains vertices / edges that
 * are on the shortest paths.
 * <p>
 * Warning: This operation could be very slow when the input graph is large.
 */
public class GetShortestPath extends Instruction
{
	// Output graph.
	private PostgreSQLGraph targetGraph;
	// Input graph.
	private PostgreSQLGraph subjectGraph;
	// Set of source vertices.
	private PostgreSQLGraph srcGraph;
	// Set of destination vertices.
	private PostgreSQLGraph dstGraph;
	// Max path length.
	private Integer maxDepth;

	public GetShortestPath(PostgreSQLGraph targetGraph, PostgreSQLGraph subjectGraph,
						   PostgreSQLGraph srcGraph, PostgreSQLGraph dstGraph,
						   Integer maxDepth)
	{
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.srcGraph = srcGraph;
		this.dstGraph = dstGraph;
		this.maxDepth = maxDepth;
	}

	@Override
	public void execute(AbstractEnvironment env, ExecutionContext ctx)
	{
		PostgresExecutor qs = (PostgresExecutor) ctx.getExecutor();

		String filter;
		qs.executeQuery("DROP TABLE IF EXISTS m_conn;" +
				"CREATE TABLE m_conn (\"" + CHILD_VERTEX_KEY + "\" UUID, \"" + PARENT_VERTEX_KEY + "\" UUID);");
		if(env.IsBaseGraph(subjectGraph))
		{
			filter = "";
			qs.executeQuery("INSERT INTO m_conn SELECT \"" + CHILD_VERTEX_KEY + "\", \"" + PARENT_VERTEX_KEY
					+ "\" FROM " + EDGE_TABLE + " GROUP BY \"" + CHILD_VERTEX_KEY + "\", \"" + PARENT_VERTEX_KEY + "\";");
		}
		else
		{
			String subjectEdgeTable = subjectGraph.getEdgeTableName();
			filter = " AND " + EDGE_TABLE + "." + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM "
					+ subjectEdgeTable + ")";
			qs.executeQuery("DROP TABLE IF EXISTS m_sgedge;" +
					"CREATE TABLE m_sgedge (\"" + CHILD_VERTEX_KEY + "\" UUID, \"" + PARENT_VERTEX_KEY + "\" UUID);" +
					"INSERT INTO m_sgedge SELECT \"" + CHILD_VERTEX_KEY + "\", \"" + PARENT_VERTEX_KEY + "\" FROM " +
					EDGE_TABLE + " WHERE " + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM " +
					subjectEdgeTable + ");" + "INSERT INTO m_conn SELECT \"" + CHILD_VERTEX_KEY + "\", \"" +
					PARENT_VERTEX_KEY + "\" FROM m_sgedge GROUP BY \"" + CHILD_VERTEX_KEY + "\", \"" +
					PARENT_VERTEX_KEY + "\";" + "DROP TABLE IF EXISTS m_sgedge;");
		}

		// Create subgraph edges table.
		qs.executeQuery("DROP TABLE IF EXISTS m_sgconn;" +
				"CREATE TABLE m_sgconn (\"" + CHILD_VERTEX_KEY + "\" UUID, \"" + PARENT_VERTEX_KEY +
				"\" UUID, reaching INT, depth INT);");

		qs.executeQuery("DROP TABLE IF EXISTS m_cur;" +
				"DROP TABLE IF EXISTS m_next;" +
				"DROP TABLE IF EXISTS m_answer;" +
				"CREATE TABLE m_cur (" + PRIMARY_KEY + " UUID, reaching INT);" +
				"CREATE TABLE m_next (" + PRIMARY_KEY + " INT, reaching INT);" +
				"CREATE TABLE m_answer (" + PRIMARY_KEY + " UUID);");

		qs.executeQuery("INSERT INTO m_cur SELECT " + PRIMARY_KEY + ", " + PRIMARY_KEY + " FROM " +
				dstGraph.getVertexTableName() + ";" +
				"INSERT INTO m_answer SELECT " + PRIMARY_KEY + " FROM m_cur GROUP BY " + PRIMARY_KEY + ";");

		String loopStmts =
				"INSERT INTO m_sgconn SELECT \"" + CHILD_VERTEX_KEY + "\", \"" + PARENT_VERTEX_KEY +
						"\", reaching, $depth" + " FROM m_cur, m_conn WHERE " + PRIMARY_KEY + " = \""
						+ PARENT_VERTEX_KEY + "\";" + "DROP TABLE IF EXISTS m_next;" + "CREATE TABLE m_next (" +
						PRIMARY_KEY + " UUID, reaching INT);" + "INSERT INTO m_next SELECT \"" + CHILD_VERTEX_KEY +
						"\", reaching" + " FROM m_cur, m_conn WHERE " + PRIMARY_KEY + " = \"" + PARENT_VERTEX_KEY +
						"\";" + "DROP TABLE IF EXISTS m_cur;" + "CREATE TABLE m_cur (" + PRIMARY_KEY +
						" UUID, reaching INT);" + "INSERT INTO m_cur SELECT " + PRIMARY_KEY + ", reaching FROM m_next" +
						" WHERE " + PRIMARY_KEY + " NOT IN (SELECT " + PRIMARY_KEY + " FROM m_answer) GROUP BY " +
						PRIMARY_KEY + ", reaching;" +
						"INSERT INTO m_answer SELECT " + PRIMARY_KEY + " FROM m_cur GROUP BY " + PRIMARY_KEY + ";";
		for(int i = 0; i < maxDepth; ++i)
		{
			qs.executeQuery(loopStmts.replace("$depth", String.valueOf(i + 1)));

			String worksetSizeQuery = "COPY (SELECT COUNT(*) FROM m_cur) TO stdout;";
			if(qs.executeQueryForLongResult(worksetSizeQuery) == 0)
			{
				break;
			}
		}

		qs.executeQuery("DROP TABLE IF EXISTS m_cur;" +
				"DROP TABLE IF EXISTS m_next;" +
				"CREATE TABLE m_cur (" + PRIMARY_KEY + " UUID);" +
				"CREATE TABLE m_next (" + PRIMARY_KEY + " UUID);");

		qs.executeQuery("INSERT INTO m_cur SELECT " + PRIMARY_KEY + " FROM " + srcGraph.getVertexTableName() +
				" WHERE " + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM m_answer);");

		qs.executeQuery("DROP TABLE IF EXISTS m_answer;" +
				"CREATE TABLE m_answer (" + PRIMARY_KEY + " UUID);" +
				"INSERT INTO m_answer SELECT " + PRIMARY_KEY + " FROM m_cur;");


		loopStmts =
				"DROP TABLE IF EXISTS m_next;" + "CREATE TABLE m_next(" + PRIMARY_KEY + " UUID);" +
						"INSERT INTO m_next SELECT MIN(\"" + PARENT_VERTEX_KEY + "\")" +
						" FROM m_cur, m_sgconn WHERE " + PRIMARY_KEY + " = \"" + CHILD_VERTEX_KEY + "\"" +
						" AND depth + $depth <= " + String.valueOf(maxDepth) + " GROUP BY \"" + CHILD_VERTEX_KEY +
						"\", reaching;" + "DROP TABLE IF EXISTS m_cur;" + "CREATE TABLE m_cur(" + PRIMARY_KEY +
						" UUID);" + "INSERT INTO m_cur SELECT " + PRIMARY_KEY + " FROM m_next WHERE " + PRIMARY_KEY +
						" NOT IN (SELECT " + PRIMARY_KEY + " FROM m_answer);" +
						"INSERT INTO m_answer SELECT " + PRIMARY_KEY + " FROM m_cur;";
		for(int i = 0; i < maxDepth; ++i)
		{
			qs.executeQuery(loopStmts.replace("$depth", String.valueOf(i)));

			String worksetSizeQuery = "COPY (SELECT COUNT(*) FROM m_cur) TO stdout;";
			if(qs.executeQueryForLongResult(worksetSizeQuery) == 0)
			{
				break;
			}
		}

		String targetVertexTable = targetGraph.getVertexTableName();
		String targetEdgeTable = targetGraph.getEdgeTableName();

		qs.executeQuery("INSERT INTO " + targetVertexTable + " SELECT " + PRIMARY_KEY + " FROM m_answer;" +
				"INSERT INTO " + targetEdgeTable + " SELECT " + PRIMARY_KEY + " FROM " + EDGE_TABLE +
				" WHERE \"" + CHILD_VERTEX_KEY + "\" IN (SELECT " + PRIMARY_KEY + " FROM m_answer)" +
				" AND \"" + PARENT_VERTEX_KEY + "\" IN (SELECT " + PRIMARY_KEY + " FROM m_answer)" + filter + ";");

		qs.executeQuery("DROP TABLE IF EXISTS m_cur;" +
				"DROP TABLE IF EXISTS m_next;" +
				"DROP TABLE IF EXISTS m_answer;" +
				"DROP TABLE IF EXISTS m_conn;" +
				"DROP TABLE IF EXISTS m_sgconn;");
	}

	@Override
	public String getLabel()
	{
		return "GetShortestPath";
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
		inline_field_names.add("srcGraph");
		inline_field_values.add(srcGraph.getName());
		inline_field_names.add("dstGraph");
		inline_field_values.add(dstGraph.getName());
		inline_field_names.add("maxDepth");
		inline_field_values.add(String.valueOf(maxDepth));
	}
}
