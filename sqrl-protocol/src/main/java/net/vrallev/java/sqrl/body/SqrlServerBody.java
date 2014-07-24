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

/**
 * Represents the whole body sent from a server to a client.
 *
 * @author Ralf Wondratschek
 */
@SuppressWarnings("UnusedDeclaration")
public class SqrlServerBody {

    private ServerParameter mServerParameter;
    private String mBody;

    public SqrlServerBody(ServerParameter serverParameter) {
        mServerParameter = serverParameter;

        mBody = "server=" + serverParameter.getParameterEncoded();
    }

    public ServerParameter getServerParameter() {
        return mServerParameter;
    }

    /**
     * @return the flattened body.
     */
    public String getBodyEncoded() {
        return mBody;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SqrlServerBody)) return false;

        SqrlServerBody that = (SqrlServerBody) o;

        if (!mBody.equals(that.mBody)) return false;
        //noinspection RedundantIfStatement
        if (!mServerParameter.equals(that.mServerParameter)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mServerParameter.hashCode();
        result = 31 * result + mBody.hashCode();
        return result;
    }
}
