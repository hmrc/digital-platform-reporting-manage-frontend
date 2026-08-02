package viewmodels

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.Using

class LayoutSpec extends AnyWordSpec with Matchers {

  private val viewsDirectory: Path =
    Paths.get("app", "/views")

  private val h1Pattern =
    """(?s)<h1\b[^>]*>""".r

  private def allPages: Seq[Path] =
    Using.resource(Files.walk(viewsDirectory)) { paths =>
      paths
        .iterator()
        .asScala
        .filter(_.toString.endsWith(".scala.html"))
        .toSeq
    }

  "All h1 elements" should {

    "use govuk-heading-l and not govuk-heading-xl" in {
      val invalidHeadings =
        allPages.flatMap { file =>
          val source = Files.readString(file)

          h1Pattern
            .findAllIn(source)
            .filterNot { h1 =>
              h1.contains("govuk-heading-l") &&
                !h1.contains("govuk-heading-xl")
            }
            .map(h1 => s"$file: $h1")
        }

      assert(
        invalidHeadings.isEmpty,
        s"""Some h1 elements do not use govuk-heading-l:
           |${invalidHeadings.mkString("\n")}
           |""".stripMargin
      )
    }
  }
}
