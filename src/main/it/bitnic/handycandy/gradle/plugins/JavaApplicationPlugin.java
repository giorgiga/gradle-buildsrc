package it.bitnic.handycandy.gradle.plugins;

import java.util.stream.Stream;

public class JavaApplicationPlugin extends BaseJavaPlugin {

	{
		configureProject(project -> {
			Stream.of(
				"application",
				"com.github.johnrengelman.shadow"
			).forEach(project.getPluginManager()::apply);
		});

		// Configure the shadow plugin as per groovy:
		//
		// shadowJar {
		//     mergeServiceFiles()
		// }
		configureTask("shadowJar", shadowJar -> {
			try {
				shadowJar.getClass().getMethod("mergeServiceFiles").invoke(shadowJar);
			} catch (Exception e) {
				throw new Error(e);
			}
		});
	}

}
