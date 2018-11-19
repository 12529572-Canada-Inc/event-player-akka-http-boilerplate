package my.artifact.myeventplayer.api

import akka.http.javadsl.model.HttpCharsets
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.MediaTypes
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.testkit.JUnitRouteTest
import my.artifact.myeventplayer.common.command.MyChangeCommand
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import com.fasterxml.jackson.databind.ObjectMapper
import my.artifact.myeventplayer.common.command.MyCreateCommand
import org.junit.After
import java.time.Duration

@ContextConfiguration(
        classes = [(ApplicationConfig::class)],
        initializers = [(MyInitializer::class)]
)
@RunWith(SpringRunner::class)
@SpringBootTest
class RouteTest : JUnitRouteTest() {

    @Autowired
    lateinit var appServer: ApplicationServer

    @Before
    fun before() {
        appServer.bind()
    }

    @After
    fun after() {
        appServer.onShutdown { binding, _ ->
            binding.terminate(
                    Duration.ofSeconds(1)
            )
        }
    }

    @Test
    fun routes() {

        //command
        val createCommandName = MyCreateCommand::class.java.name.toLowerCase()
        testRoute(appServer.route).run(
                HttpRequest.POST("/my/cmd").withEntity(
                        MediaTypes.applicationWithFixedCharset("vnd.$createCommandName.api.v1+json", HttpCharsets.UTF_8).toContentType(),
                        ObjectMapper().writeValueAsString(MyCreateCommand("initial blah"))
                )
        ).assertStatusCode(StatusCodes.OK)

        val changeCommandName = MyChangeCommand::class.java.name.toLowerCase()
        testRoute(appServer.route).run(
                HttpRequest.POST("/my/cmd/1").withEntity(
                        MediaTypes.applicationWithFixedCharset("vnd.$changeCommandName.api.v1+json", HttpCharsets.UTF_8).toContentType(),
                        ObjectMapper().writeValueAsString(MyChangeCommand("blah"))
                        )
        ).assertStatusCode(StatusCodes.OK)

        //dto
        testRoute(appServer.route).run(
                HttpRequest.GET("/my/items")
        )
                .assertStatusCode(StatusCodes.OK)
                .assertEntity("[{\"legend\":{\"aggregateId\":{\"value\":1},\"latestVersion\":2},\"myVal\":\"blah\"}]")

        testRoute(appServer.route).run(
                HttpRequest.GET("/my/item/1")
        )
                .assertStatusCode(StatusCodes.OK)
                .assertEntity("{\"legend\":{\"aggregateId\":{\"value\":1},\"latestVersion\":2},\"myVal\":\"blah\"}")


        //todo: generic errors is likely a sign which testing a library maybe more suitable that testing at the api level -- identify if this is possible
        //error
        testRoute(appServer.route).run(
                HttpRequest.POST("/my/cmd/2").withEntity(
                        MediaTypes.applicationWithFixedCharset("vnd.$changeCommandName.api.v1+json", HttpCharsets.UTF_8).toContentType(),
                        ObjectMapper().writeValueAsString(MyChangeCommand("blah"))
                )
        ).assertStatusCode(StatusCodes.NOT_FOUND)

        testRoute(appServer.route).run(
                HttpRequest.POST("/my/cmd").withEntity(
                        MediaTypes.applicationWithFixedCharset("vnd.$createCommandName.api.v1+json", HttpCharsets.UTF_8).toContentType(),
                        ObjectMapper().writeValueAsString(MyCreateCommand(""))
                )
        ).assertStatusCode(StatusCodes.BAD_REQUEST)

        testRoute(appServer.route).run(
                HttpRequest.POST("/my/cmd/1").withEntity(
                        MediaTypes.applicationWithFixedCharset("vnd.$changeCommandName.api.v1+json", HttpCharsets.UTF_8).toContentType(),
                        ObjectMapper().writeValueAsString(MyChangeCommand(""))
                )
        ).assertStatusCode(StatusCodes.BAD_REQUEST)


        testRoute(appServer.route).run(
                HttpRequest.GET("/my/item/2")
        ).assertStatusCode(StatusCodes.NOT_FOUND)
    }
}