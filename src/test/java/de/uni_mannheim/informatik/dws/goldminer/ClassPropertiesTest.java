package de.uni_mannheim.informatik.dws.goldminer;

import de.uni_mannheim.informatik.dws.goldminer.modules.ClassPropertiesModule;
import de.uni_mannheim.informatik.dws.goldminer.modules.MinerModuleException;
import de.uni_mannheim.informatik.dws.goldminer.util.Settings;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Ignore
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
