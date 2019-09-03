package spade.query.quickgrail.core.kernel;

import spade.query.quickgrail.core.utility.StorageExecutor;
import spade.query.quickgrail.neo4j.utility.Environment;
import spade.storage.neo4j.Neo4jExecutor;
import spade.storage.postgresql.PostgresExecutor;
import spade.storage.quickstep.QuickstepExecutor;

public class EnvironmentFactory
{
	public static AbstractEnvironment createEnvironment(String storageName, StorageExecutor executor)
	{
		AbstractEnvironment environment = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			environment = new Environment((Neo4jExecutor) executor);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			environment = new spade.query.quickgrail.postgresql.utility.Environment((PostgresExecutor) executor);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			environment = new spade.query.quickgrail.quickstep.utility.Environment((QuickstepExecutor) executor);
		}
		// add a condition above to add support for a new storage environment
		return environment;
	}
}
