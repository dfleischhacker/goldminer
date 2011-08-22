package miner.sparql;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;


public class ResultsIterator implements Iterator<String> {

	private QueryEngine m_engine;
	
	private String m_sQuery;
	
	private int m_iMaxChunkSize;
	
	private List<String> m_nextChunk;
	
	private int iNext = 0;
	
	private int m_iOffset = 0;
	
	private String filter;
	
	private boolean failed;
	
	
	protected ResultsIterator( QueryEngine engine, String sQuery, String filter ){
		m_sQuery = sQuery;
		m_engine = engine;
		m_nextChunk = new ArrayList<String>();
		m_iMaxChunkSize = m_engine.getChunkSize();
		this.failed = false;
		this.filter = filter;
	}
	
	public boolean hasNext() {
		int iChunkSize = m_nextChunk.size();
		if( iNext < iChunkSize ){
			return true;
		}
		else if( iChunkSize > 0 && iChunkSize < m_iMaxChunkSize ){
			return false;
		}
		try {
			m_nextChunk = m_engine.execute( m_sQuery +" LIMIT "+ m_iMaxChunkSize +" OFFSET "+ m_iOffset, "x", filter );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		m_iOffset += m_nextChunk.size();
		iNext = 0;
		return m_nextChunk.size() > 0;
	}
	
	public String next(){
		return m_nextChunk.get( iNext++ );
	}
	
	public boolean isFailed() {
		return this.failed;
	}

	public void remove(){}
}

