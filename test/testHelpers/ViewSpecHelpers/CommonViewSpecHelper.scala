
package testHelpers.ViewSpecHelpers

import config.wiring.PlaFormPartialRetriever
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import testHelpers.{CommonErrorMessages, MockTemplateRenderer}
import uk.gov.hmrc.play.frontend.filters.MicroserviceFilterSupport
import uk.gov.hmrc.play.test.UnitSpec
import utils.PlaTestContext

trait CommonViewSpecHelper extends UnitSpec with MicroserviceFilterSupport with GuiceOneAppPerSuite with CommonErrorMessages{

  //TODO
  //mock logger

  implicit lazy val fakeRequest = FakeRequest()
  implicit lazy val context = PlaTestContext
  implicit lazy val retriever = PlaFormPartialRetriever
  implicit lazy val renderer = MockTemplateRenderer

}
