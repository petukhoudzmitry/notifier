package com.tlscontact.serializers

import android.util.Log
import androidx.datastore.core.Serializer
import com.tlscontact.models.Page
import dagger.spi.internal.shaded.kotlin.metadata.internal.protobuf.InvalidProtocolBufferException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object PageSerializer : Serializer<Page> {
    override val defaultValue: Page = Page.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): Page {
        try {
            return Page.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            Log.e("PAGE_SERIALIZER", exception.message.toString())
        }
        return Page.getDefaultInstance()
    }

    @Throws(IOException::class)
    override suspend fun writeTo(t: Page, output: OutputStream) = t.writeTo(output)
}