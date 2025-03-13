package com.tlscontact.serializers

import androidx.datastore.core.Serializer
import android.util.Log
import com.tlscontact.models.Configuration
import dagger.spi.internal.shaded.kotlin.metadata.internal.protobuf.InvalidProtocolBufferException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object ConfigurationSerializer : Serializer<Configuration> {
    override val defaultValue: Configuration = Configuration.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): Configuration {
        try {
            return Configuration.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            Log.e("CONFIGURATION_SERIALIZER", exception.message.toString())
        }
        return Configuration.getDefaultInstance()
    }

    @Throws(IOException::class)
    override suspend fun writeTo(t: Configuration, output: OutputStream) = t.writeTo(output)
}