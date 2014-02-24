package de.uni_mannheim.informatik.dws.goldminer.sparql;

import java.util.ArrayList;
import java.util.List;


public class SPARQLResultPairsIterator implements ResultPairsIterator {

	private SPARQLQueryEngine m_engine;
	
	private String m_sQuery;
	
	private int m_iMaxChunkSize;
	
	private List<String[]> m_nextChunk;
	
	private int iNext = 0;
	
	private int m_iOffset = 0;
	
	private String filter;
	
	
	protected SPARQLResultPairsIterator(SPARQLQueryEngine engine, String sQuery, String filter){
		m_sQuery = sQuery;
		m_engine = engine;
		m_nextChunk = new ArrayList<String[]>();
		m_iMaxChunkSize = m_engine.getChunkSize();
		this.filter = filter;
	}
	
	@Override
    public boolean hasNext() {
		int iChunkSize = m_nextChunk.size();
		if( iNext < iChunkSize ){
			return true;
		}
		else if( iChunkSize > 0 && iChunkSize < m_iMaxChunkSize ){
			return false;
		}
		try {
			m_nextChunk = m_engine.execute( m_sQuery +" LIMIT "+ m_iMaxChunkSize +" OFFSET "+ m_iOffset, "x", "y", filter );
			m_iOffset += m_nextChunk.size();
			iNext = 0;
			return m_nextChunk.size() > 0;
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
    public String[] next(){
		return m_nextChunk.get( iNext++ );
	}

	@Override
    public void remove(){}
}

