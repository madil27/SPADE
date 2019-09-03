package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.GraphMetadata;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraphMetadata;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraphMetadata;
import spade.query.quickgrail.quickstep.entities.QuickstepGraphMetadata;

public class CreateOverwriteGraphMetadata extends InstructionFactory
{
	private GraphMetadata targetMetadata;
	private GraphMetadata lhsMetadata;
	private GraphMetadata rhsMetadata;

	public CreateOverwriteGraphMetadata(GraphMetadata targetMetadata,
										GraphMetadata lhsMetadata, GraphMetadata rhsMetadata)
	{
		super();
		this.targetMetadata = targetMetadata;
		this.lhsMetadata = lhsMetadata;
		this.rhsMetadata = rhsMetadata;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.OverwriteGraphMetadata((Neo4jGraphMetadata) targetMetadata,
					(Neo4jGraphMetadata) lhsMetadata, (Neo4jGraphMetadata) rhsMetadata);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.OverwriteGraphMetadata(
					(PostgreSQLGraphMetadata) targetMetadata,
					(PostgreSQLGraphMetadata) lhsMetadata, (PostgreSQLGraphMetadata) rhsMetadata);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.OverwriteGraphMetadata(
					(QuickstepGraphMetadata) targetMetadata,
					(QuickstepGraphMetadata) lhsMetadata, (QuickstepGraphMetadata) rhsMetadata);
		}

		return instruction;
	}
}
