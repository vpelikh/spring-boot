/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.maven;

import java.nio.file.Path;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link BuildImageMojo}.
 *
 * @author Vasily Pelikh
 */
class BuildImageMojoTests {

	private final TestBuildImageMojo mojo = new TestBuildImageMojo();

	@BeforeEach
	void setUp() {
		this.mojo.setProject(projectWithDirectory("/project/target"));
	}

	@Test
	void applyAotCacheConfigurationWhenPropertyEnabledEnablesCache() {
		this.mojo.setAotCacheRecord(true);
		Image image = new Image();
		this.mojo.applyAotCacheConfiguration(image);
		assertThat(image.getAotCacheRecord()).isTrue();
		assertThat(image.cacheDirectory).isEqualTo(Path.of("/project/target/aot-cache"));
	}

	@Test
	void applyAotCacheConfigurationWhenImageConfiguredEnablesCache() {
		Image image = new Image();
		image.setAotCacheRecord(true);
		this.mojo.applyAotCacheConfiguration(image);
		assertThat(image.getAotCacheRecord()).isTrue();
		assertThat(image.cacheDirectory).isEqualTo(Path.of("/project/target/aot-cache"));
	}

	@Test
	void applyAotCacheConfigurationWhenDisabledDoesNothing() {
		Image image = new Image();
		this.mojo.applyAotCacheConfiguration(image);
		assertThat(image.getAotCacheRecord()).isNull();
		assertThat(image.cacheDirectory).isNull();
	}

	private MavenProject projectWithDirectory(String directory) {
		MavenProject project = new MavenProject(new Model());
		Build build = new Build();
		build.setDirectory(directory);
		project.getModel().setBuild(build);
		return project;
	}

	private static final class TestBuildImageMojo extends BuildImageMojo {

		private TestBuildImageMojo() {
			super(mock(MavenProjectHelper.class));
		}

		void setProject(MavenProject project) {
			this.project = project;
		}

	}

}