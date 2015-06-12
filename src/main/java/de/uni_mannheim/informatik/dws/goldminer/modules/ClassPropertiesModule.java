package de.uni_mannheim.informatik.dws.goldminer.modules;

import de.uni_mannheim.informatik.dws.goldminer.database.*;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Set;

public class ClassPropertiesModule extends MinerModule {
	
	private SQLDatabase sqlDatabase;
	private SQLFactory _sqlFactory;
	private TerminologyExtractor _terminologyExtractor;
	private IndividualsExtractor _individualsExtractor;
	private TablePrinter _tablePrinter;
	
	public ClassPropertiesModule() throws MinerModuleException {
		try {
			this.sqlDatabase = SQLDatabase.instance();
			this._tablePrinter = new TablePrinter();
			this._sqlFactory = new SQLFactory();
			this._terminologyExtractor = new TerminologyExtractor();
			this._individualsExtractor = new IndividualsExtractor();
		} catch (FileNotFoundException e) {
			throw new MinerModuleException(e);
		} catch (SQLException e) {
			throw new MinerModuleException(e);
		} catch (IOException e) {
			throw new MinerModuleException(e);
		}
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getModuleName() {
		return "classProperties";
	}

	@Override
	public String getFileString() {
		return "classProperties";
	}

	@Override
	public String getDescription() {
		return "find properties that are mandatory for a specific class.";
	}

	@Override
	public void setupSchema() throws MinerModuleException {
		this.sqlDatabase.execute(this._sqlFactory.dropTables());
		this.sqlDatabase.execute(this._sqlFactory.createClassesTable());
		this.sqlDatabase.execute(this._sqlFactory.createIndividualsTable());
		this.sqlDatabase.execute(this._sqlFactory.createPropertiesTable());
		this.sqlDatabase.execute(this._sqlFactory.createDatatypePropertiesTable());
	}

	@Override
	public void acquireTerminology() throws MinerModuleException {
		this._terminologyExtractor.initClassesTable();
		try {
			this._individualsExtractor.initIndividualsTable();
		} catch (SQLException e) {
			throw new MinerModuleException("Error while filling individuals table!", e);
		}
		this._terminologyExtractor.initPropertiesTable();
		this._terminologyExtractor.initDatatypePropertiesTable();
	}

	@Override
	public void generateTransactionTable(OutputStream output) {
		try {
			this._tablePrinter.printClassProperties(output);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void generateAssociationRules(File inputFile, File outputFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<OWLAxiom> readAssociationRules(File inputFile) {
		// TODO Auto-generated method stub
		return null;
	}

}
