package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;

public class CreateIntersectGraph extends InstructionFactory
{
	// Output graph.
	private Graph outputGraph;
	// Input graphs.
	private Graph lhsGraph;
	private Graph rhsGraph;

	public CreateIntersectGraph(Graph outputGraph, Graph lhsGraph, Graph rhsGraph)
	{
		super();
		this.outputGraph = outputGraph;
		this.lhsGraph = lhsGraph;
		this.rhsGraph = rhsGraph;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.IntersectGraph((Neo4jGraph) outputGraph,
					(Neo4jGraph) lhsGraph, (Neo4jGraph) rhsGraph);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.GetSubgraph((PostgreSQLGraph) outputGraph,
					(PostgreSQLGraph) lhsGraph, (PostgreSQLGraph) rhsGraph);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.GetSubgraph((QuickstepGraph) outputGraph,
					(QuickstepGraph) lhsGraph, (QuickstepGraph) rhsGraph);
		}

		return instruction;
	}
}
