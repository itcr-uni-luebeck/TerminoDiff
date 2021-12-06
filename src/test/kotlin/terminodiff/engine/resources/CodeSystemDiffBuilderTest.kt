package terminodiff.engine.resources

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.apache.commons.lang3.builder.Diff
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class CodeSystemDiffBuilderTest {
    @Test
    fun build() {
        assertDoesNotThrow {
            val builder = CodeSystemDiffBuilder(DiffResources.fhirContext, DiffResources.cs1, DiffResources.cs2)
            val result = builder.build()
            assert(result.metadata.diffResults.isNotEmpty())
            result.metadata.diffResults.forEach {
                println(it.toString())
            }
        }

    }
}