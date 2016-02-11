package info.novatec.inspectit.storage.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.storage.StorageFileType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the file visitors we have implemented.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class FileVisitorsTest {

	private Path testPath = Paths.get("testPath");

	@BeforeMethod
	public void init() throws IOException {
		Files.createDirectories(testPath);
	}

	@AfterMethod
	public void delete() throws IOException {
		Files.deleteIfExists(testPath);
	}

	/**
	 * Test that deleting of directory and it content works, but that the parent dir won't be
	 * touched.
	 */
	@Test
	public void deleteFileVisitor() throws IOException {
		// we create additional dir and file in that dir
		Path dir = testPath.resolve("dir");
		Files.createDirectories(dir);
		Path file = dir.resolve(".testFile");
		Files.createFile(file);

		Files.walkFileTree(dir, new DeleteFileVisitor());

		assertThat(Files.exists(file), is(false));
		assertThat(Files.exists(dir), is(false));
		assertThat(Files.exists(testPath), is(true));
	}

	/**
	 * Test that the deleting of the storage files works with visitor.
	 */
	@Test
	public void storageDeleteFileVisitor() throws IOException {
		StorageFileType storageFileType = StorageFileType.DATA_FILE;

		// we create additional dir and file in that dir
		Path dir = testPath.resolve("dir");
		Files.createDirectories(dir);
		Path file = dir.resolve(storageFileType.getExtension());
		Files.createFile(file);

		// first with no dir deletion
		Files.walkFileTree(dir, new StorageDeleteFileVisitor(new StorageFileType[] { storageFileType }, false));

		assertThat(Files.exists(file), is(false));
		assertThat(Files.exists(dir), is(true));
		assertThat(Files.exists(testPath), is(true));

		// then with dir deletion
		Files.walkFileTree(dir, new StorageDeleteFileVisitor(new StorageFileType[] { storageFileType }, true));

		assertThat(Files.exists(dir), is(false));
		assertThat(Files.exists(testPath), is(true));
	}

	/**
	 * Tests if copying files works with visitor.
	 */
	@Test
	public void copyFileVisitor() throws IOException {
		Path source = testPath.resolve("source");
		Files.createDirectories(source);
		Path file = source.resolve(".testFile");
		Files.createFile(file);

		Path target = testPath.resolve("target");
		Path copiedFile = target.resolve(".testFile");
		Files.walkFileTree(source, new CopyMoveFileVisitor(source, target));

		assertThat(Files.exists(target), is(true));
		assertThat(Files.exists(copiedFile), is(true));

		Files.deleteIfExists(file);
		Files.deleteIfExists(copiedFile);
		Files.deleteIfExists(target);
		Files.delete(source);
	}

	/**
	 * Tests if moving files works with visitor.
	 */
	@Test
	public void moveFileVisitor() throws IOException {
		Path source = testPath.resolve("source");
		Files.createDirectories(source);
		Path file = source.resolve(".testFile");
		Files.createFile(file);

		Path target = testPath.resolve("target");
		Path copiedFile = target.resolve(".testFile");
		Files.walkFileTree(source, new CopyMoveFileVisitor(source, target, true));

		assertThat(Files.exists(file), is(false));
		assertThat(Files.exists(source), is(false));
		assertThat(Files.exists(target), is(true));
		assertThat(Files.exists(copiedFile), is(true));

		Files.deleteIfExists(copiedFile);
		Files.deleteIfExists(target);
	}
}
