package info.novatec.inspectit.storage.util;

import info.novatec.inspectit.storage.StorageFileType;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.lang.ArrayUtils;

/**
 * Delete file visitor that can delete storage files based on the type.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageDeleteFileVisitor extends DeleteFileVisitor {

	/**
	 * File types to delete.
	 */
	private StorageFileType[] storageFileTypes;

	/**
	 * If directories should be deleted.
	 */
	private boolean deleteDirs;

	/**
	 * Default constructor.
	 * 
	 * @param storageFileTypes
	 *            File types to delete.
	 * @param deleteDirs
	 *            If directories should be deleted.
	 */
	public StorageDeleteFileVisitor(StorageFileType[] storageFileTypes, boolean deleteDirs) {
		super();
		this.storageFileTypes = storageFileTypes;
		this.deleteDirs = deleteDirs;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Only deletes the files that have extension matching the supplied {@link #storageFileTypes}.
	 */
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if (ArrayUtils.isNotEmpty(storageFileTypes)) {
			for (StorageFileType storageFileType : storageFileTypes) {
				if (file.endsWith(storageFileType.getExtension())) {
					return super.visitFile(file, attrs);
				}
			}
		}
		return FileVisitResult.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		if (deleteDirs) {
			try {
				return super.postVisitDirectory(dir, exc);
			} catch (IOException e) {
				// if directory delete fails, we still want to continue
				return FileVisitResult.CONTINUE;
			}
		}
		return FileVisitResult.CONTINUE;
	}

}
