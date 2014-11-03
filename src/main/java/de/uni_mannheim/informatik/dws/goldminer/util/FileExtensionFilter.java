package de.uni_mannheim.informatik.dws.goldminer.util;

import java.io.File;
import java.io.FileFilter;

/**
 * Helper class for filtering file names based on their extension
 * 
 * @author Jakob
 *
 */
public class FileExtensionFilter implements FileFilter {
    private String extension;

    /**
     * Initializes this filter for only accepting files ending with the given
     * extension. The initial "." has to be included in <code>extension</code>.
     *
     * @param extension extension to accept (including initial dot)
     */
    public FileExtensionFilter(String extension) {
        this.extension = extension;
    }

	@Override
	public boolean accept(File file) {
		if(file.getPath().endsWith(extension)) {
			return true;
		}
		return false;
	}
}
