package it.bitnic.handycandy.gradle.plugins;

import java.util.stream.Stream;

public class JavaLibraryPlugin extends BaseJavaPlugin {

	{
		configureProject(project -> {
			Stream.of(
				"java-library"
			).forEach(project.getPluginManager()::apply);
		});
	}

}
