package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;

public class CreateGetLink extends InstructionFactory
{
	private Graph targetGraph;
	private Graph subjectGraph;
	private Graph sourceGraph;
	private Graph destinationGraph;
	private Integer maxDepth;

	public CreateGetLink(Graph targetGraph, Graph subjectGraph,
						 Graph sourceGraph, Graph destinationGraph,
						 Integer maxDepth)
	{
		super();
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.sourceGraph = sourceGraph;
		this.destinationGraph = destinationGraph;
		this.maxDepth = maxDepth;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.GetLink((Neo4jGraph) targetGraph,
					(Neo4jGraph) subjectGraph, (Neo4jGraph) sourceGraph,
					(Neo4jGraph) destinationGraph, maxDepth);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.GetLink((PostgreSQLGraph) targetGraph,
					(PostgreSQLGraph) subjectGraph, (PostgreSQLGraph) sourceGraph,
					(PostgreSQLGraph) destinationGraph, maxDepth);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.GetLink((QuickstepGraph) targetGraph,
					(QuickstepGraph) subjectGraph, (QuickstepGraph) sourceGraph,
					(QuickstepGraph) destinationGraph, maxDepth);
		}

		return instruction;
	}
}
