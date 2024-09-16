/***
 * Copyright 2006 bsmith@qq.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */
package com.thewizrd.weather_api.google.utils

/**
 * base 16 hex encoding.
 * https://gist.github.com/turbidsoul/5226998
 *
 * @author bsmith.zhao
 * @date 2006-05-16 11:22:12
 */
object Base16 {
    // encoding characters table.
    private val ENC_TAB =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    // decoding characters table.
    private val DEC_TAB = byteArrayOf(
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00, // 16
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00, // 32
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00, // 48
        0x00,
        0x01,
        0x02,
        0x03,
        0x04,
        0x05,
        0x06,
        0x07,
        0x08,
        0x09,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00, // 64

        0x00,
        0x0A,
        0x0B,
        0x0C,
        0x0D,
        0x0E,
        0x0F,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00, // 80
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00, // 96
        0x00,
        0x0A,
        0x0B,
        0x0C,
        0x0D,
        0x0E,
        0x0F,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00, // 112
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00
    )

    /**
     * encode byte array data to base 16 hex string.
     *
     * @param data byte array data.
     * @return base 16 hex string.
     */
    @JvmOverloads
    fun encode(data: ByteArray, offset: Int = 0, length: Int = data.size): String {
        val buff = StringBuffer(length * 2)
        var i = offset
        val total = offset + length
        while (i < total) {
            buff.append(ENC_TAB[data[i].toInt() and 0xF0 shr 4])
            buff.append(ENC_TAB[data[i].toInt() and 0x0F])
            i++
        }
        return buff.toString()
    }

    /**
     * decode base 16 hex string to byte array.
     *
     * @param hex base 16 hex string.
     * @return byte array data.
     */
    fun decode(hex: String): ByteArray {
        val data = ByteArray(hex.length / 2)
        decode(hex, data, 0)
        return data
    }

    /**
     * decode base 16 hex string to byte array.
     *
     * @param hex base 16 hex string.
     * @param data byte array data.
     * @param offset byte array data start index, included.
     */
    fun decode(hex: String, data: ByteArray, offset: Int) {
        var i = 0
        val total = hex.length / 2 * 2
        var idx = offset
        while (i < total) {
            data[idx++] =
                (DEC_TAB[hex[i++].code].toInt() shl 4 or DEC_TAB[hex[i++].code].toInt()).toByte()
        }
    }
}