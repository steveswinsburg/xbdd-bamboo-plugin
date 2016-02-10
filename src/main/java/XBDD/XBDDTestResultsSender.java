/**
 * Copyright (C) 2015 Orion Health (Orchestral Development Ltd)
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
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.sun.syndication.io.impl.Base64;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

import static org.apache.commons.io.FileUtils.readFileToString;

public class XBDDTestResultsSender implements TaskType
{
	private URL url;
	private String creds;

	/**
	 * @param taskContext The context passed from the bamboo build runner
	 * @return TaskResult A bamboo TaskResult object that indicates whether the Task completed successfully
	 * @throws TaskException
	 *
	 * Receives a taskContext from bamboo and attempts to fetch the test logs and send them to XBDD.  If the send fails
	 * a failure TaskResult is returned and the build is halted.
	 */
	@NotNull
	@Override
	public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException
	{
		final BuildLogger buildLogger = taskContext.getBuildLogger();
		// Fetches the product name from the configuration and escapes spaces and slashes to place as a URL variable
		final String productName = taskContext.getConfigurationMap().get("product").replace(" ", "%20").replace("/", "-");
		String XBDD = taskContext.getConfigurationMap().get("host");

		// Fetches the global bamboo variables xbdd.username and xbdd.username, if these variables aren't set the build
		// will fail
		Map<String, VariableDefinitionContext> globals = taskContext.getBuildContext().getVariableContext().getDefinitions();
		if (globals.containsKey("xbdd.username") && globals.containsKey("xbdd.password")) {
			final String username = taskContext.getBuildContext().getVariableContext().getDefinitions().get("xbdd.username").getValue();
			final String password = taskContext.getBuildContext().getVariableContext().getDefinitions().get("xbdd.password").getValue();
			creds = Base64.encode(username + ":" + password);
		} else {
			buildLogger.addErrorLogEntry("ERROR: The global XBDD username and password bamboo variables are not set, please set `xbdd.username` and `xbdd.password`");
			return TaskResultBuilder.create(taskContext).failed().build();
		}

		String path = "";
		try {
			this.url = new URL(XBDD + "/rest/reports/" + productName + "/" + this.getVersionString(taskContext) + "/" + taskContext.getConfigurationMap().get("plug-build"));
			final String[] fileList = taskContext.getConfigurationMap().get("path").split(",");
			final Path rootDir = taskContext.getRootDirectory().toPath();
			for (String filePath : fileList) {
				path = rootDir + "/" + filePath.trim();
				final File file = new File(path);
				buildLogger.addBuildLogEntry(this.sendJSONReport(readFileToString(file)).toString());
			}
		} catch (FileNotFoundException e) {
			// Catch the specific file not found exception
			buildLogger.addErrorLogEntry("ERROR: Log File Not Found: " + path + ", please make sure you file path config is correct");
			return TaskResultBuilder.create(taskContext).failed().build();
		} catch (Exception e) {
			// An exception will only be thrown if the PUT request to XBDD failed
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			buildLogger.addBuildLogEntry(errors.toString());
			buildLogger.addErrorLogEntry("ERROR: Failed to connect to XBDD at host: " + XBDD + ", please ensure the XBDD server is up and running");
			return TaskResultBuilder.create(taskContext).failed().build();
		}

		return TaskResultBuilder.create(taskContext).success().build();
	}

	/**
	 * @param fileContents The JSON test data to send to XBDD
	 * @return Integer The response code returned from the HttpUrlConnection to XBDD
	 * @throws Exception If the connection to XBDD fails
	 *
	 * Sends a stringified JSON object to XBDD.  If the JSON is invalid a non-200 response code will be returned.
	 * If the credentials are invalid a 401 error will be returned but the task will return success
	 */
	private Integer sendJSONReport(final String fileContents) throws Exception {
		HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();

		TrustModifier.relaxHostChecking(connection);

		connection.setRequestProperty("Authorization", "Basic " + creds);

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

	/**
	 * @param taskContext The taskContext passed from the bamboo build runner
	 * @return String The version string
	 *
	 * Fetches the major, minor and service pack variables from the taskContext and concatenates them into a dot
	 * separated string
	 */
	private String getVersionString(TaskContext taskContext) {
		return taskContext.getConfigurationMap().get("major") + "."
				+ taskContext.getConfigurationMap().get("minor") + "."
				+ taskContext.getConfigurationMap().get("servicepack");
	}
}