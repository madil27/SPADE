package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;

public class CreateLimitGraph extends InstructionFactory
{
	private Graph targetGraph;
	private Graph sourceGraph;
	private int limit;

	public CreateLimitGraph(Graph targetGraph, Graph sourceGraph, int limit)
	{
		super();
		this.targetGraph = targetGraph;
		this.sourceGraph = sourceGraph;
		this.limit = limit;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.LimitGraph((Neo4jGraph) targetGraph,
					(Neo4jGraph) sourceGraph, limit);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.LimitGraph((PostgreSQLGraph) targetGraph,
					(PostgreSQLGraph) sourceGraph, limit);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.LimitGraph((QuickstepGraph) targetGraph,
					(QuickstepGraph) sourceGraph, limit);
		}

		return instruction;
	}
}
