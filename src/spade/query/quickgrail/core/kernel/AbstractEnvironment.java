package spade.query.quickgrail.core.kernel;


import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.entities.GraphMetadata;

import java.util.Map;

public interface AbstractEnvironment
{
	boolean IsBaseGraph(Graph graph);

	void clear();

	void gc();

	String lookup(String symbol);

	void eraseSymbol(String symbol);

	void addSymbol(String symbol, String value);

	Map<String, String> getSymbols();

	void setResultGraphName(String graphName);

	Graph allocateGraph();

	GraphMetadata allocateGraphMetadata();

	Graph allocateGraph(String graphName);

	GraphMetadata allocateGraphMetadata(String graphName);
}
