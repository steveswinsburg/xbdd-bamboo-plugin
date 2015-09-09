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

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.opensymphony.xwork.TextProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class XBDDTestResultsSenderConfigurator extends AbstractTaskConfigurator
{
	private TextProvider textProvider;

	@NotNull
	@Override
	public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
	{
		final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

		config.put("host", params.getString("host"));
		config.put("path", params.getString("path"));
		config.put("product", params.getString("product"));
		config.put("major", params.getString("major"));
		config.put("minor", params.getString("minor"));
		config.put("servicepack", params.getString("servicepack"));
		config.put("plug-build", params.getString("plug-build"));

		return config;
	}

	@Override
	public void populateContextForCreate(@NotNull final Map<String, Object> context)
	{
		super.populateContextForCreate(context);

		context.put("host", "https://xbdd");
		context.put("path", "target/cukes-report.json");
		context.put("product", "");
		context.put("major", "");
		context.put("minor", "");
		context.put("servicepack", "");
		context.put("plug-build", "${bamboo.buildNumber}");
	}

	@Override
	public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
	{
		super.populateContextForEdit(context, taskDefinition);

		context.put("host", taskDefinition.getConfiguration().get("host"));
		context.put("path", taskDefinition.getConfiguration().get("path"));
		context.put("product", taskDefinition.getConfiguration().get("product"));
		context.put("major", taskDefinition.getConfiguration().get("major"));
		context.put("minor", taskDefinition.getConfiguration().get("minor"));
		context.put("servicepack", taskDefinition.getConfiguration().get("servicepack"));
		context.put("plug-build", taskDefinition.getConfiguration().get("plug-build"));
	}

	@Override
	public void populateContextForView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
	{
		super.populateContextForView(context, taskDefinition);
		context.put("host", taskDefinition.getConfiguration().get("host"));
		context.put("path", taskDefinition.getConfiguration().get("path"));
		context.put("product", taskDefinition.getConfiguration().get("product"));
		context.put("major", taskDefinition.getConfiguration().get("major"));
		context.put("minor", taskDefinition.getConfiguration().get("minor"));
		context.put("servicepack", taskDefinition.getConfiguration().get("servicepack"));
		context.put("plug-build", taskDefinition.getConfiguration().get("plug-build"));
	}

	@Override
	public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
	{
		super.validate(params, errorCollection);

		final String host = params.getString("host");
		if (StringUtils.isEmpty(host))
		{
			errorCollection.addError("host", textProvider.getText("xbdd.host.error"));
		}
		if (host != null && !host.contains("http")) {
			errorCollection.addError("host", textProvider.getText("xbdd.http.error"));
		}

		final String path1Value = params.getString("path");
		if (StringUtils.isEmpty(path1Value))
		{
			errorCollection.addError("path", textProvider.getText("xbdd.path.error"));
		}

		final String build = params.getString("plug-build");
		if (StringUtils.isEmpty(build))
		{
			errorCollection.addError("plug-build", textProvider.getText("xbdd.build.error"));
		}

		final String productName = params.getString("product");
		if (StringUtils.isEmpty(productName))
		{
			errorCollection.addError("product", textProvider.getText("xbdd.product.error"));
		}

		final String[] versionFields = {"major", "minor", "servicepack"};

		for (String field : versionFields) {
			try {
				final Integer fieldVal = Integer.parseInt(params.getString(field));
				if (StringUtils.isEmpty(params.getString(field)) || fieldVal < 0) {
					errorCollection.addError(field, textProvider.getText("xbdd.version.error"));
				}
			} catch (Exception e) {
				errorCollection.addError(field, textProvider.getText("XBDD.version.error"));
			}
		}
	}

	public void setTextProvider(final TextProvider textProvider)
	{
		this.textProvider = textProvider;
	}
}