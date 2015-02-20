/**
 * Copyright (C) ${project.inceptionYear} Orion Health (Orchestral Development Ltd)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package XBDD;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.sun.syndication.io.impl.Base64;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;

import static org.apache.commons.io.FileUtils.readFileToString;

public class XBDDTestResultsSender implements TaskType
{
	private URL url;
	private String XBDD;
	private String creds;

	@Override
	public TaskResult execute(final TaskContext taskContext) throws TaskException
	{
		final BuildLogger buildLogger = taskContext.getBuildLogger();
		final String productName = taskContext.getConfigurationMap().get("product").replace(" ", "%20").replace("/", "-");
		XBDD = taskContext.getConfigurationMap().get("host");

		final String username = taskContext.getBuildContext().getVariableContext().getDefinitions().get("xbdd.username").getValue();
		final String password = taskContext.getBuildContext().getVariableContext().getDefinitions().get("xbdd.password").getValue();
		creds = Base64.encode(username + ":" + password);

		try {
			this.url = new URL(XBDD +"/rest/reports/" + productName + "/" + this.getVersionString(taskContext) + "/" + taskContext.getConfigurationMap().get("plug-build"));
			final String[] fileList = taskContext.getConfigurationMap().get("path").split(",");
			final Path rootDir = taskContext.getRootDirectory().toPath();
			for (String filePath : fileList) {
				final String path = rootDir + "/" + filePath.trim();
				final File file = new File(path);
				buildLogger.addBuildLogEntry(this.sendJSONReport(readFileToString(file)).toString());
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			buildLogger.addBuildLogEntry(errors.toString());
			buildLogger.addErrorLogEntry("Failed to connect to XBDD at host: " + XBDD + ", please ensure the XBDD server is up and running");
			return TaskResultBuilder.create(taskContext).failed().build();
		}

		return TaskResultBuilder.create(taskContext).success().build();
	}

	private Integer sendJSONReport(final String fileContents) throws Exception {
		HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();

		TrustModifier.relaxHostChecking(connection);

		connection.setRequestProperty("Authorization", "Basic "+creds);

		connection.setRequestMethod("PUT");
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Accept", "application/json");
		OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
		osw.write(String.format(fileContents));
		osw.flush();
		osw.close();
		return connection.getResponseCode();
	}

	private String getVersionString(TaskContext taskContext) {
		return taskContext.getConfigurationMap().get("major") + "."
				+ taskContext.getConfigurationMap().get("minor") + "."
				+ taskContext.getConfigurationMap().get("servicepack");
	}
}