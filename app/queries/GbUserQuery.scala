package queries

import play.api.libs.json.JsPath

case object GbUserQuery extends Gettable[Boolean] with Settable[Boolean] {

  override def path: JsPath = JsPath \ "gbUser"
}
