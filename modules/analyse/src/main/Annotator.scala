package lila.analyse

import chess.format.pgn.{ Glyphs, Move, Pgn, Tag, PgnStr, Comment }
import chess.opening.*
import chess.{ Color, Status }

import lila.game.GameDrawOffers
import lila.game.Game

final class Annotator(netDomain: lila.common.config.NetDomain):

  def apply(p: Pgn, game: Game, analysis: Option[Analysis]): Pgn =
    annotateStatus(game.winnerColor, game.status) {
      annotateOpening(game.opening) {
        annotateTurns(
          annotateDrawOffers(p, game.drawOffers),
          analysis.??(_.advices)
        )
      }.copy(
        tags = p.tags + Tag(_.Annotator, netDomain)
      )
    }

  def addEvals(p: Pgn, analysis: Analysis): Pgn =
    analysis.infos.foldLeft(p) { (pgn, info) =>
      pgn
        .updatePly(
          info.ply,
          move => move.copy(comments = info.pgnComment.toList ::: move.comments)
        )
        .getOrElse(pgn)
    }

  def toPgnString(pgn: Pgn): PgnStr = PgnStr {
    // merge analysis & eval comments
    // 1. e4 { [%eval 0.17] } { [%clk 0:00:30] }
    // 1. e4 { [%eval 0.17] [%clk 0:00:30] }
    s"$pgn\n\n\n".replaceIf("] } { [", "] [")
  }

  private def annotateStatus(winner: Option[Color], status: Status)(p: Pgn) =
    lila.game.StatusText(status, winner, chess.variant.Standard) match
      case ""   => p
      case text => p.updateLastPly(_.copy(result = text.some))

  private def annotateOpening(opening: Option[Opening.AtPly])(p: Pgn) =
    opening.fold(p) { o =>
      p.updatePly(o.ply, _.copy(opening = s"${o.opening.eco} ${o.opening.name}".some)).getOrElse(p)
    }

  // add advices into mainline
  private def annotateTurns(p: Pgn, advices: List[Advice]): Pgn =
    advices.foldLeft(p) { (pgn, advice) =>
      pgn
        .updatePly(
          advice.ply,
          move =>
            move.copy(
              glyphs = Glyphs.fromList(advice.judgment.glyph :: Nil),
              comments = advice.makeComment(withEval = true, withBestMove = true) :: move.comments
            )
        )
        .getOrElse(pgn)
    }

  private def annotateDrawOffers(pgn: Pgn, drawOffers: GameDrawOffers): Pgn =
    if drawOffers.isEmpty then pgn
    else
      drawOffers.normalizedPlies.foldLeft(pgn) { (pgn, ply) =>
        pgn
          .updatePly(
            ply,
            move => move.copy(comments = Comment(s"${!ply.color} offers draw") :: move.comments)
          )
          .getOrElse(pgn)
      }
