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

import net.vrallev.java.sqrl.ecc.EccKeyPair;
import net.vrallev.java.sqrl.ecc.EccProvider25519;
import net.vrallev.java.sqrl.util.SqrlCipherTool;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ralf Wondratschek
 */
public final class TestUtils {

    private TestUtils() {
        // no op
    }

    public static byte[][] createServerKeys(Identities identity, EccProvider25519 eccProvider) {
        SqrlCipherTool cipherTool = new SqrlCipherTool();

        byte[] randomLockKey = cipherTool.createRandomHash(256);
        EccKeyPair keyPair = eccProvider.computeKeyPair(randomLockKey);
        randomLockKey = keyPair.getPrivateKey();
        byte[] serverUnlockKey = keyPair.getPublicKeyDiffieHellman();

        byte[] identityLockKey = identity.getIdentityLockKey(eccProvider);
        byte[] identityUnlockKey = identity.getIdentityUnlockKey();

        assertThat(eccProvider.computeKeyPair(identityUnlockKey).getPublicKeyDiffieHellman()).isEqualTo(identityLockKey);
        assertThat(eccProvider.computeKeyPair(identityUnlockKey).getPrivateKey()).isEqualTo(identityUnlockKey);

        byte[] unlockRequestSigningKey = eccProvider.diffieHellman(randomLockKey, identityLockKey);
        byte[] verifyUnlockKey = eccProvider.computeKeyPair(unlockRequestSigningKey).getPublicKeySignature();

        return new byte[][] {
            serverUnlockKey, verifyUnlockKey
        };
    }
}
