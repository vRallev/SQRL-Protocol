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
package net.vrallev.java.sqrl.test;

import net.vrallev.java.sqrl.SqrlException;
import net.vrallev.java.sqrl.SqrlProtocol;
import net.vrallev.java.sqrl.body.SqrlServerBody;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ralf Wondratschek
 */
public class ParameterTest {

    @Test
    public void testAdditionalServerParameter() {
        // contains qry parameter
        String serverBodyString = "server=dmVyPTENCm51dD1iWGtnYm5WMA0KdGlmPTENCnNmbj1NeSBzZXJ2ZXINCnFyeT0vbXkvcXVlcnk_cGFyYW09dmFsdWUmcGFyYW0yPXZhbHVlMg0K";

        try {

            SqrlServerBody serverBody = SqrlProtocol.instance()
                    .readSqrlServerBody()
                    .from(serverBodyString)
                    .parsed();

            assertThat(serverBody.getBodyEncoded()).isEqualTo(serverBodyString);

        } catch (SqrlException e) {
            // should not happen
            assert false;
        }
    }

}
