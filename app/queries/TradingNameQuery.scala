package queries

import play.api.libs.json.JsPath

case object TradingNameQuery extends Gettable[Boolean] with Settable[Boolean] {

  override def path: JsPath = JsPath \ "tradingName"
}
