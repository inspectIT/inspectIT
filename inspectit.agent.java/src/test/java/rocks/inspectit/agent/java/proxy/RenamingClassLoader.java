package rocks.inspectit.agent.java.proxy;

import info.novatec.inspectit.org.objectweb.asm.ClassReader;
import info.novatec.inspectit.org.objectweb.asm.ClassWriter;
import info.novatec.inspectit.org.objectweb.asm.commons.Remapper;
import info.novatec.inspectit.org.objectweb.asm.commons.RemappingClassAdapter;

import java.io.IOException;
import java.io.InputStream;


/**
 * @author Jonas Kunz
 *
 */
public class RenamingClassLoader extends ClassLoader {

	private Class<?>[] classesToRenameHere;
	private String newPrefix;

	public RenamingClassLoader(ClassLoader parent, String newPrefix, Class<?>... classesToRenameHere) {
		super(parent);
		this.classesToRenameHere = classesToRenameHere;
		this.newPrefix = newPrefix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
		for (Class<?> orginalClass : classesToRenameHere) {
			final String originalName = orginalClass.getName();
			boolean testVar = (newPrefix + originalName).equals(name);
			if (testVar) {
				try {
					InputStream stream = ClassLoader.getSystemResourceAsStream(orginalClass.getName().replace('.', '/') + ".class");
					ClassReader reader = new ClassReader(stream);
					ClassWriter writer = new ClassWriter(reader, 0);
					RemappingClassAdapter remapper = new RemappingClassAdapter(writer, new Remapper() {
						/**
						 * {@inheritDoc}
						 */
						@Override
						public String mapType(String paramString) {
							if (paramString.equals(originalName.replace('.', '/'))) {
								return name.replace('.', '/');
							}
							return super.mapType(paramString);
						}

					/* skipped */ });
					reader.accept(remapper, ClassReader.EXPAND_FRAMES);
					byte[] source = writer.toByteArray();
					return defineClass(name, source, 0, source.length);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return super.findClass(name);
	}

}
