package net.runeduniverse.tools.maven.r4m.grm.api;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.grm.model.data.EntityData;
import net.runeduniverse.tools.maven.r4m.grm.model.data.GoalData;
import net.runeduniverse.tools.maven.r4m.grm.model.data.ProjectData;
import net.runeduniverse.tools.maven.r4m.grm.model.data.RuntimeData;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;

public interface GoalRequirementDataFactory {

	public ProjectData createProjectData(final MavenProject mvnProject);

	public RuntimeData createRuntimeData(final ExecutionArchiveSelectorConfig cnf, final String phase);

	public GoalData createGoalData(final MojoExecution mojoExecution);

	public EntityData createEntityData(final ProjectData projectData, final RuntimeData runtimeData,
			final GoalData goalData);

}
