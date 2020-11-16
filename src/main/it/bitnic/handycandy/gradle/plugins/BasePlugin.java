package it.bitnic.handycandy.gradle.plugins;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.dsl.RepositoryHandler;

public abstract class BasePlugin implements Plugin<Project> {

	private final List<Consumer<Project>> onApply = new ArrayList<>();

	protected void configureProject(Consumer<Project> configurator) {
		onApply.add(configurator);
	}

	private final Map<Class<? extends Task>,List<Consumer<? extends Task>>> byTaskClassConfigurators = new LinkedHashMap<>();
	private final Map<String,List<Consumer<? extends Task>>> byTaskNameConfigurators = new LinkedHashMap<>();

	protected <T extends Task> void configureTasks(Class<T> taskClass, Consumer<T> action) {
		byTaskClassConfigurators.computeIfAbsent(taskClass, _taskClass -> new ArrayList<>()).add(action);
	}

	protected <T extends Task> void configureTask(String taskName, Consumer<T> action) {
		byTaskNameConfigurators.computeIfAbsent(taskName, _taskName -> new ArrayList<>()).add(action);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final void apply(Project project) {

		// configure default repositories

		RepositoryHandler repos = project.getRepositories();
		Stream.of(
			// repos.mavenLocal(),
			repos.mavenCentral(),
			repos.jcenter(),
			repos.google()
		).forEach(repos::add);

		// set project group based on project path

		project.setGroup(project.getRootProject().getName() + parentPath(project).replace(':', '.'));

		// run subclass-defined actions

		onApply.forEach(action -> action.accept(project));

		// setup task configurations to be run at the very end

		project.getGradle().getTaskGraph().whenReady(tg -> {
			tg.getAllTasks().stream().forEach(task -> {
				byTaskClassConfigurators.entrySet().forEach(entry -> {
					if (entry.getKey().isAssignableFrom(task.getClass())) {
						entry.getValue().forEach(configurator -> {
							((Consumer)configurator).accept(task);
						});
					}
				});
				byTaskNameConfigurators.entrySet().forEach(entry -> {
					if (entry.getKey().equals(task.getName())) {
						entry.getValue().forEach(configurator -> {
							((Consumer)configurator).accept(task);
						});
					}
				});
			});
		});
	}

	private static String parentPath(Project project) {
		String path = project.getPath();
		return path.substring(0, path.lastIndexOf(':'));
	}

}
