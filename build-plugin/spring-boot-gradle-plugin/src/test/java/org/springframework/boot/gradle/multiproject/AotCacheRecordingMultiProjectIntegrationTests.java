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

package org.springframework.boot.gradle.multiproject;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.TestTemplate;

import org.springframework.boot.gradle.junit.GradleCompatibility;
import org.springframework.boot.testsupport.gradle.testkit.GradleBuild;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests verifying that AOT cache recording is scoped to the project whose
 * {@code bootBuildImage} task is scheduled, and not triggered by another project's task
 * that happens to share the same name.
 *
 * @author Vasily Pelikh
 */
@GradleCompatibility(configurationCache = true)
class AotCacheRecordingMultiProjectIntegrationTests {

	@SuppressWarnings("NullAway.Init")
	GradleBuild gradleBuild;

	@TestTemplate
	void aotCacheRecordingNotConfiguredWhenOnlyOtherProjectBuildsImage() throws IOException {
		createSubprojects();
		// Only :other:bootBuildImage is scheduled, so :app:test must not get the AOT arg.
		// A same-named task in another project must not trigger recording on :app:test
		// (the multi-project gating fix).
		this.gradleBuild.build("verifyRecordingNotConfigured", ":other:bootBuildImage");
	}

	@TestTemplate
	void aotCacheRecordingConfiguredWhenOwnProjectBuildsImage() throws IOException {
		createSubprojects();
		// :app:bootBuildImage is scheduled alongside :app:test, so recording is enabled.
		this.gradleBuild.build("verifyRecordingConfigured", ":app:bootBuildImage");
	}

	private void createSubprojects() throws IOException {
		createMainSource("app");
		createMainSource("other");
	}

	private void createMainSource(String subproject) throws IOException {
		File examplePackage = new File(this.gradleBuild.getProjectDir(), subproject + "/src/main/java/com/example");
		examplePackage.mkdirs();
		try (java.io.Writer writer = new java.io.FileWriter(new File(examplePackage, "Application.java"))) {
			writer.write(
					"package com.example;\npublic class Application {\n\tpublic static void main(String[] args) {\n\t}\n}\n");
		}
	}

}