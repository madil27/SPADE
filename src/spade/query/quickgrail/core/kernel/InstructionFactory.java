package spade.query.quickgrail.core.kernel;

import spade.core.AbstractQuery;

public abstract class InstructionFactory
{
	protected String storageName;

	public abstract Instruction createInstruction();

	protected InstructionFactory()
	{
		this.storageName = AbstractQuery.getCurrentStorageName();
	}
}
