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
package it.myfirstplugin;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import XBDD.XBDDPluginComponent;
import com.atlassian.sal.api.ApplicationProperties;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class XBDDPluginComponentWiredTest
{
    private final ApplicationProperties applicationProperties;
    private final XBDDPluginComponent xbddPluginComponent;

    public XBDDPluginComponentWiredTest(ApplicationProperties applicationProperties, XBDDPluginComponent xbddPluginComponent)
    {
        this.applicationProperties = applicationProperties;
        this.xbddPluginComponent = xbddPluginComponent;
    }

    @Test
    public void testMyName()
    {
        assertEquals("names do not match!", "Component:" + applicationProperties.getDisplayName(), xbddPluginComponent.getName());
    }
}