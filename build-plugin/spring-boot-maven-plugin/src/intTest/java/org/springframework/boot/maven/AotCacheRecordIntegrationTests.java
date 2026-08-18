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

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

/**
 * Integration tests for the AOT cache recording integration.
 *
 * @author Vasily Pelikh
 */
@ExtendWith(MavenBuildExtension.class)
class AotCacheRecordIntegrationTests {

	@TestTemplate
	void recordsAotCacheFromIntegrationTests(MavenBuild mavenBuild) {
		mavenBuild.project("aot-cache-record").goals("initialize").execute((project) -> {
			String output = contentOf(project.toPath().resolve("target/build.log").toFile());
			assertThat(output).contains("Configured test AOT cache recording with argLine:");
			assertThat(output).contains("-XX:AOTCacheOutput=");
			assertThat(output).contains("aot-cache/application.aot");
		});
	}

	@TestTemplate
	void appendsToExistingArgLine(MavenBuild mavenBuild) {
		mavenBuild.project("aot-cache-record")
			.goals("initialize", "help:evaluate", "-Dexpression=argLine", "-DforceStdout")
			.execute((project) -> {
				String output = contentOf(project.toPath().resolve("target/build.log").toFile());
				assertThat(output).contains("-Xmx512m");
				assertThat(output).contains("-XX:AOTCacheOutput=");
			});
	}

	@TestTemplate
	void skipsRecordingWhenBuildImageIsNotScheduled(MavenBuild mavenBuild) {
		mavenBuild.project("aot-cache-record-test-only").goals("initialize").execute((project) -> {
			String output = contentOf(project.toPath().resolve("target/build.log").toFile());
			assertThat(output).contains("spring-boot:build-image goal is not in the execution plan");
			assertThat(output).contains("Cache recording will be skipped");
			assertThat(output).doesNotContain("-XX:AOTCacheOutput=");
		});
	}

	@TestTemplate
	void failsClosedWhenCacheFileIsMissing(MavenBuild mavenBuild) {
		mavenBuild.project("aot-cache-fail-closed")
			.goals("package", "spring-boot:build-image-no-fork")
			.executeAndFail((project) -> {
				String output = contentOf(project.toPath().resolve("target/build.log").toFile());
				assertThat(output).contains("aot-cache/application.aot");
				assertThat(output).contains("mvn test spring-boot:build-image");
			});
	}

}