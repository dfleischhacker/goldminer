package de.uni_mannheim.informatik.dws.goldminer.util;

import java.io.File;
import java.io.FileFilter;

/**
 * Filter helps to identify .txt files in a folder.
 * 
 * @author Jakob
 *
 */
public class TextFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		if(file.getPath().endsWith(".txt")) {
			return true;
		}
		return false;
	}
}
