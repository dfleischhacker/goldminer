package test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import miner.modules.ClassPropertiesModule;
import miner.modules.MinerModuleException;
import miner.util.Settings;

import org.junit.Test;


public class ClassPropertiesTest {

	@Test
	public void test() {
		OutputStream output;
		try {
			Settings.load();
			ClassPropertiesModule module = new ClassPropertiesModule();
			output = new FileOutputStream(
					Settings.getString("association_rules") + 
					module.getFileString() + 
					".txt");
			module.setupSchema();
			module.acquireTerminology();
			module.generateTransactionTable(output);
			output.close();
		} catch (MinerModuleException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
