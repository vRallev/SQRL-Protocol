/*
 * Copyright (C) 2014 Ralf Wondratschek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.vrallev.java.sqrl.body;


import net.vrallev.java.sqrl.SqrlException;

import java.util.Map;

/**
 *
 * A parser to create a {@link SqrlServerBody} from flattened data.
 *
 * @author Ralf Wondratschek
 */
public class SqrlServerBodyParser {

    private Map<String, String> mParameters;

    /**
     * Set the data, which should be parsed.
     */
    public SqrlServerBodyParser from(String serverBody) {
        return from(SqrlRequestUtil.splitKeyValue(serverBody, "&", "="));
    }

    /**
     * Set the data, which should be parsed.
     */
    public SqrlServerBodyParser from(Map<String, String> parameters) {
        mParameters = parameters;
        return this;
    }

    /**
     * Parses the data.
     *
     * @return the parsed body.
     * @throws SqrlException if an important parameter is missing.
     */
    public SqrlServerBody parsed() throws SqrlException {
        if (!mParameters.containsKey("server")) {
            throw new SqrlException("missing important parameter");
        }

        ServerParameter serverParameter = new ServerParameterParser().parse(mParameters.get("server"), true);
        return new SqrlServerBody(serverParameter);
    }
}
