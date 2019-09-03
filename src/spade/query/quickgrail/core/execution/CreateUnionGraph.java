package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;

public class CreateUnionGraph extends InstructionFactory
{
	// The target graph.
	private Graph targetGraph;
	// The source graph to be unioned into the target graph.
	private Graph sourceGraph;

	public CreateUnionGraph(Graph targetGraph, Graph sourceGraph)
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
			instruction = new spade.query.quickgrail.neo4j.execution.UnionGraph((Neo4jGraph) targetGraph,
					(Neo4jGraph) sourceGraph);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.UnionGraph((PostgreSQLGraph) targetGraph,
					(PostgreSQLGraph) sourceGraph);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.UnionGraph((QuickstepGraph) targetGraph,
					(QuickstepGraph) sourceGraph);
		}

		return instruction;
	}
}
