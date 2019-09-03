package spade.query.quickgrail.core.entities;

public abstract class GraphMetadata extends Entity
{
	public static final String kDigits = "0123456789ABCDEF";

	public abstract String getName();

	public abstract String getVertexTableName();

	public abstract String getEdgeTableName();

	public enum GraphMetadataComponent
	{
		kVertex,
		kEdge,
		kBoth
	}
}
