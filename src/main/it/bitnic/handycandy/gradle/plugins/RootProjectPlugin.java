package it.bitnic.handycandy.gradle.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;

public class RootProjectPlugin extends BasePlugin {

	{
		// -------------------------------------------------------------------------------------------------------------
		// Use UTF-8 encoding
		// -------------------------------------------------------------------------------------------------------------
		configureTasks(JavaCompile.class, javac -> {
			CompileOptions options = javac.getOptions();
			options.setEncoding(StandardCharsets.UTF_8.name());
		});

		// -------------------------------------------------------------------------------------------------------------
		// Generate method parameter name metadata
		// -------------------------------------------------------------------------------------------------------------
		configureTask("eclipse", task -> {
			task.doLast(eclipse -> {
				addToEclipseJdtPrefs( eclipse.getProject(),
				                      Map.of("org.eclipse.jdt.core.compiler.codegen.methodParameters", "generate") );
			});
		});
		configureTasks(JavaCompile.class, javac -> {
			javac.getOptions().getCompilerArgs().add("-parameters");
		});

		// -------------------------------------------------------------------------------------------------------------
		// Use Junit 5
		// -------------------------------------------------------------------------------------------------------------
		configureTasks(Test.class, test -> {
			test.useJUnitPlatform();
		});

		// -------------------------------------------------------------------------------------------------------------
		// Use assertions
		// -------------------------------------------------------------------------------------------------------------
		configureTasks(Test.class, test -> {
			test.setEnableAssertions(true);
		});
		configureTasks(JavaExec.class, javaExec -> {
			javaExec.setEnableAssertions(true);
		});
		// TODO see if we can enable them in eclipse too (both when running tests and when running main())

		// -------------------------------------------------------------------------------------------------------------
		// Have javac complain less
		// -------------------------------------------------------------------------------------------------------------
		configureTasks(JavaCompile.class, javac -> {
			javac.getOptions().setWarnings(false);
		});

		// -------------------------------------------------------------------------------------------------------------
		// Enable java preview features (mostly because of text blocks in java 13)
		// -------------------------------------------------------------------------------------------------------------
		configureTasks(JavaCompile.class, javac -> {
			javac.getOptions().getCompilerArgs().add("--enable-preview");
		});
		configureTasks(Test.class, test -> {
			test.jvmArgs("--enable-preview");
		});
		configureTasks(JavaExec.class, javaExec -> {
			javaExec.jvmArgs("--enable-preview");
		});
		configureProject(project -> {
			project.subprojects(subproject -> {
				subproject.afterEvaluate(_subproject -> {
					JavaApplication application = subproject.getExtensions().findByType(JavaApplication.class);
					if (application != null) {
						List<String> jvmArgs = StreamSupport.stream(application.getApplicationDefaultJvmArgs().spliterator(), false)
						                                    .collect(Collectors.toList());
						jvmArgs.add("--enable-preview");
						application.setApplicationDefaultJvmArgs(jvmArgs);
					}
				});
			});
		});
		configureTask("eclipse", task -> {
			task.doLast(eclipse -> {
				addToEclipseJdtPrefs( eclipse.getProject(),
				                      Map.of( "org.eclipse.jdt.core.compiler.problem.enablePreviewFeatures", "enabled",
				                              "org.eclipse.jdt.core.compiler.problem.reportPreviewFeatures", "warning" ) );
			});
		});

		// -------------------------------------------------------------------------------------------------------------
		// Configure eclipse task tags
		// -------------------------------------------------------------------------------------------------------------
		configureTask("eclipse", task -> {
			task.doLast(eclipse -> {
				Map<String,String> tags = new LinkedHashMap<>();
				tags.put("XXX",    "HIGH");
				tags.put("FIXME",  "HIGH");
				tags.put("TODO",   "NORMAL");
				tags.put("THINKME","LOW");
				tags.put("LATER",  "LOW");
				addToEclipseJdtPrefs( eclipse.getProject(),
				                      Map.of( "org.eclipse.jdt.core.compiler.taskCaseSensitive", "enabled",
				                              "org.eclipse.jdt.core.compiler.taskTags",       tags.keySet().stream().collect(Collectors.joining(",")),
				                              "org.eclipse.jdt.core.compiler.taskPriorities", tags.values().stream().collect(Collectors.joining(",")) ) );
			});
		});

