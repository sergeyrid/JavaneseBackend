package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.Html
import online.javanese.krud.kwery.Uuid
import java.time.LocalDateTime


class Article(
        val basicInfo: BasicInfo,
        val heading: String,
        val bodyMarkup: Html,
        val published: Boolean,
        val vkPostInfo: VkPostInfo?,
        val tgPost: String
) {

    class BasicInfo(
            val id: Uuid,
            val linkText: String,
            val meta: Meta,
            val urlSegment: String,
            val createdAt: LocalDateTime,
            val lastModified: LocalDateTime,
            val pinned: Boolean
    )

}


private val _path = Article::basicInfo
private val _id = Article.BasicInfo::id
private val _linkText = Article.BasicInfo::linkText
private val _urlSegment = Article.BasicInfo::urlSegment
private val _createdAt = Article.BasicInfo::createdAt
private val _lastModified = Article.BasicInfo::lastModified
private val _pinned = Article.BasicInfo::pinned

private val _meta = Article.BasicInfo::meta

private val _metaPath = { a: Article -> a.basicInfo.meta }


object ArticleTable : Table<Article, Uuid>("articles") {

    val Id by idCol(_id, _path)
    val LinkText by linkTextCol(_linkText, _path)

    val MetaTitle by metaTitleCol(_metaPath)
    val MetaDescription by metaDescriptionCol(_metaPath)
    val MetaKeywords by metaKeywordsCol(_metaPath)

    val UrlSegment by urlSegmentCol(_urlSegment, _path)
    val LastModified by lastModifiedCol(_lastModified, _path)
    val CreatedAt by col(_createdAt, _path, name = "createdAt")
    val Pinned by col(_pinned, _path, name = "pinned")

    val Heading by headingCol(Article::heading)
    val BodyMarkup by col(Article::bodyMarkup, name = "bodyMarkup")
    val Published by col(Article::published, name = "published")

    val VkPostId by vkPostIdCol(Article::vkPostInfo)
    val VkPostHash by vkPostHashCol(Article::vkPostInfo)
    val TgPost by tgPostCol(Article::tgPost)


    override fun idColumns(id: Uuid): Set<Pair<Column<Article, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<Article>): Article = Article(
            basicInfo = Article.BasicInfo(
                    id = value of Id,
                    linkText = value of LinkText,
                    meta = Meta(
                            title = value of MetaTitle,
                            description = value of MetaDescription,
                            keywords = value of MetaKeywords
                    ),
                    urlSegment = value of UrlSegment,
                    lastModified = value of LastModified,
                    createdAt = value of CreatedAt,
                    pinned = value of Pinned
            ),
            heading = value of Heading,
            bodyMarkup = value of BodyMarkup,
            published = value of Published,
            vkPostInfo = VkPostInfo.fromComponentsOrNull(
                    id = value of VkPostId,
                    hash = value of VkPostHash
            ),
            tgPost = value of TgPost
    )

}


object BasicArticleInfoTable : Table<Article.BasicInfo, Uuid>("articles") {

    val Id by idCol(_id)
    val LinkText by linkTextCol(_linkText)
    val MetaTitle by metaTitleCol(_meta)
    val MetaDescription by metaDescriptionCol(_meta)
    val MetaKeywords by metaKeywordsCol(_meta)
    val UrlSegment by urlSegmentCol(_urlSegment)
    val CreatedAt by col(_createdAt, name = "createdAt")
    val LastModified by lastModifiedCol(_lastModified)
    val Pinned by col(_pinned, name = "pinned")

    override fun idColumns(id: Uuid): Set<Pair<Column<Article.BasicInfo, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<Article.BasicInfo>): Article.BasicInfo = Article.BasicInfo(
            id = value of Id,
            linkText = value of LinkText,
            meta = Meta(
                    title = value of MetaTitle,
                    description = value of MetaDescription,
                    keywords = value of MetaKeywords
            ),
            urlSegment = value of UrlSegment,
            createdAt = value of CreatedAt,
            lastModified = value of LastModified,
            pinned = value of Pinned
    )

}


class ArticleDao(
        session: Session
) : AbstractDao<Article, Uuid>(session, ArticleTable, ArticleTable.Id.property) {

    private val tableName = ArticleTable.name
    private val basicCols = """"id", "linkText", "metaTitle", "metaDescription", "metaKeywords", "urlSegment", "createdAt", "lastModified", "pinned" """
    private val urlComponentColName = ArticleTable.UrlSegment.name
    private val publishedColName = ArticleTable.Published.name
    private val createdAtColName = ArticleTable.CreatedAt.name
    private val pinnedColName = ArticleTable.Pinned.name

    override val defaultOrder: Map<Column<Article, *>, OrderByDirection> =
            mapOf(ArticleTable.Pinned to OrderByDirection.DESC, ArticleTable.CreatedAt to OrderByDirection.DESC)

    private val naturalOrder = """ ORDER BY "$pinnedColName" DESC, "$createdAtColName" DESC """

    fun findAllBasicPublished(): List<Article.BasicInfo> =
            session.select(
                    sql = """SELECT $basicCols FROM "$tableName" WHERE "$publishedColName" = true $naturalOrder""",
                    mapper = BasicArticleInfoTable.rowMapper()
            )

    fun findByUrlSegment(segment: String): Article? =
            session.select(
                    sql = """SELECT * FROM "$tableName" WHERE "$urlComponentColName" = :segment LIMIT 1""",
                    parameters = mapOf("segment" to segment),
                    mapper = ArticleTable.rowMapper()
            ).firstOrNull()

}


/*
CREATE TABLE public.articles (
	id uuid NOT NULL,
	"linkText" varchar(256) NOT NULL,
	"urlSegment" varchar(64) NOT NULL,
	"metaTitle" varchar(256) NOT NULL,
	"metaDescription" varchar(256) NOT NULL,
	"metaKeywords" varchar(256) NOT NULL,
	"heading" varchar(256) NOT NULL,
	"bodyMarkup" text NOT NULL,
	"published" bool NOT NULL,
	"vkPostId" varchar(64) NOT NULL,
	"vkPostHash" varchar(64) NOT NULL,
	"tgPost" varchar(64) NOT NULL,
	"createdAt" timestamp NOT NULL,
	"lastModified" timestamp NOT NULL,
	"pinned" bool NOT NULL,
	CONSTRAINT articles_pk PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
) ;
CREATE UNIQUE INDEX articles_urlsegment_idx ON public.articles ("urlSegment") ;
 */
