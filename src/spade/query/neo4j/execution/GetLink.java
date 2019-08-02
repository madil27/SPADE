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
 * Similar to GetPath but treats the graph edges as indirected.
 */
public class GetLink extends Instruction
{
	// Output graph.
	private Graph targetGraph;
	// Input graph.
	private Graph subjectGraph;
	// Set of source vertices.
	private Graph srcGraph;
	// Set of destination vertices.
	private Graph dstGraph;
	// Max path length.
	private Integer maxDepth;

	public GetLink(Graph targetGraph, Graph subjectGraph,
				   Graph srcGraph, Graph dstGraph,
				   Integer maxDepth)
	{
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.srcGraph = srcGraph;
		this.dstGraph = dstGraph;
		this.maxDepth = maxDepth;
	}

	@Override
	public void execute(Environment env, ExecutionContext ctx)
	{
		Neo4jExecutor ns = ctx.getExecutor();

		ns.executeQuery("DROP TABLE IF EXISTS m_cur;" +
				"DROP TABLE IF EXISTS m_next;" +
				"DROP TABLE IF EXISTS m_answer;" +
				"CREATE TABLE m_cur (" + PRIMARY_KEY + " UUID);" +
				"CREATE TABLE m_next (" + PRIMARY_KEY + " UUID);" +
				"CREATE TABLE m_answer (" + PRIMARY_KEY + " UUID);");

		String filter;
		if(Environment.IsBaseGraph(subjectGraph))
		{
			filter = "";
		}
		else
		{
			filter = " AND " + EDGE_TABLE + "." + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM " +
					subjectGraph.getEdgeTableName() + ")";
		}

		// Create subgraph edges table.
		ns.executeQuery("DROP TABLE IF EXISTS m_sgconn;" +
				"CREATE TABLE m_sgconn ( \"" + CHILD_VERTEX_KEY + "\" UUID, \"" + PARENT_VERTEX_KEY + "\" UUID, depth INT);");

		ns.executeQuery("INSERT INTO m_cur SELECT " + PRIMARY_KEY + " FROM " + dstGraph.getVertexTableName() + ";" +
				"INSERT INTO m_answer SELECT " + PRIMARY_KEY + " FROM m_cur;");

		String loopStmts =
				"INSERT INTO m_sgconn SELECT \"" + CHILD_VERTEX_KEY + "\", \"" + PARENT_VERTEX_KEY + "\", $depth FROM " + EDGE_TABLE +
						" WHERE \"" + PARENT_VERTEX_KEY + "\" IN (SELECT " + PRIMARY_KEY + " FROM m_cur)" + filter + ";" +
						"INSERT INTO m_sgconn SELECT \"" + CHILD_VERTEX_KEY + "\", \"" + PARENT_VERTEX_KEY + "\", $depth FROM " + EDGE_TABLE +
						" WHERE \"" + CHILD_VERTEX_KEY + "\" IN (SELECT " + PRIMARY_KEY + " FROM m_cur)" + filter + ";" +
						"DROP TABLE IF EXISTS m_next;" + "CREATE TABLE m_next (" + PRIMARY_KEY + " UUID);" +
						"INSERT INTO m_next SELECT \"" + CHILD_VERTEX_KEY + "\" FROM " + EDGE_TABLE +
						" WHERE \"" + PARENT_VERTEX_KEY + "\" IN (SELECT " + PRIMARY_KEY + " FROM m_cur)" + filter + " GROUP BY \"" + CHILD_VERTEX_KEY + "\";" +
						"INSERT INTO m_next SELECT \"" + PARENT_VERTEX_KEY + "\" FROM " + EDGE_TABLE +
						" WHERE \"" + CHILD_VERTEX_KEY + "\" IN (SELECT " + PRIMARY_KEY + " FROM m_cur)" + filter + " GROUP BY \"" + PARENT_VERTEX_KEY + "\";" +
						"DROP TABLE IF EXISTS m_cur;" + "CREATE TABLE m_cur (" + PRIMARY_KEY + " UUID);" +
						"INSERT INTO m_cur SELECT " + PRIMARY_KEY + " FROM m_next WHERE " + PRIMARY_KEY + " NOT IN (SELECT " + PRIMARY_KEY + " FROM m_answer);" +
						"INSERT INTO m_answer SELECT " + PRIMARY_KEY + " FROM m_cur;";
		for(int i = 0; i < maxDepth; ++i)
		{
			ns.executeQuery(loopStmts.replace("$depth", String.valueOf(i + 1)));

			String worksetSizeQuery = "COPY (SELECT COUNT(*) FROM m_cur) TO stdout;";
			if(ns.executeQueryForLongResult(worksetSizeQuery) == 0)
			{
				break;
			}
		}

		ns.executeQuery("DROP TABLE IF EXISTS m_cur;" +
				"DROP TABLE IF EXISTS m_next;" +
				"CREATE TABLE m_cur (" + PRIMARY_KEY + " UUID);" +
				"CREATE TABLE m_next (" + PRIMARY_KEY + " UUID);");

		ns.executeQuery("INSERT INTO m_cur SELECT " + PRIMARY_KEY + " FROM " + srcGraph.getVertexTableName() +
				" WHERE + " + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM m_answer);");

		ns.executeQuery("DROP TABLE IF EXISTS m_answer;" +
				"CREATE TABLE m_answer (" + PRIMARY_KEY + " UUID);" +
				"INSERT INTO m_answer SELECT " + PRIMARY_KEY + " FROM m_cur;");

		loopStmts =
				"DROP TABLE IF EXISTS m_next;" + "CREATE TABLE m_next (" + PRIMARY_KEY + " UUID);" +
						"INSERT INTO m_next SELECT \"" + PARENT_VERTEX_KEY + "\" FROM m_sgconn" +
						" WHERE \"" + CHILD_VERTEX_KEY + "\" IN (SELECT + " + PRIMARY_KEY + " FROM m_cur)" +
						" AND depth + $depth <= " + maxDepth + " GROUP BY \"" + PARENT_VERTEX_KEY + "\";" +
						"INSERT INTO m_next SELECT \"" + CHILD_VERTEX_KEY + "\" FROM m_sgconn" +
						" WHERE \"" + PARENT_VERTEX_KEY + "\" IN (SELECT + " + PRIMARY_KEY + " FROM m_cur)" +
						" AND depth + $depth <= " + maxDepth + " GROUP BY \"" + CHILD_VERTEX_KEY + "\";" +
						"DROP TABLE IF EXISTS m_cur;" + "CREATE TABLE m_cur (" + PRIMARY_KEY + " UUID);" +
						"INSERT INTO m_cur SELECT " + PRIMARY_KEY + " FROM m_next WHERE " + PRIMARY_KEY + " " +
						"NOT IN (SELECT " + PRIMARY_KEY + " FROM m_answer);" +
						"INSERT INTO m_answer SELECT " + PRIMARY_KEY + " FROM m_cur;";
		for(int i = 0; i < maxDepth; ++i)
		{
			ns.executeQuery(loopStmts.replace("$depth", String.valueOf(i)));

			String worksetSizeQuery = "COPY (SELECT COUNT(*) FROM m_cur) TO stdout;";
			if(ns.executeQueryForLongResult(worksetSizeQuery) == 0)
			{
				break;
			}
		}

		String targetVertexTable = targetGraph.getVertexTableName();
		String targetEdgeTable = targetGraph.getEdgeTableName();

		ns.executeQuery("INSERT INTO " + targetVertexTable + " SELECT " + PRIMARY_KEY + " FROM m_answer;" +
				"INSERT INTO " + targetEdgeTable + " SELECT " + PRIMARY_KEY + " FROM " + EDGE_TABLE +
				" WHERE \"" + CHILD_VERTEX_KEY + "\" IN (SELECT " + PRIMARY_KEY + " FROM m_answer)" +
				" AND \"" + PARENT_VERTEX_KEY + "\" IN (SELECT + " + PRIMARY_KEY + " FROM m_answer)" + filter + ";");

		ns.executeQuery("DROP TABLE IF EXISTS m_cur;" +
				"DROP TABLE IF EXISTS m_next;" +
				"DROP TABLE IF EXISTS m_answer;" +
				"DROP TABLE IF EXISTS m_sgconn;");
	}

	@Override
	public String getLabel()
	{
		return "GetLink";
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
