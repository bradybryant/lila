package lila.core
package actorApi

import _root_.chess.format.{ Fen, Uci }
import _root_.chess.format.pgn.PgnStr
import play.api.libs.json.*
import java.time.Duration
import scala.concurrent.ExecutionContext

import lila.core.userId.*
import lila.core.id.GameId

package streamer:
  case class StreamStart(userId: UserId, streamerName: String)
  case class StreamersOnline(streamers: Iterable[(UserId, String)])

package map:
  case class Tell(id: String, msg: Any)
  case class TellIfExists(id: String, msg: Any)
  case class TellMany(ids: Seq[String], msg: Any)
  case class TellAll(msg: Any)
  case class Exists(id: String, promise: Promise[Boolean])

package clas:
  case class AreKidsInSameClass(kid1: UserId, kid2: UserId, promise: Promise[Boolean])
  case class IsTeacherOf(teacher: UserId, student: UserId, promise: Promise[Boolean])
  case class ClasMatesAndTeachers(kid: UserId, promise: Promise[Set[UserId]])

package puzzle:
  case class StormRun(userId: UserId, score: Int)
  case class RacerRun(userId: UserId, score: Int)
  case class StreakRun(userId: UserId, score: Int)

package lpv:
  enum LpvEmbed:
    case PublicPgn(pgn: PgnStr)
    case PrivateStudy
  type LinkRender = (String, String) => Option[scalatags.Text.Frag]
  case class AllPgnsFromText(text: String, promise: Promise[Map[String, LpvEmbed]])
  case class LpvLinkRenderFromText(text: String, promise: Promise[LinkRender])

package mailer:
  case class CorrespondenceOpponent(
      opponentId: Option[UserId],
      remainingTime: Option[Duration],
      gameId: GameId
  )
  case class CorrespondenceOpponents(userId: UserId, opponents: List[CorrespondenceOpponent])

package notify:
  case class NotifiedBatch(userIds: Iterable[UserId])

package evaluation:
  case class AutoCheck(userId: UserId)
  case class Refresh(userId: UserId)

package plan:
  case class ChargeEvent(username: UserName, cents: Int, percent: Int, date: Instant)
  case class MonthInc(userId: UserId, months: Int)
  case class PlanStart(userId: UserId)
  case class PlanGift(from: UserId, to: UserId, lifetime: Boolean)
  case class PlanExpire(userId: UserId)

package push:
  case class TourSoon(tourId: String, tourName: String, userIds: Iterable[UserId], swiss: Boolean)

package oauth:
  case class TokenRevoke(id: String)
