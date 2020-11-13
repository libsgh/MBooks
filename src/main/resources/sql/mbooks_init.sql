CREATE TABLE IF NOT EXISTS "book" (
  "id" TEXT NOT NULL,
  "source" TEXT,
  "name" TEXT,
  "author" TEXT,
  "categoryName" TEXT,
  "cover" TEXT,
  "shortSummary" TEXT,
  "cpName" TEXT,
  "lastChapterName" TEXT,
  "lastChapterUpdateTime" INTEGER,
  "createTime" INTEGER,
  "status" TEXT,
  "use" INTEGER,
  PRIMARY KEY ("id")
);
CREATE TABLE IF NOT EXISTS "chapter" (
  "id" TEXT,
  "bookId" TEXT,
  "name" TEXT,
  "content" TEXT,
  "urls" TEXT,
  "updateTime" INTEGER,
  "index" INTEGER,
  "wordSum" INTEGER
);