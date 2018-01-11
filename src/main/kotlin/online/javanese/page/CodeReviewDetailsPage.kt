package online.javanese.page

import kotlinx.html.*
import online.javanese.model.CodeReview
import online.javanese.model.Meta
import online.javanese.model.Page

class CodeReviewDetailsPage(
        private val indexPage: Page,
        private val codeReviewsPage: Page,
        private val codeReview: CodeReview,
        private val pageLink: Link<Page>
) : Layout.Page {

    override val meta: Meta get() = codeReview.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardDiv {

            nav {
                pageLink.insert(this, indexPage)
                +" / "
                pageLink.insert(this, codeReviewsPage)
            }

            h1 {
                +codeReview.meta.title
            }

            blockQuote {
                +codeReview.problemStatement
            }
            p(classes = "mdl-typography--text-right") {
                style = "margin-top: -12px; margin: 0 40px"

                +codeReview.senderName
            }

            hr()

            unsafe {
                +codeReview.reviewMarkup
            }

        }
    }

    override fun scripts(body: BODY) = Unit

}