		// -------------------------------------------------------------------------------------------------------------
		// Configure eclipse compiler warnings
		// -------------------------------------------------------------------------------------------------------------
		configureTask("eclipse", task -> {
			task.doLast(eclipse -> {
				addToEclipseJdtPrefs( eclipse.getProject(),
				                      Map.ofEntries( Map.entry("org.eclipse.jdt.core.compiler.annotation.inheritNullAnnotations", "disabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.annotation.missingNonNullByDefaultAnnotation", "ignore"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.annotation.nonnull.secondary", "org.eclipse.jdt.annotation.NonNull"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.annotation.nonnull", "org.eclipse.jdt.annotation.NonNull"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.annotation.nonnullbydefault.secondary", "org.eclipse.jdt.annotation.NonNullByDefault"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.annotation.nonnullbydefault", "org.eclipse.jdt.annotation.NonNullByDefault"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.annotation.nullable.secondary", "org.eclipse.jdt.annotation.Nullable"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.annotation.nullable", "org.eclipse.jdt.annotation.Nullable"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.annotation.nullanalysis", "disabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.APILeak", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.annotationSuperInterface", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.autoboxing", "ignore"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.comparingIdentical", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.deadCode", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.deprecation", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.deprecationInDeprecatedCode", "disabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.deprecationWhenOverridingDeprecatedMethod", "disabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.discouragedReference", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.emptyStatement", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.explicitlyClosedAutoCloseable", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.fallthroughCase", "ignore"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.fatalOptionalError", "disabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.fieldHiding", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.finalParameterBound", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.finallyBlockNotCompletingNormally", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.forbiddenReference", "error"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.hiddenCatchBlock", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.includeNullInfoFromAsserts", "enabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.incompatibleNonInheritedInterfaceMethod", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.incompleteEnumSwitch", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.indirectStaticAccess", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.localVariableHiding", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.methodWithConstructorName", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.missingDefaultCase", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.missingDeprecatedAnnotation", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.missingEnumCaseDespiteDefault", "enabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.missingHashCodeMethod", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotation", "ignore"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotationForInterfaceMethodImplementation", "enabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.missingSerialVersion", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.missingSynchronizedOnInheritedMethod", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.noEffectAssignment", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.noImplicitStringConversion", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral", "ignore"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.nonnullParameterAnnotationDropped", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.nonnullTypeVariableFromLegacyInvocation", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.nullAnnotationInferenceConflict", "error"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.nullReference", "error"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.nullSpecViolation", "error"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.nullUncheckedConversion", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.overridingPackageDefaultMethod", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.parameterAssignment", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.pessimisticNullAnalysisForFreeTypeVariables", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.possibleAccidentalBooleanAssignment", "ignore"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.potentialNullReference", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.potentiallyUnclosedCloseable", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.rawTypeReference", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.redundantNullAnnotation", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.redundantNullCheck", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.redundantSpecificationOfTypeArguments", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.redundantSuperinterface", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.reportMethodCanBePotentiallyStatic", "ignore"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.reportMethodCanBeStatic", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.specialParameterHidingField", "disabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.staticAccessReceiver", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.suppressOptionalErrors", "disabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.suppressWarnings", "enabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.syntacticNullAnalysisForFields", "enabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.syntheticAccessEmulation", "info"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.terminalDeprecation", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.typeParameterHiding", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unavoidableGenericTypeProblems", "enabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.uncheckedTypeOperation", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unclosedCloseable", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.undocumentedEmptyBlock", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unhandledWarningToken", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unlikelyCollectionMethodArgumentType", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unlikelyCollectionMethodArgumentTypeStrict", "disabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unlikelyEqualsArgumentType", "info"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unnecessaryElse", "ignore"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unnecessaryTypeCheck", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unqualifiedFieldAccess", "ignore"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unstableAutoModuleName", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownException", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionExemptExceptionAndThrowable", "enabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionIncludeDocCommentReference", "enabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionWhenOverriding", "disabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedExceptionParameter", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedImport", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedLabel", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedLocal", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedObjectAllocation", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedParameter", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedParameterIncludeDocCommentReference", "enabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedParameterWhenImplementingAbstract", "disabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedParameterWhenOverridingConcrete", "disabled"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedPrivateMember", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedTypeParameter", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.unusedWarningToken", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.problem.varargsArgumentNeedCast", "warning"),
				                                     Map.entry("org.eclipse.jdt.core.compiler.release", "disabled") ) );
			});
		});
	}

	private static void addToEclipseJdtPrefs(Project project, Map<String,String> additionalPrefs) {
		File jdtprefsFile = project.file(".settings/org.eclipse.jdt.core.prefs");
		Properties jdtprefs = new Properties();
		try (InputStream in = new FileInputStream(jdtprefsFile)) {
			jdtprefs.load(in);
		} catch (IOException e) {
			throw new Error("Couldn't read " + jdtprefsFile.getPath(), e);
		}
		jdtprefs.putAll(additionalPrefs);
		try (OutputStream out = new FileOutputStream(jdtprefsFile)) {
			jdtprefs.store(out, null);
		} catch (IOException e) {
			throw new Error("Couldn't write " + jdtprefsFile.getPath(), e);
		}
	}

}
