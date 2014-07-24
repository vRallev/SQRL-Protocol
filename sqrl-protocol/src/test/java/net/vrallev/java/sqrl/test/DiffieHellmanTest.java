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

import net.vrallev.java.sqrl.Identities;
import net.vrallev.java.sqrl.SqrlProtocol;
import net.vrallev.java.sqrl.ecc.EccKeyPair;
import net.vrallev.java.sqrl.ecc.EccProvider25519;
import net.vrallev.java.sqrl.util.SqrlCipherTool;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ralf Wondratschek
 */
public class DiffieHellmanTest {

    @Test
    public void testDiffieHellman() {
        SqrlCipherTool cipherTool = SqrlProtocol.instance().getSqrlCipherTool();
        EccProvider25519 eccProvider = SqrlProtocol.instance().getEccProvider();

        byte[] randomLockKey = eccProvider.computeKeyPair(cipherTool.createRandomHash(256)).getPrivateKey();
        byte[] identityUnlockKey = eccProvider.computeKeyPair(cipherTool.createRandomHash(256)).getPrivateKey();

        EccKeyPair keyPair1 = eccProvider.computeKeyPair(randomLockKey);
        EccKeyPair keyPair2 = eccProvider.computeKeyPair(identityUnlockKey);

        byte[] sharedSecret1 = eccProvider.diffieHellman(keyPair1.getPrivateKey(), keyPair2.getPublicKeyDiffieHellman());
        byte[] sharedSecret2 = eccProvider.diffieHellman(keyPair2.getPrivateKey(), keyPair1.getPublicKeyDiffieHellman());

        assertThat(sharedSecret1).isNotNull().isEqualTo(sharedSecret2);
    }

    @Test
    public void testIdentities() {
        for (Identities identity : Identities.values()) {
            testDiffieHellmanIdentity(identity);
        }
    }

    protected void testDiffieHellmanIdentity(Identities identity) {
        SqrlCipherTool cipherTool = new SqrlCipherTool();
        EccProvider25519 eccProvider = SqrlProtocol.instance().getEccProvider();

        byte[] randomLockKey = cipherTool.createRandomHash(256);
        EccKeyPair keyPair = eccProvider.computeKeyPair(randomLockKey);
        randomLockKey = keyPair.getPrivateKey();                      // private
        byte[] serverUnlockKey = keyPair.getPublicKeyDiffieHellman(); // public

        byte[] identityUnlockKey = identity.getIdentityUnlockKey();   // private
        keyPair = eccProvider.computeKeyPair(identityUnlockKey);
        identityUnlockKey = keyPair.getPrivateKey();
        byte[] identityLockKey = keyPair.getPublicKeyDiffieHellman(); // public

        assertThat(eccProvider.diffieHellman(randomLockKey, identityLockKey)).isNotNull().isEqualTo(eccProvider.diffieHellman(identityUnlockKey, serverUnlockKey));
    }
}
