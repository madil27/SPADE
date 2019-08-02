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
 * Get the lineage of a set of vertices in a graph.
 */
public class GetLineage extends Instruction
{
	// Output graph.
	private Graph targetGraph;
	// Input graph.
	private Graph subjectGraph;
	// Set of starting vertices.
	private Graph startGraph;
	// Max depth.
	private Integer depth;
	// Direction (ancestors / descendants, or both).
	private Direction direction;

	public GetLineage(Graph targetGraph, Graph subjectGraph,
					  Graph startGraph, Integer depth, Direction direction)
	{
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.startGraph = startGraph;
		this.depth = depth;
		this.direction = direction;
	}

	@Override
	public void execute(Environment env, ExecutionContext ctx)
	{
		ArrayList<Direction> oneDirs = new ArrayList<>();
		if(direction == Direction.kBoth)
		{
			oneDirs.add(Direction.kAncestor);
			oneDirs.add(Direction.kDescendant);
		}
		else
		{
			oneDirs.add(direction);
		}

		Neo4jExecutor ns = ctx.getExecutor();

		String targetVertexTable = targetGraph.getVertexTableName();
		String targetEdgeTable = targetGraph.getEdgeTableName();

		String subjectEdgeTable = subjectGraph.getEdgeTableName();
		String filter = "";
		if(!Environment.IsBaseGraph(subjectGraph))
		{
			filter = " AND edge." + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM " + subjectEdgeTable + ")";
		}

		for(Direction oneDir : oneDirs)
		{
			executeOneDirection(oneDir, ns, filter);
			ns.executeQuery("INSERT INTO " + targetVertexTable + " SELECT " + PRIMARY_KEY + " FROM m_answer;" +
					"INSERT INTO " + targetEdgeTable + " SELECT " + PRIMARY_KEY +
					" FROM m_answer_edge GROUP BY " + PRIMARY_KEY + ";");
		}

		ns.executeQuery("DROP TABLE IF EXISTS m_cur;" +
				"DROP TABLE IF EXISTS m_next;" +
				"DROP TABLE IF EXISTS m_answer;" +
				"DROP TABLE IF EXISTS m_answer_edge;");
	}

	private void executeOneDirection(Direction dir, Neo4jExecutor ns, String filter)
	{
		String src, dst;
		if(dir == Direction.kAncestor)
		{
			src = "\"" + CHILD_VERTEX_KEY + "\"";
			dst = "\"" + PARENT_VERTEX_KEY + "\"";
		}
		else
		{
			assert dir == Direction.kDescendant;
			src = "\"" + PARENT_VERTEX_KEY + "\"";
			dst = "\"" + CHILD_VERTEX_KEY + "\"";
		}

		ns.executeQuery("DROP TABLE IF EXISTS m_cur;" +
				"DROP TABLE IF EXISTS m_next;" +
				"DROP TABLE IF EXISTS m_answer;" +
				"DROP TABLE IF EXISTS m_answer_edge;" +
				"CREATE TABLE m_cur (" + PRIMARY_KEY + " UUID);" +
				"CREATE TABLE m_next (" + PRIMARY_KEY + " UUID);" +
				"CREATE TABLE m_answer (" + PRIMARY_KEY + " UUID);" +
				"CREATE TABLE m_answer_edge (" + PRIMARY_KEY + " UUID);");

		String startVertexTable = startGraph.getVertexTableName();
		ns.executeQuery("INSERT INTO m_cur SELECT " + PRIMARY_KEY + " FROM " + startVertexTable + ";" +
				"INSERT INTO m_answer SELECT " + PRIMARY_KEY + " FROM m_cur;");

		String loopStmts =
				"DROP TABLE IF EXISTS m_next;" + "CREATE TABLE m_next (" + PRIMARY_KEY + " UUID);" +
						"INSERT INTO m_next SELECT " + dst + " FROM " + EDGE_TABLE +
						" WHERE " + src + " IN (SELECT " + PRIMARY_KEY + " FROM m_cur)" + filter +
						" GROUP BY " + dst + ";" +
						"INSERT INTO m_answer_edge SELECT " + PRIMARY_KEY + " FROM " + EDGE_TABLE +
						" WHERE " + src + " IN (SELECT " + PRIMARY_KEY + " FROM m_cur)" + filter + ";" +
						"DROP TABLE IF EXISTS m_cur;" + "CREATE TABLE m_cur (" + PRIMARY_KEY + " UUID);" +
						"INSERT INTO m_cur SELECT " + PRIMARY_KEY + " FROM m_next WHERE " + PRIMARY_KEY +
						" NOT IN (SELECT " + PRIMARY_KEY + " FROM m_answer);" +
						"INSERT INTO m_answer SELECT " + PRIMARY_KEY + " FROM m_cur;";
		for(int i = 0; i < depth; ++i)
		{
			ns.executeQuery(loopStmts);

			String worksetSizeQuery = "COPY (SELECT COUNT(*) FROM m_cur) TO stdout;";
			if(ns.executeQueryForLongResult(worksetSizeQuery) == 0)
			{
				break;
			}
		}
	}

	@Override
	public String getLabel()
	{
		return "GetLineage";
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
		inline_field_names.add("startGraph");
		inline_field_values.add(startGraph.getName());
		inline_field_names.add("depth");
		inline_field_values.add(String.valueOf(depth));
		inline_field_names.add("direction");
		inline_field_values.add(direction.name().substring(1));
	}

	public enum Direction
	{
		kAncestor,
		kDescendant,
		kBoth
	}
}
