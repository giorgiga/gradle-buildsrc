package it.bitnic.handycandy.gradle.tasks;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import it.bitnic.handycandy.gradle.util.GradleUtil;

public class MkDirsTask extends DefaultTask {

	public MkDirsTask() {
		setGroup("IDE");
		setDescription("Creates missing source directories");
	}

	public static void install(Project project, String taskName) {
		MkDirsTask task = project.getTasks().create(taskName, MkDirsTask.class);

		// run before generating eclipse or idea projects
		project.afterEvaluate(prj -> {
			Stream.of(
				"eclipseClasspath", "idea"
			).forEach(dependentTaskName -> project.getTasks().named(dependentTaskName, t -> t.dependsOn(task)) );
		});
	}

	@TaskAction public void mkdirs() {
		Optional<String> pkgDir = Optional.ofNullable(getProject().getRootProject().getProperties().get("stereotypical.basePackage"))
		                                  .map(Object::toString)
		                                  .map(pkg -> pkg.replace('.', '/') + getProject().getPath().replace(':', '/'));
		GradleUtil.sourceDirs(getProject())
		          .map( src -> pkgDir.map(pkg -> new File(src, pkg)).orElse(src) )
		          .forEach(dir -> {
		              if (dir.mkdirs()) getProject().getLogger().lifecycle("\tmkdir " + dir.getAbsolutePath());
		          });
	}

}
