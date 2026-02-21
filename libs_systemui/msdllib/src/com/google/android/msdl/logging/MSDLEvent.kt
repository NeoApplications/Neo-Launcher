/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.google.android.msdl.logging

import com.google.android.msdl.data.model.MSDLToken
import com.google.android.msdl.domain.InteractionProperties

/**
 * A summary that represents a MSDL event. The event summarizes the delivery of playback from a
 * [MSDLToken] along with optional [InteractionProperties].
 *
 * @param[tokenName] The name of the [MSDLToken] played.
 * @param[properties] The text representation of [InteractionProperties] used to play the token.
 * @param[timeStamp] A formatted time stamp for when the event occurred. The format for this time
 *   stamp is [MSDLHistoryLogger.DATE_FORMAT]
 */
data class MSDLEvent(val tokenName: String, val properties: String?, val timeStamp: String) {
    constructor(
        token: MSDLToken,
        properties: InteractionProperties?,
    ) : this(
        token.name,
        properties?.toString(),
        MSDLHistoryLogger.DATE_FORMAT.format(System.currentTimeMillis()),
    )

    override fun toString(): String = "$timeStamp | token: $tokenName | properties: $properties"
}
