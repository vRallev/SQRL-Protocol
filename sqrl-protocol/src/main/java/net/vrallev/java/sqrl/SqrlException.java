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

/**
 * The default exception which occurs, if something unexpected happens.
 *
 * @author Ralf Wondratschek
 */
@SuppressWarnings("UnusedDeclaration")
public class SqrlException extends Exception {

    public SqrlException() {
    }

    public SqrlException(String detailMessage) {
        super(detailMessage);
    }

    public SqrlException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public SqrlException(Throwable throwable) {
        super(throwable);
    }
}
