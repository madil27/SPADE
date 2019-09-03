package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.entities.Graph.EdgeComponent;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;

public class CreateGetEdgeEndpoint extends InstructionFactory
{
	private Graph targetGraph;
	private Graph subjectGraph;
	private EdgeComponent component;

	public CreateGetEdgeEndpoint(Graph targetGraph, Graph subjectGraph, EdgeComponent component)
	{
		super();
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.component = component;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.GetEdgeEndpoint((Neo4jGraph) targetGraph,
					(Neo4jGraph) subjectGraph, component);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.GetEdgeEndpoint((PostgreSQLGraph) targetGraph,
					(PostgreSQLGraph) subjectGraph, component);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.GetEdgeEndpoint((QuickstepGraph) targetGraph,
					(QuickstepGraph) subjectGraph, component);
		}

		return instruction;
	}
}
