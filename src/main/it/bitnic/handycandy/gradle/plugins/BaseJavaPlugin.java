package it.bitnic.handycandy.gradle.plugins;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.plugins.ide.eclipse.model.EclipseClasspath;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;

import groovy.util.Node;
import it.bitnic.handycandy.gradle.tasks.MkDirsTask;

public abstract class BaseJavaPlugin extends BasePlugin {

	{
		configureProject(project -> {

			Stream.of(
				"java",
				"net.ltgt.apt-eclipse",
				"net.ltgt.apt-idea",
				"com.gorylenko.gradle-git-properties"
			).forEach(project.getPluginManager()::apply);

			// Use the same directory for both java sources and resources
			project.getExtensions().getByType(SourceSetContainer.class).forEach(srcSet -> {
				List<File> dirs = Arrays.asList(project.file("src/" + srcSet.getName()));
				srcSet.getJava().setSrcDirs(dirs);
				srcSet.getResources().setSrcDirs(dirs);
			});

			// JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
			EclipseModel eclipse = project.getExtensions().getByType(EclipseModel.class);

			// Use "build-eclipse/base" instead of "bin" as the default eclipse output directory
			EclipseClasspath eclipseClasspath = eclipse.getClasspath();
			eclipseClasspath.setDefaultOutputDir( project.file("build-eclipse/base"));
			//
			// We should be done at this point, but we are far from done :(
			//
			// The line above cause the .classpath file to look like:
			//
			// <classpath>
			//   <classpathentry kind="output" path="build-eclipse/base"/>
			//   <classpathentry kind="src"    path="src/main" output="bin/main"> ... </classpathentry>
			// </classpath>
			//
			// In other words the above line is near useless and we have to go change each source directory output setting.
			//
			// https://github.com/gradle/gradle/issues/3839 suggests using
			//
			// eclipseClasspath.getFile().whenMerged(__ -> {
			//   eclipseClasspath.resolveDependencies().forEach(classpathEntry -> {
			//     if ("src".equals(classpathEntry.getKind())) {
			//       SourceFolder sourceFolder = SourceFolder.class.cast(classpathEntry);
			//       sourceFolder.setOutput(sourceFolder.getOutput().replaceFirst("^bin/", "build-eclipse/"));
			//     }
			//   });
			// });
			//
			// But that doesn't seem to work, so we'll have to resort to something even uglier:
			//
			eclipseClasspath.getFile().whenMerged(__ -> {
				eclipseClasspath.getFile().withXml(xml -> {
					@SuppressWarnings("unchecked") List<Node> nodes = xml.asNode().breadthFirst();
					nodes.forEach(node -> {
						boolean isSrcDir = "classpathentry".equals(node.name())
						                && "src".equals(node.attribute("kind"))
						                && node.attributes().containsKey("output"); // referenced projects are kind="src" too
						if (isSrcDir) {
							@SuppressWarnings("unchecked") Map<Object,Object> attributes = node.attributes();
							String oldoutput = attributes.getOrDefault("output", "").toString();
							if (oldoutput.startsWith("bin/")) {
								attributes.put("output", oldoutput.replaceFirst("^bin/", "build-eclipse/"));
							}
						}
					});
				});
			});
			//
			// The above causes duplicate <classpathentry>s in .classpath files (couldn't find out why);
			// this works around the problem
			//
			project.getTasks().getByName("eclipse").dependsOn(project.getTasks().getByName("cleanEclipse"));

			// Set eclipse project name based on project group (eg. com.example.group:project)
			eclipse.getProject().setName(project.getGroup().toString() + ":" + project.getName());

			// Add mkdirs task to create missing source directories
			MkDirsTask.install(project, "mkdirs");
		});
	}

}
