import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@Ignore
class ApplicationTest {

    @Test
    @Ignore
    fun testRoot() = testApplication {
        application {
            module()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

}
