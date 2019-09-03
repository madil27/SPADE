package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.entities.Graph.GraphComponent;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;

public class CreateSubtractGraph extends InstructionFactory
{
	private Graph outputGraph;
	private Graph minuendGraph;
	private Graph subtrahendGraph;
	private GraphComponent component;

	public CreateSubtractGraph(Graph outputGraph, Graph minuendGraph, Graph subtrahendGraph, GraphComponent component)
	{
		super();
		this.outputGraph = outputGraph;
		this.minuendGraph = minuendGraph;
		this.subtrahendGraph = subtrahendGraph;
		this.component = component;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.SubtractGraph((Neo4jGraph) outputGraph,
					(Neo4jGraph) minuendGraph, (Neo4jGraph) subtrahendGraph, component);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.SubtractGraph((PostgreSQLGraph) outputGraph,
					(PostgreSQLGraph) minuendGraph, (PostgreSQLGraph) subtrahendGraph, component);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.SubtractGraph((QuickstepGraph) outputGraph,
					(QuickstepGraph) minuendGraph, (QuickstepGraph) subtrahendGraph, component);
		}

		return instruction;
	}
}
