package it.bitnic.handycandy.gradle.util;

import java.io.File;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

public class GradleUtil {

	private GradleUtil() {
		// utility class
	}

	public static Stream<SourceSet> sourceSets(Project project) {
		return project.getExtensions().getByType(SourceSetContainer.class).stream();
	}

	public static Stream<File> sourceDirs(Project project) {
		return sourceSets(project).flatMap(srcSet -> {
			return Stream.of(srcSet.getJava(), srcSet.getResources())
			             .map(SourceDirectorySet::getSrcDirs)
			             .map(Iterable::spliterator)
			             .flatMap(split -> StreamSupport.stream(split, false));
		});
	}

}
