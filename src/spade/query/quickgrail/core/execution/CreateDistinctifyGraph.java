package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;

public class CreateDistinctifyGraph extends InstructionFactory
{
	private Graph targetGraph;
	private Graph sourceGraph;

	public CreateDistinctifyGraph(Graph targetGraph, Graph sourceGraph)
	{
		super();
		this.targetGraph = targetGraph;
		this.sourceGraph = sourceGraph;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.DistinctifyGraph((Neo4jGraph) targetGraph,
					(Neo4jGraph) sourceGraph);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.DistinctifyGraph((PostgreSQLGraph) targetGraph,
					(PostgreSQLGraph) sourceGraph);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.DistinctifyGraph((QuickstepGraph) targetGraph,
					(QuickstepGraph) sourceGraph);
		}

		return instruction;
	}
}
