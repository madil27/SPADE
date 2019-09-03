package spade.query.quickgrail.core.entities;

public abstract class Graph extends Entity
{
	// limits for exporting graph to dot and text files
	public static final int kNonForceVisualizeLimit = 1000;
	public static final int kNonForceDumpLimit = 100;

	public abstract String getName();

	public abstract String getTableName(GraphComponent component);

	public abstract String getVertexTableName();

	public abstract String getEdgeTableName();

	public enum GraphComponent
	{
		kVertex,
		kEdge
	}

	public enum ExportFormat
	{
		kNormal,
		kDot
	}

	public enum EdgeComponent
	{
		kSource,
		kDestination,
		kBoth
	}

	public enum Direction
	{
		kAncestor,
		kDescendant,
		kBoth
	}
}
