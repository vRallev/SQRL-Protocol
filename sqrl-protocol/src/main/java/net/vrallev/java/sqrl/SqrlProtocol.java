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
package net.vrallev.java.sqrl;

import net.vrallev.java.sqrl.body.ServerParameter;
import net.vrallev.java.sqrl.body.SqrlClientBody;
import net.vrallev.java.sqrl.body.SqrlClientBodyParser;
import net.vrallev.java.sqrl.body.SqrlServerBody;
import net.vrallev.java.sqrl.body.SqrlServerBodyParser;
import net.vrallev.java.sqrl.ecc.AndroidEccProvider;
import net.vrallev.java.sqrl.ecc.EccProvider25519;
import net.vrallev.java.sqrl.ecc.JavaEccProvider;
import net.vrallev.java.sqrl.util.SqrlCipherTool;

/**
 * The entry point of library. It caches and reuses several helper objects.
 * <br>
 * <br>
 * You might want to use the singleton object or you can use the {@link SqrlProtocol.Builder} class
 * to create your own instance.
 *
 * @author Ralf Wondratschek
 */
@SuppressWarnings("UnusedDeclaration")
public final class SqrlProtocol {

    private static SqrlProtocol instance;

    /**
     * @return the default instance.
     */
    public static SqrlProtocol instance() {
        if (instance == null) {
            synchronized (SqrlProtocol.class) {
                if (instance == null) {
                    instance = new Builder().build();
                }
            }
        }
        return instance;
    }

    /**
     * @param instance the default instance.
     */
    public static synchronized void instance(SqrlProtocol instance) {
        SqrlProtocol.instance = instance;
    }

    private final EccProvider25519 mEccProvider;
    private final SqrlCipherTool mSqrlCipherTool;

    private SqrlProtocol(EccProvider25519 eccProvider, SqrlCipherTool sqrlCipherTool) {
        if (eccProvider == null) {
            throw new IllegalArgumentException("you must provide an EccProvider25519");
        }

        mEccProvider = eccProvider;
        mSqrlCipherTool = sqrlCipherTool;
    }

    public EccProvider25519 getEccProvider() {
        return mEccProvider;
    }

    public SqrlCipherTool getSqrlCipherTool() {
        return mSqrlCipherTool;
    }

    /**
     * Start the authentication process from the client side.
     *
     * @param masterKey the decrypted 32 byte master key.
     * @param siteKey the website specific site key, e.g. <i>sqrl-login.appspot.com</i>. Take a look
     *                at the <a href="https://www.grc.com/sqrl/protocol.htm">specification</a> for
     *                more information.
     * @return a builder object to supply more data and information.
     */
    public SqrlClientBody.Builder authenticate(byte[] masterKey, String siteKey) {
        return new SqrlClientBody.Builder(mSqrlCipherTool, mEccProvider, masterKey, siteKey);
    }

    /**
     * Answer a previous server response. If the server sent the <i>server unlock key</i> and <i>verify
     * unlock key</i>, these keys are automatically set.
     *
     * @param masterKey the decrypted 32 byte master key.
     * @param siteKey the website specific site key, e.g. <i>sqrl-login.appspot.com</i>. Take a look
     *                at the <a href="https://www.grc.com/sqrl/protocol.htm">specification</a> for
     *                more information.
     * @param serverBody the previously parsed server response.
     * @return a builder object to supply more data and information.
     */
    public SqrlClientBody.Builder answerServer(byte[] masterKey, String siteKey, SqrlServerBody serverBody) {
        byte[] suk = serverBody.getServerParameter().getServerUnlockKeyDecoded();
        byte[] vuk = serverBody.getServerParameter().getVerifyUnlockKeyDecoded();

        return new SqrlClientBody.Builder(mSqrlCipherTool, mEccProvider, masterKey, siteKey)
                .withPreviousServerKeys(suk, vuk);
    }

    /**
     * @return a {@link SqrlClientBodyParser} to read and parse data received from a client.
     */
    public SqrlClientBodyParser readSqrlClientBody() {
        return new SqrlClientBodyParser(mEccProvider);
    }

    /**
     * Answer a client request.
     *
     * @param clientBody the previous parsed request.
     * @param tif transaction information flag. You can combine multiple flags with a logical or.
     *            Take a look at the {@link ServerParameter} class for all possible flags or open
     *            the <a href="https://www.grc.com/sqrl/semantics.htm">specification</a>.
     * @return a builder object to append more information.
     */
    public ServerParameter.Builder answerClient(SqrlClientBody clientBody, int tif) {
        String nut = clientBody.getServerParameter().getNutDecoded();
        return new ServerParameter.Builder(nut, tif);
    }

    /**
     * Answer a client request. This method combines all passed transaction information flags.
     *
     * @see SqrlProtocol#answerClient(SqrlClientBody, int)
     */
    public ServerParameter.Builder answerClient(SqrlClientBody clientBody, int... tifs) {
        int tif = 0;
        for (int flag : tifs) {
            tif |= flag;
        }

        return answerClient(clientBody, tif);
    }

    /**
     * @return a {@link SqrlServerBodyParser} to read and parse data received from a server.
     */
    public SqrlServerBodyParser readSqrlServerBody() {
        return new SqrlServerBodyParser();
    }

    public static class Builder {

        private EccProvider25519 mEccProvider;
        private SqrlCipherTool mSqrlCipherTool;

        public Builder setEccProvider(EccProvider25519 eccProvider) {
            mEccProvider = eccProvider;
            return this;
        }

        public Builder setSqrlCipherTool(SqrlCipherTool sqrlCipherTool) {
            mSqrlCipherTool = sqrlCipherTool;
            return this;
        }

        public SqrlProtocol build() {
            if (mEccProvider == null) {
                mEccProvider = tryToFindProvider();
            }
            if (mSqrlCipherTool == null) {
                mSqrlCipherTool = new SqrlCipherTool();
            }

            return new SqrlProtocol(mEccProvider, mSqrlCipherTool);
        }
    }

    private static EccProvider25519 tryToFindProvider() {
        try {
            Class.forName("net.vrallev.java.ecc.Ecc25519Helper");
            return new JavaEccProvider(true);
        } catch (ClassNotFoundException e) {
            // ignore
        }

        try {
            Class.forName("net.vrallev.android.ecc.Ecc25519Helper");
            return new AndroidEccProvider();
        } catch (ClassNotFoundException e) {
            // ignore
        }

        return null;
    }
}
