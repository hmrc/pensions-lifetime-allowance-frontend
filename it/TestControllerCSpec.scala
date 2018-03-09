import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.{Application, Configuration}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.play.test.UnitSpec
import play.api.http.Status._

class TestControllerCSpec extends UnitSpec with GuiceOneServerPerSuite {

  val localHost = "localhost"
  val localPort: Int = port
  val localUrl  = s"http://$localHost:$localPort"

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
      .configure(Configuration("testserver.port" -> s"$localPort"))
      .configure(Configuration("application.router" -> "testOnlyDoNotUseInAppConf.Routes"))
    .build()

  val protectionInsertUrl = s"$localUrl/protect-your-lifetime-allowance/test-only/protections/insert"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /protections/insert route" should {
    "return a 200 and valid result for protections" when {
      "nino: AA123456" in {
        def request: WSResponse = ws.url(protectionInsertUrl)
            .withHeaders(("Csrf-Token" , "nocheck"),("Content-Type" , "application/json"))
          .post(
            Json.parse(
              """{
                |  "pensionSchemeAdministratorCheckReference": "PSA12345678A",
                |  "nino": "AA123456",
                |  "id": 4294967270,
                |  "version": 10,
                |  "type": 2,
                |  "status": 1,
                |  "protectionReference": "FAKE1PRIMARY",
                |  "certificateDate" : "2015-12-18",
                |  "certificateTime" : "14:30:40",
                |  "protectedAmount": 1250000.00,
                |  "relevantAmount": 2000000.00,
                |  "uncrystallisedRights" : 500000.00,
                |  "preADayPensionInPayment": 500000.00,
                |  "postADayBCE": 500000.00,
                |  "nonUKRights": 500000.00
                |}""".stripMargin)
          )

        request.status shouldBe OK
      }
    }
  }

  val protectionDeleteAllUrl = s"$localUrl/protect-your-lifetime-allowance/test-only/protections/removeAll"

  "Hitting the /protections/removeAll route" should {
    "return a 200 and valid result for deletion" when {
      "deleting all" in {
        def request: WSResponse = ws.url(protectionDeleteAllUrl)
          .withHeaders(("Csrf-Token" , "nocheck"),("Content-Type" , "application/json"))
          .delete()

        request.status shouldBe OK
        request.body shouldBe  "All protections deleted"
      }
    }
  }

  val nino = "AA123456"
  val protectionDeleteNinoUrl = s"$localUrl/protect-your-lifetime-allowance/test-only/individuals/$nino/protections"

  "Hitting the /individuals/:nino/protections route" should {
    "return a 200 and valid result for nino deletion" when {
      "deleting a specific nino" in {
        def request: WSResponse = ws.url(protectionDeleteNinoUrl)
          .withHeaders(("Csrf-Token" , "nocheck"),("Content-Type" , "application/json"))
          .delete()

        request.status shouldBe OK
        request.body shouldBe  s"$nino deleted"
      }
    }
  }
}

