package info.novatec.inspectit.storage.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * File visitor that can copy or move directories.
 * 
 * @author Ivan Senic
 * 
 */
public class CopyMoveFileVisitor extends DeleteFileVisitor {

	/**
	 * Source path.
	 */
	private Path fromPath;

	/**
	 * Destination path.
	 */
	private Path toPath;

	/**
	 * If source should be deleted.
	 */
	private boolean deleteSource;

	/**
	 * Default constructor. Only performs the copy.
	 * 
	 * @param fromPath
	 *            Source path.
	 * @param toPath
	 *            Destination path.
	 */
	public CopyMoveFileVisitor(Path fromPath, Path toPath) {
		this(fromPath, toPath, false);
	}

	/**
	 * Secondary constructor. Allow user to set if the source of the copy will be deleted after
	 * copying producing the move operation.
	 * 
	 * @param fromPath
	 *            Source path.
	 * @param toPath
	 *            Destination path.
	 * @param deleteSource
	 *            If source should be deleted.
	 */
	public CopyMoveFileVisitor(Path fromPath, Path toPath, boolean deleteSource) {
		this.fromPath = fromPath;
		this.toPath = toPath;
		this.deleteSource = deleteSource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		Path targetPath = toPath.resolve(fromPath.relativize(dir));
		if (!Files.exists(targetPath)) {
			Files.createDirectory(targetPath);
		}
		return FileVisitResult.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		if (deleteSource) {
			return super.postVisitDirectory(dir, exc);
		}
		return FileVisitResult.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		Files.copy(file, toPath.resolve(fromPath.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
		if (deleteSource) {
			return super.visitFile(file, attrs);
		} else {
			return FileVisitResult.CONTINUE;
		}
	}

}
