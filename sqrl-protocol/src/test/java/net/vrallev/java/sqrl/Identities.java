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

import net.vrallev.java.sqrl.ecc.EccProvider25519;

/**
 * @author Ralf Wondratschek
 */
public enum Identities {
    ID1(
            new byte[]{125,103,90,-98,-3,-124,2,-40,92,102,70,-108,55,-3,12,90,-27,98,25,123,63,-116,89,-58,18,-120,-39,17,-18,13,107,72},
            new byte[]{10,-27,71,-110,-124,76,100,-89,-99,-116,-119,-107,84,-48,-110,-105,85,-18,4,62,51,-85,53,83,127,-29,-61,-39,-60,-125,-26,-101}
    ),
    ID2(
            new byte[]{80,-78,-58,9,-26,29,-68,112,-17,79,-75,53,124,-106,-4,-19,-62,80,-56,-35,-33,80,-60,75,23,-62,-40,-26,-72,-99,-45,118},
            new byte[]{-123,-73,-58,66,77,113,-70,107,-102,-79,-43,-15,3,83,-118,43,124,-108,98,70,-51,-64,32,2,-31,126,34,-72,109,-98,-116,102}
    ),
    ID3(
            new byte[]{90,31,-28,-113,96,32,-62,-76,69,-122,83,77,-49,-106,96,-19,82,26,8,-59,0,-76,-13,27,12,-55,114,-1,100,-35,-42,-61},
            new byte[]{38,-94,114,40,-43,9,-37,19,-23,-9,59,48,16,-37,102,-54,78,19,21,110,90,31,106,-65,-119,-21,-35,122,101,38,-101,-40}
    )
    ;

    private final byte[] mIdentityUnlockKey;
    private final byte[] mMasterKey;

    Identities(byte[] identityUnlockKey, byte[] masterKey) {
        mIdentityUnlockKey = identityUnlockKey;
        mMasterKey = masterKey;
    }

    public byte[] getIdentityUnlockKey() {
        return mIdentityUnlockKey;
    }

    public byte[] getIdentityLockKey(EccProvider25519 eccProvider) {
        return eccProvider.computeKeyPair(mIdentityUnlockKey).getPublicKeyDiffieHellman();
    }

    public byte[] getMasterKey() {
        return mMasterKey;
    }
}
