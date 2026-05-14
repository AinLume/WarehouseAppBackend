import io.ktor.server.application.*
import io.ktor.server.netty.*
import plugins.configureDI
import plugins.configureDatabase
import plugins.configureRouting
import plugins.configureSerialization
import plugins.configureStatusPages

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    configureDI()
    configureDatabase()
    configureSerialization()
    configureStatusPages()
    configureRouting()
}