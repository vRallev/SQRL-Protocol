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
import net.vrallev.java.sqrl.ecc.EccProvider25519;

import java.util.Map;

/**
 * A parser to create a {@link SqrlClientBody} from flattened data.
 *
 * @author Ralf Wondratschek
 */
@SuppressWarnings("UnusedDeclaration")
public class SqrlClientBodyParser {

    private final EccProvider25519 mProvider;

    private Map<String, String> mParameters;
    private byte[] mStoredServerUnlockKey;
    private byte[] mStoredVerifyUnlockKey;

    public SqrlClientBodyParser(EccProvider25519 provider) {
        mProvider = provider;
    }

    /**
     * Set the data, which should be parsed.
     */
    public SqrlClientBodyParser from(String clientBody) {
        return from(SqrlRequestUtil.splitKeyValue(clientBody, "&", "="));
    }

    /**
     * Set the data, which should be parsed.
     */
    public SqrlClientBodyParser from(Map<String, String> parameters) {
        mParameters = parameters;
        return this;
    }

    /**
     * Set the stored server keys. These are necessary, if you want to verify the unlock request
     * signature.
     */
    public SqrlClientBodyParser withStoredKeys(byte[] serverUnlockKey, byte[] verifyUnlockKey) {
        mStoredServerUnlockKey = serverUnlockKey;
        mStoredVerifyUnlockKey = verifyUnlockKey;
        return this;
    }

    /**
     * Parses the data without checking signatures.
     * <br>
     * <br>
     * <b>Warning:</b> this is not recommended. Use {@link #verified()} instead.
     *
     * @return the parsed body.
     * @throws SqrlException if an important parameter is missing.
     */
    public SqrlClientBody execute() throws SqrlException {
        return parseInternal(false);
    }

    /**
     * Parses the data and checks all necessary signatures.
     *
     * @return the parsed body.
     * @throws SqrlException if an important parameter is missing or a signature didn't match.
     */
    public SqrlClientBody verified() throws SqrlException {
        return parseInternal(true);
    }

    private SqrlClientBody parseInternal(boolean checkSignatures) throws SqrlException {
        if (!mParameters.containsKey("client") || !mParameters.containsKey("server") || !mParameters.containsKey("ids")) {
            throw new SqrlException("missing important parameter");
        }

        String clientParameterDecoded = new String(SqrlRequestUtil.decodeBase64(mParameters.get("client")), SqrlRequestUtil.ASCII);
        ClientParameter clientParameter = new ClientParameterParser().parse(clientParameterDecoded);
        ServerParameter serverParameter = new ServerParameterParser().parse(mParameters.get("server"), true);
        byte[] concatenation = (mParameters.get("client") + mParameters.get("server")).getBytes(SqrlRequestUtil.ASCII);

        byte[] ids = SqrlRequestUtil.decodeBase64(mParameters.get("ids"));

        if (checkSignatures && !mProvider.isValidSignature(concatenation, ids, clientParameter.getIdentityKeyDecoded())) {
            throw new SqrlException("signature mismatch");
        }

        byte[] pids = null;
        if (mParameters.containsKey("pids")) {
            pids = SqrlRequestUtil.decodeBase64(mParameters.get("pids"));
            if (checkSignatures && !mProvider.isValidSignature(concatenation, pids, clientParameter.getPreviousIdentityKeyDecoded())) {
                throw new SqrlException("signature mismatch");
            }
        }

        byte[] urs = null;
        if (mParameters.containsKey("urs")) {
            urs = SqrlRequestUtil.decodeBase64(mParameters.get("urs"));
            if (mStoredServerUnlockKey != null && mStoredVerifyUnlockKey != null) {
               if (checkSignatures && !mProvider.isValidSignature(mStoredVerifyUnlockKey, urs, mStoredVerifyUnlockKey)) {
                   throw new SqrlException("signature mismatch");
               }

            } else if (checkSignatures && !mProvider.isValidSignature(clientParameter.getVerifyUnlockKeyDecoded(), urs, clientParameter.getVerifyUnlockKeyDecoded())) {
                throw new SqrlException("signature mismatch");
            }
        }

        return new SqrlClientBody(clientParameter, serverParameter, ids, pids, urs);
    }
}
