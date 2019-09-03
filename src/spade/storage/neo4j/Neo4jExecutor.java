package spade.storage.neo4j;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import spade.query.quickgrail.core.utility.StorageExecutor;
import spade.storage.Neo4j;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static spade.core.AbstractQuery.getCurrentStorage;
import static spade.query.quickgrail.neo4j.utility.CommonVariables.EDGE_ALIAS;
import static spade.query.quickgrail.neo4j.utility.CommonVariables.VERTEX_ALIAS;

/**
 * Helper class for encapsulation of related methods.
 */
public class Neo4jExecutor extends StorageExecutor
{
	private Neo4j currentStorage;

	public Neo4jExecutor()
	{
		this.currentStorage = (Neo4j) getCurrentStorage();
	}

	private void setCurrentStorage()
	{
		this.currentStorage = (Neo4j) getCurrentStorage();
	}

	/**
	 * Submit a query and wait for the result.
	 */
	public Result executeQuery(String query)
	{
		setCurrentStorage();
		return currentStorage.executeQuery(query.trim());
	}

	/**
	 * Submit a query and cast result as long type.
	 */
	public Long executeQueryForLongResult(String query)
	{
		setCurrentStorage();
		currentStorage.globalTxCheckin(true);
		try(Result result = executeQuery(query))
		{
			if(result.hasNext())
			{
				List<String> columns = result.columns();
				String column = columns.get(0);
				Map<String, Object> row = result.next();
				return (Long) row.get(column);
			}
		}
		catch(Exception ex)
		{
			throw new RuntimeException(
					"Unexpected result from Neo4j: expecting a long value");
		}
		finally
		{
			currentStorage.globalTxCheckin(true);
		}
		return null;
	}

	public Set<String> executeQueryForLabels(String query)
	{
		setCurrentStorage();
		Set<String> labelSet = new HashSet<>();
		currentStorage.globalTxCheckin(true);
		try(Result result = executeQuery(query))
		{
			if(result.hasNext())
			{
				Iterator<String> labels = result.columnAs("label");
				while(labels.hasNext())
				{
					labelSet.add(labels.next());
				}
			}
		}
		catch(Exception ex)
		{
			throw new RuntimeException(
					"Unexpected result from Neo4j: expecting a Map value");
		}
		finally
		{
			currentStorage.globalTxCheckin(true);
		}
		return labelSet;
	}

	public Set<Node> executeQueryForNodeSetResult(String query)
	{
		setCurrentStorage();
		Set<Node> nodeSet = new HashSet<>();
		currentStorage.globalTxCheckin(true);
		try(Result result = currentStorage.executeQuery(query))
		{
			Iterator<Node> nodes = result.columnAs(VERTEX_ALIAS);
			while(nodes.hasNext())
			{
				nodeSet.add(nodes.next());
			}
		}
		catch(Exception ex)
		{
			throw new RuntimeException(
					"Unexpected result from Neo4j: expecting a Set of Nodes");
		}
		finally
		{
			currentStorage.globalTxCheckin(true);
		}
		return nodeSet;
	}

	public Set<Relationship> executeQueryForRelationshipSetResult(String query)
	{
		setCurrentStorage();
		Set<Relationship> relationshipSet = new HashSet<>();
		currentStorage.globalTxCheckin(true);
		try(Result result = currentStorage.executeQuery(query))
		{
			Iterator<Relationship> relationships = result.columnAs(EDGE_ALIAS);
			while(relationships.hasNext())
			{
				relationshipSet.add(relationships.next());
			}
		}
		catch(Exception ex)
		{
			throw new RuntimeException(
					"Unexpected result from Neo4j: expecting a Set of Relationships");
		}
		finally
		{
			currentStorage.globalTxCheckin(true);
		}
		return relationshipSet;
	}

}
