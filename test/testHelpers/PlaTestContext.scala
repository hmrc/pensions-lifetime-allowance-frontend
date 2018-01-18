package testHelpers

import config.PlaContext
import config.PlaContextImpl.{baseUrl, getString}
import play.api.i18n.Messages

case object PlaTestContext extends PlaContext {

  override def getPageHelpPartial()(messages: Messages): String = s"${baseUrl("contact-frontend")}/contact/problem_reports"

  override def assetsUrl: String = s"${getString("assets.url")}${getString("assets.version")}/"

}
