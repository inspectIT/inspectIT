package info.novatec.inspectit.storage.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * File visitor for deleting directories.
 * 
 * @author Ivan Senic
 * 
 */
public class DeleteFileVisitor extends SimpleFileVisitor<Path> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		Files.delete(file);
		return FileVisitResult.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		if (null == exc) {
			Files.delete(dir);
			return FileVisitResult.CONTINUE;
		} else {
			throw exc;
		}
	}

}
