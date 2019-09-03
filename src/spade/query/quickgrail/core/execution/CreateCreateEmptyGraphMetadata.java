package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.GraphMetadata;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraphMetadata;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraphMetadata;
import spade.query.quickgrail.quickstep.entities.QuickstepGraphMetadata;

public class CreateCreateEmptyGraphMetadata extends InstructionFactory
{
	private GraphMetadata metadata;

	public CreateCreateEmptyGraphMetadata(GraphMetadata metadata)
	{
		super();
		this.metadata = metadata;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.CreateEmptyGraphMetadata((Neo4jGraphMetadata) metadata);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.CreateEmptyGraphMetadata((PostgreSQLGraphMetadata) metadata);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.CreateEmptyGraphMetadata((QuickstepGraphMetadata) metadata);
		}

		return instruction;
	}
}
