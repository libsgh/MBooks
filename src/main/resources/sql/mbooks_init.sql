CREATE TABLE IF NOT EXISTS "public"."book" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "source" varchar(255) COLLATE "pg_catalog"."default",
  "name" varchar(1024) COLLATE "pg_catalog"."default",
  "author" varchar(100) COLLATE "pg_catalog"."default",
  "categoryName" varchar(200) COLLATE "pg_catalog"."default",
  "cover" varchar(1024) COLLATE "pg_catalog"."default",
  "shortSummary" text COLLATE "pg_catalog"."default",
  "cpName" varchar(500) COLLATE "pg_catalog"."default",
  "lastChapterName" varchar(500) COLLATE "pg_catalog"."default",
  "lastChapterUpdateTime" int4,
  "createTime" int4,
  "status" varchar(10) COLLATE "pg_catalog"."default",
  "use" int4,
  CONSTRAINT "book_pkey" PRIMARY KEY ("id")
);
CREATE TABLE IF NOT EXISTS "public"."chapter" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "bookId" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(1024) COLLATE "pg_catalog"."default",
  "content" text COLLATE "pg_catalog"."default",
  "urls" text COLLATE "pg_catalog"."default",
  "updateTime" int4,
  "index" int4,
  "wordSum" int4,
  CONSTRAINT "chapter_pkey" PRIMARY KEY ("id")
);