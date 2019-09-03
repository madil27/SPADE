package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;

public class CreateGetSubgraph extends InstructionFactory
{
	private Graph targetGraph;
	private Graph subjectGraph;
	private Graph skeletonGraph;

	public CreateGetSubgraph(Graph targetGraph, Graph subjectGraph, Graph skeletonGraph)
	{
		super();
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.skeletonGraph = skeletonGraph;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.GetSubgraph((Neo4jGraph) targetGraph,
					(Neo4jGraph) subjectGraph, (Neo4jGraph) skeletonGraph);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.GetSubgraph((PostgreSQLGraph) targetGraph,
					(PostgreSQLGraph) subjectGraph, (PostgreSQLGraph) skeletonGraph);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.GetSubgraph((QuickstepGraph) targetGraph,
					(QuickstepGraph) subjectGraph, (QuickstepGraph) skeletonGraph);
		}

		return instruction;
	}
}
