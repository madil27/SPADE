package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.entities.GraphMetadata;
import spade.query.quickgrail.core.entities.GraphMetadata.GraphMetadataComponent;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.neo4j.entities.Neo4jGraphMetadata;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraphMetadata;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;
import spade.query.quickgrail.quickstep.entities.QuickstepGraphMetadata;

public class CreateSetGraphMetadata extends InstructionFactory
{
	private GraphMetadata targetMetadata;
	private GraphMetadataComponent component;
	private Graph sourceGraph;
	private String name;
	private String value;

	public CreateSetGraphMetadata(GraphMetadata targetMetadata, GraphMetadataComponent component,
								  Graph sourceGraph, String name, String value)
	{
		super();
		this.targetMetadata = targetMetadata;
		this.component = component;
		this.sourceGraph = sourceGraph;
		this.name = name;
		this.value = value;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.SetGraphMetadata((Neo4jGraphMetadata) targetMetadata,
					component, (Neo4jGraph) sourceGraph, name, value);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.SetGraphMetadata(
					(PostgreSQLGraphMetadata) targetMetadata, component,
					(PostgreSQLGraph) sourceGraph, name, value);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.SetGraphMetadata(
					(QuickstepGraphMetadata) targetMetadata, component,
					(QuickstepGraph) sourceGraph, name, value);
		}

		return instruction;
	}
}
